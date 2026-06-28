const fs = require('fs');
const code = `(() => {
  const el = document.querySelector('div[style*="max-height: 80vh"]');
  if (!el) return 'not found';
  return JSON.stringify({scrollHeight: el.scrollHeight, clientHeight: el.clientHeight, scrollTop: el.scrollTop, className: el.className});
})()`;
const b64 = Buffer.from(code, 'utf-8').toString('base64');
fs.writeFileSync('check_modal.b64', b64);
console.log(b64);