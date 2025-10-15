const state = {
    meta: { year: new Date().getFullYear() },
    intro: {
        title: "김개발 / Backend Developer",
        name: "Kim Dev",
        birth: "1996-07-15",
        city: "Seoul, Korea",
        summary:
            "서비스의 안정성과 확장성을 최우선으로 생각하는 백엔드 엔지니어 [이름] 입니다. 트래픽 급증 상황에서도 일관된 성능을 내는 아키텍처를 고민하고, 지표 기반으로 병목을 찾아 개선합니다. 주요 스택은 Java/Spring, JPA, PostgreSQL, Redis, Kafka이며, CI/CD 자동화와 모니터링 환경 구축을 즐깁니다. 협업에서는 코드 가독성과 문서화를 통해 팀의 학습 비용을 줄이는 데 집중합니다.",
        email: "me@example.com",
        phone: "010-1234-5678",
        site: "github.com/me",
        photo: "", // dataURL
    },
    edu: {
        univ1: {
            name: "KAFLAB University",
            major: "컴퓨터공학과 (B.S.)",
            period: "2015 – 2019",
        },
        univ2: {
            name: "KAFLAB Graduate",
            major: "데이터공학 (M.S.)",
            period: "2019 – 2021",
        },
    },
    exp: {
        job1: {
            company: "Acme Corp",
            period: "2021.03 – 2023.12 · Backend Engineer",
            desc1: "Spring 기반 주문 시스템 마이크로서비스 전환",
            desc2: "MongoDB → PostgreSQL 마이그레이션 주도",
            desc3: "CI/CD 최적화로 배포시간 40% 단축",
        },
        job2: {
            company: "Beta Studio",
            period: "2024.01 – 현재 · Senior Backend",
            desc1: "대규모 이벤트 트래픽 대응 아키텍처 설계",
            desc2: "Kafka 스트림 기반 통계 파이프라인 구축",
            desc3: "성능 튜닝으로 P95 응답 320ms → 140ms",
        },
    },
    skills: { spring: 90, db: 80, devops: 70 },
    strengths: {
        1: "확장성과 유지보수를 고려한 모듈 설계",
        2: "문제 재현/측정/개선의 성과 지향 워크플로",
        3: "명확한 문서화와 팀 커뮤니케이션",
    },
    proj1: {
        title: "실시간 주문 처리 시스템",
        meta: "2024 · Backend Lead · Spring / Kafka / Redis",
        desc: "대규모 트래픽에 대응하는 이벤트 기반 아키텍처로 재설계.",
        link: "github.com/acme/order",
        thumb: "",
    },
    proj2: {
        title: "추천 모델 서빙 플랫폼",
        meta: "2023 · Backend · Spring / gRPC / Kubernetes",
        desc: "모델 버전 관리와 롤백이 쉬운 서빙 파이프라인 구축.",
        link: "github.com/acme/reco",
        thumb: "",
    },
    awards: {
        1: "2024 NAVI Hackathon – 최우수상",
        2: "2023 회사 기술 공모 – 최우수",
    },
    cert: {
        1: "AWS Certified Solutions Architect – Associate",
        2: "SQLD / 정보처리기사",
    },
};

/* ---------- 데이터 바인딩 ---------- */
function applyBindings() {
    document.querySelectorAll("[data-bind]").forEach((el) => {
        const path = el.getAttribute("data-bind");
        el.textContent = get(path) ?? "";
    });
    // meter style
    document.querySelectorAll("[data-bind-style]").forEach((el) => {
        const path = el.getAttribute("data-bind-style");
        const v = get(path);
        if (v != null) el.style.width = v + "%";
    });
    // images
    if (state.intro.photo)
        document.getElementById("photo1").src = state.intro.photo;
    if (state.proj1.thumb)
        document.getElementById("thumb1").src = state.proj1.thumb;
    if (state.proj2.thumb)
        document.getElementById("thumb2").src = state.proj2.thumb;
    syncFormInputs();
}
function syncFormInputs() {
    if (!pane) return;
    pane.querySelectorAll("[data-model]").forEach((inp) => {
        const path = inp.getAttribute("data-model");
        const val = get(path);
        if (inp.type === "number") {
            if (Number(inp.value) !== Number(val)) inp.value = Number(val ?? 0);
        } else {
            if (inp.value !== (val ?? "")) inp.value = val ?? "";
        }
    });
}

function get(path) {
    return path.split(".").reduce((o, k) => o && o[k], state);
}
function set(path, value) {
    const keys = path.split(".");
    const last = keys.pop();
    const obj = keys.reduce((o, k) => o[k] ?? (o[k] = {}), state);
    obj[last] = value;
    applyBindings();
}

