(function () {
    // ========= 공통 유틸 =========
    const openModal = (m) => {
        if (!m) return;
        m.style.display = "flex";
        m.setAttribute("aria-hidden", "false");
    };
    const closeModal = (m) => {
        if (!m) return;
        m.style.display = "none";
        m.setAttribute("aria-hidden", "true");
    };
    const on = (el, ev, fn) => {
        if (el) el.addEventListener(ev, fn, { passive: true });
    };

    const show = (el, msg, ok) => {
        if (!el) return;
        el.textContent = msg;
        el.className = `note-banner${ok ? "" : " error"}`;
        el.hidden = false;
    };
    const hide = (el) => {
        if (el) el.hidden = true;
    };

    // ========= 프로필 모달 =========
    const profileModal = document.getElementById("profileModal");
    const btnOpenProfile = document.getElementById("btnOpenProfile");
    const ovProfile = document.getElementById("ovProfile");
    const btnCloseProfile = document.getElementById("btnCloseProfile");
    const btnCancelProfile = document.getElementById("btnCancelProfile");

    on(btnOpenProfile, "click", () => openModal(profileModal));
    on(ovProfile, "click", () => closeModal(profileModal));
    on(btnCloseProfile, "click", () => closeModal(profileModal));
    on(btnCancelProfile, "click", () => closeModal(profileModal));

    // ========= 비밀번호 모달 =========
    const passwordModal = document.getElementById("passwordModal");
    const btnOpenPassword = document.getElementById("btnOpenPassword");
    const ovPassword = document.getElementById("ovPassword");
    const btnClosePassword = document.getElementById("btnClosePassword");
    const btnCancelPassword = document.getElementById("btnCancelPassword");

    // 진행 중 서버검증 취소용
    let currCtrl = null;

    // 상태줄 없으면 생성
    function ensureStatusUnder(input, id) {
        if (!input) return null;
        let el = input.nextElementSibling;
        if (!el || !el.classList || !el.classList.contains("note-banner")) {
            el = document.createElement("p");
            el.className = "note-banner";
            el.hidden = true;
            if (id) el.id = id;
            input.insertAdjacentElement("afterend", el);
        }
        return el;
    }

    // 🔹 비밀번호 모달 리셋 (열기 직전 & 닫은 직후)
    function resetPasswordModal() {
        const form = passwordModal?.querySelector("form");
        const curr = passwordModal?.querySelector(
            'input[name="currentPassword"]'
        );
        const neo = passwordModal?.querySelector('input[name="newPassword"]');
        const conf = passwordModal?.querySelector(
            'input[name="confirmNewPassword"]'
        );
        if (!form || !curr || !neo || !conf) return;

        // 배너 핸들
        const sCurr =
            document.getElementById("status-current") ||
            ensureStatusUnder(curr, "status-current");
        const sNew =
            document.getElementById("status-new") ||
            ensureStatusUnder(neo, "status-new");
        const sConf =
            document.getElementById("status-confirm") ||
            ensureStatusUnder(conf, "status-confirm");

        // 1) 폼 리셋 + 값 수동 초기화
        form.reset();
        [curr, neo, conf].forEach((el) => {
            el.value = "";
            el.autocomplete = "off";
            try {
                el.setCustomValidity("");
            } catch (_) {}
        });

        // 2) 배너/스타일 초기화
        [sCurr, sNew, sConf].forEach((el) => {
            if (!el) return;
            el.textContent = "";
            el.classList.remove("error");
            el.hidden = true;
            el.style.opacity = "";
            el.style.display = "";
        });

        // 3) 진행 중 요청 취소
        try {
            currCtrl?.abort();
        } catch (_) {}
        currCtrl = null;

        // 4) 오토필 이중 rAF 클리어
        requestAnimationFrame(() => {
            [curr, neo, conf].forEach((el) => {
                el.value = "";
            });
            requestAnimationFrame(() => {
                [curr, neo, conf].forEach((el) => {
                    el.value = "";
                    el.dispatchEvent(new Event("input", { bubbles: true }));
                });
            });
        });
    }

    // 열기: 열기 직전 초기화 → 오픈
    on(btnOpenPassword, "click", () => {
        resetPasswordModal();
        openModal(passwordModal);
    });

    // 닫기: 닫은 직후 초기화
    [ovPassword, btnClosePassword, btnCancelPassword].forEach((el) => {
        on(el, "click", () => {
            closeModal(passwordModal);
            resetPasswordModal();
        });
    });

    // ESC: 비번 모달만 초기화
    document.addEventListener(
        "keydown",
        (e) => {
            if (e.key !== "Escape") return;
            if (passwordModal && passwordModal.style.display !== "none") {
                closeModal(passwordModal);
                resetPasswordModal();
            }
            // 프로필 모달은 닫기만 (초기화 요구 없음)
            if (profileModal && profileModal.style.display !== "none")
                closeModal(profileModal);
        },
        { passive: true }
    );

    // ========= 이미지 미리보기 =========
    const photoInput = document.getElementById("photoInput");
    const previewImage = document.getElementById("previewImage");
    if (photoInput && previewImage) {
        photoInput.addEventListener(
            "change",
            (e) => {
                const file = e.target.files?.[0];
                if (!file) return;
                const reader = new FileReader();
                reader.onload = (evt) => {
                    previewImage.src = evt.target.result;
                };
                reader.readAsDataURL(file);
            },
            { passive: true }
        );
    }

    // ========= 비밀번호 실시간 검증 =========
    if (passwordModal) {
        const curr = passwordModal.querySelector(
            'input[name="currentPassword"]'
        );
        const neo = passwordModal.querySelector('input[name="newPassword"]');
        const conf = passwordModal.querySelector(
            'input[name="confirmNewPassword"]'
        );
        if (!curr || !neo || !conf) return;

        const sCurr = ensureStatusUnder(curr, "status-current");
        const sNew = ensureStatusUnder(neo, "status-new");
        const sConf = ensureStatusUnder(conf, "status-confirm");

        const csrfHeader = document.querySelector(
            'meta[name="_csrf_header"]'
        )?.content;
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

        async function checkCurrent() {
            const v = curr.value.trim();
            if (!v) {
                hide(sCurr);
                return;
            }
            try {
                currCtrl?.abort();
                currCtrl = new AbortController();
                const res = await fetch("/mypage/api/check-password", {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded; charset=UTF-8",
                        ...(csrfHeader && csrfToken
                            ? { [csrfHeader]: csrfToken }
                            : {}),
                    },
                    body: new URLSearchParams({ currentPassword: v }),
                    signal: currCtrl.signal,
                });
                const ok = await res.json();
                ok
                    ? show(sCurr, "비밀번호가 확인되었습니다.", true)
                    : show(sCurr, "현재 비밀번호가 올바르지 않습니다.", false);
            } catch (e) {
                if (e.name === "AbortError") return;
                show(sCurr, "서버 확인 실패", false);
            }
        }

        function showStrength() {
            const len = neo.value.trim().length;
            if (!len) {
                hide(sNew);
                return;
            }
            let label = "약함",
                ok = false;
            if (len >= 10) {
                label = "강함";
                ok = true;
            } else if (len >= 8) {
                label = "보통";
                ok = true;
            }
            show(sNew, `강도: ${label}`, ok);
        }

        function checkMatch() {
            const a = neo.value.trim();
            const b = conf.value.trim();
            if (!a) {
                hide(sConf);
                hide(sNew);
                return;
            }
            if (!b) {
                hide(sConf);
                return;
            }
            a === b
                ? show(sConf, "일치합니다.", true)
                : show(
                      sConf,
                      "새 비밀번호와 확인 값이 일치하지 않습니다.",
                      false
                  );
        }

        curr.addEventListener("input", checkCurrent, { passive: true });
        curr.addEventListener("blur", checkCurrent, { passive: true });
        neo.addEventListener(
            "input",
            () => {
                showStrength();
                checkMatch();
            },
            { passive: true }
        );
        neo.addEventListener(
            "blur",
            () => {
                showStrength();
                checkMatch();
            },
            { passive: true }
        );
        conf.addEventListener("input", checkMatch, { passive: true });
        conf.addEventListener("blur", checkMatch, { passive: true });
    }
})();

