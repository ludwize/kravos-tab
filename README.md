# Kravos Tab

Personal Android bass tab player in the spirit of Songsterr: full bass tab, MIDI band to play along with,
chord names and note functions (R, 5, b3, b7…) on the tab, a one-line hint per song section, tempo and loop.

- Songs come from the **"Kravos Tab" folder on Google Drive** (Guitar Pro .gp/.gpx/.gp5/.gp4/.gp3 or MusicXML).
  Drop a file there from the computer and it shows up on the phone after "Osveži". Opened songs are kept for offline use.
- Files can also be opened straight from the phone.
- The same page runs as a Claude artifact on the computer (`web/page.src.html`); `web/build_app_page.py`
  turns it into the offline app page (`app/src/main/assets/web/index.html`, Serbian Latin UI).
- Rendering and playback: [alphaTab](https://alphatab.net) 1.8.4 (MPL-2.0) with the bundled Sonivox soundfont.

## One-time Google Cloud setup
Same project as Kravos Player (Drive API and consent screen already set up):
**Clients → Create client → Android**, package `net.kravos.tab`,
SHA-1 `67:E5:37:7F:F3:B2:79:DA:30:68:A0:17:53:5C:FB:6B:45:5B:A5:76`.

## Building
CI builds a signed APK on every push to `main` and publishes it as GitHub Release `build-N`;
the app updates itself from there. The keystore is stored encrypted in `signing/release.jks.enc`;
CI needs the repository secret `KP_SIGNING_PASS`.
