const { app, BrowserWindow, ipcMain, shell, safeStorage } = require('electron');
const { spawn } = require('child_process');
const path = require('path'), fs = require('fs');
const { Client } = require('minecraft-launcher-core');
const { Auth } = require('msmc');

const ROOT = path.join(app.getPath('home'), '.scxfyy-launcher');
const GAME = path.join(ROOT, 'game'), SKINS = path.join(ROOT, 'skins'), CFG = path.join(ROOT, 'config.json');
const inst = id => path.join(GAME, 'instances', id);
const { srcHash } = require('./lib/modsrc');
const APP = app.isPackaged ? process.resourcesPath : __dirname;   // extra files live next to the app when installed
const BUILT = path.join(APP, 'mod', 'build', 'libs', 'scxfyy-client-1.0.0.jar');
const PRE = path.join(APP, 'prebuilt', 'scxfyy-client-1.0.0.jar'), HASHF = path.join(APP, 'prebuilt', 'source.hash');
const jarOk = () => { try { return fs.existsSync(PRE) && fs.readFileSync(HASHF, 'utf8') === srcHash(path.join(APP, 'mod')); } catch { return false; } };
const UA = { 'User-Agent': 'scxfyy-launcher/3.0' };
const DEF = { instances: [{ id: 'scxfyy', name: 'scxfyy', version: '1.21.4', fabric: true }],
  width: 1280, height: 720, fullscreen: false, fov: 70, gamma: 1, renderDistance: 12, maxFps: 260, vsync: true,
  guiScale: 0, server: 'strongmc.net', javaPath: '', jvmArgs: '', hideLauncher: true, cfKey: '' };
const load = () => { try { const s = JSON.parse(fs.readFileSync(CFG)); if (s.server === '' && !s.serverTouched) s.server = 'strongmc.net'; return { ...DEF, ...s }; } catch { return DEF; } };
const save = c => { fs.mkdirSync(ROOT, { recursive: true }); fs.writeFileSync(CFG, JSON.stringify(c, null, 1)); return c; };
const getI = id => load().instances.find(i => i.id === id);
const ACC = path.join(ROOT, 'account.json');
let win, auth = null, authAt = 0;

app.whenReady().then(() => {
  win = new BrowserWindow({ width: 1000, height: 680, minWidth: 860, minHeight: 560, frame: false, backgroundColor: '#16121a',
    title: 'scxfyy launcher', icon: path.join(__dirname, 'assets', 'icon.ico'), webPreferences: { preload: path.join(__dirname, 'preload.js') } });
  win.loadFile('index.html');
});
app.on('window-all-closed', () => app.quit());

