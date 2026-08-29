(function(){
  const el=document.getElementById('records-content');
  function refresh(){const rows=Calc.rows(Store.today()),average=Calc.weighted(rows);el.innerHTML=UI.topbar('今日记录','点击记录可以编辑')+`<div class="segmented"><button class="on">全部</button><button>进行中</button><button>已完成</button></div>${rows.map(r=>`<div class="record-time"><span><i style="color:${r.p.color}">●</i>　${r.start}–${Calc.endTime(r.start,r.minutes)}</span><span class="edit-link">编辑 ›</span></div>${UI.card(r)}`).join('')||'<div class="empty">暂无记录</div>'}<div class="average-card"><span>加权平均</span><strong>${Math.round(average)}%</strong><small>按项目工时加权</small></div>`;el.querySelectorAll('[data-record-id]').forEach(c=>c.onclick=()=>App.openRecord(Number(c.dataset.recordId)))}
  window.Pages=window.Pages||{};Pages.records={refresh};
})();
