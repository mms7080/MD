console.log("✅ portfolios-detail.js loaded");
window.enableEdit = function (button) {
  const card = button.closest(".comment");
  const editForm = card.querySelector(".edit-form");
  console.log("🔍 editForm:", editForm);
};
let currentIndex = 0;
let images = [];

/* -----------------------------
   이미지 모달 관련
----------------------------- */
function loadImages() {
  const mainImage = document.getElementById("mainImage");
  const thumbs = Array.from(document.querySelectorAll(".thumb"));
  images = [];
  if (mainImage) images.push(mainImage.src);
  if (thumbs.length > 0) images.push(...thumbs.map(img => img.src));
}

window.openModal = function (index) {
  loadImages();
  currentIndex = index;
  const modal = document.getElementById("imageModal");
  const modalImage = document.getElementById("modalImage");
  if (modal && modalImage) {
    modal.style.display = "flex";
    modalImage.src = images[currentIndex];
  }
};

window.closeModal = function () {
  const modal = document.getElementById("imageModal");
  if (modal) modal.style.display = "none";
};

window.changeImage = function (step) {
  if (images.length === 0) return;
  currentIndex = (currentIndex + step + images.length) % images.length;
  const modalImage = document.getElementById("modalImage");
  if (modalImage) modalImage.src = images[currentIndex];
};

/* -----------------------------
   태그: 아래로 펼치기(자연스러운 두 줄) + 우측 버튼 고정
----------------------------- */

/** 각 tags-box마다 .tags-extra 만들고, 6개 이후는 거기로 "이동" */
function initTagsExpandable(){
  document.querySelectorAll(".tags-box").forEach(box=>{
    const list = box.querySelector(".tags-list");
    const label = box.querySelector(".tags-label");
    if(!list || !label) return;

    // 이미 초기화면 스킵
    if (box.dataset.tagsInited === "true") return;

    const iconLeft = box.closest(".icon-left");
    if(!iconLeft) return;

    // 2행 컨테이너 생성
    let extra = iconLeft.querySelector(".tags-extra");
    if(!extra){
      extra = document.createElement("div");
      extra.className = "tags-extra";
      iconLeft.appendChild(extra);  // 그리드 2행에 붙음
    }else{
      extra.innerHTML = "";
    }

    // 기본 6개는 첫 줄, 나머지는 아래 줄로 이동
    const tags = Array.from(list.querySelectorAll(".tag-item"));
    tags.forEach((tag, idx)=>{
      if(idx < 6){
        tag.style.display = "inline-block";
        list.appendChild(tag);
      }else{
        tag.style.display = "inline-block";
        extra.appendChild(tag);
      }
    });

    // 라벨 폭만큼 들여쓰기 계산 → 두 번째 줄을 첫 태그 시작점에 맞춤
    const listRect  = list.getBoundingClientRect();
    const boxRect   = box.getBoundingClientRect();
    const startPx   = Math.max(0, Math.round(listRect.left - boxRect.left));
    extra.style.setProperty('--tags-start', startPx + 'px');

    // 더보기 버튼 표시/문구
    const moreBtn = box.querySelector(".tag-more-btn");
    if(moreBtn){
      moreBtn.style.display = extra.children.length ? "inline-block" : "none";
      moreBtn.dataset.expanded = "false";
      moreBtn.textContent = "+ 더보기";
    }

    // 리사이즈 시 들여쓰기 재계산
    let rAF;
    const recalc = ()=>{
      cancelAnimationFrame(rAF);
      rAF = requestAnimationFrame(()=>{
        const lr = list.getBoundingClientRect();
        const br = box.getBoundingClientRect();
        extra.style.setProperty('--tags-start', Math.max(0, Math.round(lr.left - br.left)) + 'px');
      });
    };
    window.addEventListener('resize', recalc);

    box.dataset.tagsInited = "true";
  });
}

window.toggleTags = function(button){
  const iconLeft = button.closest(".icon-left");
  if(!iconLeft) return;
  const extra = iconLeft.querySelector(".tags-extra");
  if(!extra) return;

  const expanded = button.dataset.expanded === "true";
  if(expanded){
    // 접기
    extra.style.display = "none";
    button.dataset.expanded = "false";
    button.textContent = "+ 더보기";
  }else{
    // 열기
    extra.style.display = "flex";
    button.dataset.expanded = "true";
    button.textContent = "접기";
  }
};

/* DOMContentLoaded */
document.addEventListener("DOMContentLoaded", () => {
  initTagsExpandable();
});


