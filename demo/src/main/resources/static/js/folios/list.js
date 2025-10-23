(function () {
    const grid = document.getElementById("folio-grid");
    const cardTmpl = document.getElementById("folio-card-template");
    const pager = document.getElementById("pagination");

    let page = 1;
    const size = 12;

    async function fetchPage(p) {
        const res = await fetch(`/api/folios?page=${p}&size=${size}`);
        if (!res.ok) throw new Error("목록 로드 실패");
        return await res.json(); // { page, items, totalPages, totalItems }
    }

    function renderItems(items) {
        grid.innerHTML = "";
        items.forEach((it) => {
            const node = cardTmpl.content.cloneNode(true);

            // 링크 & 썸네일
            const aThumb = node.querySelector(".kf-card__thumb");
            const img = node.querySelector(".kf-card__thumb img");
            aThumb.href = `/folios/detail/${it.id}`;
            img.src =
                it.thumbnail ||
                "https://picsum.photos/seed/placeholder/400/250";

            // 제목(=이름) + 작성일
            const titleA = node.querySelector(".kf-card__title a");
            titleA.textContent = it.title || "Untitled";
            titleA.href = `/folios/detail/${it.id}`;

            // 태그 영역은 요구사항상 숨기거나 skills 일부만 출력하고 싶다면 아래 사용
            const tags = node.querySelector(".kf-card__tags");
            tags.innerHTML = `
        <span class="pill">${(it.createdAt || "").slice(0, 10)}</span>
      `;

            grid.appendChild(node);
        });
    }

    function renderPager(pageNum, totalPages) {
        pager.innerHTML = "";
        const prev = document.createElement("button");
        prev.textContent = "이전";
        prev.disabled = pageNum <= 1;
        prev.onclick = () => {
            if (page > 1) {
                page--;
                load();
            }
        };

        const next = document.createElement("button");
        next.textContent = "다음";
        next.disabled = pageNum >= totalPages;
        next.onclick = () => {
            if (page < totalPages) {
                page++;
                load();
            }
        };

        pager.appendChild(prev);
        const info = document.createElement("span");
        info.style.margin = "0 12px";
        info.textContent = `${pageNum} / ${totalPages}`;
        pager.appendChild(info);
        pager.appendChild(next);
    }

    async function load() {
        try {
            const data = await fetchPage(page);
            renderItems(data.items || []);
            renderPager(data.page || 1, data.totalPages || 1);
        } catch (e) {
            console.error(e);
        }
    }

    load();
})();
