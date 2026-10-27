#!/usr/bin/env bash
# Installs the APK on the emulator, opens the app, walks the main controls, records screenshots and crashes.
set -u
OUT=smoke
mkdir -p "$OUT"
APK=$(ls out/*.apk | head -1)
PKG=net.kravos.tab

adb install -r "$APK" > "$OUT/install.txt" 2>&1
adb logcat -c
adb shell am start -W -n $PKG/.MainActivity > "$OUT/start.txt" 2>&1
sleep 30

shot() { adb exec-out screencap -p > "$OUT/$1.png"; echo "shot $1" >> "$OUT/steps.txt"; }
dump() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null 2>&1; }
tap_text() {
  dump
  python3 - "$1" "$OUT/ui.xml" <<'PY' > "$OUT/tap.txt"
import re, sys, xml.etree.ElementTree as ET
want, path = sys.argv[1], sys.argv[2]
try:
    root = ET.parse(path).getroot()
except Exception:
    sys.exit(0)
for n in root.iter('node'):
    if (n.get('text') or '').strip() == want or (n.get('content-desc') or '').strip() == want:
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
PY
  if [ -s "$OUT/tap.txt" ]; then adb shell input tap $(cat "$OUT/tap.txt"); echo "tapped $1" >> "$OUT/steps.txt"; else echo "not found: $1" >> "$OUT/steps.txt"; fi
  sleep 3
}

shot 01-start
tap_text "Pusti"; sleep 6; shot 02-playing
tap_text "Pauza"; sleep 1
tap_text "Detalji"; sleep 2; shot 03-details
tap_text "Sve funkcije nota (3, b7, prolazne…)"; sleep 5; shot 04-all-functions
tap_text "Detalji"; sleep 1
tap_text "Podešavanja"; sleep 2; shot 05-settings
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell am start -n $PKG/.MainActivity >/dev/null 2>&1; sleep 2
adb shell settings put system user_rotation 1; adb shell settings put system accelerometer_rotation 0; sleep 6; shot 06-landscape

PID=$(adb shell pidof $PKG | tr -d '\r')
echo "pid=$PID" > "$OUT/status.txt"
adb logcat -d > "$OUT/logcat.txt"
grep -n -A 40 "FATAL EXCEPTION" "$OUT/logcat.txt" > "$OUT/crash.txt" || true
grep -n -E "KravosTab|chromium|Console|AndroidRuntime|net.kravos" "$OUT/logcat.txt" | tail -400 > "$OUT/app-log.txt" || true
if [ -s "$OUT/crash.txt" ]; then echo "CRASH" >> "$OUT/status.txt"; fi
rm -f "$OUT/logcat.txt" "$OUT/ui.xml"
cat "$OUT/status.txt" "$OUT/steps.txt"
exit 0