/* -----------------------------
   댓글 수정 기능
----------------------------- */
window.enableEdit = function (button) {
  const card = button.closest(".comment");
  const contentEl = card.querySelector(".content");
  const editForm = card.querySelector(".edit-form");
  const readonlyStars = card.querySelector(".stars.readonly");
  if (contentEl) contentEl.style.display = "none";
  if (readonlyStars) readonlyStars.style.display = "none";
  if (editForm) {
    editForm.style.display = "flex";
    editForm.querySelector("textarea").value = contentEl.innerText.trim();
    const rating = parseInt(readonlyStars?.dataset.rating) || 0;
    if (rating > 0) {
      const radio = editForm.querySelector(`input[name="rating"][value="${rating}"]`);
      if (radio) radio.checked = true;
    }
  }
};

window.cancelEdit = function (button) {
  const editForm = button.closest(".edit-form");
  const card = editForm.closest(".comment");
  const contentEl = card.querySelector(".content");
  const readonlyStars = card.querySelector(".stars.readonly");
  editForm.style.display = "none";
  if (contentEl) contentEl.style.display = "block";
  if (readonlyStars) readonlyStars.style.display = "block";
};

/* -----------------------------
   DOMContentLoaded 초기화
----------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  // 태그 초기화
  document.querySelectorAll(".tags-list").forEach(list => {
    const tags = list.querySelectorAll(".tag-item");
    tags.forEach((tag, i) => (tag.style.display = i < 6 ? "inline-block" : "none"));
  });

  // 별점 렌더링
  document.querySelectorAll(".stars.readonly").forEach(starBox => {
    if (starBox.dataset.starsRendered === "true") return;
    const rating = parseInt(starBox.dataset.rating) || 0;
    starBox.innerHTML = "";
    for (let i = 1; i <= 5; i++) {
      const span = document.createElement("span");
      span.textContent = i <= rating ? "★" : "☆";
      span.style.color = i <= rating ? "gold" : "#555";
      starBox.appendChild(span);
    }
    starBox.dataset.starsRendered = "true";
  });

  // 좋아요
  const likeBtn = document.getElementById("likeBtn");
  const likeCountEl = document.getElementById("likeCount");
  const viewCountEl = document.getElementById("viewCount");
  const isAuthenticated = likeBtn?.dataset.authenticated === "true";

  if (likeBtn && viewCountEl) {
    const portfolioId = viewCountEl.dataset.id;
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

    likeBtn.addEventListener("change", function (e) {
      if (!isAuthenticated) {
        e.preventDefault();
        this.checked = false;
        alert("로그인 후 이용 가능합니다.");
        return;
      }

      fetch(`/portfolios/${portfolioId}/like`, {
        method: "POST",
        headers: {
          [csrfHeader]: csrfToken,
          "Content-Type": "application/json",
          "X-Requested-With": "XMLHttpRequest",
        },
      })
        .then(async res => {
          if (!res.ok) throw new Error(await res.text());
          return res.text();
        })
        .then(count => {
          const num = parseInt(count);
          if (!isNaN(num)) likeCountEl.textContent = num;
        })
        .catch(err => {
          console.error("좋아요 오류:", err);
          alert("좋아요 중 오류 발생");
          this.checked = !this.checked;
        });
    });
  }
});

/* -----------------------------
   삭제 모달 열기/닫기
----------------------------- */
window.openDeleteModal = function () {
  const modal = document.getElementById("deleteModal");
  if (modal) {
    modal.style.display = "flex";
    modal.style.justifyContent = "center";
    modal.style.alignItems = "center";
  }
};

window.closeDeleteModal = function () {
  const modal = document.getElementById("deleteModal");
  if (modal) {
    modal.style.display = "none";
  }
};

// 모달 바깥 클릭 시 닫기
document.addEventListener("click", (e) => {
  const modal = document.getElementById("deleteModal");
  if (modal && e.target === modal) {
    modal.style.display = "none";
  }
});


let currentCommentForm = null;

// 댓글 삭제 모달 열기
function openCommentDeleteModal(form) {
  currentCommentForm = form; // 클릭된 폼 저장
  const modal = document.getElementById("commentDeleteModal");
  modal.style.display = "flex"; // 보이게
  return false; // 기본 submit 막기
}

// 댓글 삭제 모달 닫기
function closeCommentDeleteModal() {
  const modal = document.getElementById("commentDeleteModal");
  modal.style.display = "none";
  currentCommentForm = null;
}

// 확인 버튼 → 실제 submit 실행
document.getElementById("commentDeleteConfirm").addEventListener("click", () => {
  if (currentCommentForm) {
    currentCommentForm.submit(); // 진짜 삭제 실행
  }
});