ipcMain.handle('win', (e, a) => a === 'min' ? win.minimize() : a === 'max' ? (win.isMaximized() ? win.unmaximize() : win.maximize()) : win.close());
ipcMain.handle('cfg:get', () => load());
ipcMain.handle('cfg:set', (e, c) => save({ ...load(), ...c }));
ipcMain.handle('folder', (e, id) => { fs.mkdirSync(path.join(inst(id), 'mods'), { recursive: true }); shell.openPath(inst(id)); });
ipcMain.handle('inst:save', (e, d) => {
  const c = load(); let i = d.id && c.instances.find(x => x.id === d.id);
  if (i) Object.assign(i, { name: d.name, version: d.version, fabric: d.fabric });
  else {
    let id = d.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '') || 'instance', n = id, k = 2;
    while (c.instances.some(x => x.id === n)) n = id + '-' + k++;
    c.instances.push({ id: n, name: d.name, version: d.version, fabric: d.fabric });
  }
  return save(c);
});
ipcMain.handle('inst:del', (e, id) => {
  const c = load(); c.instances = c.instances.filter(i => i.id !== id);
  try { fs.rmSync(inst(id), { recursive: true, force: true }); } catch {}
  return save(c);
});
ipcMain.handle('versions', async () => {
  try {
    const m = await (await fetch('https://launchermeta.mojang.com/mc/game/version_manifest_v2.json')).json();
    const rel = m.versions.filter(v => v.type === 'release').map(v => v.id);
    const lo = rel.indexOf('1.21.4'), hi = Math.max(rel.indexOf('26.2'), 0);
    return lo < 0 ? rel.slice(hi) : rel.slice(hi, lo + 1);
  } catch { return ['1.21.4']; }
});
let saveErr = '';
const readAcc = () => { try { return JSON.parse(fs.readFileSync(ACC)); } catch { return null; } };
const readTok = () => { try { const a = readAcc(); return a && a.t ? safeStorage.decryptString(Buffer.from(a.t, 'base64')) : null; } catch { return null; } };
const writeAcc = (tok, name) => {
  try {
    if (!safeStorage.isEncryptionAvailable()) { saveErr = 'Windows could not encrypt the login, so it was not saved.'; return; }
    if (!tok) { saveErr = 'Microsoft did not return a token to remember.'; return; }
    if (typeof tok !== 'string') tok = JSON.stringify(tok);
    fs.mkdirSync(ROOT, { recursive: true });
    fs.writeFileSync(ACC, JSON.stringify({ t: safeStorage.encryptString(tok).toString('base64'), name })); saveErr = '';
  } catch (e) { saveErr = 'Could not save the login: ' + e.message; }
};
async function useXbox(x) {
  const t = await x.getMinecraft(); auth = t.mclc(); authAt = Date.now();
  let tok; try { tok = x.save(); } catch (e) { saveErr = 'Could not read the login token: ' + e.message; }
  writeAcc(tok, t.profile.name); return t.profile.name;
}
async function freshAuth() {
  if (auth && Date.now() - authAt < 3600e3) return;
  const tok = readTok(); if (!tok) return;
  try { await useXbox(await new Auth('select_account').refresh(tok)); } catch {}
}
ipcMain.handle('login', async () => {
  try { saveErr = ''; const name = await useXbox(await new Auth('select_account').launch('electron')); return { name, warn: saveErr }; }
  catch (err) { return { error: String(err.message || err) }; }
});
ipcMain.handle('login:cached', () => { const a = readAcc(); return a && a.name ? a.name : null; });
ipcMain.handle('login:auto', async () => {
  const tok = readTok(); if (!tok) return {};
  try { return { name: await useXbox(await new Auth('select_account').refresh(tok)) }; }
  catch (e) { return { error: 'Saved login could not be refreshed: ' + String(e.message || e) + '. Sign in again.' }; }
});
ipcMain.handle('logout', () => { auth = null; try { fs.unlinkSync(ACC); } catch {} });

async function ensureFabric(mc) {
  const j = await (await fetch(`https://meta.fabricmc.net/v2/versions/loader/${mc}`)).json();
  if (!j.length) throw new Error('Fabric does not support ' + mc + ' yet. Edit the instance and untick Fabric.');
  const lv = j[0].loader.version, id = `fabric-loader-${lv}-${mc}`;
  const dir = path.join(GAME, 'versions', id); fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, id + '.json'), await (await fetch(`https://meta.fabricmc.net/v2/versions/loader/${mc}/${lv}/profile/json`)).text());
  return id;
}
ipcMain.handle('launch', async (e, id, srv) => {
  await freshAuth();
  if (!auth) return { error: 'Sign in with Microsoft first (Accounts, bottom left).' };
  try {
    const i = getI(id), c = { ...load(), ...i, ...(srv ? { server: srv } : {}) }, dir = inst(i.id), log = m => win.webContents.send('log', m);
    const version = { number: c.version, type: 'release' };
    if (c.fabric) {
      version.custom = await ensureFabric(c.version);
      const md = path.join(dir, 'mods'); fs.mkdirSync(md, { recursive: true });
      if (!fs.readdirSync(md).some(f => /^fabric-api/i.test(f))) {
        log('Installing Fabric API...'); await mrInstall('P7dR8mSH', c.version, new Set(), dir).catch(x => log('Fabric API: ' + x.message));
      }
      if (c.version !== '1.21.4') log('NOTE: the in-game menu only works on Minecraft 1.21.4. This instance is ' + c.version + '.');
      else {
        let jar = jarOk() ? PRE : null;
        if (!jar) { log('Preparing the in-game menu. This happens once and takes a few minutes...'); if (await buildMod(log) || fs.existsSync(PRE)) jar = PRE; }
        if (jar) { fs.copyFileSync(jar, path.join(md, path.basename(jar))); log('Added the scxfyy client mod (Right Shift menu).'); }
        else log('NOTE: the in-game menu could not be built, so it is missing this time. See the log above.');
      }
    }
    if (!c.fabric) log('NOTE: tick Fabric on this instance (gear icon) to use the in-game menu.');
    const l = new Client();
    l.on('debug', log); l.on('data', log);
    l.on('close', () => { win.show(); win.webContents.send('closed', id); });
    await l.launch({ authorization: auth, root: GAME, overrides: { gameDirectory: dir }, version,
      memory: { max: '4G', min: '2G' }, javaPath: c.javaPath || 'java', customArgs: c.jvmArgs.split(' ').filter(Boolean),
      customLaunchArgs: c.server ? ['--quickPlayMultiplayer', c.server] : [],
      window: { width: c.width, height: c.height, fullscreen: c.fullscreen } });
    if (c.hideLauncher) win.hide();
    return { ok: true };
  } catch (err) { return { error: String(err.message || err) }; }
});