/* ---------- 페이지 전환 ---------- */
let page = 1;
const slides = [...document.querySelectorAll(".slide")];
function go(n) {
    page = Math.min(5, Math.max(1, n));
    slides.forEach((s) => (s.hidden = s.dataset.page != page));
    document.getElementById("pageChip").textContent = page + " / 5";
    renderForm(page);
}
document.querySelectorAll(".top [data-goto]").forEach((btn) => {
    btn.addEventListener("click", () => go(+btn.dataset.goto));
});

/* ---------- 사이드 폼 ---------- */
const pane = document.getElementById("formPane");

const forms = {
    1: () => `
        <div class="panel"><h3>소개</h3>
          <div class="row"><label>이름/타이틀</label><input class="inpt" data-model="intro.title" value="${state.intro.title}"></div>
          <div class="two">
            <div class="row"><label>이름(footer)</label><input class="inpt" data-model="intro.name" value="${state.intro.name}"></div>
            <div class="row"><label>생년월일</label><input class="inpt" data-model="intro.birth" value="${state.intro.birth}"></div>
          </div>
          <div class="row"><label>거주지</label><input class="inpt" data-model="intro.city" value="${state.intro.city}"></div>
          <div class="row"><label>자기소개</label><textarea rows="4" data-model="intro.summary">${state.intro.summary}</textarea></div>
        </div>
        <div class="panel"><h3>연락처</h3>
          <div class="two">
            <div class="row"><label>이메일</label><input class="inpt" data-model="intro.email" value="${state.intro.email}"></div>
            <div class="row"><label>전화번호</label><input class="inpt" data-model="intro.phone" value="${state.intro.phone}"></div>
          </div>
          <div class="row"><label>사이트</label><input class="inpt" data-model="intro.site" value="${state.intro.site}"></div>
          <div class="row"><label>프로필 사진</label>
            <label class="inbtn">업로드<input type="file" hidden accept="image/*" data-upload="intro.photo"></label>
          </div>
        </div>
      `,
    2: () => `
        <div class="panel"><h3>학력</h3>
          <div class="row"><label>학교 1</label><input class="inpt" data-model="edu.univ1.name" value="${
              state.edu.univ1.name
          }"></div>
          <div class="two">
            <div class="row"><label>전공</label><input class="inpt" data-model="edu.univ1.major" value="${
                state.edu.univ1.major
            }"></div>
            <div class="row"><label>기간</label><input class="inpt" data-model="edu.univ1.period" value="${
                state.edu.univ1.period
            }"></div>
          </div>
          <hr class="small" style="border:none;border-top:1px solid var(--border)">
          <div class="row"><label>학교 2</label><input class="inpt" data-model="edu.univ2.name" value="${
              state.edu.univ2.name
          }"></div>
          <div class="two">
            <div class="row"><label>전공</label><input class="inpt" data-model="edu.univ2.major" value="${
                state.edu.univ2.major
            }"></div>
            <div class="row"><label>기간</label><input class="inpt" data-model="edu.univ2.period" value="${
                state.edu.univ2.period
            }"></div>
          </div>
        </div>
        <div class="panel"><h3>경력</h3>
          ${[1, 2]
              .map(
                  (i) => `
            <div class="row"><label>회사 ${i}</label><input class="inpt" data-model="exp.job${i}.company" value="${
                      state.exp["job" + i].company
                  }"></div>
            <div class="row"><label>기간/직무</label><input class="inpt" data-model="exp.job${i}.period" value="${
                      state.exp["job" + i].period
                  }"></div>
            <div class="row"><label>성과1</label><input class="inpt" data-model="exp.job${i}.desc1" value="${
                      state.exp["job" + i].desc1
                  }"></div>
            <div class="row"><label>성과2</label><input class="inpt" data-model="exp.job${i}.desc2" value="${
                      state.exp["job" + i].desc2
                  }"></div>
            <div class="row"><label>성과3</label><input class="inpt" data-model="exp.job${i}.desc3" value="${
                      state.exp["job" + i].desc3
                  }"></div>
            ${
                i === 1
                    ? '<hr class="small" style="border:none;border-top:1px solid var(--border)">'
                    : ""
            }
          `
              )
              .join("")}
        </div>
      `,
    3: () => `
        <div class="panel"><h3>기술 숙련도(%)</h3>
          <div class="row"><label>Java/Spring</label><input type="number" min="0" max="100" class="inpt" data-model="skills.spring" value="${
              state.skills.spring
          }"></div>
          <div class="row"><label>DB/SQL</label><input type="number" min="0" max="100" class="inpt" data-model="skills.db" value="${
              state.skills.db
          }"></div>
          <div class="row"><label>Cloud/DevOps</label><input type="number" min="0" max="100" class="inpt" data-model="skills.devops" value="${
              state.skills.devops
          }"></div>
        </div>
        <div class="panel"><h3>보유역량</h3>
          ${[1, 2, 3]
              .map(
                  (i) => `
            <div class="row"><label>역량 ${i}</label><input class="inpt" data-model="strengths.${i}" value="${state.strengths[i]}"></div>
          `
              )
              .join("")}
        </div>
      `,
    4: () => `
        <div class="panel"><h3>프로젝트 1</h3>
          <div class="row"><label>제목</label><input class="inpt" data-model="proj1.title" value="${state.proj1.title}"></div>
          <div class="row"><label>메타</label><input class="inpt" data-model="proj1.meta" value="${state.proj1.meta}"></div>
          <div class="row"><label>설명</label><textarea rows="3" data-model="proj1.desc">${state.proj1.desc}</textarea></div>
          <div class="row"><label>링크</label><input class="inpt" data-model="proj1.link" value="${state.proj1.link}"></div>
          <div class="row"><label>썸네일</label><label class="inbtn">업로드<input type="file" hidden accept="image/*" data-upload="proj1.thumb"></label></div>
        </div>
        <div class="panel"><h3>프로젝트 2</h3>
          <div class="row"><label>제목</label><input class="inpt" data-model="proj2.title" value="${state.proj2.title}"></div>
          <div class="row"><label>메타</label><input class="inpt" data-model="proj2.meta" value="${state.proj2.meta}"></div>
          <div class="row"><label>설명</label><textarea rows="3" data-model="proj2.desc">${state.proj2.desc}</textarea></div>
          <div class="row"><label>링크</label><input class="inpt" data-model="proj2.link" value="${state.proj2.link}"></div>
          <div class="row"><label>썸네일</label><label class="inbtn">업로드<input type="file" hidden accept="image/*" data-upload="proj2.thumb"></label></div>
        </div>
      `,
    5: () => `
        <div class="panel"><h3>수상</h3>
          ${[1, 2]
              .map(
                  (i) =>
                      `<div class="row"><label>Award ${i}</label><input class="inpt" data-model="awards.${i}" value="${state.awards[i]}"></div>`
              )
              .join("")}
        </div>
        <div class="panel"><h3>자격</h3>
          ${[1, 2]
              .map(
                  (i) =>
                      `<div class="row"><label>Cert ${i}</label><input class="inpt" data-model="cert.${i}" value="${state.cert[i]}"></div>`
              )
              .join("")}
        </div>
        <div class="panel"><h3>푸터</h3>
          <div class="row"><label>표기 연도</label><input class="inpt" data-model="meta.year" value="${
              state.meta.year
          }"></div>
        </div>
      `,
};

