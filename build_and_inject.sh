!/bin/bash
# ponytail: AGP 8.x 构建后注入 .so + zipalign + 重签名
set -e
cd /storage/emulated/0/MT2/mcp/apk_build/bank/Bank2Mp3
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64

echo ">>> Building..."
bash gradlew assembleDebug -q 2>&1 | tail -1

APK=app/build/outputs/apk/debug/app-debug.apk
LIBS=app/build/intermediates/merged_native_libs/debug/out/lib
TMPDIR=/tmp/apk_inject_$$
rm -rf "$TMPDIR"; mkdir -p "$TMPDIR"; cd "$TMPDIR"

unzip -q "$OLDPWD/$APK"
cp -r "$OLDPWD/$LIBS" ./
echo "  +$(find lib/ -name '*.so' | wc -l) .so files"

zip -r -0 -q unsigned.apk .

zipalign -p -f 4 unsigned.apk aligned.apk

apksigner sign \
  --ks ~/.android/debug.keystore \
  --ks-pass pass:android \
  --ks-key-alias androiddebugkey \
  --key-pass pass:android \
  aligned.apk

mv aligned.apk /sdcard/Download/Bank2Mp3.apk
cd /; rm -rf "$TMPDIR"
echo ">>> $(ls -lh /sdcard/Download/Bank2Mp3.apk | awk '{print $5}')"
