const fs = require('fs');
const code = `document.querySelector('div[style*="max-height: 80vh"]')?.scrollBy(0, 300)`;
const b64 = Buffer.from(code, 'utf-8').toString('base64');
fs.writeFileSync('scroll_modal.b64', b64);
console.log(b64);