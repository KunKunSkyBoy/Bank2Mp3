# Bank2Mp3 — FMOD .bank 音频提取器

> 将 FMOD Studio 的 .bank 文件提取为 WAV/MP3/FLAC/AAC/OGG/OPUS 格式

## 功能
- 🔌 HTTP 桥接 Operit 终端，利用 proot 执行 Python + ffmpeg
- 📦 内置 fsb5 解析库
- 🐍 内置 rootfs: python3.12 + ffmpeg + libfmod (24MB)
- 🎵 6 种输出格式
- 📂 36 类中文自动分类

## 构建
```bash
cd Bank转音频
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

## 使用
1. 安装 APK
2. 打开 Operit → 确保终端在线
3. 点 🚀启动 → 🟢已连接
4. 选 .bank 文件 → ▶ 转换

## 技术栈
- Kotlin / Android
- Python 3.12 (fsb5 + ffmpeg)
- Operit Terminal Bridge (proot)

## 文件结构
```
app/src/main/java/com/bank2mp3/app/
├── MainActivity.kt       # UI + 桥接
├── TerminalBridge.kt     # HTTP 客户端
├── BridgeStarter.kt      # 启动 bridge
├── PythonRuntime.kt      # rootfs 管理
└── PythonDecoder.kt      # 解码封装

scripts/
├── decode.py             # FSB5→WAV
└── terminal_server.py    # Bridge server
```