# Bank2Mp3 — FMOD .bank 音频提取器

> 将 FMOD Studio 的 .bank 文件提取为 WAV / MP3 / FLAC / AAC / OGG / OPUS

Android APK，自给自足。依赖 [Operit](https://github.com/AAswordman/Operit) 终端的 proot 虚拟化环境执行 Python + ffmpeg 解码。

## 功能

| 功能 | 说明 |
|------|------|
| ▶ 单文件转换 | .bank → WAV |
| 📁 批量目录 | 目录下所有 .bank → WAV / MP3 |
| 📂 批量分类 | 按 36 类游戏音频自动分中文目录 |
| ⬆ 格式转换 | WAV → MP3(192k/320k) / AAC M4A / FLAC / OGG / OPUS |
| 🚀 一键启动 | 在 Operit 终端启动 Bridge Server |

## 快速开始

### 环境要求

- **编译**：JDK 17 + Android SDK 34 + Gradle 8.2
- **运行**：Android 8.0+ 设备 + [Operit](https://github.com/AAswordman/Operit) App（提供 proot 环境）

### 构建 APK

```bash
git clone https://github.com/MuXi36/Bank2Mp3.git
cd Bank2Mp3

# 确保 Gradle wrapper 可执行
chmod +x gradlew

# 编译 Debug 版本
./gradlew assembleDebug

# APK 输出位置
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

> **注意**：首次构建需要下载 Gradle 和依赖，约 5-10 分钟。APK 约 33MB（内含 24MB rootfs 运行时）。

### 安装与使用

1. 安装编译好的 APK 到设备
2. 打开 Operit App → 进入终端
3. 打开 Bank2Mp3 App → 状态栏显示 🔴 → 点 🚀启动
4. 等待状态变为 🟢终端桥接已连接
5. 选择 .bank 文件或目录 → 点击对应转换按钮
6. 输出在 `Download/Bank2Mp3_output/`

## 架构

```
┌──────────────────────────┐     HTTP      ┌──────────────────────────┐
│   Bank2Mp3.apk (33MB)    │ ← localhost → │    Operit 终端 (proot)    │
│                          │    :8899      │                          │
│  MainActivity.kt         │ ──exec──→     │  terminal_server.py      │
│  TerminalBridge.kt       │ ←──json──     │  decode.py (fsb5+ffmpeg) │
│  BridgeStarter.kt        │               │  class.py (36类分类)     │
│  PythonRuntime.kt        │               │                          │
│  PythonDecoder.kt        │               │  rootfs:                 │
│                          │               │  python3.12 + ffmpeg     │
│  assets/                 │               │  + libogg + libvorbis    │
│  ├── rootfs.dat (24MB)   │               │  + libfmod.so            │
│  └── scripts/            │               │                          │
│      ├── decode.py       │               │                          │
│      └── fsb5/*.py       │               │                          │
└──────────────────────────┘               └──────────────────────────┘
```

解码链路：`.bank → FSB5解析 (Python) → Vorbis/PCM解码 (ctypes→libvorbis) → ffmpeg转码 → WAV/MP3/FLAC...`

## 项目结构

```
Bank2Mp3/
├── build.gradle                 # 顶层 Gradle 配置
├── settings.gradle               # 模块配置
├── gradle.properties             # JDK/SDK 路径
├── gradlew / gradlew.bat         # Gradle Wrapper
├── build_and_inject.sh           # 备选：构建+注入.so+签名
│
├── scripts/                      # Python 脚本（终端侧运行）
│   ├── terminal_server.py        # Bridge HTTP Server (:8899)
│   ├── decode.py                 # FSB5 → WAV 核心解码
│   └── fsb5/                     # fsb5 解析库
│       ├── __init__.py           # FSB5 容器解析器
│       ├── utils.py              # BinaryReader + 库加载
│       ├── vorbis.py             # Vorbis ctypes 解码
│       ├── vorbis_headers.py     # 预计算 Vorbis 表 (2.3MB)
│       └── pcm.py                # PCM → WAV
│
└── app/
    ├── build.gradle              # 应用构建配置
    └── src/main/
        ├── AndroidManifest.xml   # 权限：INTERNET + 存储
        ├── res/layout/
        │   └── activity_main.xml # 完整 UI 布局
        ├── java/com/bank2mp3/app/
        │   ├── MainActivity.kt   # UI + 文件选择 + 桥接调用
        │   ├── TerminalBridge.kt # HTTP → localhost:8899
        │   ├── BridgeStarter.kt  # 一键启动终端 server
        │   ├── PythonRuntime.kt  # rootfs.dat 解压管理
        │   └── PythonDecoder.kt  # 内置终端解码（备用）
        └── assets/
            ├── rootfs.dat        # Linux ARM64 运行时 (24MB)
            └── Bank2Mp3/scripts/ # 脚本副本（编译进 APK）
```

## 依赖

| 依赖 | 用途 |
|------|------|
| [Operit](https://github.com/AAswordman/Operit) | proot 终端环境 |
| [fsb5](https://github.com/HearthSim/python-fsb5) | FSB5 容器解析（内置） |
| Python 3.12 + ffmpeg | 音频解码与转码（rootfs 内置） |
| libogg + libvorbis | Vorbis 编解码（rootfs 内置） |

## 鸣谢

- [Fmod-Bank-Tools](https://github.com/Wouldubeinta/Fmod-Bank-Tools) — FMOD bank 分析工具
- [AAswordman/Operit](https://github.com/AAswordman/Operit) — Android proot 终端环境

## License

MIT
