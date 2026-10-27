// Web stand-in for the Android bridge (window.KravosNative), so the same page runs at tab.kravos.net.
// Google sign-in with Google Identity Services (token flow, drive.readonly), Drive REST from the browser.
(()=>{
const CLIENT_ID='__GOOGLE_WEB_CLIENT_ID__';
const SCOPE='https://www.googleapis.com/auth/drive.readonly';
const FOLDER='Kravos Tab',EXT=['gp','gpx','gp5','gp4','gp3','xml','musicxml','mxl'];
const API='https://www.googleapis.com/drive/v3';
const ss={get(k){try{return JSON.parse(sessionStorage.getItem(k))}catch(e){return null}},set(k,v){try{sessionStorage.setItem(k,JSON.stringify(v))}catch(e){}}};
const ls={get(k){try{return JSON.parse(localStorage.getItem(k))}catch(e){return null}},set(k,v){try{localStorage.setItem(k,JSON.stringify(v))}catch(e){}}};
let tok=ss.get('kt-token'),tokenClient=null,songs=[],local=[],files=new Map(),afterToken=null;
const KT=()=>window.KT||{};
const valid=()=>tok&&tok.exp>Date.now()+60000;
function b64(u){let s='';for(let i=0;i<u.length;i+=0x8000)s+=String.fromCharCode.apply(null,u.subarray(i,i+0x8000));return btoa(s)}
function emitSongs(err){const list=songs.concat(local).map(s=>({id:s.id,name:s.name,local:!!s.local,cached:files.has(s.id)}));KT().onSongs&&KT().onSongs(list,err||null)}
function state(){return {connected:valid(),account:ls.get('kt-account')||'',version:'web'}}
function initGis(){
  if(tokenClient||!(window.google&&google.accounts&&google.accounts.oauth2))return !!tokenClient;
  tokenClient=google.accounts.oauth2.initTokenClient({client_id:CLIENT_ID,scope:SCOPE,callback:r=>{
    if(r&&r.access_token){tok={t:r.access_token,exp:Date.now()+(r.expires_in||3600)*1000};ss.set('kt-token',tok);ls.set('kt-was-connected',true);
      api('/about?fields=user(emailAddress)').then(a=>{ls.set('kt-account',a.user&&a.user.emailAddress||'');KT().onAuth&&KT().onAuth(state())}).catch(()=>{});
      KT().onAuth&&KT().onAuth(state());const f=afterToken;afterToken=null;if(f)f()}
    else KT().onError&&KT().onError('Prijava na Google nije uspela.')},
    error_callback:e=>{afterToken=null;KT().onError&&KT().onError(e&&e.type==='popup_closed'?'Prijava je prekinuta.':'Prijava na Google nije uspela.')}});
  return true;
}
function needToken(then){
  if(valid())return then();
  if(!initGis()){KT().onError&&KT().onError('Google prijava se još učitava, pokušaj ponovo.');return}
  afterToken=then;tokenClient.requestAccessToken({prompt:ls.get('kt-was-connected')?'':'consent'});
}
async function api(path,raw){
  const r=await fetch(API+path,{headers:{Authorization:'Bearer '+tok.t}});
  if(r.status===401){tok=null;ss.set('kt-token',null);throw new Error('auth')}
  if(!r.ok)throw new Error('HTTP '+r.status);
  return raw?new Uint8Array(await r.arrayBuffer()):r.json();
}
async function query(q){
  const out=[];let page='';
  do{const j=await api('/files?pageSize=200&supportsAllDrives=true&includeItemsFromAllDrives=true&fields=nextPageToken,files(id,name,modifiedTime)&orderBy=name&q='+encodeURIComponent(q)+(page?'&pageToken='+page:''));
    out.push(...(j.files||[]));page=j.nextPageToken||''}while(page);
  return out;
}
async function listDrive(){
  const folders=await query("name = '"+FOLDER+"' and mimeType = 'application/vnd.google-apps.folder' and trashed = false");
  if(!folders.length)return emitSongs('Na Drive-u nema foldera „'+FOLDER+'“. Napravi ga i ubaci .gp fajlove.');
  const all=[];for(const f of folders)all.push(...await query("'"+f.id+"' in parents and trashed = false and mimeType != 'application/vnd.google-apps.folder'"));
  songs=all.filter(f=>EXT.includes((f.name.split('.').pop()||'').toLowerCase())).map(f=>({id:f.id,name:f.name}));
  emitSongs(songs.length?null:'Folder „'+FOLDER+'“ je prazan. Ubaci u njega .gp fajlove.');
}
const picker=document.createElement('input');picker.type='file';picker.accept='.gp,.gpx,.gp5,.gp4,.gp3,.xml,.musicxml,.mxl';picker.hidden=true;
picker.onchange=async()=>{const f=picker.files[0];picker.value='';if(!f)return;const id='local-'+Date.now(),data=new Uint8Array(await f.arrayBuffer());
  files.set(id,data);local.push({id,name:f.name,local:true});emitSongs();KT().onLocalAdded&&KT().onLocalAdded(id)};
document.addEventListener('DOMContentLoaded',()=>document.body.appendChild(picker));
window.KravosNative={
  state:()=>JSON.stringify(state()),
  connect(){needToken(()=>listDrive().catch(e=>emitSongs('Drive nije dostupan.')))},
  refresh(){
    if(!valid()&&!ls.get('kt-was-connected')){emitSongs(local.length?null:'Poveži Google Drive (⚙ gore desno) ili otvori fajl sa računara.');return}
    if(!valid()){emitSongs('Prijava je istekla. ⚙ → Poveži Google Drive.');return}
    listDrive().catch(e=>emitSongs(e.message==='auth'?'Prijava je istekla. ⚙ → Poveži Google Drive.':'Drive nije dostupan.'));
  },
  load(id){
    if(files.has(id)){setTimeout(()=>KT().onSong(id,b64(files.get(id))));return}
    needToken(()=>api('/files/'+encodeURIComponent(id)+'?alt=media',true).then(d=>{files.set(id,d);KT().onSong(id,b64(d))})
      .catch(()=>KT().onError&&KT().onError('Pesma nije preuzeta. Pokušaj ponovo.')));
  },
  openLocal(){picker.click()},
  removeLocal(id){local=local.filter(s=>s.id!==id);files.delete(id);emitSongs()},
  checkUpdate(){},
  installUpdate(){}
};
})();
