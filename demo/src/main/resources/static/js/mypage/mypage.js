// /js/mypage/mypage.js
document.addEventListener("DOMContentLoaded", () => {
    const countEl = document.getElementById("folio-count");
    const listEl = document.getElementById("folio-recent-list");

    const fmt = (ts) => {
        if (!ts) return "";
        const d = new Date(ts);
        const p = (n) => String(n).padStart(2, "0");
        return `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(
            d.getHours()
        )}:${p(d.getMinutes())}`;
    };

    async function loadBuckets() {
        const countEl = document.getElementById("folio-count");
        const draftEl = document.getElementById("folio-draft-list");
        const pubEl = document.getElementById("folio-pub-list");

        const fmt = (ts) => {
            if (!ts) return "";
            const d = new Date(ts);
            const p = (n) => String(n).padStart(2, "0");
            return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(
                d.getDate()
            )} ${p(d.getHours())}:${p(d.getMinutes())}`;
        };

        try {
            const res = await fetch("/api/folios/me/buckets");
            if (res.status === 403) {
                if (countEl) countEl.textContent = "0";
                if (draftEl)
                    draftEl.innerHTML =
                        '<li class="ghost">로그인이 필요합니다.</li>';
                if (pubEl)
                    pubEl.innerHTML =
                        '<li class="ghost">로그인이 필요합니다.</li>';
                return;
            }
            if (!res.ok) throw new Error("failed");

            const data = await res.json();
            const drafts = Array.isArray(data.DRAFT) ? data.DRAFT : [];
            const pubs = Array.isArray(data.PUBLISHED) ? data.PUBLISHED : [];

            // 총 개수
            if (countEl)
                countEl.textContent = String(drafts.length + pubs.length);

            // 공통 렌더러
            const renderList = (ul, items, isDraft) => {
                if (!ul) return;
                ul.innerHTML = "";
                if (!items.length) {
                    ul.innerHTML = `<li class="ghost">${
                        isDraft ? "임시저장 없음" : "업로드 없음"
                    }</li>`;
                    return;
                }
                items.slice(0, 5).forEach((f) => {
                    const li = document.createElement("li");
                    const a = document.createElement("a");

                    if (isDraft) {
                        a.href = `/folios/edit?id=${encodeURIComponent(f.id)}`;
                        a.textContent = f.title || "제목 없음"; // ⬅︎ [임시저장] 라벨 제거
                        a.style.color = "#ffb74d";
                    } else {
                        a.href = `/folios/detail/${encodeURIComponent(f.id)}`;
                        a.textContent = f.title || "제목 없음";
                    }
                    li.appendChild(a);

                    if (f.updatedAt) {
                        const small = document.createElement("small");
                        small.textContent = fmt(f.updatedAt); // ⬅︎ 날짜+시간 출력
                        li.appendChild(small);
                    }
                    ul.appendChild(li);
                });
            };

            renderList(draftEl, drafts, true);
            renderList(pubEl, pubs, false);
        } catch (e) {
            if (countEl) countEl.textContent = "0";
            if (draftEl)
                draftEl.innerHTML =
                    '<li class="ghost">불러오지 못했습니다.</li>';
            if (pubEl)
                pubEl.innerHTML = '<li class="ghost">불러오지 못했습니다.</li>';
        }
    }

    loadBuckets();
});
