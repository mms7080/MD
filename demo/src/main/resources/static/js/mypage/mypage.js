(function () {
  // 공통 함수
  const openModal  = (m) => { if (!m) return; m.style.display = 'flex'; m.setAttribute('aria-hidden','false'); };
  const closeModal = (m) => { if (!m) return; m.style.display = 'none'; m.setAttribute('aria-hidden','true'); };
  const on = (el, ev, fn) => { if (el) el.addEventListener(ev, fn); }; // 안전 바인딩 헬퍼

  // 프로필 모달
  const profileModal   = document.getElementById('profileModal');
  const btnOpenProfile = document.getElementById('btnOpenProfile');
  const ovProfile      = document.getElementById('ovProfile');
  const btnCloseProfile= document.getElementById('btnCloseProfile');
  const btnCancelProfile=document.getElementById('btnCancelProfile');

  on(btnOpenProfile, 'click', () => openModal(profileModal));
  on(ovProfile,      'click', () => closeModal(profileModal));
  on(btnCloseProfile,'click', () => closeModal(profileModal));
  on(btnCancelProfile,'click', () => closeModal(profileModal));

  // 비밀번호 모달
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

  // 이미지 미리보기 (둘 다 있을 때만)
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
})();