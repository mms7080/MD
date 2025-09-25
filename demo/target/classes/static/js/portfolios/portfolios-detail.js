let currentIndex = 0;
let images = [];

/* -----------------------------
   이미지 모달 관련
----------------------------- */
function loadImages() {
  const mainImage = document.getElementById("mainImage");
  const thumbs = Array.from(document.querySelectorAll(".thumb"));

  images = [];
  if (mainImage) {
    images.push(mainImage.getAttribute("src")); // 대표 이미지 1장
  }
  if (thumbs.length > 0) {
    images.push(...thumbs.map(img => img.getAttribute("src"))); // 나머지 썸네일
  }
}

// 썸네일 클릭 → 대표 이미지 변경
function changeMainImage(element, index) {
  const mainImage = document.getElementById("mainImage");
  mainImage.src = element.src;
  currentIndex = index;
}

// 대표/썸네일 클릭 → 모달 열기
function openModal(index) {
  loadImages();
  currentIndex = index;

  const modal = document.getElementById("imageModal");
  const modalImage = document.getElementById("modalImage");

  modal.style.display = "flex";
  modalImage.src = images[currentIndex];
}

// 모달 닫기
function closeModal() {
  document.getElementById("imageModal").style.display = "none";
}

// 좌우 이동 (슬라이드)
function changeImage(step) {
  if (images.length === 0) return;
  currentIndex = (currentIndex + step + images.length) % images.length;
  document.getElementById("modalImage").src = images[currentIndex];
}

// ESC/←/→ 키보드 지원
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeModal();
  if (e.key === "ArrowLeft") changeImage(-1);
  if (e.key === "ArrowRight") changeImage(1);
});

/* -----------------------------
   조회수 (localStorage 예시)
----------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  const viewCountEl = document.getElementById("viewCount");
  if (viewCountEl) {
    let count = parseInt(localStorage.getItem("viewCount") || "0");
    count++;
    viewCountEl.textContent = count;
    localStorage.setItem("viewCount", count);
  }
});

/* -----------------------------
   삭제 모달
----------------------------- */
function openDeleteModal() {
  document.getElementById("deleteModal").style.display = "flex";
}
function closeDeleteModal() {
  document.getElementById("deleteModal").style.display = "none";
}

/* -----------------------------
   태그 더보기 / 접기
----------------------------- */
function toggleTags(button) {
  const tagsList = button.previousElementSibling; // .tags-list
  const allTags = tagsList.querySelectorAll(".tag-item");
  const visibleCount = tagsList.querySelectorAll(".tag-item:not([style*='display: none'])").length;

  if (button.dataset.expanded === "true") {
    // 접기 → 처음 8개만 보이게
    allTags.forEach((tag, i) => {
      tag.style.display = i < 6 ? "inline-block" : "none";
    });
    button.textContent = "+ 더보기";
    button.dataset.expanded = "false";
  } else {
    // 더보기 → 현재 보이는 개수 + 8개 더
    for (let i = visibleCount; i < visibleCount + 6 && i < allTags.length; i++) {
      allTags[i].style.display = "inline-block";
    }

    // 다 보이면 접기로 변경
    if (visibleCount + 6 >= allTags.length) {
      button.textContent = "접기";
      button.dataset.expanded = "true";
    }
  }
}

// 초기화 (처음에 8개만 보이게)
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".tags-list").forEach(tagsList => {
    const allTags = tagsList.querySelectorAll(".tag-item");
    allTags.forEach((tag, i) => {
      tag.style.display = i < 6 ? "inline-block" : "none";
    });
  });
});
