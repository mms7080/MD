/* ========== 아이디 중복 확인 ========== */
(function () {
    const btn = document.getElementById("btnCheckId");
    const input = document.getElementById("username");
    const status = document.getElementById("usernameStatus"); // 있으면 사용

    function show(msg, ok) {
        if (!status) {
            alert(msg);
            return;
        }
        status.textContent = msg;
        status.className = `note-banner${ok ? "" : " error"}`;
        status.hidden = false;
    }

    btn?.addEventListener("click", async () => {
        const username = (input?.value || "").trim();
        if (!username) {
            alert("아이디를 입력하세요.");
            input?.focus();
            return;
        }

        btn.disabled = true;
        try {
            const res = await fetch(
                `/api/check-username?username=${encodeURIComponent(username)}`
            );
            const exists = await res.json(); // 서버가 true/false 반환한다고 가정
            if (exists) show("이미 사용 중인 아이디입니다.", false);
            else show("사용 가능한 아이디입니다.", true);
        } catch {
            show("중복 확인 중 오류가 발생했습니다.", false);
        } finally {
            btn.disabled = false;
        }
    });
})();

/* ========== 생년월일 → 만 나이 자동 계산 ========== */
(function () {
    const birth = document.getElementById("birth");
    const ageHidden = document.getElementById("ageHidden");
    if (!birth || !ageHidden) return;

    // 미래 금지 / 최소연도
    const today = new Date();
    birth.max = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(
        2,
        "0"
    )}-${String(today.getDate()).padStart(2, "0")}`;
    birth.min = `1900-01-01`;

    function syncAge() {
        const val = birth.value; // yyyy-mm-dd
        if (!/^\d{4}-\d{2}-\d{2}$/.test(val)) {
            ageHidden.value = "";
            return;
        }
        const [y, m, d] = val.split("-").map(Number);
        const b = new Date(y, m - 1, d);
        if (isNaN(b)) {
            ageHidden.value = "";
            return;
        }

        const now = new Date();
        let age = now.getFullYear() - b.getFullYear();
        const beforeBirthday =
            now.getMonth() < b.getMonth() ||
            (now.getMonth() === b.getMonth() && now.getDate() < b.getDate());
        if (beforeBirthday) age--;
        ageHidden.value = age >= 0 && age < 200 ? String(age) : "";
    }

    birth.addEventListener("change", syncAge);
    birth.addEventListener("input", syncAge);
    if (!ageHidden.value) syncAge();
})();

/* ========== 이메일 콤보(아이디/도메인) → hidden email 동기화 + 인증코드 발송 ========== */
(function () {
    const combo = document.getElementById("emailCombo");
    const localEl = document.getElementById("emailLocal");
    const selEl = document.getElementById("emailDomain");
    const custEl = document.getElementById("emailDomainCustom");
    const hidden = document.getElementById("email");
    const btnSend = document.getElementById("btnSendCode"); // 인증코드 발송 버튼
    const codeBlock = document.getElementById("codeBlock"); // 인증코드 입력 블록
    const emailCode = document.getElementById("emailCode"); // 인증코드 입력칸
    const notice = document.getElementById("ajaxNotice"); // 상태 표시

    const csrfHeader = document.querySelector(
        'meta[name="_csrf_header"]'
    )?.content;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

    const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

    function setNotice(msg, ok) {
        if (!notice) {
            alert(msg);
            return;
        }
        notice.textContent = msg;
        notice.className = `note-banner${ok ? "" : " error"}`;
        notice.hidden = false;
    }

    // 이메일 동기화
    function syncEmail() {
        const local = (localEl?.value || "").trim();
        const domain =
            selEl?.value === "__custom__"
                ? (custEl?.value || "").trim()
                : selEl?.value || "";
        const val = local && domain ? `${local}@${domain}` : "";
        hidden.value = val;

        // 버튼 활성화
        btnSend.disabled = !isEmail(val);

        // 이메일 변경 시 코드 입력 영역 초기화
        hideCodeBlock();
    }

    // 직접 입력 토글
    selEl?.addEventListener("change", () => {
        const custom = selEl.value === "__custom__";
        combo?.classList.toggle("is-custom", custom);
        // 보조: display 토글(프로젝트 CSS에 맞춰 hidden 클래스를 쓰고 있다면 그대로 두세요)
        if (custEl) custEl.classList.toggle("hidden", !custom);
        if (custom) custEl?.focus();
        syncEmail();
    });

    // 로컬/커스텀 입력 변화
    [localEl, custEl].forEach((el) => el?.addEventListener("input", syncEmail));

    // 'aaa@gmail.com' 전체 붙여넣기 보정
    localEl?.addEventListener("input", () => {
        const raw = localEl.value;
        if (raw.includes("@")) {
            const [id, ...rest] = raw.split("@");
            localEl.value = id;
            if (selEl) selEl.value = "__custom__";
            combo?.classList.add("is-custom");
            if (custEl) {
                custEl.classList.remove("hidden");
                custEl.value = rest.join("@");
            }
        }
        syncEmail();
    });

    // 인증코드 영역 / 타이머
    let cooldownTimer = null;
    function showCodeBlock() {
        if (codeBlock) codeBlock.hidden = false;
        emailCode?.focus({ preventScroll: true });
    }
    function hideCodeBlock() {
        if (codeBlock) codeBlock.hidden = true;
        if (emailCode) emailCode.value = "";
        stopCooldown();
    }
    function startCooldown(seconds = 60) {
        let left = seconds;
        btnSend.disabled = true;
        const original = "인증코드 발송";
        btnSend.textContent = `재발송(${left}s)`;
        cooldownTimer = setInterval(() => {
            left--;
            btnSend.textContent = `재발송(${left}s)`;
            if (left <= 0) {
                clearInterval(cooldownTimer);
                cooldownTimer = null;
                btnSend.textContent = original;
                btnSend.disabled = !isEmail(hidden.value);
            }
        }, 1000);
    }
    function stopCooldown() {
        if (cooldownTimer) {
            clearInterval(cooldownTimer);
            cooldownTimer = null;
        }
        btnSend.textContent = "인증하기";
    }

    // 인증코드 발송 (AJAX)
    btnSend?.addEventListener("click", async () => {
        const email = hidden.value;
        if (!isEmail(email)) return;

        btnSend.disabled = true;
        const body = new URLSearchParams({ email });
        try {
            const res = await fetch("/api/send-code", {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type":
                        "application/x-www-form-urlencoded; charset=UTF-8",
                    ...(csrfHeader && csrfToken
                        ? { [csrfHeader]: csrfToken }
                        : {}),
                },
                body,
            });

            const data = await res.json().catch(() => ({}));
            if (res.ok && (data.ok ?? true)) {
                setNotice(
                    data.message || "인증 코드가 발송되었습니다. (5분 유효)",
                    true
                );
                showCodeBlock();
                startCooldown(60);
            } else {
                setNotice(
                    data.message ||
                        `메일 발송에 실패했습니다. (status ${res.status})`,
                    false
                );
                btnSend.disabled = !isEmail(hidden.value);
            }
        } catch (e) {
            setNotice("네트워크 오류로 발송 실패", false);
            btnSend.disabled = !isEmail(hidden.value);
        }
    });

    // 초기값 세팅
    if (selEl && !selEl.value) selEl.value = "gmail.com";
    syncEmail();
})();
