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



(function() {
  const combo   = document.getElementById("emailCombo");
  const localEl = document.getElementById("emailLocal");
  const selEl   = document.getElementById("emailDomain");
  const custEl  = document.getElementById("emailDomainCustom");
  const hidden  = document.getElementById("email");
  const btn     = document.getElementById("btnCheckEmail"); 
  const notice  = document.getElementById("ajaxNotice");
  if (!combo || !localEl || !selEl || !hidden || !btn) return;

  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;

  const isEmail = v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
  const setNotice = (msg, ok) => {
    if (!notice) return alert(msg);
    notice.textContent = msg;
    notice.className = `note-banner${ok ? "" : " error"}`;
    notice.hidden = false;
  };
  const clearNotice = () => {
    if (!notice) return;
    notice.hidden = true;
    notice.textContent = "";
    notice.className = "note-banner";
  };

  const composeEmail = () => {
    const id = (localEl.value || "").trim();
    const domain = selEl.value === "__custom__" ? (custEl?.value || "").trim() : (selEl.value || "");
    return id && domain ? `${id}@${domain}` : "";
  };

  const syncEmail = () => {
    const v = composeEmail();
    hidden.value = v;
    btn.disabled = !isEmail(v);
    hidden.setCustomValidity("");
    clearNotice();
  };

  // 도메인 select ↔ 직접입력
  selEl.addEventListener("change", () => {
    const custom = selEl.value === "__custom__";
    combo.classList.toggle("is-custom", custom);
    if (custEl) {
      custEl.classList.toggle("hidden", !custom);
      if (custom) custEl.focus();
    }
    syncEmail();
  });

  [localEl, custEl].forEach(el => el?.addEventListener("input", syncEmail));

  // 'id@domain.com' 통째로 입력 시 자동 분리
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

  // 중복 확인
  const checkEmailDup = async () => {
    const email = composeEmail();
    hidden.value = email;

    if (!isEmail(email)) {
      setNotice("올바른 이메일을 입력하세요.", false);
      validityTarget.setCustomValidity?.("올바른 이메일을 입력하세요.");
      hidden.reportValidity?.();
      return;
    }

    btn.disabled = true;
    try {
      const res = await fetch(`/api/check-email?email=${encodeURIComponent(email)}`, {
        method: "GET",
        headers: {
          Accept: "application/json",
          ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
        },
      });
      const exists = await res.json(); // true: 중복, false: 사용 가능
      if (exists) {
        setNotice("이미 사용 중인 이메일입니다.", false);
        hidden.setCustomValidity("이미 사용 중인 이메일입니다.");
      } else {
        setNotice("사용 가능한 이메일입니다.", true);
        hidden.setCustomValidity("");
      }
    } catch {
      setNotice("중복 확인 중 오류가 발생했습니다.", false);
      hidden.setCustomValidity("이메일 중복 확인 실패");
    } finally {
      btn.disabled = !isEmail(hidden.value);
    }
  };

  btn.addEventListener("click", checkEmailDup);

  // 초기값
  if (!selEl.value) selEl.value = "gmail.com";
  syncEmail();
})();

/* ===== 모달 닫힘/열림 시 회원가입 폼 초기화 ===== */
document.addEventListener("DOMContentLoaded", () => {
  const rNone   = document.getElementById("modal-none");
  const rSignin = document.getElementById("modal-signin");
  const rSignup = document.getElementById("modal-signup");

  const signupForm = document.querySelector('.kf-auth-modal.signup form.auth-form');
  if (!signupForm || !rNone || !rSignin || !rSignup) return;

  // 초기화 함수
  function resetSignupForm() {
    // 1) 기본 form reset
    signupForm.reset();

    // 2) 수동으로 남는 값/배너/클래스 정리
    //   - 아이디 중복 배너
    const userStatus = document.getElementById("usernameStatus");
    if (userStatus) {
      userStatus.textContent = "";
      userStatus.hidden = true;
      userStatus.className = "note-banner inline";
    }

    //   - 비밀번호 상태 배너
    const pwStatus = document.getElementById("password-status");
    if (pwStatus) {
      pwStatus.textContent = "";
      pwStatus.style.display = "none";
      pwStatus.style.opacity = "0";
      pwStatus.classList.remove("error");
    }

    //   - 이메일 컴포즈/중복배너/버튼 상태
    const combo   = document.getElementById("emailCombo");
    const localEl = document.getElementById("emailLocal");
    const selEl   = document.getElementById("emailDomain");
    const custEl  = document.getElementById("emailDomainCustom");
    const hidden  = document.getElementById("email");
    const btnMail = document.getElementById("btnCheckEmail");
    const ajaxNt  = document.getElementById("ajaxNotice");

    if (ajaxNt) {
      ajaxNt.textContent = "";
      ajaxNt.hidden = true;
      ajaxNt.className = "note-banner";
    }
    if (hidden) hidden.value = "";

    // 도메인은 기본값으로
    if (selEl) selEl.value = "gmail.com";
    // 직접 입력 필드/클래스 숨김
    if (combo) combo.classList.remove("is-custom");
    if (custEl) {
      custEl.value = "";
      custEl.classList.add("hidden");
    }
    // 로컬 아이디도 비움
    if (localEl) localEl.value = "";

    // 이메일 버튼 비활성화
    if (btnMail) btnMail.disabled = true;

    //   - 아이디 중복확인 버튼 비활성화 (입력 없으므로)
    const btnId = document.getElementById("btnCheckId");
    if (btnId) btnId.disabled = false; // 확인 버튼 자체는 클릭 가능하게 두고,
    // 필요하면 true로 막고 입력 시 활성화하도록 바꿔도 됨

    //   - 커스텀 유효성 메시지 제거
    ["username","password","confirmPassword","name","birth","ageHidden"].forEach(id=>{
      const el = document.getElementById(id);
      try { el?.setCustomValidity?.(""); } catch (e) {}
    });

    // 3) 자동완성 흔적 제거(브라우저가 고집 세면 수동 초기화)
    signupForm.querySelectorAll('input[type="text"], input[type="password"], input[type="date"]').forEach(el=>{
      if (el.id !== "emailDomain") el.value = "";
    });

    // 4) 필요하면 포커스(선택)
    const username = document.getElementById("username");
    username?.focus();
  }

  // “닫힘(=modal-none)으로 전환될 때” 초기화
  rNone.addEventListener("change", () => {
    if (rNone.checked) resetSignupForm();
  });

  // “회원가입 모달을 열 때”도 항상 초기화(원하면 주석 처리)
  rSignup.addEventListener("change", () => {
    if (rSignup.checked) resetSignupForm();
  });
});


