/* /resources/static/js/folios/detail.js (최종 수정 완료) */
document.addEventListener("DOMContentLoaded", () => {
    const pathParts = window.location.pathname.split("/");
    const folioId = pathParts[pathParts.length - 1];

    const contentDiv = document.getElementById("folio-detail-content");

    if (!folioId || folioId.trim() === "detail" || folioId.trim() === "") {
        contentDiv.innerHTML =
            '<p class="muted">잘못된 접근입니다. Folio ID가 필요합니다.</p>';
        return;
    }

    loadFolioDetails(folioId);
});

async function loadFolioDetails(id) {
    const contentDiv = document.getElementById("folio-detail-content");
    try {
        const response = await fetch(`/api/folios/${id}`);

        if (!response.ok) {
            contentDiv.innerHTML = `<div class="detail-section"><h2>오류</h2><p class="muted">Folio를 찾을 수 없습니다. (ID: ${id})</p></div>`;
            return;
        }

        const data = await response.json();

        // --- 수정된 부분: HTML ID와 정확히 일치하도록 수정 ---
        document.getElementById("profile-image").src =
            data.photos?.[0] || "https://picsum.photos/seed/default/150";
        document.getElementById("profile-name").textContent =
            data.user?.name || "이름 없음";
        document.getElementById("profile-job").textContent =
            data.introduction || "자기소개가 없습니다."; // profile-intro 대신 profile-job에 자기소개 표시

        const skillsContainer = document.getElementById("profile-skills");
        skillsContainer.innerHTML = "";
        (data.skills || []).forEach((skill) => {
            const tagEl = document.createElement("span");
            tagEl.className = "tag";
            tagEl.textContent = `#${skill}`;
            skillsContainer.appendChild(tagEl);
        });

        // 자기소개 본문 채우기 (ID가 introduction-text인 요소에)
        document.getElementById("introduction-text").textContent =
            data.introduction || "작성된 자기소개가 없습니다.";

        const projectList = document.getElementById("project-list");
        projectList.innerHTML = "";
        (data.projects || []).forEach((project) => {
            const projectCard = `
                <a href="/portfolios/detail/${project.portfolioId}" class="project-card">
                    <img src="https://picsum.photos/seed/${project.portfolioId}/300/200" alt="${project.title} 썸네일">
                    <div class="project-title">${project.title}</div>
                </a>`;
            projectList.insertAdjacentHTML("beforeend", projectCard);
        });

        const photoGallery = document.getElementById("photo-gallery");
        photoGallery.innerHTML = "";
        (data.photos || []).forEach((photoUrl) => {
            const photoImg = document.createElement("img");
            photoImg.src = photoUrl;
            photoImg.alt = "개인 사진";
            photoGallery.appendChild(photoImg);
        });
    } catch (error) {
        console.error("상세 데이터를 불러오는 중 오류 발생:", error);
        contentDiv.innerHTML =
            '<p class="muted">데이터를 불러오는 중 오류가 발생했습니다.</p>';
    }
}
