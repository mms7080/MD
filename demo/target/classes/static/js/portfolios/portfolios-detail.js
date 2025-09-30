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
    images.push(mainImage.getAttribute("src")); // 대표 이미지
  }
  if (thumbs.length > 0) {
    images.push(...thumbs.map(img => img.getAttribute("src"))); // 썸네일
  }
}

// 썸네일 클릭 → 대표 이미지 변경
function changeMainImage(element, index) {
  const mainImage = document.getElementById("mainImage");
  if (mainImage) {
    mainImage.src = element.src;
    currentIndex = index;
  }
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
   삭제 모달
----------------------------- */
window.openDeleteModal = function () {
  document.getElementById("deleteModal").style.display = "flex";
};

window.closeDeleteModal = function () {
  document.getElementById("deleteModal").style.display = "none";
};

/* -----------------------------
   태그 더보기 / 접기
----------------------------- */
function toggleTags(button) {
  const tagsList = button.previousElementSibling; // .tags-list
  const allTags = tagsList.querySelectorAll(".tag-item");
  const visibleCount = tagsList.querySelectorAll(".tag-item:not([style*='display: none'])").length;

  if (button.dataset.expanded === "true") {
    // 접기 → 처음 6개만
    allTags.forEach((tag, i) => {
      tag.style.display = i < 6 ? "inline-block" : "none";
    });
    button.textContent = "+ 더보기";
    button.dataset.expanded = "false";
  } else {
    // 더보기 → 현재 보이는 개수 + 6개 더
    for (let i = visibleCount; i < visibleCount + 6 && i < allTags.length; i++) {
      allTags[i].style.display = "inline-block";
    }

    // 다 보이면 접기 버튼
    if (visibleCount + 6 >= allTags.length) {
      button.textContent = "접기";
      button.dataset.expanded = "true";
    }
  }
}

/* -----------------------------
   초기화 (DOMContentLoaded)
----------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  /* ✅ 조회수 처리 */
  const viewCountEl = document.getElementById("viewCount");
  if (viewCountEl) {
    const portfolioId = viewCountEl.getAttribute("data-id");
    const token = document.querySelector("meta[name='_csrf']")?.content;
    const header = document.querySelector("meta[name='_csrf_header']")?.content;

    fetch(`/portfolios/${portfolioId}/views`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [header]: token
      }
    })
      .then(res => {
        if (!res.ok) throw new Error(`조회수 요청 실패: ${res.status}`);
        return res.json();
      })
      .then(count => {
        viewCountEl.textContent = count;
      })
      .catch(err => console.error("조회수 증가 실패:", err));
  }

  /* ✅ 태그 더보기 초기화 */
  document.querySelectorAll(".tags-list").forEach(tagsList => {
    const allTags = tagsList.querySelectorAll(".tag-item");
    if (allTags.length > 0) {
      allTags.forEach((tag, i) => {
        tag.style.display = i < 6 ? "inline-block" : "none";
      });
    }
  });

  /* ✅ 평균 별점 표시 */
  const avgScoreEl = document.getElementById("avgScore");
  const ratingCountEl = document.getElementById("ratingCount");
  const avgStars = document.getElementById("avgStars");

  if (avgScoreEl && ratingCountEl && avgStars) {
    const comments = document.querySelectorAll(".stars.readonly[data-rating]");
    let sum = 0;

    comments.forEach(starBox => {
      sum += parseInt(starBox.getAttribute("data-rating")) || 0;
    });

    const count = comments.length;
    const avg = count > 0 ? (sum / count).toFixed(1) : 0.0;

    avgScoreEl.textContent = avg;
    ratingCountEl.textContent = count;

    avgStars.innerHTML = "";
    for (let i = 1; i <= 5; i++) {
      avgStars.innerHTML += i <= Math.round(avg) ? "★" : "☆";
    }
  }

/* ✅ 각 댓글의 "읽기 전용" 별점만 처리 */
document.querySelectorAll(".stars.readonly").forEach(starBox => {
  const rating = parseInt(starBox.getAttribute("data-rating")) || 0;
  starBox.innerHTML = ""; 
  for (let i = 1; i <= 5; i++) {
    const span = document.createElement("span");
    span.textContent = i <= rating ? "★" : "☆";
    span.style.color = i <= rating ? "gold" : "#555";
    starBox.appendChild(span);
  }
});



});

/* -----------------------------
   댓글 수정 기능
----------------------------- */
window.enableEdit = function (button) {
  const commentCard = button.closest(".comment");
  const contentEl = commentCard.querySelector(".content");
  const editForm = commentCard.querySelector(".edit-form");

  // 본문 숨기고 수정폼 보이기
  contentEl.style.display = "none";
  editForm.style.display = "block";

  // textarea에 기존 본문 채우기
  editForm.querySelector("textarea").value = contentEl.innerText.trim();

 // 기존 댓글 별점 값에 맞게 라디오 선택
const rating = parseInt(commentCard.querySelector(".stars.readonly")?.dataset.rating) || 0;
if (rating > 0) {
  const radio = editForm.querySelector(`input[name="rating"][value="${rating}"]`);
  if (radio) radio.checked = true;
}
};

window.cancelEdit = function (button) {
  const editForm = button.closest(".edit-form");
  const commentCard = editForm.closest(".comment");
  const contentEl = commentCard.querySelector(".content");

  editForm.style.display = "none";
  contentEl.style.display = "block";
};
