(() => {
    const grid = document.getElementById("folio-grid");
    const tpl = document.getElementById("folio-card-template");
    const pager = document.getElementById("pagination");

    const fmtDate = (iso) => {
        try {
            const d = new Date(iso);
            const y = d.getFullYear();
            const m = String(d.getMonth() + 1).padStart(2, "0");
            const day = String(d.getDate()).padStart(2, "0");
            return `${y}.${m}.${day}`;
        } catch {
            return "";
        }
    };

    const renderEmpty = (msg) => {
        grid.innerHTML = `<div class="empty-message">${msg}</div>`;
        pager.innerHTML = "";
    };

    const renderPage = (page) => {
        grid.innerHTML = "";
        const items = page.content || [];
        if (!items.length) {
            renderEmpty("아직 등록된 Folio가 없습니다.");
            return;
        }

        for (const it of items) {
            const node = tpl.content.firstElementChild.cloneNode(true);

            // 링크
            const link = node.querySelector(".kf-card__thumb");
            link.href = `/folios/detail/${encodeURIComponent(it.id)}`;

            // 썸네일 (없으면 placeholder)
            const img = node.querySelector("img");
            img.src =
                it.thumbnail && it.thumbnail.trim()
                    ? it.thumbnail
                    : "https://picsum.photos/seed/folio/600/338";

            // 제목: 이름 / 작성날짜
            const titleAnchor = node.querySelector(".kf-card__title > a");
            titleAnchor.textContent = `${it.title || "제목 없음"} / ${fmtDate(
                it.updatedAt
            )}`;
            titleAnchor.href = link.href;

            // 태그(상태)
            const tags = node.querySelector(".kf-card__tags");
            tags.innerHTML = `<span class="fl-badge">${it.status}</span>`;

            grid.appendChild(node);
        }

        // 페이지네이션
        const { number, totalPages } = page;
        pager.innerHTML = "";
        if (totalPages <= 1) return;

        const makeBtn = (label, pageIdx, disabled, active) => {
            const b = document.createElement("button");
            b.className = "page-btn" + (active ? " active" : "");
            b.textContent = label;
            b.disabled = !!disabled;
            b.addEventListener("click", () => fetchAndRender(pageIdx));
            return b;
        };

        pager.appendChild(makeBtn("이전", number - 1, number === 0, false));

        // 간단: 현재 페이지 기준 앞뒤 최대 2페이지
        const start = Math.max(0, number - 2);
        const end = Math.min(totalPages - 1, number + 2);
        for (let i = start; i <= end; i++) {
            pager.appendChild(makeBtn(String(i + 1), i, false, i === number));
        }

        pager.appendChild(
            makeBtn("다음", number + 1, number >= totalPages - 1, false)
        );
    };

    const fetchAndRender = async (page = 0, size = 12) => {
        grid.innerHTML = `<div class="loading-message">불러오는 중...</div>`;
        pager.innerHTML = "";
        try {
            const res = await fetch(`/api/folios?page=${page}&size=${size}`, {
                credentials: "same-origin",
            });
            if (!res.ok) throw new Error("목록 조회 실패");
            const json = await res.json();
            renderPage(json);
        } catch (e) {
            console.error(e);
            renderEmpty("목록을 불러오지 못했습니다.");
        }
    };

    fetchAndRender(0, 12);
})();