(async function attachMyFolioBox() {
    const box = document.getElementById("my-folio-box");
    if (!box) return;
    try {
        const res = await fetch("/api/folios/me");
        if (res.status === 403) {
            box.innerHTML = '<p class="muted">로그인이 필요합니다.</p>';
            return;
        }
        const data = await res.json();

        const draft = data.draft;
        const pub = data.published;

        const parts = [];
        if (draft) {
            parts.push(`
        <div class="row">
          <div class="col">
            <strong>임시저장</strong> <small>(${
                draft.updatedAt ?? ""
            })</small><br/>
            <a class="btn" href="/folios/edit">이어쓰기</a>
          </div>
        </div>
      `);
        }
        if (pub) {
            parts.push(`
        <div class="row">
          <div class="col">
            <strong>발행본</strong> <small>(${pub.updatedAt ?? ""})</small><br/>
            <a class="btn" href="/folios/detail/${pub.id}">상세보기</a>
          </div>
        </div>
      `);
        }
        parts.push(
            `<div class="row"><a class="btn" href="/folios/write">새로 쓰기</a></div>`
        );

        box.innerHTML = parts.join("");
    } catch (e) {
        box.innerHTML = '<p class="muted">불러오는 중 오류가 발생했습니다.</p>';
    }
})();

