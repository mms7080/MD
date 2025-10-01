(function () {
  // ========= 공통 유틸 =========
  const openModal  = (m) => { if (!m) return; m.style.display = 'flex'; m.setAttribute('aria-hidden','false'); };
  const closeModal = (m) => { if (!m) return; m.style.display = 'none';  m.setAttribute('aria-hidden','true'); };
  const on = (el, ev, fn) => { if (el) el.addEventListener(ev, fn); };

  const show = (el, msg, ok) => {
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

  on(btnOpenPassword,  'click', () => openModal(passwordModal));
  on(ovPassword,       'click', () => closeModal(passwordModal));
  on(btnClosePassword, 'click', () => closeModal(passwordModal));
  on(btnCancelPassword,'click', () => closeModal(passwordModal));

  // ESC 키로 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeModal(profileModal);
      closeModal(passwordModal);
    }
  });

  // ========= 이미지 미리보기 (존재 시만) =========
  const photoInput   = document.getElementById('photoInput');
  const previewImage = document.getElementById('previewImage');
  if (photoInput && previewImage) {
    photoInput.addEventListener('change', (e) => {
      const file = e.target.files && e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (evt) => { previewImage.src = evt.target.result; };
      reader.readAsDataURL(file);
    });
  }

  // ========= 비밀번호 검증 (실시간) =========
  if (passwordModal) {
    const curr = passwordModal.querySelector('input[name="currentPassword"]');
    const neo  = passwordModal.querySelector('input[name="newPassword"]');
    const conf = passwordModal.querySelector('input[name="confirmNewPassword"]');
    if (!curr || !neo || !conf) return;

    // 상태줄 없으면 자동 생성
    function ensureStatusUnder(input, id) {
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
    const sCurr = ensureStatusUnder(curr, 'status-current');
    const sNew  = ensureStatusUnder(neo,  'status-new');
    const sConf = ensureStatusUnder(conf, 'status-confirm');

    // CSRF 메타
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;

    // 1) 현재 비밀번호: 입력할 때마다 서버 검증 (AbortController로 레이스 방지)
    let currCtrl = null;
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

        const ok = await res.json(); // true/false
        ok ? show(sCurr, '비밀번호가 확인되었습니다.', true)
           : show(sCurr, '현재 비밀번호가 올바르지 않습니다.', false);
      } catch (e) {
        if (e.name === 'AbortError') return; // 최신 입력으로 대체됨
        show(sCurr, '서버 확인 실패', false);
      }
    }

    // 2) 새 비밀번호 강도: 길이 기준 실시간
    // ≤5: 약함 / 8~9: 보통 / ≥10: 강함 (6~7은 약함 처리)
    function showStrength() {
      const len = neo.value.trim().length;
      if (!len) { hide(sNew); return; }
      let label = '약함', ok = false;
      if (len >= 10) { label = '강함'; ok = true; }
      else if (len >= 8) { label = '보통'; ok = true; }
      show(sNew, `강도: ${label}`, ok);
    }

    // 3) 새 비밀번호 확인: 실시간 일치 여부
    function checkMatch() {
      const a = neo.value.trim();
      const b = conf.value.trim();
      if (!b) { hide(sConf); return; }
      a === b ? show(sConf, '일치합니다.', true)
              : show(sConf, '새 비밀번호와 확인 값이 일치하지 않습니다.', false);
    }

    // 이벤트 등록 (실시간)
    curr.addEventListener('input', checkCurrent);
    neo .addEventListener('input', showStrength);
    conf.addEventListener('input', checkMatch);

    // 포커스 아웃 시에도 한 번 더 확정 검증(선택)
    curr.addEventListener('blur', checkCurrent);
    neo .addEventListener('blur', showStrength);
    conf.addEventListener('blur', checkMatch);
  }
})();
