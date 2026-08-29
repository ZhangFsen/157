(function(){
  const KEY='efficiency-helper-v1';
  const isoToday=()=>{const d=new Date(),off=d.getTimezoneOffset();return new Date(d.getTime()-off*60000).toISOString().slice(0,10)};
  const fresh=()=>({projects:[{id:1,name:'打螺丝',unit:'个',rate:1,color:'#1677ff'},{id:2,name:'包装',unit:'箱',rate:1,color:'#21b7aa'},{id:3,name:'质检',unit:'件',rate:.5,color:'#ff9f43'},{id:4,name:'装箱',unit:'箱',rate:.8,color:'#7b61ff'}],records:[{id:1,pid:1,date:isoToday(),start:'08:00',minutes:120,actual:60},{id:2,pid:2,date:isoToday(),start:'10:20',minutes:180,actual:180}],version:'1.0.0'});
  let data;try{data=JSON.parse(localStorage.getItem(KEY))||fresh()}catch(e){data=fresh()}
  const persist=()=>localStorage.setItem(KEY,JSON.stringify(data));
  window.Store={today:isoToday,get data(){return data},project:id=>data.projects.find(p=>p.id==id),recordsFor:date=>data.records.filter(r=>r.date===date).sort((a,b)=>a.start.localeCompare(b.start)),saveRecord(record){const old=data.records.find(r=>r.id===record.id);if(old)Object.assign(old,record);else data.records.push({...record,id:Date.now()});persist()},deleteRecord(id){data.records=data.records.filter(r=>r.id!==id);persist()},saveProject(project){const old=data.projects.find(p=>p.id===project.id);if(old)Object.assign(old,project);else data.projects.push({...project,id:Date.now()});persist()},deleteProject(id){data.projects=data.projects.filter(p=>p.id!==id);data.records=data.records.filter(r=>r.pid!==id);persist()},reset(){data=fresh();persist()}};
})();
