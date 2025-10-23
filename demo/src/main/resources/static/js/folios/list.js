/* /resources/static/js/folios/list.js */
document.addEventListener('DOMContentLoaded', function() {
    const grid = document.getElementById('folio-grid');
    const paginationContainer = document.getElementById('pagination');
    const cardTemplate = document.getElementById('folio-card-template');

    if (!grid || !paginationContainer || !cardTemplate) {
        console.error('필수 HTML 요소(grid, pagination, template)를 찾을 수 없습니다.');
        return;
    }

    async function fetchAndRenderFolios(page = 1) {
        grid.innerHTML = '<p class="loading-message muted">데이터를 불러오는 중입니다...</p>';
        paginationContainer.innerHTML = '';

        try {
            const response = await fetch(`/api/folios?page=${page - 1}&size=10`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            
            grid.innerHTML = '';

            if (data.items && data.items.length > 0) {
                data.items.forEach(folio => {
                    const cardClone = cardTemplate.content.cloneNode(true);
                    
                    const detailUrl = `/folios/detail/${folio.folioId}`;
                    cardClone.querySelector('.kf-card__thumb').href = detailUrl;
                    cardClone.querySelector('.kf-card__title a').href = detailUrl;

                    const thumbnail = cardClone.querySelector('.kf-card__thumb img');
                    thumbnail.src = folio.thumbnail || 'https://picsum.photos/seed/placeholder/400/250';
                    
                    // --- 수정된 부분 ---
                    thumbnail.alt = `${folio.userName}의 Folio 썸네일`;
                    cardClone.querySelector('.kf-card__title a').textContent = folio.userName;
                    // --------------------

                    const tagsContainer = cardClone.querySelector('.kf-card__tags');
                    tagsContainer.innerHTML = '';
                    (folio.skills || []).slice(0, 4).forEach(skill => {
                        const tagEl = document.createElement('span');
                        tagEl.className = 'kf-tag';
                        tagEl.textContent = `#${skill}`;
                        tagsContainer.appendChild(tagEl);
                    });
                    
                    grid.appendChild(cardClone);
                });

                // API 응답의 page는 0부터 시작하므로 currentPage는 data.page + 1
                renderPagination(data.page + 1, data.totalPages);
            } else {
                grid.innerHTML = '<p class="muted empty-message">아직 등록된 Folio가 없습니다.</p>';
            }
        } catch (error) {
            console.error('Folio 데이터를 불러오는 데 실패했습니다:', error);
            grid.innerHTML = '<p class="muted empty-message">데이터를 불러오는 중 오류가 발생했습니다.</p>';
        }
    }

    function renderPagination(currentPage, totalPages) {
        paginationContainer.innerHTML = '';
        if (totalPages <= 1) return;

        for (let i = 1; i <= totalPages; i++) {
            const pageButton = document.createElement('button');
            pageButton.className = 'page-btn';
            pageButton.textContent = i;
            pageButton.dataset.page = i;

            if (i === currentPage) {
                pageButton.classList.add('active');
                pageButton.disabled = true;
            }

            pageButton.addEventListener('click', (e) => {
                const targetPage = e.target.dataset.page;
                fetchAndRenderFolios(Number(targetPage));
            });
            
            paginationContainer.appendChild(pageButton);
        }
    }

    fetchAndRenderFolios(1);
});