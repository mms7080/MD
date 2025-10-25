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
   태그 더보기 / 접기
----------------------------- */
window.toggleTags = function (button) {
  const tagsList = button.previousElementSibling;
  if (!tagsList) return;

  const allTags = tagsList.querySelectorAll(".tag-item");
  const visibleCount = tagsList.querySelectorAll(".tag-item:not([style*='display: none'])").length;

  if (button.dataset.expanded === "true") {
    allTags.forEach((tag, i) => (tag.style.display = i < 6 ? "inline-block" : "none"));
    button.textContent = "+ 더보기";
    button.dataset.expanded = "false";
  } else {
    for (let i = visibleCount; i < visibleCount + 6 && i < allTags.length; i++) {
      allTags[i].style.display = "inline-block";
    }
    if (visibleCount + 6 >= allTags.length) {
      button.textContent = "접기";
      button.dataset.expanded = "true";
    }
  }
};

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
