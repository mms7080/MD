(function () {
    const combo = document.getElementById("emailCombo");
    const localEl = document.getElementById("emailLocal");
    const selEl = document.getElementById("emailDomain");
    const custEl = document.getElementById("emailDomainCustom");
    const hidden = document.getElementById("email"); // 실제 제출용
    const btnSend = document.getElementById("btnSendCode");

    // 이메일 유효성 체크
    const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

    // local + domain → hidden email 동기화
    function syncEmail() {
        const local = (localEl.value || "").trim();
        const domain =
            selEl.value === "__custom__"
                ? (custEl.value || "").trim()
                : selEl.value;

        const val = local && domain ? `${local}@${domain}` : "";
        hidden.value = val;

        // 인증코드 버튼 활성화
        btnSend.disabled = !isEmail(val);
    }

    // “직접 입력” 선택시 토글
    selEl.addEventListener("change", () => {
        const custom = selEl.value === "__custom__";
        combo.classList.toggle("is-custom", custom);
        if (custom) custEl.focus();
        syncEmail();
    });

    // 입력 이벤트들
    [localEl, custEl].forEach((el) => el.addEventListener("input", syncEmail));

    // “aaa@gmail.com” 전체 붙여넣기 케어 (자동 분해)
    localEl.addEventListener("input", () => {
        const raw = localEl.value;
        if (raw.includes("@")) {
            const [id, ...rest] = raw.split("@");
            localEl.value = id;
            selEl.value = "__custom__";
            combo.classList.add("is-custom");
            custEl.value = rest.join("@");
        }
        syncEmail();
    });

    // 초기값
    selEl.value = selEl.value || "gmail.com";
    syncEmail();
})();

(function () {
    const birth = document.getElementById("birth");
    const ageHidden = document.getElementById("ageHidden");

    if (!birth || !ageHidden) return;

    // 오늘까지 허용(미래 날짜 금지), 최솟값은 취향껏
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    birth.max = `${yyyy}-${mm}-${dd}`;
    birth.min = `1900-01-01`;

    // 날짜 지원 안 하는 브라우저를 위한 하이픈 자동 삽입(직접 입력 보강)
    const isDateSupported = birth.type === "date";
    if (!isDateSupported) {
        birth.placeholder = "YYYY-MM-DD";
        birth.addEventListener("input", (e) => {
            let v = e.target.value.replace(/[^\d]/g, "").slice(0, 8);
            if (v.length >= 5)
                v = `${v.slice(0, 4)}-${v.slice(4, 6)}${
                    v.length > 6 ? "-" + v.slice(6) : ""
                }`;
            else if (v.length >= 4) v = `${v.slice(0, 4)}-${v.slice(4)}`;
            e.target.value = v;
            syncAge();
        });
    }

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

        // 만 나이 계산
        const now = new Date();
        let age = now.getFullYear() - b.getFullYear();
        const hasntHadBirthday =
            now.getMonth() < b.getMonth() ||
            (now.getMonth() === b.getMonth() && now.getDate() < b.getDate());
        if (hasntHadBirthday) age--;

        // 음수/비정상 guard
        ageHidden.value = age >= 0 && age < 200 ? String(age) : "";
    }

    birth.addEventListener("change", syncAge);
    birth.addEventListener("input", syncAge);

    // 초기 동기화(서버가 age를 채워줬다면 그대로 유지)
    if (!ageHidden.value) syncAge();
})();

(function () {
    const combo = document.getElementById("emailCombo");
    const localEl = document.getElementById("emailLocal");
    const selEl = document.getElementById("emailDomain");
    const custEl = document.getElementById("emailDomainCustom");
    const hiddenMail = document.getElementById("email"); // 최종 이메일(hidden)
    const btnSend = document.getElementById("btnSendCode"); // 인증코드 발송 버튼
    const codeBlock = document.getElementById("codeBlock"); // 인증코드 블록
    const emailCode = document.getElementById("emailCode"); // 인증코드 입력칸

    const csrfHeader = document.querySelector(
        'meta[name="_csrf_header"]'
    )?.content;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

    const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

    // 이메일 동기화(기존 콤보 로직과 동일)
    function syncEmail() {
        const local = (localEl?.value || "").trim();
        const domain =
            selEl?.value === "__custom__"
                ? (custEl?.value || "").trim()
                : selEl?.value || "";

        const val = local && domain ? `${local}@${domain}` : "";
        hiddenMail.value = val;

        // 발송 버튼 활성화/비활성
        btnSend.disabled = !isEmail(val);

        // 이메일 바뀌면 코드 블록/타이머 초기화
        hideCodeBlock();
    }

    // 도메인 선택
    selEl?.addEventListener("change", () => {
        const custom = selEl.value === "__custom__";
        combo?.classList.toggle("is-custom", custom);
        if (custom) custEl?.focus();
        syncEmail();
    });

    // 입력 변화
    [localEl, custEl].forEach((el) => el?.addEventListener("input", syncEmail));

    // 붙여넣기 보정(aaa@gmail.com 형식)
    localEl?.addEventListener("input", () => {
        const raw = localEl.value;
        if (raw.includes("@")) {
            const [id, ...rest] = raw.split("@");
            localEl.value = id;
            selEl.value = "__custom__";
            combo?.classList.add("is-custom");
            if (custEl) custEl.value = rest.join("@");
        }
        syncEmail();
    });

    // ===== 인증코드 발송/표시/타이머 =====
    let cooldownTimer = null;
    function startCooldown(seconds = 60) {
        let left = seconds;
        btnSend.disabled = true;
        btnSend.textContent = `재발송(${left}s)`;
        cooldownTimer = setInterval(() => {
            left--;
            btnSend.textContent = `재발송(${left}s)`;
            if (left <= 0) {
                clearInterval(cooldownTimer);
                cooldownTimer = null;
                btnSend.disabled = !isEmail(hiddenMail.value);
                btnSend.textContent = "인증코드 발송";
            }
        }, 1000);
    }
    function stopCooldown() {
        if (cooldownTimer) {
            clearInterval(cooldownTimer);
            cooldownTimer = null;
        }
        btnSend.textContent = "인증코드 발송";
    }
    function showCodeBlock() {
        codeBlock.hidden = false;
        emailCode?.focus({ preventScroll: true });
    }
    function hideCodeBlock() {
        codeBlock.hidden = true;
        if (emailCode) emailCode.value = "";
        stopCooldown();
    }

    btnSend?.addEventListener("click", async () => {
        if (!isEmail(hiddenMail.value)) return;

        // --- 실제 발송 요청(백엔드 엔드포인트에 맞게 수정) ---
        try {
            // 예시: /auth/email-code 로 POST
            // await fetch('/auth/email-code', {
            //   method: 'POST',
            //   headers: {
            //     'Content-Type': 'application/json',
            //     ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            //   },
            //   body: JSON.stringify({ email: hiddenMail.value })
            // });

            // 발송 성공 시 UI 표시
            showCodeBlock();
            startCooldown(60); // 60초 쿨다운
        } catch (e) {
            console.error(e);
            // 에러 메시지 표시를 원하면 여기에서 처리
        }
    });

    // 초기화
    if (selEl && !selEl.value) selEl.value = "gmail.com";
    syncEmail();
})();