// ===== Folio 통계 카드 채우기 =====
(function attachFolioStatCard() {
    const card = document.getElementById("folio-stat-card");
    const numEl = document.getElementById("folio-count");
    const listEl = document.getElementById("folio-recent-list");
    if (!card || !numEl || !listEl) return;

    // 날짜 포맷 보조
    const fmt = (ts) => {
        if (!ts) return "";
        const d = new Date(ts);
        const pad = (n) => String(n).padStart(2, "0");
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(
            d.getDate()
        )} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };

    (async () => {
        try {
            // 페이지당 50개 정도 가져와서 클라이언트에서 집계 (컨트롤러가 id 내림차순 정렬 적용)
            const res = await fetch("/api/folios?page=0&size=50");
            if (!res.ok) throw new Error("fail to load folios");
            const payload = await res.json();
            const items = Array.isArray(payload.items) ? payload.items : [];

            // 총합/상태별 집계
            const total = items.length;
            let drafts = 0,
                pubs = 0;
            items.forEach((it) => {
                const s = (it.status || "").toUpperCase();
                if (s === "DRAFT") drafts++;
                else if (s === "PUBLISHED") pubs++;
            });

            // 상단 숫자: 전체 개수 (D+P)
            numEl.textContent = String(total);

            // 최근 3개 목록 (id 내림차순 가정)
            const top3 = items.slice(0, 3);
            listEl.innerHTML = "";
            if (top3.length === 0) {
                listEl.innerHTML = `<li class="ghost">작성한 Folio가 없습니다. <a href="/folios/write">새로 쓰기</a></li>`;
            } else {
                top3.forEach((it) => {
                    const id = it.id || it.folioId; // DTO 호환
                    const title = it.title || "Untitled";
                    const st = (it.status || "").toUpperCase();
                    const when = fmt(it.updatedAt || it.createdAt);
                    const badge =
                        st === "PUBLISHED"
                            ? "발행"
                            : st === "DRAFT"
                            ? "임시"
                            : "기타";
                    const href = id ? `/folios/detail/${id}` : "#";
                    const safeAttr = id ? `href="${href}"` : "";
                    listEl.insertAdjacentHTML(
                        "beforeend",
                        `<li>
              <a ${safeAttr}>${title}</a>
              <small style="margin-left:6px;opacity:.7;">${badge}${
                            when ? " · " + when : ""
                        }</small>
            </li>`
                    );
                });
            }

            // 배지 영역에 상태별 개수도 함께 표기(디자인 유지)
            // ex) "총 개수" 옆에 D/P 요약을 title 속성으로 노출(툴팁)
            const badge = card.querySelector(".stat-badge");
            if (badge) {
                badge.title = `임시저장: ${drafts} · 발행본: ${pubs}`;
            }
        } catch (e) {
            // 실패 시 기본 메시지 유지
            // 필요하면 콘솔 참고
            // console.error(e);
            numEl.textContent = "0";
            listEl.innerHTML = `<li class="ghost">불러오지 못했습니다.</li>`;
        }
    })();
})();
