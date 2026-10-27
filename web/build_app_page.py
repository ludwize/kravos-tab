"""Builds the app page and the web site from the shared page source (web/page.src.html).
The same source is used for the Claude artifact; here the library comes from the Android bridge
(or, for the site, web/web_native.js, which mimics that bridge with Google sign-in + Drive REST)
and every Cyrillic string is transliterated to Serbian Latin.

usage: build_app_page.py            -> app/src/main/assets/web/index.html
       build_app_page.py --site DIR -> DIR/index.html + assets (GitHub Pages, tab.kravos.net)"""
import pathlib, re, shutil, sys
SITE = sys.argv[2] if len(sys.argv) > 2 and sys.argv[1] == '--site' else None
ROOT = pathlib.Path(__file__).resolve().parent.parent
src = (ROOT / 'web/page.src.html').read_text()
adapter = (ROOT / 'web/app_adapter.js').read_text()
lessons = (ROOT / 'web/lessons.js').read_text()

def rep(a, b):
    global src
    if a not in src:
        sys.exit('missing: ' + a[:60])
    src = src.replace(a, b, 1)

# offline assets
src = re.sub(r'<link data-kt rel="(preconnect|stylesheet)"[^>]*>\n', '', src)
rep('<script data-kt src="https://cdn.jsdelivr.net/npm/@coderline/alphatab@1.8.4/dist/alphaTab.min.js"></script>',
    '<script src="alphaTab.min.js"></script>')
src = re.sub(r'<script data-kt>\nconst BRAVURA_B64="__BRAVURA__";\nconst SF3_B64="__SF3__";\n</script>\n', '', src)
rep("st.core.smuflFontSources=new Map([[AT.FontFileFormat.Woff2,'data:font/woff2;base64,'+BRAVURA_B64]]);",
    "st.core.smuflFontSources=new Map([[AT.FontFileFormat.Woff2,new URL('Bravura.woff2',location.href).href]]);")
rep("try{api.loadSoundFont(b64bytes(SF3_B64),false)}catch(e){console.error(e)}",
    "fetch('sonivox.sf3').then(r=>r.arrayBuffer()).then(b=>api.loadSoundFont(new Uint8Array(b),false)).catch(e=>console.error(e));")
rep('__LESSONS__', lessons)
rep('const PRISTINE=[...document.querySelectorAll("[data-kt]")].map(e=>e.outerHTML).join("\\n");\n', '')

# header: phone buttons instead of file input / HTML export
rep('''    <label class="btn" for="fGp"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>Додај песму</label>
    <input type="file" id="fGp" accept=".gp,.gpx,.gp5,.gp4,.gp3,.xml,.musicxml,.mxl">''',
'''    <button class="btn" id="addBtn"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>Са телефона</button>''')
src = re.sub(r'\n    <button class="btn" id="saveBtn" hidden>.*?</button>', '''
    <details class="more set" id="setMenu"><summary class="btn" aria-label="Подешавања"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></svg></summary>
      <div class="menu down">
        <span id="setDrive"></span>
        <button class="btn" id="connectBtn">Повежи Google Drive</button>
        <button class="btn" id="refreshBtn">Освежи листу песама</button>
        <span class="lbl">Песме се читају из фолдера „Kravos Tab“ на твом Drive-у.</span>
        <span class="lbl" id="setVer"></span>
      </div>
    </details>''', src, count=1, flags=re.S)
rep('<div data-kt class="hint gut" id="hint">', '''<div class="upd gut" id="updBar" hidden><span id="updText"></span><button class="btn warn" id="updBtn">Инсталирај</button><button class="btn quiet" id="updLater">Касније</button></div>
<div data-kt class="hint gut" id="hint">''')
rep('.more .menu label{', '''.more .menu.down{bottom:auto;top:calc(100% + 8px);left:auto;right:0}
.set .menu span{font-size:14px}
.upd{display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding-block:8px;background:color-mix(in srgb,var(--accent) 14%,var(--bg));font-size:14px}
.more .menu label{''')

# library: Android bridge instead of artifact storage + HTML export
i = src.index('// ---------- library (saved in the artifact) ----------')
j = src.index('// ---------- transport ----------')
src = src[:i] + adapter + src[j:]

if SITE:
    rep('Са телефона', 'Са рачунара')
    src = src.replace("' (телефон)'", "' (рачунар)'").replace("Покрени ово у апликацији Kravos Tab.", "Google пријава није доступна.")
    shim = (ROOT / 'web/web_native.js').read_text()
    import os
    shim = shim.replace('__GOOGLE_WEB_CLIENT_ID__', os.environ.get('GOOGLE_WEB_CLIENT_ID', '__GOOGLE_WEB_CLIENT_ID__'))
    rep('<script src="alphaTab.min.js"></script>',
        '<script src="https://accounts.google.com/gsi/client" async></script>\n<script>\n' + shim + '</script>\n<script src="alphaTab.min.js"></script>')
    rep('<title data-kt>Kravos Tab</title>', '<title data-kt>Kravos Tab</title>\n<meta name="robots" content="noindex">')

# Cyrillic -> Serbian Latin (UI text of the app is Latin)
CY = dict(zip('абвгдђежзијклљмнњопрстћуфхцчџшАБВГДЂЕЖЗИЈКЛЉМНЊОПРСТЋУФХЦЧЏШ',
    ['a','b','v','g','d','đ','e','ž','z','i','j','k','l','lj','m','n','nj','o','p','r','s','t','ć','u','f','h','c','č','dž','š',
     'A','B','V','G','D','Đ','E','Ž','Z','I','J','K','L','Lj','M','N','Nj','O','P','R','S','T','Ć','U','F','H','C','Č','Dž','Š']))
src = ''.join(CY.get(ch, ch) for ch in src)
src = src.replace('<html lang="sr">', '<html lang="sr-Latn">')

page = ('<!doctype html><html lang="sr-Latn"><head><meta charset="utf-8">'
        '<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">'
        '<style>:root{color-scheme:light}body{margin:0}[hidden]{display:none!important}</style></head><body>\n'
        + src + '\n</body></html>\n')
if SITE:
    out_dir = pathlib.Path(SITE)
    out_dir.mkdir(parents=True, exist_ok=True)
    for f in ('alphaTab.min.js', 'Bravura.woff2', 'sonivox.sf3'):
        shutil.copy(ROOT / 'app/src/main/assets/web' / f, out_dir / f)
    (out_dir / 'CNAME').write_text('tab.kravos.net\n')
    (out_dir / 'robots.txt').write_text('User-agent: *\nDisallow: /\n')
    (out_dir / '.nojekyll').write_text('')
    out = out_dir / 'index.html'
else:
    out = ROOT / 'app/src/main/assets/web/index.html'
out.write_text(page)
print('wrote', out, len(page))
