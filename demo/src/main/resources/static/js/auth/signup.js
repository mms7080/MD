    // 아이디 중복 확인 버튼
    (function () {
      const btn = document.getElementById("btnCheckId");
      const input = document.getElementById("username");
      const status = document.getElementById("usernameStatus");

      function show(msg, ok) {
        status.textContent = msg;
        status.className = `auth-alert ${ok ? "is-success" : "is-error"}`;
        status.style.display = "block";
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
          const res = await fetch(`/api/check-username?username=${encodeURIComponent(username)}`);
          const exists = await res.json(); // true = 이미 사용중
          if (exists) {
            show("이미 사용 중인 아이디입니다.", false);
          } else {
            show("사용 가능한 아이디입니다.", true);
          }
        } catch {
          show("중복 확인 중 오류가 발생했습니다.", false);
        } finally {
          btn.disabled = false;
        }
      });
    })();
    
    
    // 성별 체크박스: 상호 배타(라디오처럼)
    (function () {
      const m = document.getElementById("genderM");
      const w = document.getElementById("genderW");
      function exclusive(e) {
        if (e.target === m && m.checked) w.checked = false;
        if (e.target === w && w.checked) m.checked = false;
      }
      m.addEventListener("change", exclusive);
      w.addEventListener("change", exclusive);
    })();

    // 비밀번호/확인 일치 검사 + 제출 차단
    (function () {
      const pw = document.getElementById("password");
      const cf = document.getElementById("confirmPassword");
      const mismatch = document.getElementById("pwMismatch");
      const ok = document.getElementById("pwOk");
      const submitBtn = document.getElementById("submitBtn");

      function validatePw() {
        const a = pw.value;
        const b = cf.value;

        if (!a || !b) {
          mismatch.style.display = "none";
          ok.style.display = "none";
          submitBtn.disabled = false;
          return;
        }
        if (a !== b) {
          mismatch.style.display = "block";
          ok.style.display = "none";
          submitBtn.disabled = true;
        } else {
          mismatch.style.display = "none";
          ok.style.display = "block";
          submitBtn.disabled = false;
        }
      }

      pw.addEventListener("input", validatePw);
      cf.addEventListener("input", validatePw);
    })();

    // 나이: 0 미만 방지
    (function () {
      const age = document.getElementById("age");
      age.addEventListener("input", () => {
        const n = Number(age.value);
        if (Number.isNaN(n)) return;
        if (n < 0) age.value = 0;
      });
    })();

    // 이메일 인증코드 발송(숨김 폼 방식, 보이는 input은 이메일 하나만)
    (function () {
    const btn = document.getElementById("btnSendCode");
    const emailInput = document.getElementById("email");
    const csrfInput = document.getElementById("csrfTokenField");
    const notice = document.getElementById("ajaxNotice");

    // 버튼이 submit로 되어 있지 않도록 (안전)
    if (btn && !btn.getAttribute("type")) btn.setAttribute("type", "button");

    function show(msg, ok) {
      if (!notice) return;
      notice.textContent = msg;
      notice.className = `auth-alert ${ok ? "is-success" : "is-error"}`;
      notice.style.display = "block";
    }

    btn?.addEventListener("click", async (e) => {
      e.preventDefault(); // 혹시 모를 기본동작 차단
      const email = (emailInput?.value || "").trim();
      if (!email) { alert("이메일을 입력하세요."); emailInput?.focus(); return; }

      btn.disabled = true;
      try {
        const res = await fetch("/api/send-code", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "X-CSRF-TOKEN": csrfInput?.value || "",
            "Accept": "application/json"
          },
          body: new URLSearchParams({ email }).toString(),
          credentials: "same-origin"
        });
        const ok = await res.json().catch(() => false);
        show(ok ? "인증 코드가 발송되었습니다." : "메일 발송에 실패했습니다. 잠시 후 다시 시도하세요.", ok);
      } catch {
        show("네트워크 오류로 발송 실패", false);
      } finally {
        btn.disabled = false;
      }
    });
  })();