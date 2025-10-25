// /js/mypage/mypage.js
document.addEventListener("DOMContentLoaded", () => {
    const countEl = document.getElementById("folio-count");
    const draftEl = document.getElementById("folio-draft-list");
    const pubEl = document.getElementById("folio-pub-list");

    const fmt = (ts) => {
        if (!ts) return "";
        const d = new Date(ts);
        const p = (n) => String(n).padStart(2, "0");
        return `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(
            d.getHours()
        )}:${p(d.getMinutes())}`;
    };

    const renderList = (ul, items, isDraft) => {
        if (!ul) return;
        ul.innerHTML = "";
        if (!items?.length) {
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
                a.textContent = f.title || "제목 없음"; // [임시저장] 라벨 제거
                a.style.color = "#ffb74d";
            } else {
                a.href = `/folios/detail/${encodeURIComponent(f.id)}`;
                a.textContent = f.title || "제목 없음";
            }

            li.appendChild(a);

            if (f.updatedAt) {
                const small = document.createElement("small");
                small.textContent = fmt(f.updatedAt); // 날짜+시간 출력
                li.appendChild(small);
            }

            ul.appendChild(li);
        });
    };

    async function loadBuckets() {
        try {
            const res = await fetch("/api/folios/me/buckets");
            if (res.status === 403) {
                if (countEl) countEl.textContent = "0";
                renderList(draftEl, [], true);
                renderList(pubEl, [], false);
                return;
            }
            if (!res.ok) throw new Error("failed");

            const data = await res.json();
            const drafts = Array.isArray(data.DRAFT) ? data.DRAFT : [];
            const pubs = Array.isArray(data.PUBLISHED) ? data.PUBLISHED : [];
            const total = drafts.length + pubs.length;

            if (countEl) countEl.textContent = String(total);

            renderList(draftEl, drafts, true);
            renderList(pubEl, pubs, false);
        } catch (e) {
            if (countEl) countEl.textContent = "0";
            renderList(draftEl, [], true);
            renderList(pubEl, [], false);
            console.error("loadBuckets error:", e);
        }
    }

    loadBuckets();
    window.refreshFolioBuckets = loadBuckets;
});

// === 모달 열기: 기존 "더보기" 버튼과 연결 ===
(() => {
    const btn = document.getElementById("folio-more-btn");
    if (!btn) return;

    const modal = document.getElementById("folioModal");
    const listDraft = document.getElementById("folioListDraft");
    const listPub = document.getElementById("folioListPub");
    const moreDraft = document.getElementById("folioMoreDraft");
    const morePub = document.getElementById("folioMorePub");

    // 페이징 상태
    const state = {
        DRAFT: { page: 0, size: 20, done: false, loading: false },
        PUBLISHED: { page: 0, size: 20, done: false, loading: false },
        active: "DRAFT",
    };

    const CSRF = (() => {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;
        return token && header ? { header, token } : null;
    })();

    const open = () => {
        // 초기화
        listDraft.innerHTML = "";
        listPub.innerHTML = "";
        Object.assign(state.DRAFT, { page: 0, done: false });
        Object.assign(state.PUBLISHED, { page: 0, done: false });
        modal.hidden = false;
        document.body.style.overflow = "hidden";
        // 기본 탭 로드
        switchTab("DRAFT");
        loadFolioTab("DRAFT");
    };

    const close = () => {
        modal.hidden = true;
        document.body.style.overflow = "";
    };

    const switchTab = (tab) => {
        state.active = tab;
        modal.querySelectorAll(".folio-modal__tabs button").forEach((b) => {
            b.classList.toggle("is-active", b.dataset.tab === tab);
        });
        listDraft.hidden = tab !== "DRAFT";
        listPub.hidden = tab !== "PUBLISHED";
        moreDraft.hidden = tab !== "DRAFT";
        morePub.hidden = tab !== "PUBLISHED";
    };

    // 목록 로드
    async function loadFolioTab(status) {
        const s = state[status];
        if (s.done || s.loading) return;
        s.loading = true;
        const url = `/api/folios/me/list?status=${encodeURIComponent(
            status
        )}&page=${s.page}&size=${s.size}`;
        try {
            const res = await fetch(url, { method: "GET" });
            if (!res.ok) {
                const msg = await res.text().catch(() => "");
                alert(
                    `불러오기 실패\n${res.status} ${
                        res.statusText
                    }\nURL: ${url}\n${msg.slice(0, 200)}`
                );
                return;
            }
            const data = await res.json(); // Page<FoliosSummaryDto>
            const items = Array.isArray(data.items) ? data.items : [];
            const ul = status === "DRAFT" ? listDraft : listPub;
            renderItems(ul, items, status);
            // 페이지네이션 상태 업데이트
            s.page += 1;
            const isLast =
                data.last === true || s.page >= (data.totalPages ?? 0);
            s.done = isLast;
            (status === "DRAFT" ? moreDraft : morePub).disabled = isLast;
        } catch (e) {
            alert(`불러오기 실패\n네트워크 오류\nURL: ${url}\n${e.message}`);
        } finally {
            s.loading = false;
        }
    }
    // 아이템 렌더
    const renderItems = (ul, items, tab) => {
        if (!items.length && !ul.children.length) {
            const li = document.createElement("li");
            li.className = "folio-item";
            li.innerHTML = `<span style="grid-column:1/-1;color:#9aa5b1;">데이터가 없습니다.</span>`;
            ul.appendChild(li);
            return;
        }

        items.forEach((item) => {
            const li = document.createElement("li");
            li.className = "folio-item";

            // 제목 링크
            const a = document.createElement("a");
            a.textContent = item.title || "제목 없음";
            a.href =
                tab === "DRAFT"
                    ? `/folios/edit?id=${encodeURIComponent(item.id)}`
                    : `/folios/detail/${encodeURIComponent(item.id)}`;

            // 날짜
            const small = document.createElement("small");
            small.textContent = fmt(item.updatedAt);

            // 삭제 버튼
            const del = document.createElement("button");
            del.className = "del";
            del.textContent = "삭제";
            del.addEventListener("click", async (ev) => {
                ev.preventDefault();
                ev.stopPropagation();
                if (!confirm("이 폴리오를 삭제할까요?")) return;

                try {
                    const opt = { method: "DELETE", headers: {} };
                    if (CSRF) opt.headers[CSRF.header] = CSRF.token;
                    const r = await fetch(
                        `/api/folios/${encodeURIComponent(item.id)}`,
                        opt
                    );
                    if (!r.ok) throw new Error();
                    li.remove();
                    // 상단 카드/카운트 갱신
                    if (typeof loadBuckets === "function") loadBuckets();
                } catch {
                    alert("삭제 실패");
                }
            });

            li.appendChild(a);
            li.appendChild(small);
            li.appendChild(del);
            ul.appendChild(li);
        });
    };

    // 이벤트
    btn.addEventListener("click", open);
    modal.addEventListener("click", (e) => {
        if (e.target.hasAttribute("data-close")) close();
    });
    modal.querySelectorAll(".folio-modal__tabs button").forEach((b) => {
        b.addEventListener("click", () => {
            switchTab(b.dataset.tab);
            // 처음 여는 탭이면 로드
            const s = state[b.dataset.tab];
            if (s.page === 0 && !s.loading) loadFolioTab(b.dataset.tab);
        });
    });
    moreDraft.addEventListener("click", () => loadFolioTab("DRAFT"));
    morePub.addEventListener("click", () => loadFolioTab("PUBLISHED"));
    window.addEventListener("keydown", (e) => {
        window.refreshFolioBuckets?.();
    });
})();
