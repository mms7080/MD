// ===== 탭 전환 =====
(function () {
  const $ = (s) => document.querySelector(s);
  const tabId   = $("#tab-id");
  const tabPw   = $("#tab-pw");
  const labelId = $("#tab-id-label");
  const labelPw = $("#tab-pw-label");
  const panelId = $("#panel-id");
  const panelPw = $("#panel-pw");

  // 플래시 메시지 숨기기 (form 아래/상단 공용 모두 처리)
  function clearFlashIn(...scopes) {
    const roots = scopes.length ? scopes : [document];
    roots.forEach(scope => {
      const root = typeof scope === "string" ? document.querySelector(scope) : scope;
      if (!root) return;
      root.querySelectorAll(".kf-find__note").forEach(el => {
        el.textContent = "";
        el.classList.remove("error");
        el.hidden = true;
        el.style.display = "none";
      });
    });
  }

  function setActive(which) {
    labelId.classList.toggle("is-active", which === "id");
    labelPw.classList.toggle("is-active", which === "pw");
    labelId.setAttribute("aria-selected", String(which === "id"));
    labelPw.setAttribute("aria-selected", String(which === "pw"));
    panelId.classList.toggle("is-active", which === "id");
    panelPw.classList.toggle("is-active", which === "pw");

    const url = new URL(window.location.href);
    url.searchParams.set("tab", which);
    window.history.replaceState({}, "", url);
  }

  function initFromURL() {
    const url = new URL(window.location.href);
    const tab = url.searchParams.get("tab") === "pw" ? "pw" : "id";
    (tab === "id" ? tabId : tabPw).checked = true;
    setActive(tab);
    // 초기 로드는 서버가 내려준 RA를 보여줘야 하므로 여기서는 clearFlashIn() 호출 안 함
  }

  tabId.addEventListener("change", () => {
    if (!tabId.checked) return;
    setActive("id");
    // 탭 전환 시 화면의 플래시 메시지 초기화
    clearFlashIn("#panel-id", "#panel-pw", document);
  });

  tabPw.addEventListener("change", () => {
    if (!tabPw.checked) return;
    setActive("pw");
    clearFlashIn("#panel-id", "#panel-pw", document);
  });

  initFromURL();
})();


// ===== 비밀번호 설정 모달 동작 =====
// - 모달 열 때: 값 주입 + 제출 버튼 활성화 + 포커스
// - 모달 닫을 때: 새 비밀번호/확인만 초기화 (다른 값 유지)
(function () {
  const $ = (s) => document.querySelector(s);

  const openBtn = $('#openPwModal');
  const nameEl  = $('#pwName');
  const userEl  = $('#pwUsername');
  const emailEl = $('#pwEmail');
  const toggle  = $('#pwModalToggle');

  const modalNameText = $('#modalNameText');
  const modalUserText = $('#modalUsernameText');

  const modalName  = $('#modalName');
  const modalUser  = $('#modalUsername');
  const modalEmail = $('#modalEmail');

  const submitBtn = $('#pwSubmit');

  if (!openBtn) return;

  // 모달 열기
  openBtn.addEventListener('click', function () {
    const name     = (nameEl?.value  || '').trim();
    const username = (userEl?.value  || '').trim();
    const email    = (emailEl?.value || '').trim();

    if (modalNameText) modalNameText.textContent = name || '-';
    if (modalUserText) modalUserText.textContent = username || '-';
    if (modalName)  modalName.value  = name;
    if (modalUser)  modalUser.value  = username;
    if (modalEmail) modalEmail.value = email;

    if (toggle) toggle.checked = true;

    // 버튼 비활성화로 인해 제출이 막히는 문제 예방
    submitBtn?.removeAttribute('disabled');

    setTimeout(() => document.getElementById('newPassword')?.focus(), 0);
  });

  // 새 비밀번호/확인만 초기화
  function resetPwInputsOnly() {
    const newPw    = document.getElementById('newPassword');
    const confPw   = document.getElementById('confirmPassword');
    const pwStatus = document.getElementById('pwStatus');

    [newPw, confPw].forEach(el => {
      if (!el) return;
      el.value = '';
      try { el.setCustomValidity(''); } catch (_) {}
      // 붙어있는 강도/일치 체크가 있다면 UI 갱신되도록 input 이벤트 트리거
      setTimeout(() => el.dispatchEvent(new Event('input', { bubbles: true })), 0);
    });

    if (pwStatus) {
      pwStatus.textContent = '';
      pwStatus.hidden = true;
      pwStatus.classList.remove('error');
    }
    // 제출 버튼은 상태 로직에 맡기고 여기선 건드리지 않음
  }

  // 체크박스 토글로 닫힐 때만 초기화
  toggle?.addEventListener('change', () => {
    if (!toggle.checked) resetPwInputsOnly();
  });

  // ESC로 닫기 보조 (토글 해제 + 초기화)
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && toggle?.checked) {
      toggle.checked = false;
      resetPwInputsOnly();
    }
  });
})();
