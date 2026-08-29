(function(){
  const el=document.getElementById('projects-content');
  function refresh(){el.innerHTML=UI.topbar('项目管理','设置项目单位和标准效率','<button class="icon-button" id="add-project" aria-label="新建项目">＋</button>')+Store.data.projects.map(p=>`<article class="project-card" data-project-id="${p.id}"><span class="project-icon" style="background:${p.color}">◇</span><div class="project-info"><b>${p.name}</b><small>每分钟 ${Calc.number(p.rate)}${p.unit} = 100% 效率</small></div><span class="chevron">›</span></article>`).join('');el.querySelector('#add-project').onclick=()=>App.openProject();el.querySelectorAll('[data-project-id]').forEach(c=>c.onclick=()=>App.openProject(Number(c.dataset.projectId)))}
  window.Pages=window.Pages||{};Pages.projects={refresh};
})();
