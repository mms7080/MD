(function () {
  // ========= 공통 유틸 =========
  const openModal  = (m) => { if (!m) return; m.style.display = 'flex'; m.setAttribute('aria-hidden','false'); };
  const closeModal = (m) => { if (!m) return; m.style.display = 'none';  m.setAttribute('aria-hidden','true'); };
  const on = (el, ev, fn) => { if (el) el.addEventListener(ev, fn, { passive:true }); };

  const show = (el, msg, ok) => {
    if (!el) return;
    el.textContent = msg;
    el.className = `note-banner${ok ? '' : ' error'}`;
    el.hidden = false;
  };
  const hide = (el) => { if (el) el.hidden = true; };

  // ========= 프로필 모달 =========
  const profileModal      = document.getElementById('profileModal');
  const btnOpenProfile    = document.getElementById('btnOpenProfile');
  const ovProfile         = document.getElementById('ovProfile');
  const btnCloseProfile   = document.getElementById('btnCloseProfile');
  const btnCancelProfile  = document.getElementById('btnCancelProfile');

  on(btnOpenProfile,  'click', () => openModal(profileModal));
  on(ovProfile,       'click', () => closeModal(profileModal));
  on(btnCloseProfile, 'click', () => closeModal(profileModal));
  on(btnCancelProfile,'click', () => closeModal(profileModal));

  // ========= 비밀번호 모달 =========
  const passwordModal     = document.getElementById('passwordModal');
  const btnOpenPassword   = document.getElementById('btnOpenPassword');
  const ovPassword        = document.getElementById('ovPassword');
  const btnClosePassword  = document.getElementById('btnClosePassword');
  const btnCancelPassword = document.getElementById('btnCancelPassword');

  // 진행 중 서버검증 취소용
  let currCtrl = null;

  // 상태줄 없으면 생성
  function ensureStatusUnder(input, id) {
    if (!input) return null;
    let el = input.nextElementSibling;
    if (!el || !el.classList || !el.classList.contains('note-banner')) {
      el = document.createElement('p');
      el.className = 'note-banner';
      el.hidden = true;
      if (id) el.id = id;
      input.insertAdjacentElement('afterend', el);
    }
    return el;
  }

  // 🔹 비밀번호 모달 리셋 (열기 직전 & 닫은 직후)
  function resetPasswordModal() {
    const form = passwordModal?.querySelector('form');
    const curr = passwordModal?.querySelector('input[name="currentPassword"]');
    const neo  = passwordModal?.querySelector('input[name="newPassword"]');
    const conf = passwordModal?.querySelector('input[name="confirmNewPassword"]');
    if (!form || !curr || !neo || !conf) return;

    // 배너 핸들
    const sCurr = document.getElementById('status-current')  || ensureStatusUnder(curr, 'status-current');
    const sNew  = document.getElementById('status-new')      || ensureStatusUnder(neo , 'status-new');
    const sConf = document.getElementById('status-confirm')  || ensureStatusUnder(conf, 'status-confirm');

    // 1) 폼 리셋 + 값 수동 초기화
    form.reset();
    [curr, neo, conf].forEach(el => {
      el.value = '';
      el.autocomplete = 'off';
      try { el.setCustomValidity(''); } catch(_) {}
    });

    // 2) 배너/스타일 초기화
    [sCurr, sNew, sConf].forEach(el => {
      if (!el) return;
      el.textContent = '';
      el.classList.remove('error');
      el.hidden = true;
      el.style.opacity = '';
      el.style.display = '';
    });

    // 3) 진행 중 요청 취소
    try { currCtrl?.abort(); } catch(_) {}
    currCtrl = null;

    // 4) 오토필 이중 rAF 클리어
    requestAnimationFrame(() => {
      [curr, neo, conf].forEach(el => { el.value = ''; });
      requestAnimationFrame(() => {
        [curr, neo, conf].forEach(el => { el.value = ''; el.dispatchEvent(new Event('input', {bubbles:true})); });
      });
    });
  }

  // 열기: 열기 직전 초기화 → 오픈
  on(btnOpenPassword, 'click', () => {
    resetPasswordModal();
    openModal(passwordModal);
  });

  // 닫기: 닫은 직후 초기화
  [ovPassword, btnClosePassword, btnCancelPassword].forEach(el => {
    on(el, 'click', () => { closeModal(passwordModal); resetPasswordModal(); });
  });

  // ESC: 비번 모달만 초기화
  document.addEventListener('keydown', (e) => {
    if (e.key !== 'Escape') return;
    if (passwordModal && passwordModal.style.display !== 'none') {
      closeModal(passwordModal);
      resetPasswordModal();
    }
    // 프로필 모달은 닫기만 (초기화 요구 없음)
    if (profileModal && profileModal.style.display !== 'none') closeModal(profileModal);
  }, { passive:true });

  // ========= 이미지 미리보기 =========
  const photoInput   = document.getElementById('photoInput');
  const previewImage = document.getElementById('previewImage');
  if (photoInput && previewImage) {
    photoInput.addEventListener('change', (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (evt) => { previewImage.src = evt.target.result; };
      reader.readAsDataURL(file);
    }, { passive:true });
  }

  // ========= 비밀번호 실시간 검증 =========
  if (passwordModal) {
    const curr = passwordModal.querySelector('input[name="currentPassword"]');
    const neo  = passwordModal.querySelector('input[name="newPassword"]');
    const conf = passwordModal.querySelector('input[name="confirmNewPassword"]');
    if (!curr || !neo || !conf) return;

    const sCurr = ensureStatusUnder(curr, 'status-current');
    const sNew  = ensureStatusUnder(neo , 'status-new');
    const sConf = ensureStatusUnder(conf, 'status-confirm');

    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;

    async function checkCurrent() {
      const v = curr.value.trim();
      if (!v) { hide(sCurr); return; }
      try {
        currCtrl?.abort();
        currCtrl = new AbortController();
        const res = await fetch('/mypage/api/check-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
          },
          body: new URLSearchParams({ currentPassword: v }),
          signal: currCtrl.signal
        });
        const ok = await res.json();
        ok ? show(sCurr, '비밀번호가 확인되었습니다.', true)
           : show(sCurr, '현재 비밀번호가 올바르지 않습니다.', false);
      } catch (e) {
        if (e.name === 'AbortError') return;
        show(sCurr, '서버 확인 실패', false);
      }
    }

    function showStrength() {
      const len = neo.value.trim().length;
      if (!len) { hide(sNew); return; }
      let label = '약함', ok = false;
      if (len >= 10) { label = '강함'; ok = true; }
      else if (len >= 8) { label = '보통'; ok = true; }
      show(sNew, `강도: ${label}`, ok);
    }

    function checkMatch() {
      const a = neo.value.trim();
      const b = conf.value.trim();
      if (!a) { hide(sConf); hide(sNew); return; }
      if (!b) { hide(sConf); return; }
      a === b ? show(sConf, '일치합니다.', true)
              : show(sConf, '새 비밀번호와 확인 값이 일치하지 않습니다.', false);
    }

    curr.addEventListener('input', checkCurrent, { passive:true });
    curr.addEventListener('blur',  checkCurrent, { passive:true });
    neo .addEventListener('input', () => { showStrength(); checkMatch(); }, { passive:true });
    neo .addEventListener('blur',  () => { showStrength(); checkMatch(); }, { passive:true });
    conf.addEventListener('input', checkMatch, { passive:true });
    conf.addEventListener('blur',  checkMatch, { passive:true });

  }
})();
