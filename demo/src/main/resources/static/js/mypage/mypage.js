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
        DRAFT: { page: 0, size: 20, done: false, loading: false, loaded: 0 },
        PUBLISHED: {
            page: 0,
            size: 20,
            done: false,
            loading: false,
            loaded: 0,
        },
        active: "DRAFT",
    };

    const CSRF = (() => {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;
        return token && header ? { header, token } : null;
    })();

    // ----- 유틸 -----
    const setMoreState = (status, { hidden, disabled, text }) => {
        const btn = status === "DRAFT" ? moreDraft : morePub;
        if (!btn) return;
        if (hidden !== undefined) btn.hidden = hidden;
        if (disabled !== undefined) btn.disabled = disabled;
        if (text !== undefined) btn.textContent = text;
    };

    const ensureEmptyRow = (ul) => {
        const had = ul.querySelector(".folio-item.empty");
        if (ul.children.length === 0) {
            if (!had) {
                const li = document.createElement("li");
                li.className = "folio-item empty";
                li.innerHTML = `<span style="grid-column:1/-1;color:#9aa5b1;">데이터가 없습니다.</span>`;
                ul.appendChild(li);
            }
            return true;
        }
        if (had) had.remove();
        return false;
    };

    const fmt = (ts) => {
        if (!ts) return "";
        const d = new Date(ts);
        const p = (n) => String(n).padStart(2, "0");
        return `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(
            d.getHours()
        )}:${p(d.getMinutes())}`;
    };

    // ----- 열기/닫기/탭 -----
    const open = () => {
        // 초기화
        listDraft.innerHTML = "";
        listPub.innerHTML = "";
        Object.assign(state.DRAFT, {
            page: 0,
            done: false,
            loading: false,
            loaded: 0,
        });
        Object.assign(state.PUBLISHED, {
            page: 0,
            done: false,
            loading: false,
            loaded: 0,
        });

        modal.hidden = false;
        document.body.style.overflow = "hidden";

        switchTab("DRAFT");
        // 최초 로드 강제
        loadFolioTab("DRAFT", { reset: true });
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

        // 더보기 버튼 가시성은 현재 탭 기준으로만
        setMoreState("DRAFT", { hidden: tab !== "DRAFT" || state.DRAFT.done });
        setMoreState("PUBLISHED", {
            hidden: tab !== "PUBLISHED" || state.PUBLISHED.done,
        });

        // 아직 안 불러온 탭이면 첫 로드
        const s = state[tab];
        if (s.page === 0 && !s.loading && s.loaded === 0) {
            loadFolioTab(tab, { reset: true });
        } else {
            // 데이터가 이미 있으면 Empty/버튼만 정리
            const ul = tab === "DRAFT" ? listDraft : listPub;
            ensureEmptyRow(ul);
            setMoreState(tab, {
                hidden: s.done,
                disabled: s.done,
                text: "더보기",
            });
        }
    };

    // ----- 목록 로드 -----
    async function loadFolioTab(status, { reset = false } = {}) {
        const s = state[status];
        const ul = status === "DRAFT" ? listDraft : listPub;

        if (s.loading || s.done) return;
        s.loading = true;

        if (reset) {
            s.page = 0;
            s.done = false;
            s.loaded = 0;
            ul.innerHTML = "";
            ensureEmptyRow(ul); // 비어있을 때 자리잡기
        }

        setMoreState(status, { disabled: true, text: "로딩 중..." });

        const url = `/api/folios/me/list?status=${encodeURIComponent(
            status
        )}&page=${s.page}&size=${s.size}`;

        try {
            const res = await fetch(url, { method: "GET" });
            if (res.status === 204) {
                // 데이터 없음
                s.done = true;
                ensureEmptyRow(ul);
                setMoreState(status, {
                    hidden: true,
                    disabled: true,
                    text: "더보기",
                });
                return;
            }
            if (!res.ok) {
                const msg = await res.text().catch(() => "");
                alert(
                    `불러오기 실패\n${res.status} ${
                        res.statusText
                    }\nURL: ${url}\n${msg.slice(0, 200)}`
                );
                return;
            }

            const data = await res.json();
            const items = Array.isArray(data.items) ? data.items : [];

            // 렌더
            if (items.length === 0 && s.page === 0) {
                // 첫 페이지가 비면 즉시 마지막 처리
                s.done = true;
                ensureEmptyRow(ul);
                setMoreState(status, {
                    hidden: true,
                    disabled: true,
                    text: "더보기",
                });
                return;
            }

            renderItems(ul, items, status);
            s.loaded += items.length;

            // 페이지/마지막 판정 (last 플래그 + items<size 보조)
            const backendLast = data.last === true;
            const totalPages = Number.isFinite(data.totalPages)
                ? data.totalPages
                : undefined;

            // next page 준비
            s.page += 1;

            const sizeBasedLast = items.length < s.size; // 한 페이지 꽉 못 채웠으면 마지막으로 간주
            const pagesBasedLast =
                totalPages !== undefined ? s.page >= totalPages : false;

            s.done = backendLast || sizeBasedLast || pagesBasedLast;

            // 버튼 상태
            if (s.done) {
                setMoreState(status, {
                    hidden: true,
                    disabled: true,
                    text: "더보기",
                });
            } else {
                setMoreState(status, {
                    hidden: false,
                    disabled: false,
                    text: "더보기",
                });
            }
        } catch (e) {
            alert(`불러오기 실패\n네트워크 오류\nURL: ${url}\n${e.message}`);
            setMoreState(status, { disabled: false, text: "더보기" });
        } finally {
            s.loading = false;
            // 현재 탭이 아닐 땐 버튼을 가려둔다
            const active = state.active;
            setMoreState("DRAFT", {
                hidden: active !== "DRAFT" || state.DRAFT.done,
            });
            setMoreState("PUBLISHED", {
                hidden: active !== "PUBLISHED" || state.PUBLISHED.done,
            });
        }
    }

    // ----- 아이템 렌더 & 삭제 -----
    const renderItems = (ul, items, tab) => {
        // 빈 안내 제거
        const emptyRow = ul.querySelector(".folio-item.empty");
        if (emptyRow) emptyRow.remove();

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
            small.textContent =
                item.updatedAt && ("" + item.updatedAt).length
                    ? fmt(item.updatedAt)
                    : "";

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

                    // DOM 제거
                    li.remove();

                    // 상단 카드/카운트 갱신
                    if (typeof window.refreshFolioBuckets === "function")
                        window.refreshFolioBuckets();

                    // 리스트가 비면 Empty 표시 및 더보기 숨김
                    const nowEmpty = ensureEmptyRow(ul);
                    if (nowEmpty) {
                        const s = state[tab];
                        s.done = true;
                        setMoreState(tab, { hidden: true, disabled: true });
                    }
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

    // ----- 이벤트 바인딩 -----
    btn.addEventListener("click", open);

    modal.addEventListener("click", (e) => {
        if (e.target.hasAttribute("data-close")) close();
    });

    modal.querySelectorAll(".folio-modal__tabs button").forEach((b) => {
        b.addEventListener("click", () => switchTab(b.dataset.tab));
    });

    moreDraft.addEventListener("click", () => loadFolioTab("DRAFT"));
    morePub.addEventListener("click", () => loadFolioTab("PUBLISHED"));
})();
