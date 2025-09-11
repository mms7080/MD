document.addEventListener('DOMContentLoaded', function() {
    const grid = document.getElementById('folio-grid');
    if (!grid) return; // folio-grid가 없는 페이지에서는 실행하지 않음

    const loadingMessage = grid.querySelector('.loading-message');

    fetch('/api/folios')
        .then(response => {
            if (!response.ok) {
                throw new Error('데이터를 불러오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            if (loadingMessage) {
                loadingMessage.remove();
            }

            if (!data.items || data.items.length === 0) {
                grid.innerHTML = '<p class="muted">등록된 Folio가 없습니다.</p>';
                return;
            }

            data.items.forEach(folio => {
                const skillsHtml = folio.skills.map(skill => `<span class="tag">${skill}</span>`).join('');
                const cardHtml = `
                    <div class="card">
                        <a href="/folios/detail/${folio.folioId}" class="card-link">
                            <div class="media">
                                <img src="${folio.thumbnail}" alt="프로필 사진" />
                            </div>
                            <div class="body">
                                <h3 class="title">${folio.user.userName}</h3>
                                <div class="tags">${skillsHtml}</div>
                            </div>
                        </a>
                    </div>
                `;
                grid.insertAdjacentHTML('beforeend', cardHtml);
            });
        })
        .catch(error => {
            if (loadingMessage) {
                loadingMessage.textContent = '오류: ' + error.message;
            }
            console.error('Fetch Error:', error);
        });
});