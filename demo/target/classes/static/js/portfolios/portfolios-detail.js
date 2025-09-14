let currentIndex = 0;
let images = [];

// 썸네일 hover/클릭 시 대표 이미지 변경
function changeMainImage(element, index) {
  const mainImage = document.getElementById("mainImage");
  mainImage.src = element.src;
  currentIndex = index; // 현재 대표 이미지 index 갱신
}

// 대표 이미지 클릭 → 모달 열기
function openModal(index) {
  images = Array.from(document.querySelectorAll(".thumb"))
                .map(img => img.getAttribute("src"));

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

// 좌우 이동
function changeImage(step) {
  currentIndex += step;

  if (currentIndex < 0) currentIndex = images.length - 1;
  if (currentIndex >= images.length) currentIndex = 0;

  document.getElementById("modalImage").src = images[currentIndex];
}

// ESC/←/→ 키보드 지원
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeModal();
  if (e.key === "ArrowLeft") changeImage(-1);
  if (e.key === "ArrowRight") changeImage(1);
});

document.addEventListener("DOMContentLoaded", () => {
  const viewCountEl = document.getElementById("viewCount");
  let count = parseInt(localStorage.getItem("viewCount") || "0"); // ✅ 브라우저에 저장된 값 불러오기
  count++;
  viewCountEl.textContent = count;
  localStorage.setItem("viewCount", count); // ✅ 새 값 저장
});