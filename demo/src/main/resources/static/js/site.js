// 푸터 연도
document.addEventListener('DOMContentLoaded', () => {
  const y = document.getElementById('year');
  if (y) y.textContent = new Date().getFullYear();
});

// 헤더/유틸 숨김(스크롤 다운 시)
(function () {
  let lastY = window.pageYOffset || 0;
  let ticking = false;
  const root = document.documentElement;
  const THRESH = 8;
  const SHOW_AT = 120;

  function onScroll() {
    const y = window.pageYOffset || 0;
    const delta = y - lastY;

    if (Math.abs(delta) > THRESH) {
      if (delta > 0 && y > SHOW_AT) {
        root.classList.add('hide-head');
      } else {
        root.classList.remove('hide-head');
      }
      lastY = y;
    }
    ticking = false;
  }

  window.addEventListener('scroll', function () {
    if (!ticking) {
      window.requestAnimationFrame(onScroll);
      ticking = true;
    }
  }, { passive: true });
})();

// 상담 폼 (데모 표시)
(function(){
  const form = document.getElementById('contactForm');
  const toast = document.getElementById('contactToast');
  if (!form || !toast) return;
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    toast.style.display = 'block';
    form.reset();
  });
})();

// 우측 하단 Top 버튼
(function(){
  const topBtn = document.getElementById('btnTop');
  if (topBtn) {
    topBtn.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
})();
