document.addEventListener('DOMContentLoaded', () => {
    const likeBtn = document.getElementById('likeBtn');
    const likeCount = document.getElementById('likeCount');
    const track = document.querySelector('.carousel-track');
    const items = document.querySelectorAll('.carousel-item');
    const btnPrev = document.querySelector('.carousel-btn.prev');
    const btnNext = document.querySelector('.carousel-btn.next');
    const viewCountEl = document.getElementById('viewCount');
  
    let currentIndex = 0;

    let viewCount = parseInt(localStorage.getItem('viewCount')) || 0;
    viewCount++;
    localStorage.setItem('viewCount',viewCount);

    // 화면에 표시
    if(viewCountEl){
        viewCountEl.textContent = viewCount;
    }
  
    // 👉 슬라이더 위치 업데이트
    function updateSliderPosition() {
      if (!items.length) return;
      const itemWidth = items[0].offsetWidth;
      track.style.transform = `translateX(-${currentIndex * itemWidth}px)`;
    }
  
    // 👉 슬라이더 버튼 이벤트
    if (track && btnPrev && btnNext && items.length > 0) {
      btnPrev.addEventListener('click', () => {
        if (currentIndex > 0) {
          currentIndex--;
          updateSliderPosition();
        }
      });
  
      btnNext.addEventListener('click', () => {
        if (currentIndex < items.length - 1) {
          currentIndex++;
          updateSliderPosition();
        }
      });
  
      // 리사이즈 시 슬라이더 위치 다시 계산
      window.addEventListener('resize', updateSliderPosition);
    }
  
    // 👉 좋아요 버튼
    if (likeBtn && likeCount) {
      likeBtn.addEventListener('click', () => {
        fetch('/api/portfolio/like/' + likeBtn.dataset.id, {
          method: 'POST'
        })
          .then(res => res.json())
          .then(data => {
            likeCount.textContent = data.likes;
          })
          .catch(err => alert("좋아요 실패: " + err.message));
      });
    }
  
    // 👉 이미지 클릭 시 모달 표시
    items.forEach(img => {
      img.addEventListener('click', () => {
        openImageModal(img.src);
      });
    });
  
    // 초기 위치 설정
    updateSliderPosition();
  });
  
  
  // ✅ 모달 열기
  function openImageModal(imgSrc) {
    const modal = document.createElement('div');
    modal.className = 'image-modal';
    modal.innerHTML = `
      <div class="image-modal-overlay"></div>
      <div class="image-modal-content">
        <img src="${imgSrc}" alt="확대 이미지" />
        <button class="modal-close">✕</button>
      </div>
    `;
    document.body.appendChild(modal);
  
    // 모달 닫기
    modal.querySelector('.modal-close').addEventListener('click', () => modal.remove());
    modal.querySelector('.image-modal-overlay').addEventListener('click', () => modal.remove());
  }
  