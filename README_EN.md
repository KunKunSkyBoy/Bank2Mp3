# 🎵 Bank2Mp3 — FMOD `.bank` Audio Extractor

[![中文](README.md)](README.md) [![EN](https://img.shields.io/badge/lang-EN-blue)]()

> Android Native App · Terminal Bridge Architecture · Van Gogh Themed Motion  
> Extract audio from game `.bank` files, batch transcode to MP3/AAC/FLAC/OGG/OPUS

[![Android](https://img.shields.io/badge/Android-8.0%2B-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?logo=kotlin)](https://kotlinlang.org)
[![Python](https://img.shields.io/badge/Python-3.8%2B-yellow?logo=python)](https://python.org)
[![Min SDK 26](https://img.shields.io/badge/minSdk-26-orange)](https://apilevels.com)

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 **.bank Parsing** | FMOD API audio extraction → lossless WAV |
| 🔄 **Multi-format** | MP3 (192k/320k) · AAC · FLAC · OGG · OPUS via FFmpeg |
| 📦 **Batch Processing** | Directory-wide conversion with recursive scanning |
| 🏷️ **Chinese Classify** | Auto-sort by Chinese audio name into folders |
| 🌐 **Terminal Bridge** | Python HTTP server ↔ APK via `localhost:8899` |
| 🎨 **Dual Themes** | Dark·Van Gogh _Starry Night_ / Light·Oil Canvas |
| 🌟 **Particle Effects** | 200+ twinkling stars, 12 glowing halos, swirl arcs |
| 📋 **Collapsible Log** | Tap title bar to fold/unfold the log panel |
| ⏱️ **Workflow Guardian** | Operit workflow keeps bridge alive every minute |
| 🐔 **Easter Egg** | A classic tribute ❤️ |

---

## 🖼️ Preview

| Dark · Starry Night | Light · Oil Canvas |
|:---:|:---:|
| Deep blue gradient + twinkling stars + swirl arcs + 12 glowing halos | Warm cream + linen weave + golden brush strokes + soft light spots |

> Backgrounds are Canvas-rendered at runtime — no static images. Stars breathe independently via sine-wave (100ms/frame).

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│              Android APK                 │
│  ┌───────────────────────────────────┐  │
│  │     MainActivity (Kotlin)          │  │
│  │  • UI / Effects / Themes           │  │
│  │  • OkHttp → localhost:8899         │  │
│  └──────────────┬────────────────────┘  │
│                 │ HTTP POST /exec        │
│                 │       /batch           │
│                 │       /wav2mp3         │
│  ┌──────────────▼────────────────────┐  │
│  │  terminal_server.py (Python)      │  │
│  │  • FMOD parsing · FFmpeg encode   │  │
│  │  • Batch · Classification          │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## 📲 Installation

### Prerequisites

- Android 8.0+ (API 26+)
- [ZeroTermux](https://github.com/hanxinhao000/ZeroTermux) or any proot terminal
- Python 3.8+ + FFmpeg (in terminal environment)

### Build

```bash
git clone https://github.com/MuXi36/Bank2Mp3.git
cd Bank2Mp3

# Requires ZeroTermux + JDK 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
bash gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### First Run

1. Install the APK
2. Start the bridge in ZeroTermux:
   ```bash
   python3 /sdcard/Download/Bank2Mp3/scripts/terminal_server.py
   ```
3. Or auto-start via Operit workflow (checks every minute)

---

## 🎮 Usage

| Operation | How |
|-----------|-----|
| Pick `.bank` file | File picker → single conversion |
| Pick directory | Document tree → batch scan |
| Terminal bridge | Start `terminal_server.py` first, then tap convert |
| Format conversion | WAV panel: MP3/AAC/FLAC/OGG/OPUS one-tap |
| Chinese classify | Batch classify → auto folder by name |
| Theme toggle | Top-right ◇/◆ button (dark/light) |
| Log collapse | Tap 「▼ ★ 日志」 title bar to toggle |

---

## 🎨 Motion System

| Effect | Description |
|--------|-------------|
| **Title Breathing** | ♫ triple slow breathe + diamond glow pulsation |
| **Kunkun Egg** | IKUN breathe scale + alpha shimmer |
| **Card Glow** | Bridge bar + log card 4.5s soft glow cycle |
| **Button Stagger** | 22 buttons bounce in (OvershootInterpolator) |
| **Chain Spring** | GSAP elastic.out multi-bounce on press |
| **Marquee** | Gold glow sweeps across terminal buttons |
| **Refresh Spin** | SVG Lucide refresh-cw icon rotation |
| **Star Twinkle** | 200 small + 12 big halos, independent sine breathe |
| **Swirl Arcs** | Van Gogh semi-transparent concentric arcs |
| **Linen Texture** | Light mode cross-hatch weave pattern |

---

## 📂 Project Structure

```
Bank2Mp3/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bank2mp3/app/
│       │   ├── MainActivity.kt       # UI + all effects
│       │   ├── BridgeClient.kt       # HTTP bridge client
│       │   └── PythonRuntime.kt      # Python runtime manager
│       ├── res/
│       │   ├── layout/activity_main.xml
│       │   ├── drawable/             # icons, backgrounds
│       │   ├── values/colors.xml     # color themes
│       │   └── raw/kunkun.mp3        # easter egg audio
│       └── jniLibs/                  # FMOD .so libs
├── scripts/
│   ├── terminal_server.py            # HTTP bridge server
│   ├── batch_convert.py              # batch conversion
│   └── fmod_extract.py               # FMOD parser
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🙏 Acknowledgments

These projects made Bank2Mp3 possible:

| Project | Role |
|---------|------|
| [FMOD](https://www.fmod.com/) | .bank audio engine core |
| [FFmpeg](https://ffmpeg.org/) | Multi-format audio transcoding |
| [ZeroTermux](https://github.com/hanxinhao000/ZeroTermux) | Android proot terminal |
| [Operit AI](https://github.com/Vael-Li/Operit) | AI assistant + workflow scheduler |
| [Android Jetpack](https://developer.android.com/jetpack) | ViewBinding · Coroutines · Lifecycle |
| [Kotlin](https://kotlinlang.org/) | Modern Android language |
| [Lucide Icons](https://lucide.dev/) | refresh-cw SVG icon |
| [Shadcn/ui](https://ui.shadcn.com/) | Design language reference |

Special thanks to all developers who helped debug Shizuku permissions, proot path mapping, and Android 14 install prompts at 3 AM 🌙

---

## 📄 License

MIT License · Copyright © 2025 MuXi

---

<p align="center">
  <sub>Made with ❤️ by <a href="https://github.com/MuXi36">MuXi36</a> · Powered by late-night coffee ☕</sub>
</p>