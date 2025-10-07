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

document.addEventListener("DOMContentLoaded", () => {
  const pw = document.getElementById("password");
  const pwCheck = document.getElementById("confirmPassword");
  const pwStatus = document.getElementById("password-status");

  if (!pw || !pwCheck || !pwStatus) return;

  function showMessage(msg, isError = false) {
  pwStatus.textContent = msg;
  pwStatus.classList.toggle("error", isError);
  pwStatus.style.display = "inline-block"; // 표시
  requestAnimationFrame(() => {
    pwStatus.style.opacity = "1";
  });
}

function hideMessage() {
  pwStatus.style.opacity = "0";
  setTimeout(() => {
    pwStatus.style.display = "none"; // 완전 숨김
    pwStatus.textContent = "";
    pwStatus.classList.remove("error");
  }, 200);
}

  // 일치 검사
  function checkPasswordMatch() {
    const a = pw.value.trim();
    const b = pwCheck.value.trim();

    if (!a && !b) return hideMessage();
    if (!a || !b) return showMessage("비밀번호가 일치하지 않습니다.", true);
    if (a === b) showMessage("비밀번호가 일치합니다.");
    else showMessage("비밀번호가 일치하지 않습니다.", true);
  }

  ["input", "change", "keyup", "paste"].forEach(evt => {
    pw.addEventListener(evt, checkPasswordMatch);
    pwCheck.addEventListener(evt, checkPasswordMatch);
  });
});



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
/* ========== 이메일 합성 + 코드 발송 성공 시 입력칸 표시 ========== */
(() => {
    // 회원가입 모달 안의 요소들
    const combo = document.getElementById("emailCombo");
    const localEl = document.getElementById("emailLocal");
    const selEl = document.getElementById("emailDomain");
    const custEl = document.getElementById("emailDomainCustom");
    const hidden = document.getElementById("email");
    const btnSend = document.getElementById("btnSendCode");
    const codeBlock = document.getElementById("codeBlock");
    const emailCode = document.getElementById("emailCode");
    const notice = document.getElementById("ajaxNotice");

    // 필수 요소 없으면 리턴
    if (
        !combo ||
        !localEl ||
        !selEl ||
        !hidden ||
        !btnSend ||
        !codeBlock ||
        !emailCode
    )
        return;

    // 기본은 숨김 (HTML에서 codeBlock에 hidden 넣어두세요)
    codeBlock.hidden = true;

    const csrfHeader = document.querySelector(
        'meta[name="_csrf_header"]'
    )?.content;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

    const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
    const setNotice = (msg, ok) => {
        if (!notice) {
            alert(msg);
            return;
        }
        notice.textContent = msg;
        notice.className = `note-banner${ok ? "" : " error"}`;
        notice.hidden = false;
    };

    // 이메일 합성/동기화
    const composeEmail = () => {
        const local = (localEl.value || "").trim();
        const domain =
            selEl.value === "__custom__"
                ? (custEl?.value || "").trim()
                : selEl.value || "";
        return local && domain ? `${local}@${domain}` : "";
    };
    const syncEmail = () => {
        const val = composeEmail();
        hidden.value = val;
        btnSend.disabled = !isEmail(val);
        // 이메일이 바뀌면 입력칸 초기화/숨김
        emailCode.value = "";
        codeBlock.hidden = true;
        codeBlock.classList.remove("is-open");
    };

    // 도메인 select ↔ custom input 토글
    selEl.addEventListener("change", () => {
        const custom = selEl.value === "__custom__";
        combo.classList.toggle("is-custom", custom);
        if (custEl) {
            custEl.classList.toggle("hidden", !custom);
            if (custom) custEl.focus();
        }
        syncEmail();
    });
    [localEl, custEl].forEach((el) => el?.addEventListener("input", syncEmail));

    // 'id@domain.com' 통째로 붙여넣기 보정
    localEl.addEventListener("input", () => {
        const raw = localEl.value;
        if (raw.includes("@")) {
            const [id, ...rest] = raw.split("@");
            localEl.value = id;
            selEl.value = "__custom__";
            combo.classList.add("is-custom");
            if (custEl) {
                custEl.classList.remove("hidden");
                custEl.value = rest.join("@");
            }
        }
        syncEmail();
    });

    // 쿨다운
    let cooldownTimer = null;
    const startCooldown = (seconds = 60) => {
        let left = seconds;
        const original = "인증코드 발송";
        btnSend.disabled = true;
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
    };
    const stopCooldown = () => {
        if (cooldownTimer) clearInterval(cooldownTimer);
        cooldownTimer = null;
        btnSend.textContent = "인증하기";
    };

    // 발송
    btnSend.addEventListener("click", async () => {
        const email = composeEmail();
        hidden.value = email;

        if (!isEmail(email)) {
            setNotice("올바른 이메일을 입력하세요.", false);
            return;
        }

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

            let data = null;
            try {
                data = await res.json();
            } catch {}
            const ok =
                typeof data === "boolean"
                    ? data
                    : data && typeof data.ok !== "undefined"
                    ? !!data.ok
                    : res.ok;

            if (ok) {
                setNotice(
                    (data && data.message) ||
                        "인증 코드가 발송되었습니다. (5분 유효)",
                    true
                );
                // ✅ 여기서만 보이게
                codeBlock.hidden = false;
                codeBlock.classList.add("is-open"); // CSS에 .code-block.is-open { display:grid } 있으면 더 안전
                emailCode.disabled = false;
                requestAnimationFrame(() =>
                    emailCode.focus({ preventScroll: true })
                );
                startCooldown(60);
            } else {
                setNotice(
                    (data && data.message) ||
                        `메일 발송 실패 (status ${res.status})`,
                    false
                );
                btnSend.disabled = !isEmail(hidden.value);
            }
        } catch {
            setNotice("네트워크 오류로 발송 실패", false);
            btnSend.disabled = !isEmail(hidden.value);
        }
    });

    // 초기화
    if (!selEl.value) selEl.value = "gmail.com";
    stopCooldown();
    syncEmail();
})();
