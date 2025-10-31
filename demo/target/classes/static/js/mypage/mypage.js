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


(function () {
  // ========= 공통 유틸 =========
  const openModal  = (m) => { if (!m) return; m.style.display = 'flex'; m.setAttribute('aria-hidden','false'); };
  const closeModal = (m) => { if (!m) return; m.style.display = 'none';  m.setAttribute('aria-hidden','true'); };
  const on = (el, ev, fn) => { if (el) el.addEventListener(ev, fn, { passive:true }); };

  const show = (el, msg, ok) => {
    if (!el) return;
    el.textContent = msg;
    el.className = `note-banner${ok ? '' : ' error'}`;
    el.hidden = false;
  };
  const hide = (el) => { if (el) el.hidden = true; };

  // ========= 프로필 모달 =========
  const profileModal      = document.getElementById('profileModal');
  const btnOpenProfile    = document.getElementById('btnOpenProfile');
  const ovProfile         = document.getElementById('ovProfile');
  const btnCloseProfile   = document.getElementById('btnCloseProfile');
  const btnCancelProfile  = document.getElementById('btnCancelProfile');

  on(btnOpenProfile,  'click', () => openModal(profileModal));
  on(ovProfile,       'click', () => closeModal(profileModal));
  on(btnCloseProfile, 'click', () => closeModal(profileModal));
  on(btnCancelProfile,'click', () => closeModal(profileModal));

  // ========= 비밀번호 모달 =========
  const passwordModal     = document.getElementById('passwordModal');
  const btnOpenPassword   = document.getElementById('btnOpenPassword');
  const ovPassword        = document.getElementById('ovPassword');
  const btnClosePassword  = document.getElementById('btnClosePassword');
  const btnCancelPassword = document.getElementById('btnCancelPassword');

  // 진행 중 서버검증 취소용
  let currCtrl = null;

  // 상태줄 없으면 생성
  function ensureStatusUnder(input, id) {
    if (!input) return null;
    let el = input.nextElementSibling;
    if (!el || !el.classList || !el.classList.contains('note-banner')) {
      el = document.createElement('p');
      el.className = 'note-banner';
      el.hidden = true;
      if (id) el.id = id;
      input.insertAdjacentElement('afterend', el);
    }
    return el;
  }

  // 🔹 비밀번호 모달 리셋 (열기 직전 & 닫은 직후)
  function resetPasswordModal() {
    const form = passwordModal?.querySelector('form');
    const curr = passwordModal?.querySelector('input[name="currentPassword"]');
    const neo  = passwordModal?.querySelector('input[name="newPassword"]');
    const conf = passwordModal?.querySelector('input[name="confirmNewPassword"]');
    if (!form || !curr || !neo || !conf) return;

    // 배너 핸들
    const sCurr = document.getElementById('status-current')  || ensureStatusUnder(curr, 'status-current');
    const sNew  = document.getElementById('status-new')      || ensureStatusUnder(neo , 'status-new');
    const sConf = document.getElementById('status-confirm')  || ensureStatusUnder(conf, 'status-confirm');

    // 1) 폼 리셋 + 값 수동 초기화
    form.reset();
    [curr, neo, conf].forEach(el => {
      el.value = '';
      el.autocomplete = 'off';
      try { el.setCustomValidity(''); } catch(_) {}
    });

    // 2) 배너/스타일 초기화
    [sCurr, sNew, sConf].forEach(el => {
      if (!el) return;
      el.textContent = '';
      el.classList.remove('error');
      el.hidden = true;
      el.style.opacity = '';
      el.style.display = '';
    });

    // 3) 진행 중 요청 취소
    try { currCtrl?.abort(); } catch(_) {}
    currCtrl = null;

    // 4) 오토필 이중 rAF 클리어
    requestAnimationFrame(() => {
      [curr, neo, conf].forEach(el => { el.value = ''; });
      requestAnimationFrame(() => {
        [curr, neo, conf].forEach(el => { el.value = ''; el.dispatchEvent(new Event('input', {bubbles:true})); });
      });
    });
  }

  // 열기: 열기 직전 초기화 → 오픈
  on(btnOpenPassword, 'click', () => {
    resetPasswordModal();
    openModal(passwordModal);
  });

  // 닫기: 닫은 직후 초기화
  [ovPassword, btnClosePassword, btnCancelPassword].forEach(el => {
    on(el, 'click', () => { closeModal(passwordModal); resetPasswordModal(); });
  });

  // ESC: 비번 모달만 초기화
  document.addEventListener('keydown', (e) => {
    if (e.key !== 'Escape') return;
    if (passwordModal && passwordModal.style.display !== 'none') {
      closeModal(passwordModal);
      resetPasswordModal();
    }
    // 프로필 모달은 닫기만 (초기화 요구 없음)
    if (profileModal && profileModal.style.display !== 'none') closeModal(profileModal);
  }, { passive:true });

  // ========= 이미지 미리보기 =========
  const photoInput   = document.getElementById('photoInput');
  const previewImage = document.getElementById('previewImage');
  if (photoInput && previewImage) {
    photoInput.addEventListener('change', (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (evt) => { previewImage.src = evt.target.result; };
      reader.readAsDataURL(file);
    }, { passive:true });
  }

  // ========= 비밀번호 실시간 검증 =========
  if (passwordModal) {
    const curr = passwordModal.querySelector('input[name="currentPassword"]');
    const neo  = passwordModal.querySelector('input[name="newPassword"]');
    const conf = passwordModal.querySelector('input[name="confirmNewPassword"]');
    if (!curr || !neo || !conf) return;

    const sCurr = ensureStatusUnder(curr, 'status-current');
    const sNew  = ensureStatusUnder(neo , 'status-new');
    const sConf = ensureStatusUnder(conf, 'status-confirm');

    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;

    async function checkCurrent() {
      const v = curr.value.trim();
      if (!v) { hide(sCurr); return; }
      try {
        currCtrl?.abort();
        currCtrl = new AbortController();
        const res = await fetch('/mypage/api/check-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
          },
          body: new URLSearchParams({ currentPassword: v }),
          signal: currCtrl.signal
        });
        const ok = await res.json();
        ok ? show(sCurr, '비밀번호가 확인되었습니다.', true)
           : show(sCurr, '현재 비밀번호가 올바르지 않습니다.', false);
      } catch (e) {
        if (e.name === 'AbortError') return;
        show(sCurr, '서버 확인 실패', false);
      }
    }

    function showStrength() {
      const len = neo.value.trim().length;
      if (!len) { hide(sNew); return; }
      let label = '약함', ok = false;
      if (len >= 10) { label = '강함'; ok = true; }
      else if (len >= 8) { label = '보통'; ok = true; }
      show(sNew, `강도: ${label}`, ok);
    }

    function checkMatch() {
      const a = neo.value.trim();
      const b = conf.value.trim();
      if (!a) { hide(sConf); hide(sNew); return; }
      if (!b) { hide(sConf); return; }
      a === b ? show(sConf, '일치합니다.', true)
              : show(sConf, '새 비밀번호와 확인 값이 일치하지 않습니다.', false);
    }

    curr.addEventListener('input', checkCurrent, { passive:true });
    curr.addEventListener('blur',  checkCurrent, { passive:true });
    neo .addEventListener('input', () => { showStrength(); checkMatch(); }, { passive:true });
    neo .addEventListener('blur',  () => { showStrength(); checkMatch(); }, { passive:true });
    conf.addEventListener('input', checkMatch, { passive:true });
    conf.addEventListener('blur',  checkMatch, { passive:true });
  }
})();