const fs = require('fs');
const code = `(() => {
  const bonusSection = document.evaluate("//span[contains(text(), '奖金记录')]/ancestor::div[contains(@class, 'bg-white/20')]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
  if (!bonusSection) return 'bonus section not found';
  const btn = bonusSection.querySelector('button');
  if (!btn) return 'button not found';
  btn.click();
  return 'clicked';
})()`;
const b64 = Buffer.from(code, 'utf-8').toString('base64');
fs.writeFileSync('click_add_bonus.b64', b64);
console.log(b64);