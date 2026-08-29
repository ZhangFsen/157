(function(){
  const el=document.getElementById('settings-content');
  function refresh(){el.innerHTML=UI.topbar('设置','数据和应用信息')+`<div class="setting-group-title">数据</div><div class="setting-card"><div class="setting-row"><b>本机离线存储</b><small>项目和工作记录仅保存在当前设备</small></div><div class="setting-row"><b>云端同步</b><small>预留功能 · 后续版本开放</small></div></div><div class="setting-group-title">关于</div><div class="setting-card"><div class="setting-row"><b>个人效率计算</b><small>版本 V1.1.0</small></div><div class="setting-row"><b>计算方式</b><small>按项目工时计算加权平均效率</small></div></div>`}
  window.Pages=window.Pages||{};Pages.settings={refresh};
})();
