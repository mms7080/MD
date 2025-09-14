document.addEventListener('DOMContentLoaded', () => {
    // URL 경로에서 Folio ID 추출 (예: /folios/detail/abc-123 -> abc-123)
    const pathParts = window.location.pathname.split('/');
    const folioId = pathParts[pathParts.length - 1];

    const contentDiv = document.getElementById('folio-detail-content');

    // ID가 없으면 함수 종료
    if (!folioId) {
        contentDiv.innerHTML = '<p class="muted">잘못된 접근입니다.</p>';
        return;
    }

    // API를 호출하여 데이터 로드 및 렌더링
    loadFolioDetails(folioId);
});

/**
 * API를 호출하여 Folio 상세 데이터를 가져와 화면에 렌더링하는 함수
 * @param {string} id - Folio ID
 */
async function loadFolioDetails(id) {
    const contentDiv = document.getElementById('folio-detail-content');
    try {
        const response = await fetch(`/api/folios/${id}`);

        // API 응답이 실패(404 Not Found 등)한 경우
        if (!response.ok) {
            contentDiv.innerHTML = `
                <div class="detail-section">
                    <h2>오류</h2>
                    <p class="muted">Folio를 찾을 수 없습니다. (ID: ${id})</p>
                </div>`;
            return;
        }

        const data = await response.json();

        // 1. 프로필 헤더 정보 채우기
        document.getElementById('profile-image').src = data.thumbnail || 'https://picsum.photos/seed/default/150';
        document.getElementById('profile-name').textContent = data.user.userName || '이름 없음';
        //자기소개 전체를 보여주도록 수정
        document.getElementById('profile-intro').textContent = `"${data.introduction || '자기소개가 없습니다.'}"`; 

        // 2. 기술 스택 태그 렌더링
        const skillsContainer = document.getElementById('profile-skills');
        skillsContainer.innerHTML = '';
        data.skills.forEach(skill => {
            const tagEl = document.createElement('span');
            tagEl.className = 'tag';
            tagEl.textContent = `#${skill}`;
            skillsContainer.appendChild(tagEl);
        });

        // 3. 자기소개 본문 채우기
        document.getElementById('introduction-text').textContent = data.introduction || '자기소개가 없습니다.';

        // 4. 참여 포트폴리오 카드 렌더링
        const projectList = document.getElementById('project-list');
        projectList.innerHTML = '';
        data.projects.forEach(project => {
            const projectCard = `
                <a href="/portfolios/${project.portfolioId}" class="project-card">
                    <img src="https://picsum.photos/seed/${project.portfolioId}/300/200" alt="${project.title} 썸네일">
                    <div class="project-title">${project.title}</div>
                </a>`;
            projectList.innerHTML += projectCard;
        });

        // 5. 개인 사진 렌더링
        const photoGallery = document.getElementById('photo-gallery');
        photoGallery.innerHTML = '';
        data.photos.forEach(photoUrl => {
            const photoImg = document.createElement('img');
            photoImg.src = photoUrl;
            photoImg.alt = '개인 사진';
            photoGallery.appendChild(photoImg);
        });

    } catch (error) {
        console.error('상세 데이터를 불러오는 중 오류 발생:', error);
        contentDiv.innerHTML = '<p class="muted">데이터를 불러오는 중 오류가 발생했습니다.</p>';
    }
}