function renderForm(n) {
    pane.innerHTML = forms[n]();
    // 입력 바인딩
    pane.querySelectorAll("[data-model]").forEach((inpt) => {
        inpt.addEventListener("input", (e) => {
            const path = inpt.getAttribute("data-model");
            let val =
                inpt.type === "number" ? Number(inpt.value || 0) : inpt.value;
            set(path, val);
        });
    });
    // 업로드 바인딩
    pane.querySelectorAll("[data-upload]").forEach((fi) => {
        fi.addEventListener("change", async (e) => {
            const file = fi.files?.[0];
            if (!file) return;
            const dataURL = await fileToDataURL(file);
            set(fi.getAttribute("data-upload"), dataURL);
            fi.value = "";
        });
    });
}

function fileToDataURL(file) {
    return new Promise((res) => {
        const fr = new FileReader();
        fr.onload = () => res(fr.result);
        fr.readAsDataURL(file);
    });
}

/* ---------- 저장/불러오기/출력 ---------- */
document.getElementById("btnSave").onclick = () => {
    localStorage.setItem("folio-dev-basic", JSON.stringify(state));
    flash("저장됨");
};
document.getElementById("btnLoad").onclick = () => {
    const raw = localStorage.getItem("folio-dev-basic");
    if (!raw) return flash("저장된 내용이 없습니다");
    const data = JSON.parse(raw);
    Object.assign(state, data);
    applyBindings();
    renderForm(page);
    flash("불러옴");
};
document.getElementById("btnPrint").onclick = () => window.print();

function flash(msg) {
    const n = document.createElement("div");
    n.textContent = msg;
    n.style.cssText =
        "position:fixed;top:12px;left:50%;transform:translateX(-50%);background:#1a2430;border:1px solid #2a3646;color:#dfe8f6;padding:8px 12px;border-radius:10px;z-index:9999";
    document.body.appendChild(n);
    setTimeout(() => n.remove(), 1200);
}

/* ---------- init ---------- */
applyBindings();
go(1);
