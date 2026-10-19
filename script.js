const letterText = `Hola amor kjasdjka
solo queria agradecerte por todo este tiempo derverdad me haces muy feliz cada dia cntg se siente muybien m siento comodo contigo,te volviste como mi hogar en este tiempo encerio te amo, quizas esto no sea mucho para ti pero quize hacerte algo te amo mucho nunca lo olvides siemrpe estare para ti para lo que nesesites. Te amo,`;

const scene = document.getElementById("scene");
const message = document.getElementById("message");
let opened = false;

function typeMessage() {
  let index = 0;
  const timer = window.setInterval(() => {
    message.textContent = letterText.slice(0, index);
    index += 1;
    if (index > letterText.length) window.clearInterval(timer);
  }, 22);
}

function openLetter() {
  if (opened) return;
  opened = true;
  scene.classList.add("open");
  window.setTimeout(typeMessage, 1500);
}

scene.addEventListener("pointerup", openLetter);
scene.addEventListener("keydown", event => {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    openLetter();
  }
});
