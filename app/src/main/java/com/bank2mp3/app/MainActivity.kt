package com.bank2mp3.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bank2mp3.app.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var job: Job? = null
    private var bridgeAlive = false
    private var isDarkTheme = true

    companion object {
        private const val PREFS = "bank2mp3_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    private val outputDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Bank2Mp3_output")
    private val inputDir  = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Bank2Mp3_input")

    private var bankPath = ""
    private var folderPath = ""
    private var outputPath = outputDir.absolutePath

    private val bankPicker   = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { onBankPicked(it) } }
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { onFolderPicked(it) } }
    private val outputPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> uri?.let { onOutputPicked(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 读取主题偏好
        isDarkTheme = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK_THEME, true)
        setTheme(if (isDarkTheme) R.style.Theme_Bank2Mp3 else R.style.Theme_Bank2Mp3_Light)
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        outputDir.mkdirs(); inputDir.mkdirs()
        log("✅ 就绪 | 输出: ${outputDir.absolutePath}")
        bindUI()
        applyButtonAnimations()
        startTitleBreathing()
        // 后台解压 rootfs + 检测桥接
        CoroutineScope(Dispatchers.IO).launch {
            try { PythonRuntime.init(this@MainActivity) } catch (e: Exception) { ui { log("rootfs: ${e.message}") } }
            checkBridge()
        }
    }

    /** 给所有按钮叠加 GSAP 级按压 + 入场 stagger 动效 */
    private fun applyButtonAnimations() {
        val btns = listOf(
            b.btnPickFile, b.btnPickFolder, b.btnPickOutput, b.btnResetOutput,
            b.btnBridgeStart, b.btnBridgeRefresh, b.btnThemeToggle,
            b.btnTerminalConvert, b.btnTerminalBatch, b.btnTerminalBatchMp3, b.btnTerminalClassify,
            b.btnWavToMp3, b.btnWavToMp3HQ, b.btnWavToAac, b.btnWavToFlac, b.btnWavToOgg, b.btnWavToOpus,
            b.btnCopyLog, b.btnClearLog
        )
        // ── 入场 stagger：从下方弹入 + 链式衰减震荡 ──
        btns.forEachIndexed { i, view ->
            view.translationY = 60f
            view.alpha = 0f
            view.animate()
                .translationY(0f).alpha(1f)
                .setDuration(450)
                .setStartDelay(i * 28L)
                .setInterpolator(android.view.animation.OvershootInterpolator(2.5f))
                .start()
        }
        // ── 按压：链式衰减震荡 (模拟 GSAP elastic.out) ──
        btns.forEach { view ->
            view.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.animate().cancel()
                        v.animate().scaleX(0.86f).scaleY(0.86f).setDuration(50).start()
                        v.animate().translationZ(-4f).setDuration(50).start()
                    }
                    android.view.MotionEvent.ACTION_UP -> {
                        v.animate().cancel()
                        chainSpring(v)
                    }
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        v.animate().cancel()
                        v.animate().scaleX(1f).scaleY(1f).translationZ(0f).setDuration(120).start()
                    }
                }
                false
            }
        }
    }

    /** 链式衰减震荡：1.07→0.95→1.025→1.0，模拟 elastic.out 多段弹跳 */
    private fun chainSpring(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(1.07f).scaleY(1.07f).translationZ(6f)
            .setDuration(180)
            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
            .withEndAction {
                view.animate()
                    .scaleX(0.95f).scaleY(0.95f).translationZ(2f)
                    .setDuration(140)
                    .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
                    .withEndAction {
                        view.animate()
                            .scaleX(1.025f).scaleY(1.025f).translationZ(3f)
                            .setDuration(100)
                            .setInterpolator(android.view.animation.DecelerateInterpolator(2f))
                            .withEndAction {
                                view.animate()
                                    .scaleX(1f).scaleY(1f).translationZ(0f)
                                    .setDuration(70)
                                    .setInterpolator(android.view.animation.DecelerateInterpolator(3f))
                            }
                    }
            }
    }

    private fun startTitleBreathing() {
        val title = b.tvTitle
        // 烛光呼吸：scale 1.0 ↔ 1.04，暖金光晕闪烁
        ObjectAnimator.ofFloat(title, "scaleX", 1f, 1.04f, 1f).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        ObjectAnimator.ofFloat(title, "scaleY", 1f, 1.04f, 1f).apply {
            duration = 2800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        // 光晕渐变：从暗金到亮金再回暗金
        ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                val t = animatedValue as Float
                val r = (0xD4 + (0xFF - 0xD4) * t).toInt()
                val g = (0x94 + (0xD6 - 0x94) * t).toInt()
                val bb = (0x3A + (0x99 - 0x3A) * t).toInt()
                title.setTextColor(Color.rgb(r, g, bb))
            }
            start()
        }
    }

    private fun bindUI() {
        b.btnPickFile.setOnClickListener   { bankPicker.launch(arrayOf("*/*")) }
        b.btnPickFolder.setOnClickListener { folderPicker.launch(null) }
        b.btnPickOutput.setOnClickListener { outputPicker.launch(null) }
        b.btnResetOutput.setOnClickListener {
            outputPath = outputDir.absolutePath; b.tvOutputDir.text = "Download/Bank2Mp3_output"; log("输出目录已重置")
        }

        b.btnBridgeRefresh.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { checkBridge() }
        }
        b.btnBridgeStart.setOnClickListener {
            b.btnBridgeStart.isEnabled = false; b.bridgeStatus.text = "⏳ 启动中..."
            CoroutineScope(Dispatchers.IO).launch {
                val msg = BridgeStarter.start(this@MainActivity)
                delay(2000)
                ui { b.btnBridgeStart.isEnabled = true; log(msg) }
                checkBridge()
            }
        }

        b.btnTerminalConvert.setOnClickListener  { if (bankPath.isNotEmpty()) terminalConvertSingle(bankPath) else toast("请先选文件") }
        b.btnTerminalBatch.setOnClickListener    { if (folderPath.isNotEmpty()) terminalBatch(folderPath, false) else toast("请先选目录") }
        b.btnTerminalBatchMp3.setOnClickListener { if (folderPath.isNotEmpty()) terminalBatch(folderPath, true) else toast("请先选目录") }
        b.btnTerminalClassify.setOnClickListener { if (folderPath.isNotEmpty()) terminalBatch(folderPath, true) else toast("请先选目录") }

        b.btnWavToMp3.setOnClickListener   { wavConvert("mp3", "192k", "MP3 192k") }
        b.btnWavToMp3HQ.setOnClickListener { wavConvert("mp3", "320k", "MP3 320k") }
        b.btnWavToAac.setOnClickListener   { wavConvert("aac", "192k", "AAC M4A") }
        b.btnWavToFlac.setOnClickListener  { wavConvert("flac", "0", "FLAC") }
        b.btnWavToOgg.setOnClickListener   { wavConvert("ogg", "6", "OGG") }
        b.btnWavToOpus.setOnClickListener  { wavConvert("opus", "128k", "OPUS") }

        b.btnCopyLog.setOnClickListener {
            val cm = getSystemService(android.content.ClipboardManager::class.java)
            cm.setPrimaryClip(android.content.ClipData.newPlainText("log", b.tvLog.text?.toString() ?: "")); toast("已复制")
        }
        b.btnClearLog.setOnClickListener {
            b.tvLog.text = "等待操作..."
        }
        // ── 主题切换 ──
        b.btnThemeToggle.text = if (isDarkTheme) "🌙" else "☀️"
        b.btnThemeToggle.setOnClickListener {
            val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_DARK_THEME, !isDarkTheme).apply()
            recreate()
        }
    }

    private fun setBtns(on: Boolean) {
        b.btnTerminalConvert.isEnabled  = on && bankPath.isNotEmpty()
        b.btnTerminalBatch.isEnabled    = on
        b.btnTerminalBatchMp3.isEnabled = on
        b.btnTerminalClassify.isEnabled = on
        b.btnWavToMp3.isEnabled = on; b.btnWavToMp3HQ.isEnabled = on
        b.btnWavToAac.isEnabled = on; b.btnWavToFlac.isEnabled = on
        b.btnWavToOgg.isEnabled = on; b.btnWavToOpus.isEnabled = on
    }

    private suspend fun checkBridge() {
        val ok = TerminalBridge.checkHealth()
        bridgeAlive = ok
        ui {
            if (ok) {
                b.bridgeStatus.text = "🟢 终端桥接已连接"; b.bridgeStatus.setTextColor(0xFF27AE60.toInt()); setBtns(true)
            } else {
                b.bridgeStatus.text = "🔴 未连接 — 点 🚀启动"; b.bridgeStatus.setTextColor(0xFFE74C3C.toInt()); setBtns(false)
            }
        }
    }

    private fun onBankPicked(uri: Uri) {
        resolveFilePath(uri)?.let {
            bankPath = it; b.tvFile.text = File(it).name
            b.btnTerminalConvert.isEnabled = bridgeAlive; log("已选择: ${File(it).name}")
            return
        }
        try {
            val name = getDisplayName(uri) ?: "input.bank"
            val tmp = File(cacheDir, name)
            contentResolver.openInputStream(uri)?.use { s -> tmp.outputStream().use { d -> s.copyTo(d) } }
            bankPath = tmp.absolutePath; b.tvFile.text = name
            b.btnTerminalConvert.isEnabled = bridgeAlive; log("已选择(缓存): $name")
        } catch (e: Exception) { log("✗ ${e.message}") }
    }
    private fun onFolderPicked(uri: Uri) {
        folderPath = resolveTreePath(uri) ?: uri.toString()
        b.tvFile.text = if (folderPath.startsWith("/")) File(folderPath).name else "已选目录"
        log("目录: $folderPath")
    }
    private fun onOutputPicked(uri: Uri) {
        resolveTreePath(uri)?.let { outputPath = it; b.tvOutputDir.text = it; File(it).mkdirs(); log("输出: $it") }
    }

    private fun terminalConvertSingle(path: String) {
        if (!bridgeAlive) { toast("桥接未连接"); return }
        job?.cancel(); job = CoroutineScope(Dispatchers.IO).launch {
            val sharedPath = if (path.startsWith(cacheDir.absolutePath) || path.startsWith(filesDir.absolutePath)) {
                val s = File(inputDir, File(path).name); File(path).copyTo(s, overwrite = true)
                ui { log("缓存 → ${s.absolutePath}") }; s.absolutePath
            } else path
            ui { showProgress(true); log("⚡ 终端: Bank → WAV...") }
            val r = TerminalBridge.convertSingle(sharedPath, outputPath)
            ui { showProgress(false); for (l in r.stdout.lines().takeLast(40)) log(l); log(if (r.ok) "✅ 完成" else "✗ ${r.error}") }
        }
    }
    private fun terminalBatch(path: String, mp3: Boolean) {
        if (!bridgeAlive) { toast("桥接未连接"); return }
        job?.cancel(); job = CoroutineScope(Dispatchers.IO).launch {
            ui { showProgress(true); log("⚡ 批量: Bank → ${if (mp3) "MP3" else "WAV"}...") }
            val r = TerminalBridge.batchConvert(path, outputPath, mp3)
            ui { showProgress(false); for (l in r.stdout.lines().takeLast(30)) log(l); log(if (r.ok) "✅ 批量完成" else "✗ ${r.error}") }
        }
    }
    private fun wavConvert(format: String, bitrate: String, label: String) {
        if (!bridgeAlive) { toast("桥接未连接"); return }
        job?.cancel(); job = CoroutineScope(Dispatchers.IO).launch {
            ui { showProgress(true); log("⬆ WAV → $label...") }
            val r = TerminalBridge.wavConvert(outputPath, format, bitrate)
            ui { showProgress(false); for (l in r.stdout.lines().takeLast(20)) log(l); log(if (r.ok) "✅ $label 完成" else "✗ ${r.error}") }
        }
    }

    private fun resolveFilePath(uri: Uri): String? {
        try {
            contentResolver.query(uri, arrayOf(android.provider.MediaStore.MediaColumns.DATA), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA))?.takeIf { File(it).exists() }?.let { return it }
            }
            if (DocumentsContract.isDocumentUri(this, uri)) {
                val docId = DocumentsContract.getDocumentId(uri); val i = docId.indexOf(':')
                if (i >= 0) { val p = "/storage/emulated/0/${docId.substring(i + 1)}"; if (File(p).exists()) return p }
            }
        } catch (_: Exception) {}
        return null
    }
    private fun resolveTreePath(uri: Uri) = try {
        val docId = DocumentsContract.getTreeDocumentId(uri); val i = docId.indexOf(':')
        if (i >= 0) "/storage/emulated/0/${docId.substring(i + 1)}".also { File(it).mkdirs() } else null
    } catch (_: Exception) { null }
    private fun getDisplayName(uri: Uri) = try {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)) else null
        }
    } catch (_: Exception) { null }

    private fun showProgress(on: Boolean) { b.progressContainer.visibility = if (on) View.VISIBLE else View.GONE; if (on) { b.progress.progress = 0; b.tvProgress.text = "0%" } }
    private fun log(msg: String) { runOnUiThread { b.tvLog.text = ((b.tvLog.text?.toString() ?: "") + "\n" + msg).lines().takeLast(150).joinToString("\n") } }
    private suspend fun ui(block: () -> Unit) = withContext(Dispatchers.Main) { block() }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}