function buildMod(log) {
  return new Promise(res => {
    if (process.platform !== 'win32') { log('Auto-build is Windows only for now.'); return res(false); }
    const p = spawn('cmd', ['/c', 'build-mod.bat', 'nopause'], { cwd: APP });
    p.stdout.on('data', d => log(d.toString().trimEnd())); p.stderr.on('data', d => log(d.toString().trimEnd()));
    p.on('error', e => { log('Could not start the build: ' + e.message); res(false); });
    p.on('close', code => {
      if (code === 0 && fs.existsSync(BUILT)) { fs.mkdirSync(path.dirname(PRE), { recursive: true }); fs.copyFileSync(BUILT, PRE); fs.writeFileSync(HASHF, srcHash(path.join(APP, 'mod'))); log('Build finished. The in-game menu is saved in the prebuilt folder.'); res(true); }
      else { log('Build failed (code ' + code + '). Copy the log above and send it to Claude.'); res(false); }
    });
  });
}
ipcMain.handle('buildmod', async () => ({ ok: await buildMod(m => win.webContents.send('log', m)) }));

// ---- skins ----
const bearer = () => ({ Authorization: 'Bearer ' + auth.access_token });
ipcMain.handle('skin:current', async () => {
  await freshAuth();
  if (!auth) return { error: 'Sign in with Microsoft to see your skin.' };
  try {
    const p = await (await fetch('https://api.minecraftservices.com/minecraft/profile', { headers: bearer() })).json();
    const s = (p.skins || []).find(x => x.state === 'ACTIVE'); if (!s) return {};
    const b = Buffer.from(await (await fetch(s.url)).arrayBuffer()).toString('base64');
    return { data: 'data:image/png;base64,' + b, variant: s.variant === 'SLIM' ? 'slim' : 'classic' };
  } catch (err) { return { error: err.message }; }
});
ipcMain.handle('skin:list', () => {
  fs.mkdirSync(SKINS, { recursive: true });
  return fs.readdirSync(SKINS).filter(f => f.endsWith('.png')).map(f => ({ file: f, name: f.split('.')[0],
    variant: f.includes('.slim.') ? 'slim' : 'classic', data: 'data:image/png;base64,' + fs.readFileSync(path.join(SKINS, f)).toString('base64') }));
});
async function uploadSkin(buf, variant) {
  await freshAuth();
  if (!auth) throw new Error('Sign in with Microsoft first.');
  const fd = new FormData(); fd.append('variant', variant);
  fd.append('file', new Blob([buf], { type: 'image/png' }), 'skin.png');
  const r = await fetch('https://api.minecraftservices.com/minecraft/profile/skins', { method: 'POST', headers: bearer(), body: fd });
  if (!r.ok) throw new Error('Skin upload failed (' + r.status + ')');
}
ipcMain.handle('skin:add', async (e, name, variant, bytes) => {
  try {
    const buf = Buffer.from(bytes); fs.mkdirSync(SKINS, { recursive: true });
    fs.writeFileSync(path.join(SKINS, `${name.replace(/[^\w-]/g, '_') || 'skin'}.${variant}.png`), buf);
    await uploadSkin(buf, variant); return { ok: true };
  } catch (err) { return { error: err.message }; }
});
ipcMain.handle('skin:apply', async (e, file) => {
  try { await uploadSkin(fs.readFileSync(path.join(SKINS, path.basename(file))), file.includes('.slim.') ? 'slim' : 'classic'); return { ok: true }; }
  catch (err) { return { error: err.message }; }
});
ipcMain.handle('skin:delete', (e, file) => { fs.unlinkSync(path.join(SKINS, path.basename(file))); });

