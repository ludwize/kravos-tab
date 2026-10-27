// ---------- library (Android: Google Drive folder "Kravos Tab" + files on the phone) ----------
const N=window.KravosNative;
const LIB={songs:[],cache:new Map(),first:true,want:null,state:{}};
const esc=s=>String(s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
function cleanName(n){return String(n).replace(/\.[^.]+$/,'').replace(/_/g,' ').replace(/\s+ver\s*\d+.*$/i,'').replace(/\s+by\s+\S+$/i,'').trim()}
function songTitle(s){const t=store.get('kt-title:'+s.id);return t?t.title+(t.artist?' · '+t.artist:''):cleanName(s.name)}
function renderShelf(){
  const sel=$('#songSel');
  const opts=LIB.songs.map(s=>`<option value="${esc(s.id)}">${esc(songTitle(s))}${s.local?' (телефон)':''}</option>`);
  if(!S.songId)opts.unshift('<option value="">Вежба R–5–8 (пример)</option>');
  sel.innerHTML=opts.join('');sel.value=S.songId||'';
  const cur=LIB.songs.find(s=>s.id===S.songId);$('#delBtn').hidden=!(cur&&cur.local);
}
function openSong(id){
  const data=LIB.cache.get(id);
  if(data){openSrc({kind:'bytes',data},id);afterOpen(id);return}
  LIB.want=id;$('#loading').hidden=false;N.load(id);
}
function afterOpen(id){
  if(S.score&&id)store.set('kt-title:'+id,{title:S.score.title||'',artist:S.score.artist||''});
  renderShelf();
}
window.KT={
  onSongs(list,err){
    LIB.songs=(list||[]).slice().sort((a,b)=>songTitle(a).localeCompare(songTitle(b)));
    if(LIB.first&&LIB.songs.length){LIB.first=false;const last=store.get('kt-last'),pick=LIB.songs.find(s=>s.id===last)||LIB.songs[0];openSong(pick.id)}
    renderShelf();updateSettings();
    if(err)toast(err,5000);
  },
  onSong(id,b64){
    let data;try{data=b64bytes(b64)}catch(e){toast('Песма није учитана.');return}
    LIB.cache.set(id,data);
    if(LIB.want===id){LIB.want=null;openSrc({kind:'bytes',data},id);afterOpen(id)}
  },
  onLocalAdded(id){LIB.cache.delete(id);openSong(id)},
  onError(msg){$('#loading').hidden=true;LIB.want=null;toast(msg,5000)},
  onAuth(state){LIB.state=state||{};updateSettings();if(LIB.state.connected)N.refresh()},
  onUpdate(info){LIB.update=info;updateSettings();if(info)$('#updBar').hidden=false},
  onUpdateProgress(p){$('#updBtn').textContent=p>=1?'Инсталирам…':'Преузимам '+Math.round(p*100)+'%'}
};
function updateSettings(){
  const st=LIB.state||{};
  $('#setDrive').textContent=st.connected?('Google Drive: повезан'+(st.account?' ('+st.account+')':'')):'Google Drive није повезан';
  $('#connectBtn').hidden=!!st.connected;
  $('#setVer').textContent='Верзија '+(st.version||'?');
  const u=LIB.update;
  $('#updText').textContent=u?('Нова верзија '+u.name):'';
}
let delArmed=false;
$('#delBtn').onclick=()=>{
  if(!delArmed){delArmed=true;$('#delBtn').textContent='Сигурно уклонити?';$('#delBtn').classList.add('warn');setTimeout(()=>{delArmed=false;$('#delBtn').textContent='Уклони';$('#delBtn').classList.remove('warn')},4000);return}
  delArmed=false;$('#delBtn').textContent='Уклони';$('#delBtn').classList.remove('warn');
  const id=S.songId;if(!id)return;N.removeLocal(id);LIB.cache.delete(id);S.songId=null;openSrc({kind:'tex',data:DEMO},null);
};
$('#songSel').onchange=e=>{const v=e.target.value;if(v)openSong(v);else openSrc({kind:'tex',data:DEMO},null)};
$('#addBtn').onclick=()=>N.openLocal();
$('#connectBtn').onclick=()=>N.connect();
$('#refreshBtn').onclick=()=>{N.refresh();toast('Освежавам листу…')};
$('#updBtn').onclick=()=>N.installUpdate();
$('#updLater').onclick=()=>{$('#updBar').hidden=true};
document.addEventListener('click',e=>{const m=$('#setMenu');if(m.open&&!m.contains(e.target))m.open=false});
function initLibrary(){
  if(!N){toast('Покрени ово у апликацији Kravos Tab.');return}
  try{LIB.state=JSON.parse(N.state())}catch(e){}
  updateSettings();
  N.refresh();
  setTimeout(()=>N.checkUpdate(),2500);
}

