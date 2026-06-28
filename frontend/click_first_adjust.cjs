const fs = require('fs');
const code = `document.evaluate("//button[contains(., '调整')]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue?.click()`;
const b64 = Buffer.from(code, 'utf-8').toString('base64');
fs.writeFileSync('click_first_adjust.b64', b64);
console.log(b64);