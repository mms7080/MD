// ==============================
// 테마 토글 (페이지 어디서든 즉시 적용)
// ==============================
(function(){
  try {
    const saved = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', saved);
    const btn = document.getElementById('btnTheme');
    if (btn) {
      btn.addEventListener('click', ()=>{
        const cur = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', cur);
        localStorage.setItem('theme', cur);
        btn.setAttribute('aria-pressed', String(cur === 'dark'));
      });
      btn.setAttribute('aria-pressed', String(saved === 'dark'));
    }
  } catch(e) { console.warn('[theme]', e); }
})();

document.addEventListener('DOMContentLoaded', () => {

  // ==============================
  // 공통 헤더: 모바일 내비
  // ==============================
  const navToggle = document.querySelector('.site-header .nav-toggle');
  const navList = document.getElementById('primary-menu');
  if (navToggle && navList){
    navToggle.addEventListener('click', ()=>{
      const open = navList.classList.toggle('open');
      navToggle.setAttribute('aria-expanded', String(open));
    });
  }

  // ==============================
  // 공통: 좋아요 / 조회수
  // ==============================
  const likeBtn     = document.getElementById('likeBtn');
  const likeCount   = document.getElementById('likeCount');
  const viewCountEl = document.getElementById('viewCount');

  // projectId 우선순위: body data-project-id > likeBtn data-id > pathname
  const projectId =
    document.body?.dataset?.projectId ||
    likeBtn?.dataset?.id ||
    location.pathname ||
    'default';

  // ---- 조회수(로컬스토리지 + 쿠키 폴백) ----
  (function updateViewCount(){
    if (!viewCountEl) return;
    const KEY = `viewCount:${projectId}`;
    const toInt = (v) => {
      const n = parseInt(v, 10);
      return Number.isFinite(n) ? n : 0;
    };
    try {
      let current = toInt(localStorage.getItem(KEY)) + 1;
      localStorage.setItem(KEY, String(current));
      viewCountEl.textContent = String(current);
    } catch (e) {
      // localStorage 불가 → cookie 폴백
      try {
        const m = document.cookie.match(new RegExp(`${KEY}=([^;]+)`));
        let current = toInt(m?.[1]) + 1;
        document.cookie = `${KEY}=${current}; path=/; max-age=31536000`; // 1년
        viewCountEl.textContent = String(current);
        console.warn('[viewCount] localStorage 실패 → cookie 폴백:', e);
      } catch (err) {
        console.error('[viewCount] 저장 실패:', err);
      }
    }
  })();

  // ---- 좋아요(로그인 사용자 전용 버튼) ----
  if (likeBtn && likeCount) {
    likeBtn.addEventListener('click', async () => {
      try {
        const res = await fetch('/api/portfolio/like/' + likeBtn.dataset.id, {
          method: 'POST',
          credentials: 'same-origin',
          headers: { 'Accept': 'application/json' }
        });
        if (!res.ok) throw new Error('Server ' + res.status);
        const data = await res.json(); // {likes: number}
        likeCount.textContent = data.likes;
      } catch (err) {
        alert('좋아요 실패: ' + (err?.message || err));
      }
    });
  }

  // ==============================
  // 비로그인 좋아요 → 모달
  // ==============================
  const likeSigninTrigger = document.getElementById('likeSigninTrigger'); // 비로그인용 <a>
  const loginModal   = document.getElementById('loginModal');
  const loginClose   = document.getElementById('loginModalClose');
  const loginGo      = document.getElementById('loginModalGo');

  let loginKeyHandler, loginBackdropHandler;

  function openLoginModal() {
    if (!loginModal) return;
    loginModal.hidden = false;
    loginModal.setAttribute('aria-hidden', 'false');
    // 배경 스크롤 잠금 (라이트박스와 동일 로직)
    lockScroll();

    loginKeyHandler = (e) => { if (e.key === 'Escape') closeLoginModal(); };
    loginBackdropHandler = (e) => { if (e.target === loginModal) closeLoginModal(); };

    window.addEventListener('keydown', loginKeyHandler);
    loginModal.addEventListener('click', loginBackdropHandler);
  }
  function closeLoginModal() {
    if (!loginModal) return;
    loginModal.hidden = true;
    loginModal.setAttribute('aria-hidden', 'true');
    // 스크롤 복원
    unlockScroll();
    window.removeEventListener('keydown', loginKeyHandler);
    loginModal.removeEventListener('click', loginBackdropHandler);
  }

  if (likeSigninTrigger && loginModal) {
    likeSigninTrigger.addEventListener('click', (e) => {
      // 원래 /signin 링크 이동 막고 모달로 유도
      e.preventDefault();
      openLoginModal();
    });
    loginClose?.addEventListener('click', closeLoginModal);
    // loginGo는 <a href="/signin"> 이므로 별도 JS 불필요 (모달에서 바로 이동)
  }

  // ==============================
  // 별점/댓글 (회원만 작성, 비회원은 평균만 표시)
  // ==============================
  const avgStars     = document.getElementById('avgStars');
  const avgScore     = document.getElementById('avgScore');
  const ratingCount  = document.getElementById('ratingCount');
  const writeStars   = document.getElementById('writeStars');   // 로그인 사용자에게만 존재
  const myScoreText  = document.getElementById('myScoreText');
  const commentList  = document.getElementById('commentList');
  const form         = document.getElementById('commentForm');
  const textarea     = document.getElementById('commentContent');

  const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]')?.content;
  const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

  // 별 DOM 생성
  const makeStars = (el, n=5) => {
    if (!el) return;
    el.innerHTML = '';
    for (let i=1; i<=n; i++){
      const s = document.createElement('span');
      s.className = 'star';
      s.textContent = '★';
      s.dataset.value = String(i);
      el.appendChild(s);
    }
  };
  // 별 채우기
  const fillStars = (el, value) => {
    if (!el) return;
    const v = Math.round(value || 0);
    [...el.querySelectorAll('.star')].forEach(st => {
      st.classList.toggle('filled', Number(st.dataset.value) <= v);
    });
  };

  // 평균 불러오기
  const loadSummary = async () => {
    if (!avgScore || !ratingCount) return;
    try {
      const res = await fetch(`/api/portfolio/${projectId}/rating`);
      const data = await res.json(); // { average: number, count: number }
      avgScore.textContent = (data.average ?? 0).toFixed(1);
      ratingCount.textContent = String(data.count ?? 0);
      if (avgStars) {
        makeStars(avgStars);
        fillStars(avgStars, data.average ?? 0);
        avgStars.classList.add('readonly');
      }
    } catch (e) {
      console.error('평균 별점 로드 실패', e);
    }
  };

  // 댓글 목록 불러오기
  const loadComments = async () => {
    if (!commentList) return;
    try {
      const res = await fetch(`/api/portfolio/${projectId}/reviews`);
      const items = await res.json(); // [{author, content, createdAt, rating?}, ...]
      commentList.innerHTML = (items || []).map(it => `
        <div class="comment">
          <p><strong>${it.author}</strong> <span class="date">${it.createdAt}</span></p>
          ${ it.rating ? `<div class="stars readonly">${'★'.repeat(it.rating)}${'☆'.repeat(5-it.rating)}</div>` : '' }
          <p>${it.content}</p>
        </div>
      `).join('') || '<p class="muted">등록된 댓글이 없습니다.</p>';
    } catch (e) {
      console.error('댓글 로드 실패', e);
    }
  };

  // 초기 로드
  loadSummary();
  loadComments();

  // 로그인 사용자 전용: 별점 인터랙션 + 등록
  if (writeStars && form && textarea) {
    makeStars(writeStars);
    let currentScore = 0;

    writeStars.addEventListener('mousemove', (e) => {
      const target = e.target.closest('.star');
      if (!target) return;
      fillStars(writeStars, Number(target.dataset.value));
    });
    writeStars.addEventListener('mouseleave', () => fillStars(writeStars, currentScore));
    writeStars.addEventListener('click', (e) => {
      const target = e.target.closest('.star');
      if (!target) return;
      currentScore = Number(target.dataset.value);
      if (myScoreText) myScoreText.textContent = String(currentScore);
      fillStars(writeStars, currentScore);
    });

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        content: textarea.value.trim(),
        rating: currentScore || null
      };
      if (!payload.content) return alert('댓글 내용을 입력하세요.');
      try {
        const res = await fetch(`/api/portfolio/${projectId}/reviews`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [CSRF_HEADER]: CSRF_TOKEN
          },
          credentials: 'include',
          body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        textarea.value = '';
        currentScore = 0;
        if (myScoreText) myScoreText.textContent = '0';
        fillStars(writeStars, 0);
        await Promise.all([loadSummary(), loadComments()]);
      } catch (err) {
        console.error(err);
        alert('등록에 실패했습니다.');
      }
    });
  }

  // ==============================
  // 라이트박스 (한 번만 초기화)
  // ==============================
  let lb = document.getElementById('lightbox');

  // 모달이 container 안에 있다면 body로 이동(항상 최상단에 뜨게)
  if (lb && lb.parentElement !== document.body) {
    document.body.appendChild(lb);
  }
  // 없으면 생성
  if (!lb) {
    lb = document.createElement('div');
    lb.id = 'lightbox';
    lb.className = 'lightbox';
    lb.hidden = true;
    lb.setAttribute('aria-hidden', 'true');
    lb.innerHTML = `
      <button class="lb-btn lb-prev" aria-label="이전">‹</button>
      <div class="lb-content">
        <button class="lb-btn lb-close" aria-label="닫기">×</button>
        <img class="lb-img" alt="preview"/>
      </div>
      <button class="lb-btn lb-next" aria-label="다음">›</button>
    `;
    document.body.appendChild(lb);
  }

  const lbImg   = lb.querySelector('.lb-img');
  const btnPrev = lb.querySelector('.lb-prev');
  const btnNext = lb.querySelector('.lb-next');
  const btnClose= lb.querySelector('.lb-close');

  const getOriginal = (el) => el.getAttribute('data-original') || el.src;

  let gallery = [];   // 이미지 URL 목록
  let idx = 0;        // 현재 인덱스
  let keyHandler, overlayHandler;

  // ===== 배경 스크롤 잠금/해제 =====
  function lockScroll() {
    const y = window.scrollY || document.documentElement.scrollTop || 0;
    document.body.dataset.scrollY = String(y);
    document.documentElement.style.scrollBehavior = 'auto';
    document.body.style.position = 'fixed';
    document.body.style.top = `-${y}px`;
    document.body.style.left = '0';
    document.body.style.right = '0';
    document.body.style.width = '100%';
  }
  function unlockScroll() {
    const y = parseInt(document.body.dataset.scrollY || '0', 10);
    document.body.style.position = '';
    document.body.style.top = '';
    document.body.style.left = '';
    document.body.style.right = '';
    document.body.style.width = '';
    delete document.body.dataset.scrollY;
    window.scrollTo(0, y);
    setTimeout(() => { document.documentElement.style.scrollBehavior = ''; }, 0);
  }

  function show(i) {
    if (!gallery.length || !lbImg) return;
    idx = (i + gallery.length) % gallery.length;
    lbImg.src = gallery[idx];
  }

  function openLightbox(startIndex = 0, list = []) {
    if (!lb || !lbImg) return;
    gallery = list;
    show(startIndex);

    lb.hidden = false;
    lb.setAttribute('aria-hidden', 'false');
    lockScroll();

    keyHandler = (e) => {
      if (e.key === 'Escape') closeLightbox();
      else if (e.key === 'ArrowLeft') show(idx - 1);
      else if (e.key === 'ArrowRight') show(idx + 1);
    };
    overlayHandler = (e) => { if (e.target === lb) closeLightbox(); };

    window.addEventListener('keydown', keyHandler);
    lb.addEventListener('click', overlayHandler);

    // 터치 스와이프
    let sx = 0;
    const onTouchStart = (e) => { sx = e.touches[0].clientX; };
    const onTouchEnd = (e) => {
      const dx = e.changedTouches[0].clientX - sx;
      if (Math.abs(dx) > 40) dx > 0 ? show(idx - 1) : show(idx + 1);
    };
    lb.addEventListener('touchstart', onTouchStart, { passive: true });
    lb.addEventListener('touchend', onTouchEnd, { passive: true });
    lb._touchCleanup = () => {
      lb.removeEventListener('touchstart', onTouchStart);
      lb.removeEventListener('touchend', onTouchEnd);
    };
  }

  function closeLightbox() {
    if (!lb) return;
    lb.hidden = true;
    lb.setAttribute('aria-hidden', 'true');
    if (lbImg) lbImg.src = '';
    unlockScroll();
    window.removeEventListener('keydown', keyHandler);
    lb.removeEventListener('click', overlayHandler);
    lb._touchCleanup && lb._touchCleanup();
  }

  btnPrev?.addEventListener('click', () => show(idx - 1));
  btnNext?.addEventListener('click', () => show(idx + 1));
  btnClose?.addEventListener('click', closeLightbox);

  // ==============================
  // 썸네일 그리드 → 라이트박스
  // ==============================
  const thumbs = Array.from(document.querySelectorAll('.thumb-grid img.thumb, .thumb-grid img'));
  if (thumbs.length) {
    const list = thumbs.map(getOriginal);
    thumbs.forEach((img, i) => {
      img.addEventListener('click', () => openLightbox(i, list));
      img.addEventListener('auxclick', (e) => { // 가운데 클릭 새 탭
        if (e.button === 1) window.open(getOriginal(img), '_blank', 'noopener,noreferrer');
      });
    });
  } else {
    // ==============================
    // (백업) 슬라이더 모드 → 라이트박스
    // ==============================
    const track = document.querySelector('.carousel-track');
    const items = track ? Array.from(track.querySelectorAll('.carousel-item')) : [];
    const btnPrevSlide = document.querySelector('.carousel-btn.prev');
    const btnNextSlide = document.querySelector('.carousel-btn.next');

    if (track && items.length) {
      let index = 0;
      const slideList = items.map(getOriginal);
      const updateSliderPosition = () => {
        const w = items[0].offsetWidth;
        track.style.transform = `translateX(${-index * w}px)`;
      };
      const go = (i) => { index = Math.max(0, Math.min(i, items.length - 1)); updateSliderPosition(); };

      btnPrevSlide?.addEventListener('click', () => go(index - 1));
      btnNextSlide?.addEventListener('click', () => go(index + 1));
      window.addEventListener('resize', () => requestAnimationFrame(updateSliderPosition));

      items.forEach((img, i) => {
        img.addEventListener('click', () => openLightbox(i, slideList));
        img.addEventListener('auxclick', (e) => {
          if (e.button === 1) window.open(getOriginal(img), '_blank', 'noopener,noreferrer');
        });
      });

      updateSliderPosition();
    }
  }


  // 비로그인 좋아요 modal
