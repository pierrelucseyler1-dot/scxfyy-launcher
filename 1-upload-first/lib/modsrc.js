const fs = require('fs'), path = require('path'), crypto = require('crypto');
exports.srcHash = modDir => {
  const h = crypto.createHash('sha1');
  const walk = d => { for (const f of fs.readdirSync(d, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name))) { const p = path.join(d, f.name); if (f.isDirectory()) walk(p); else h.update(f.name).update(fs.readFileSync(p)); } };
  try { walk(path.join(modDir, 'src')); ['build.gradle', 'gradle.properties'].forEach(n => h.update(fs.readFileSync(path.join(modDir, n)))); } catch {}
  return h.digest('hex');
};
