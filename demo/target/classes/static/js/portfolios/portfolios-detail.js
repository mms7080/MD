document.addEventListener('DOMContentLoaded', () => {
    // -----------------------------
    // 공통: 좋아요 / 조회수
    // -----------------------------
    const likeBtn = document.getElementById('likeBtn');
    const likeCount = document.getElementById('likeCount');
    const viewCountEl = document.getElementById('viewCount');
  
    const projectId =
      (likeBtn && likeBtn.dataset.id) ||
      document.body.dataset?.projectId ||
      location.pathname;
  
    // 조회수 (프로젝트별 key)
    try {
      const VIEW_KEY = `viewCount:${projectId}`;
      let viewCount = Number(localStorage.getItem(VIEW_KEY)) || 0;
      localStorage.setItem(VIEW_KEY, String(++viewCount));
      if (viewCountEl) viewCountEl.textContent = viewCount;
    } catch {}
  
    // 좋아요
    if (likeBtn && likeCount) {
      likeBtn.addEventListener('click', async () => {
        try {
          const res = await fetch('/api/portfolio/like/' + likeBtn.dataset.id, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
          });
          if (!res.ok) throw new Error('Server ' + res.status);
          const data = await res.json();
          likeCount.textContent = data.likes;
        } catch (err) {
          alert('좋아요 실패: ' + (err?.message || err));
        }
      });
    }
  
    // -----------------------------
    // 라이트박스 (한 번만 초기화)
    // -----------------------------
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
  
    // ===== 배경 스크롤 잠금/해제 (점프 없이 원래 위치로 복귀) =====
    function lockScroll() {
      const y = window.scrollY || document.documentElement.scrollTop || 0;
      document.body.dataset.scrollY = String(y);
      document.documentElement.style.scrollBehavior = 'auto'; // 부드러운 스크롤 잠시 끔
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
      // 부드러운 스크롤 복원(선택)
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
  
    // -----------------------------
    // ① 썸네일 그리드(.thumb-grid) → 라이트박스
    // -----------------------------
    const thumbs = Array.from(document.querySelectorAll('.thumb-grid img.thumb, .thumb-grid img'));
    if (thumbs.length) {
      const list = thumbs.map(getOriginal);
      thumbs.forEach((img, i) => {
        img.addEventListener('click', () => openLightbox(i, list));
        img.addEventListener('auxclick', (e) => { // 가운데 클릭 새 탭
          if (e.button === 1) window.open(getOriginal(img), '_blank', 'noopener,noreferrer');
        });
      });
      return; // 그리드가 있으면 여기서 끝 (슬라이더 백업 불필요)
    }
  
    // -----------------------------
    // ② (백업) 슬라이더 모드 → 라이트박스
    // -----------------------------
    const track = document.querySelector('.carousel-track');
    const items = track ? Array.from(track.querySelectorAll('.carousel-item')) : [];
    const btnPrevSlide = document.querySelector('.carousel-btn.prev');
    const btnNextSlide = document.querySelector('.carousel-btn.next');
  
    if (!track || !items.length) return;
  
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
  });
  