// ---- mods ----
ipcMain.handle('mods:list', (e, iid) => { try { return fs.readdirSync(path.join(inst(iid), 'mods')).filter(f => f.endsWith('.jar')); } catch { return []; } });
ipcMain.handle('mods:del', (e, iid, f) => { try { fs.unlinkSync(path.join(inst(iid), 'mods', path.basename(f))); } catch {} });
async function download(url, name, dir) {
  fs.mkdirSync(path.join(dir, 'mods'), { recursive: true });
  const r = await fetch(url, { headers: UA }); if (!r.ok) throw new Error('Download failed: ' + r.status);
  fs.writeFileSync(path.join(dir, 'mods', name), Buffer.from(await r.arrayBuffer()));
}
ipcMain.handle('mr:search', async (e, q, v) => {
  const f = encodeURIComponent(JSON.stringify([['project_type:mod'], ['categories:fabric'], [`versions:${v}`]]));
  const r = await (await fetch(`https://api.modrinth.com/v2/search?query=${encodeURIComponent(q)}&limit=20&facets=${f}`, { headers: UA })).json();
  return r.hits.map(h => ({ id: h.project_id, title: h.title, desc: h.description, icon: h.icon_url }));
});
async function mrInstall(id, v, seen, dir) {
  if (seen.has(id)) return []; seen.add(id);
  const q = `loaders=${encodeURIComponent('["fabric"]')}&game_versions=${encodeURIComponent(`["${v}"]`)}`;
  const vs = await (await fetch(`https://api.modrinth.com/v2/project/${id}/version?${q}`, { headers: UA })).json();
  if (!vs.length) throw new Error(`No Fabric build for ${v}`);
  const f = vs[0].files.find(x => x.primary) || vs[0].files[0];
  await download(f.url, f.filename, dir);
  let got = [f.filename];
  for (const d of vs[0].dependencies.filter(d => d.dependency_type === 'required' && d.project_id))
    got = got.concat(await mrInstall(d.project_id, v, seen, dir).catch(() => []));
  return got;
}
ipcMain.handle('mr:install', async (e, pid, iid) => {
  try { const i = getI(iid); return { ok: (await mrInstall(pid, i.version, new Set(), inst(i.id))).join(', ') }; }
  catch (err) { return { error: err.message }; }
});
const cf = (p, key) => fetch('https://api.curseforge.com/v1' + p, { headers: { 'x-api-key': key, Accept: 'application/json' } }).then(r => r.json());
ipcMain.handle('cf:search', async (e, q, v) => {
  const key = load().cfKey; if (!key) return { error: 'Add your CurseForge API key in Settings first.' };
  const r = await cf(`/mods/search?gameId=432&classId=6&searchFilter=${encodeURIComponent(q)}&gameVersion=${v}&modLoaderType=4&pageSize=20`, key);
  return (r.data || []).map(m => ({ id: m.id, title: m.name, desc: m.summary, icon: m.logo && m.logo.thumbnailUrl }));
});
ipcMain.handle('cf:install', async (e, pid, iid) => {
  try {
    const i = getI(iid);
    const r = await cf(`/mods/${pid}/files?gameVersion=${i.version}&modLoaderType=4&pageSize=1`, load().cfKey);
    const f = r.data && r.data[0]; if (!f) throw new Error(`No Fabric file for ${i.version}`);
    if (!f.downloadUrl) throw new Error('The author disabled third-party downloads. Get it from curseforge.com.');
    await download(f.downloadUrl, f.fileName, inst(i.id)); return { ok: f.fileName };
  } catch (err) { return { error: err.message }; }
});
