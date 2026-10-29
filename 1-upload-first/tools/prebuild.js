const fs = require('fs'), path = require('path'), { spawnSync } = require('child_process'), { srcHash } = require('../lib/modsrc');
const root = path.join(__dirname, '..'), mod = path.join(root, 'mod'), pre = path.join(root, 'prebuilt');
const jar = path.join(pre, 'scxfyy-client-1.0.0.jar'), hf = path.join(pre, 'source.hash'), built = path.join(mod, 'build', 'libs', 'scxfyy-client-1.0.0.jar');
const cur = srcHash(mod); let ok = false;
try { ok = fs.existsSync(jar) && fs.readFileSync(hf, 'utf8') === cur; } catch {}
if (!ok) {
  console.log('Building the in-game menu first. This takes a few minutes...');
  const r = spawnSync('cmd', ['/c', 'build-mod.bat', 'nopause'], { cwd: root, stdio: 'inherit' });
  if (r.status !== 0 || !fs.existsSync(built)) { console.error('The in-game menu build failed. Copy the messages above and send them to Claude, then run build-exe.bat again.'); process.exit(1); }
  fs.mkdirSync(pre, { recursive: true }); fs.copyFileSync(built, jar); fs.writeFileSync(hf, cur);
}
console.log('In-game menu is ready.');
