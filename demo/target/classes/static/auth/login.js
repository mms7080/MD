// static/auth/login.js
document.addEventListener('DOMContentLoaded', () => {
  /* 1) 연도 표기 */
  const yearEl = document.getElementById('year');
  if (yearEl) yearEl.textContent = new Date().getFullYear();

  /* 2) 비밀번호 보기: 누르고 있는 동안 */
  (function () {
    const holdBtn = document.querySelector('.pw__hold');
    const input   = document.querySelector('.pw input[type="password"], .pw input[type="text"]');
    if (!holdBtn || !input) return;

    const show = () => { input.type = 'text';  };
    const hide = () => { input.type = 'password'; };

    // Mouse
    holdBtn.addEventListener('mousedown', show);
    holdBtn.addEventListener('mouseup', hide);
    holdBtn.addEventListener('mouseleave', hide);
    // Touch
    holdBtn.addEventListener('touchstart', (e)=>{ e.preventDefault(); show(); }, {passive:false});
    holdBtn.addEventListener('touchend', hide);
    holdBtn.addEventListener('touchcancel', hide);
  })();

  /* 3) 오버레이 없는 모달 열기/닫기 (푸터 링크용) */
  const openTriggers = document.querySelectorAll('[data-modal-open]');
  const closeTriggers = document.querySelectorAll('[data-modal-close]');
  const openStack = [];

  const getModalElement = (key) => document.getElementById(`modal-${key}`);

  function openModal(key, opener) {
    const modal = getModalElement(key);
    if (!modal) return;
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');

    const card = modal.querySelector('.modal__card');
    if (card) {
      card.dataset.openerId = opener ? (opener.id || '') : '';
      card.focus({ preventScroll: true });
    }
    openStack.push(modal);
  }

  function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');

    const card = modal.querySelector('.modal__card');
    if (card && card.dataset.openerId) {
      const opener = document.getElementById(card.dataset.openerId);
      if (opener) opener.focus({ preventScroll: true });
      card.removeAttribute('data-opener-id');
    }
    const idx = openStack.lastIndexOf(modal);
    if (idx > -1) openStack.splice(idx, 1);
  }

  // 열기 트리거: footer 링크(privacy/terms)
  openTriggers.forEach((btn, i) => {
    if (!btn.id) btn.id = `modal-opener-${i+1}`;
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const key = btn.getAttribute('data-modal-open'); // 'privacy' | 'terms'
      if (!key) return;
      openModal(key, btn);
    });
  });

  // 닫기 버튼
  closeTriggers.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      closeModal(btn.closest('.modal'));
    });
  });

  // ESC 닫기 (가장 최근 모달)
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && openStack.length) {
      e.preventDefault();
      closeModal(openStack[openStack.length - 1]);
    }
  });

  // 배경 클릭/스크롤 허용: 별도 처리 없음 (오버레이가 없기 때문)
});