function openLoginRequiredModal(url = '/signin') {
  // 오버레이
  const overlay = document.createElement('div');
  overlay.setAttribute('role', 'dialog');
  overlay.setAttribute('aria-modal', 'true');
  overlay.setAttribute('aria-label', '로그인 안내');
  Object.assign(overlay.style, {
    position: 'fixed',
    inset: '0',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'rgba(0,0,0,.45)',
    zIndex: '10000'
  });

  // 다이얼로그 박스
  const box = document.createElement('div');
  Object.assign(box.style, {
    background: '#fff',
    color: '#111',
    padding: '20px',
    borderRadius: '12px',
    width: 'min(400px, 92vw)',
    boxShadow: '0 10px 30px rgba(0,0,0,.25)',
    textAlign: 'center',
    lineHeight: '1.6'
  });

  const msg = document.createElement('p');
  msg.textContent = '로그인이 필요한 서비스 입니다';

  const btn = document.createElement('button');
  btn.textContent = '확인';
  Object.assign(btn.style, {
    marginTop: '16px',
    padding: '8px 16px',
    border: 'none',
    borderRadius: '6px',
    background: '#365cff',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '15px'
  });

  btn.addEventListener('click', () => {
    window.location.href = url;
  });

  box.appendChild(msg);
  box.appendChild(btn);
  overlay.appendChild(box);
  document.body.appendChild(overlay);
}

if (likeSigninTrigger) {
  likeSigninTrigger.addEventListener('click', (e) => {
    e.preventDefault(); // 바로 이동 막기
    openLoginRequiredModal('/signin');
  });
}
});
