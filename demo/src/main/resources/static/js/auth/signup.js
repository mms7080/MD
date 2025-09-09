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

      btn?.addEventListener("click", function () {
        const email = emailInput.value.trim();
        if (!email) { alert("이메일을 입력하세요."); emailInput.focus(); return; }

        // 숨김 폼을 만들어 POST (CSRF 포함)
        const form = document.createElement("form");
        form.method = "POST";
        form.action = "/send-code";

        const emailField = document.createElement("input");
        emailField.type = "hidden";
        emailField.name = "email";
        emailField.value = email;
        form.appendChild(emailField);

        // CSRF
        const csrfField = document.createElement("input");
        csrfField.type = "hidden";
        csrfField.name = csrfInput.getAttribute("name");
        csrfField.value = csrfInput.value;
        form.appendChild(csrfField);

        document.body.appendChild(form);
        form.submit();
      });
    })();