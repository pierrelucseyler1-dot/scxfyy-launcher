const { contextBridge, ipcRenderer: ipc } = require('electron');
const call = (n, ...a) => ipc.invoke(n, ...a);
contextBridge.exposeInMainWorld('api', {
  win: a => call('win', a), getCfg: () => call('cfg:get'), setCfg: c => call('cfg:set', c), versions: () => call('versions'),
  login: () => call('login'), autoLogin: () => call('login:auto'), cachedAccount: () => call('login:cached'), logout: () => call('logout'), launch: (id, srv) => call('launch', id, srv), folder: id => call('folder', id),
  buildMod: () => call('buildmod'),
  modsList: i => call('mods:list', i), modsDel: (i, f) => call('mods:del', i, f),
  saveInst: d => call('inst:save', d), delInst: id => call('inst:del', id),
  search: (s, q, v) => call(s + ':search', q, v), install: (s, pid, iid) => call(s + ':install', pid, iid),
  skin: { current: () => call('skin:current'), list: () => call('skin:list'), add: (n, v, b) => call('skin:add', n, v, b),
    apply: f => call('skin:apply', f), del: f => call('skin:delete', f) },
  onLog: fn => ipc.on('log', (e, m) => fn(String(m))), onClosed: fn => ipc.on('closed', (e, id) => fn(id))
});
