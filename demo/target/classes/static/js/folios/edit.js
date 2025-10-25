// /js/folios/edit.js
document.addEventListener("DOMContentLoaded", () => {
    // ---------- 유틸 ----------
    const qs = (s, r = document) => r.querySelector(s);
    const qsa = (s, r = document) => [...r.querySelectorAll(s)];
    const _hiddenMap = new Map();

    const toast = (msg) => {
        const n = document.createElement("div");
        n.textContent = msg;
        n.style.cssText =
            "position:fixed;top:12px;left:50%;transform:translateX(-50%);background:#1a2430;border:1px solid #2a3646;color:#dfe8f6;padding:8px 12px;border-radius:10px;z-index:9999";
        document.body.appendChild(n);
        setTimeout(() => n.remove(), 1200);
    };

    const getCsrf = () => {
        const token = qs('meta[name="_csrf"]')?.content;
        const header = qs('meta[name="_csrf_header"]')?.content;
        return token && header ? { header, token } : null;
    };
    const CSRF = getCsrf();
    const JSON_HEADERS = {
        "Content-Type": "application/json",
        ...(CSRF ? { [CSRF.header]: CSRF.token } : {}),
    };

    // 현재 작업 folio 식별자 (?id=...)
    const params = new URLSearchParams(location.search);
    let currentFolioId = params.get("id") || null;

    // 인증 가드
    const guardFetch = async (url, opts = {}) => {
        const res = await fetch(url, opts);
        if (res.status === 401 || res.status === 403) {
            location.href = "/home?modal=signin";
            throw new Error("인증 필요");
        }
        return res;
    };

    const fileToDataURL = (file) =>
        new Promise((res) => {
            const fr = new FileReader();
            fr.onload = () => res(fr.result);
            fr.readAsDataURL(file);
        });

    // data:URL → 서버 업로드 후 URL 받기
    async function normalizeImageUrl(urlOrData) {
        if (!urlOrData) return null;
        if (!urlOrData.startsWith("data:")) return urlOrData;
        try {
            const fd = new FormData();
            const blob = await (await fetch(urlOrData)).blob();
            fd.append("file", blob, "thumbnail.png");
            const headers = {};
            if (CSRF) headers[CSRF.header] = CSRF.token;
            const res = await guardFetch("/api/uploads/images", {
                method: "POST",
                headers,
                body: fd,
            });
            if (!res.ok) throw new Error("이미지 업로드 실패");
            const json = await res.json();
            return json.url || null;
        } catch {
            return null;
        }
    }

    // ---------- 기본 템플릿(원본) ----------
    const defaultState = {
        meta: { year: new Date().getFullYear() },
        intro: {
            title: "“사용자 성장에 맞춰 확장되는 백엔드를 설계합니다.”",
            name: "Kim Dev",
            birth: "1996-07-15",
            city: "Seoul, Korea",
            summary:
                "서비스의 안정성과 확장성을 최우선으로 생각하는 백엔드 엔지니어 [이름] 입니다...",
            email: "me@example.com",
            phone: "010-1234-5678",
            site: "github.com/me",
            photo: "",
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
            meta: "2024 · Backend Lead · Spring/Kafka/Redis",
            desc: "대규모 트래픽 대응 아키텍처로 재설계.",
            link: "github.com/acme/order",
            thumb: "",
        },
        proj2: {
            title: "추천 모델 서빙 플랫폼",
            meta: "2023 · Backend · Spring/gRPC/K8s",
            desc: "버전 관리·롤백 쉬운 서빙 파이프라인.",
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

    // 깊은 복사/병합
    const deepClone = (o) => JSON.parse(JSON.stringify(o || {}));
    const deepMerge = (base, patch) => {
        if (!patch || typeof patch !== "object") return deepClone(base);
        const out = deepClone(base);
        const walk = (t, s) => {
            Object.keys(s).forEach((k) => {
                if (s[k] && typeof s[k] === "object" && !Array.isArray(s[k])) {
                    t[k] = t[k] && typeof t[k] === "object" ? t[k] : {};
                    walk(t[k], s[k]);
                } else {
                    t[k] = s[k];
                }
            });
        };
        walk(out, patch);
        return out;
    };

    // 작업용 상태(항상 기본 템플릿의 clone에서 시작)
    let state = deepClone(defaultState);

    const get = (path) => path.split(".").reduce((o, k) => o && o[k], state);
    const set = (path, value) => {
        const keys = path.split(".");
        const last = keys.pop();
        const obj = keys.reduce((o, k) => (o[k] ??= {}), state);
        obj[last] = value;
        applyBindings();
    };

    // ---------- 바인딩 ----------
    function applyBindings() {
        qsa("[data-bind]").forEach(
            (el) => (el.textContent = get(el.getAttribute("data-bind")) ?? "")
        );
        qsa("[data-bind-style]").forEach((el) => {
            const v = get(el.getAttribute("data-bind-style"));
            if (v != null) el.style.width = v + "%";
        });
        if (state.intro?.photo)
            qs("#photo1")?.setAttribute("src", state.intro.photo);
        if (state.proj1?.thumb)
            qs("#thumb1")?.setAttribute("src", state.proj1.thumb);
        if (state.proj2?.thumb)
            qs("#thumb2")?.setAttribute("src", state.proj2.thumb);
        syncFormInputs();
    }

    function syncFormInputs() {
        const pane = qs("#formPane");
        if (!pane) return;
        qsa("[data-model]", pane).forEach((inp) => {
            const path = inp.getAttribute("data-model");
            const val = get(path);
            if (inp.type === "number") inp.value = Number(val ?? 0);
            else inp.value = val ?? "";
        });
    }

    // ---------- 페이지 전환 ----------
    let page = 1;
    const slides = qsa(".slide[data-page]");
    const LAST = slides.length;

    function showActiveTab() {
        qsa(".top [data-goto]").forEach((b) => {
            const active = +b.dataset.goto === page;
            b.classList.toggle("is-active", active);
            b.setAttribute("aria-current", active ? "page" : "false");
        });
    }

    function go(n) {
        page = ((n - 1 + LAST) % LAST) + 1;
        slides.forEach((s) => (s.hidden = s.dataset.page != page));
        const chip = qs("#pageChip");
        if (chip) chip.textContent = `${page} / ${LAST}`;
        renderForm(page);
        showActiveTab();
    }

    qsa(".top [data-goto]").forEach((btn) =>
        btn.addEventListener("click", () => go(+btn.dataset.goto))
    );

    // ---------- 사이드 폼 ----------
    const forms = {
        1: () => `
      <div class="panel"><h3>소개</h3>
        <div class="row"><label>타이틀</label><input class="inpt" data-model="intro.title" value="${state.intro.title}"></div>
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
      </div>`,
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
      </div>`,
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
      </div>`,
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
      </div>`,
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
      </div>`,
    };

    function renderForm(n) {
        const pane = qs("#formPane");
        pane.innerHTML = forms[n]();

        // 입력 바인딩
        qsa("[data-model]", pane).forEach((inpt) => {
            inpt.addEventListener("input", () => {
                const path = inpt.getAttribute("data-model");
                const val =
                    inpt.type === "number"
                        ? Number(inpt.value || 0)
                        : inpt.value;
                set(path, val);
            });
        });

        // 이미지 업로드 -> dataURL 상태 반영
        qsa("[data-upload]", pane).forEach((fi) => {
            fi.addEventListener("change", async () => {
                const file = fi.files?.[0];
                if (!file) return;
                const dataURL = await fileToDataURL(file);
                set(fi.getAttribute("data-upload"), dataURL);
                fi.value = "";
            });
        });
    }

    // ---------- 저장/불러오기/프린트/나가기 ----------
    const btnSave = qs("#btnSave"); // 임시저장(DRAFT)
    const btnLoad = qs("#btnLoad"); // 수동 불러오기
    const btnPrint = qs("#btnPrint");
    const btnExit = qs("#btnExit");

    // 초기 로드: id가 있을 때만 해당 초안을 자동 로드 / id 없으면 기본 템플릿로 시작
    async function loadInitial() {
        try {
            if (currentFolioId) {
                const res = await guardFetch(
                    `/api/folios/${encodeURIComponent(currentFolioId)}`
                );
                if (!res.ok) throw new Error("불러오기 실패");
                const data = await res.json();
                const incoming =
                    (data &&
                    typeof data.state === "object" &&
                    Object.keys(data.state).length
                        ? data.state
                        : null) ||
                    (data && typeof data.contentJson === "string"
                        ? JSON.parse(data.contentJson)
                        : null);

                if (incoming) state = deepMerge(defaultState, incoming);
                if (data.id || data.folioId)
                    currentFolioId = data.id || data.folioId;
            }
            // ★ id가 없으면 서버 자동 불러오기 하지 않음(기본 템플릿 유지)
        } catch (e) {
            console.warn("초기 로드 실패(기본 템플릿로 진행):", e);
        }
    }

    // (선택) 수동 불러오기 버튼: 내 최신 DRAFT를 가져와 적용
    btnLoad?.addEventListener("click", async () => {
        try {
            const res = await guardFetch("/api/folios/me/dev-basic");
            if (res.status === 204) {
                toast("서버 임시저장 없음");
                return;
            }
            if (!res.ok) throw new Error("불러오기 실패");
            const data = await res.json();
            const incoming = data?.contentJson
                ? JSON.parse(data.contentJson)
                : null;
            if (incoming) state = deepMerge(defaultState, incoming);
            if (data.id) currentFolioId = data.id;
            applyBindings();
            go(1);
            toast("불러오기 완료");
        } catch (e) {
            console.error(e);
            if (e.message !== "인증 필요") toast("불러오기 중 오류");
        }
    });

    // 임시저장(DRAFT)
    btnSave?.addEventListener("click", async () => {
        try {
            const snapshot = JSON.parse(JSON.stringify(state));
            const contentJson = JSON.stringify(snapshot);
            const firstImg =
                snapshot.intro?.photo ||
                snapshot.proj1?.thumb ||
                snapshot.proj2?.thumb ||
                null;
            const thumbnail =
                (await normalizeImageUrl(firstImg)) ||
                "https://picsum.photos/seed/default/300";

            const payload = {
                folioId: currentFolioId || null, // ★ 같은 글 업데이트
                template: "dev-basic",
                contentJson,
                status: "DRAFT",
                thumbnail,
                title: (snapshot.intro?.name || "Untitled").trim(),
            };

            const res = await guardFetch("/api/folios/dev-basic", {
                method: "POST",
                headers: JSON_HEADERS,
                body: JSON.stringify(payload),
            });
            if (!res.ok) throw new Error(await res.text());
            const saved = await res.json(); // { id: ... }
            if (saved?.id) currentFolioId = saved.id;

            toast("임시저장 완료");
        } catch (e) {
            console.error(e);
            if (e.message !== "인증 필요") toast("임시저장 중 오류");
        }
    });

    // 프린트: 모두 보였다가 복원
    function unhideAllForPrint() {
        qsa(".slide").forEach((s) => {
            _hiddenMap.set(s, s.hidden);
            s.hidden = false;
        });
    }
    function restoreHiddenAfterPrint() {
        qsa(".slide").forEach((s) => {
            const prev = _hiddenMap.get(s);
            if (prev !== undefined) s.hidden = prev;
        });
        _hiddenMap.clear();
    }
    btnPrint?.addEventListener("click", () => window.print());
    window.addEventListener("beforeprint", unhideAllForPrint);
    window.addEventListener("afterprint", restoreHiddenAfterPrint);

    // 나가기
    btnExit?.addEventListener("click", (e) => {
        e.preventDefault();
        const ref = document.referrer;
        try {
            if (ref) {
                const prev = new URL(ref);
                if (prev.origin === location.origin) {
                    history.back();
                    return;
                }
            }
        } catch {}
        location.href = "/";
    });

    // --- 발행(PUBLISHED): 이미지 렌더 → 업로드(JSON) ---
    async function captureSlidesAsImages(maxSlides = 1) {
        try {
            if (document.fonts?.ready) await document.fonts.ready;
        } catch {}
        const slides = [...document.querySelectorAll(".slide")];
        const prevHidden = slides.map((s) => s.hidden);
        slides.forEach((s) => (s.hidden = false));

        const images = [];
        try {
            for (let i = 0; i < slides.length && i < maxSlides; i++) {
                const el = slides[i];
                const canvas = await html2canvas(el, {
                    backgroundColor: "#ffffff",
                    scale: 1, // 🔽 낮춤
                    useCORS: true,
                    removeContainer: true,
                });
                const dataUrl = canvas.toDataURL("image/jpeg", 0.6); // 🔽 낮춤
                images.push(dataUrl);
            }
        } finally {
            slides.forEach((s, i) => (s.hidden = prevHidden[i]));
        }
        return images;
    }

    document
        .querySelector("#btnUpload")
        ?.addEventListener("click", async () => {
            const button = document.querySelector("#btnUpload");
            try {
                button.disabled = true;

                const snapshot = JSON.parse(JSON.stringify(state));
                const contentJson = JSON.stringify(snapshot);

                // (선택) 첫 슬라이드 캡처, 하지만 DB에는 base64를 넣지 않습니다.
                const cap = await captureSlidesAsImages(1); // 이미 있던 함수
                // 썸네일 원본 후보: 캡처 1장 또는 기존 입력 썸네일들
                const thumbSource =
                    cap?.[0] ||
                    snapshot.intro?.photo ||
                    snapshot.proj1?.thumb ||
                    snapshot.proj2?.thumb ||
                    null;

                // 🔴 여기! base64 → /api/uploads/images 로 업로드 → 짧은 URL로 변환
                const thumbnail =
                    (await normalizeImageUrl(thumbSource)) ||
                    "https://picsum.photos/seed/default/300";

                const title =
                    document
                        .querySelector('[data-bind="intro.name"]')
                        ?.textContent?.trim() ||
                    snapshot?.intro?.name ||
                    "Untitled";

                const payload = {
                    folioId: currentFolioId || null,
                    template: "dev-basic",
                    title,
                    contentJson,
                    status: "PUBLISHED",
                    thumbnail, // ← URL만 저장
                    images: [], // ← 지금 단계에선 비워서 가볍게 보냅니다
                };

                const res = await guardFetch("/api/folios/dev-basic/publish", {
                    method: "POST",
                    headers: JSON_HEADERS,
                    body: JSON.stringify(payload),
                });
                if (!res.ok) throw new Error(await res.text());

                // 지금은 상세페이지 패스하고 마이페이지로 이동해서 업로드 칸에 바로 보이게
                location.href = "/mypage/home";
            } catch (e) {
                console.error(e);
                if (e.message !== "인증 필요") flash("업로드 중 오류");
            } finally {
                button.disabled = false;
            }
        });

    // 키보드 네비
    qs(".navArrow.left")?.addEventListener("click", () => go(page - 1));
    qs(".navArrow.right")?.addEventListener("click", () => go(page + 1));
    window.addEventListener("keydown", (e) => {
        if (e.key === "ArrowLeft") go(page - 1);
        if (e.key === "ArrowRight") go(page + 1);
    });

    // ========== init ==========
    (async function init() {
        await loadInitial(); // id 있으면 그 초안만 자동 로드
        applyBindings();
        go(1);
    })();
});
