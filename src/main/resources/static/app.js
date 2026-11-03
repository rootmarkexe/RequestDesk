'use strict';
const $=id=>document.getElementById(id);
const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const statusName={OPEN:'Открыта',IN_PROGRESS:'В работе',RESOLVED:'Решена'};
const priorityName={LOW:'Низкий',NORMAL:'Обычный',HIGH:'Высокий'};
let token=sessionStorage.getItem('deskToken'), role='', page=0, totalPages=0, selectedId=null, register=false;
function feedback(message,bad=false){const el=$('feedback');el.textContent=message;el.classList.toggle('error',bad);el.style.display='block';}
async function api(path,opts={}){const headers={...opts.headers};if(opts.body)headers['Content-Type']='application/json';if(token)headers.Authorization=`Bearer ${token}`;
  const res=await fetch(path,{...opts,headers});if(res.status===204)return null;let data;try{data=await res.json();}catch{data={};}
  if(!res.ok){if(res.status===401&&!path.startsWith('/api/auth/'))signOut();throw Error(data.error||`HTTP ${res.status}`);}return data;}
function authPanel(show){$('authPanel').classList.toggle('hide',!show);$('appPanel').classList.toggle('hide',show);$('logout').classList.toggle('hide',show);$('role').classList.toggle('hide',show);}
function signOut(){token=null;role='';selectedId=null;sessionStorage.removeItem('deskToken');$('identity').textContent='';authPanel(true);}
async function enter(){try{const me=await api('/api/me');role=me.role;$('identity').textContent=me.email;$('role').textContent=role==='AGENT'?'Оператор':'Пользователь';$('newPanel').classList.toggle('hide',role==='AGENT');$('pageTitle').textContent=role==='AGENT'?'Очередь заявок':'Мои обращения';$('pageSubtitle').textContent=role==='AGENT'?'Возьмите заявку в работу и ответьте пользователю':'Отслеживайте статусы и ответы операторов';authPanel(false);await loadTickets();}catch(err){feedback(err.message,true);signOut();}}
$('logout').onclick=signOut;$('reload').onclick=()=>loadTickets();
$('authMode').onclick=()=>{register=!register;$('authSubmit').textContent=register?'Зарегистрироваться':'Войти';$('authMode').textContent=register?'Уже есть аккаунт? Войти':'Нет аккаунта? Регистрация';};
$('authForm').onsubmit=async e=>{e.preventDefault();try{const url=register?'/api/auth/register':'/api/auth/login';const data=await api(url,{method:'POST',body:JSON.stringify({email:$('email').value,password:$('password').value})});token=data.token;sessionStorage.setItem('deskToken',token);$('password').value='';await enter();feedback('Вход выполнен');}catch(err){feedback(err.message,true);}};
$('createForm').onsubmit=async e=>{e.preventDefault();try{const ticket=await api('/api/tickets',{method:'POST',body:JSON.stringify({title:$('title').value,description:$('description').value,priority:$('priority').value})});$('createForm').reset();page=0;selectedId=ticket.id;await loadTickets();feedback('Заявка отправлена');}catch(err){feedback(err.message,true);}};
async function loadTickets(){try{const data=await api(`/api/tickets?page=${page}&size=10`);totalPages=data.totalPages;
  $('ticketCount').textContent=String(data.totalElements);$('pageLabel').textContent=`${data.number+1} / ${Math.max(1,data.totalPages)}`;$('prev').disabled=page===0;$('next').disabled=page+1>=totalPages;
  $('tickets').innerHTML=data.content.length?data.content.map(t=>`<div class="ticket ${selectedId===t.id?'active':''}" data-ticket="${t.id}" tabindex="0" role="button"><div class="row"><strong>#${t.id} ${esc(t.title)}</strong><span class="pill ${t.status==='RESOLVED'?'good':t.status==='OPEN'?'warn':''}">${statusName[t.status]}</span></div><div class="meta"><span>${priorityName[t.priority]}</span><span>${esc(new Date(t.createdAt).toLocaleDateString('ru-RU'))}</span><span>${esc(t.assignee||'Без исполнителя')}</span></div></div>`).join(''):'<div class="empty">Заявок пока нет</div>';
  if(selectedId) await openTicket(selectedId);else if(data.content.length) await openTicket(data.content[0].id);
}catch(err){feedback(err.message,true);}}
$('tickets').onclick=e=>{const card=e.target.closest('[data-ticket]');if(card)openTicket(Number(card.dataset.ticket));};
$('tickets').onkeydown=e=>{if(e.key==='Enter'){const card=e.target.closest('[data-ticket]');if(card)openTicket(Number(card.dataset.ticket));}};
async function openTicket(id){try{const t=await api(`/api/tickets/${id}`);selectedId=id;document.querySelectorAll('[data-ticket]').forEach(el=>el.classList.toggle('active',Number(el.dataset.ticket)===id));
let actions='';if(role==='AGENT'&&t.status!=='RESOLVED'){
  actions=t.assignee?`<div class="toolbar"><label>Статус<select id="newStatus"><option value="IN_PROGRESS" ${t.status==='IN_PROGRESS'?'selected':''}>В работе</option><option value="RESOLVED">Решена</option></select></label><button id="saveStatus" ${t.assignee!==$('identity').textContent?'disabled':''}>Сохранить</button></div>`:`<button id="takeTicket">Взять в работу</button>`;
}
$('detail').innerHTML=`<div class="row"><h2>#${t.id} ${esc(t.title)}</h2><span class="pill ${t.status==='RESOLVED'?'good':'warn'}">${statusName[t.status]}</span></div><p id="detailDescription">${esc(t.description)}</p><div class="meta" style="margin:15px 0 22px;color:#7c899f;font-size:13px">Приоритет: ${priorityName[t.priority]} · Автор: ${esc(t.creator)} · Оператор: ${esc(t.assignee||'Не назначен')}</div>${actions}<hr style="border:0;border-top:1px solid #edf0f5;margin:24px 0"/><h3>Обсуждение</h3><div>${t.comments.length?t.comments.map(c=>`<div class="comment"><strong>${esc(c.author)}</strong><small style="margin-left:8px;color:#8b98aa">${new Date(c.createdAt).toLocaleString('ru-RU')}</small><p>${esc(c.body)}</p></div>`).join(''):'<p class="empty">Пока нет комментариев</p>'}</div>${t.status!=='RESOLVED'?'<form id="commentForm"><label>Ответ<textarea id="comment" required maxlength="1000" placeholder="Напишите сообщение..."></textarea></label><button type="submit">Отправить комментарий</button></form>':'<p class="note">Заявка закрыта. Новые комментарии недоступны.</p>'}`;
if($('takeTicket'))$('takeTicket').onclick=()=>updateTicket(`/api/tickets/${id}/take`,{method:'POST'});
if($('saveStatus'))$('saveStatus').onclick=()=>updateTicket(`/api/tickets/${id}/status`,{method:'PATCH',body:JSON.stringify({status:$('newStatus').value})});
if($('commentForm'))$('commentForm').onsubmit=async e=>{e.preventDefault();try{await api(`/api/tickets/${id}/comments`,{method:'POST',body:JSON.stringify({body:$('comment').value})});await openTicket(id);feedback('Комментарий добавлен');}catch(err){feedback(err.message,true);}};
}catch(err){feedback(err.message,true);}}
async function updateTicket(url,opts){try{await api(url,opts);await loadTickets();feedback('Заявка обновлена');}catch(err){feedback(err.message,true);}}
$('prev').onclick=()=>{page=Math.max(0,page-1);selectedId=null;loadTickets();};$('next').onclick=()=>{page++;selectedId=null;loadTickets();};
if(token)enter();else authPanel(true);
