(function(){
  let toastTimer;
  const showSheet=id=>{const el=document.getElementById(id);el.classList.add('show');el.setAttribute('aria-hidden','false');document.body.style.overflow='hidden'};
  const closeSheets=()=>{document.querySelectorAll('.sheet.show').forEach(el=>{el.classList.remove('show');el.setAttribute('aria-hidden','true')});document.body.style.overflow=''};
  const toast=message=>{const el=document.getElementById('toast');el.textContent=message;el.classList.add('show');clearTimeout(toastTimer);toastTimer=setTimeout(()=>el.classList.remove('show'),1800)};
  const topbar=(title,sub,action='')=>`<div class="topbar"><div><h1>${title}</h1><p>${sub||''}</p></div>${action}</div>`;
  const card=r=>`<article class="work-card" data-record-id="${r.id}" style="--color:${r.p.color};--value:${Math.min(r.efficiency,100)}"><div class="work-card-head"><div class="work-card-title"><i class="project-dot"></i>${r.p.name}</div><div class="work-efficiency">${Math.round(r.efficiency)}%</div></div><div class="work-meta">工时 ${Calc.duration(r.minutes)}　实际 ${Calc.number(r.actual)}${r.p.unit}　标准 ${Calc.number(r.standard)}${r.p.unit}</div><div class="progress"><i></i></div></article>`;
  window.UI={showSheet,closeSheets,toast,topbar,card};
  document.querySelectorAll('[data-close-sheet]').forEach(el=>el.addEventListener('click',closeSheets));document.addEventListener('keydown',e=>{if(e.key==='Escape')closeSheets()});
})();
