/* =========================================================
   KAFOLIO – Global + Page JS (home, detail, signin, signup)
   - 기존 홈/디테일 로직 유지
   - signin/signup 인라인 스크립트 통합 (data-page 분기)
   ========================================================= */

/* ===== 공통 유틸 ===== */
const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

const state = {
    theme: localStorage.getItem("theme") || "light",
    query: "",
    sort: "latest",
    page: 1,
    pageSize: 9, // Home 기본 9, Detail에서는 9로 유지(필요 시 5로 조정)
};

const MAX_TAGS_PER_CARD = 3;

/* ===== 데모 데이터 ===== */
const sampleProjects = [
    {
        id: "p1",
        title: "핀테크 대시보드 리디자인",
        creator: "J. Kim",
        tags: ["React", "TypeScript", "Tailwind", "Dashboard", "DesignSystem"],
        likes: 128,
        createdAt: "2025-08-15",
        cover: "https://picsum.photos/seed/fin/800/500",
        desc: "금융 데이터 시각화 중심의 관리자 대시보드 리디자인 프로젝트.",
        link: "https://example.com/fin",
    },
    {
        id: "p2",
        title: "헬스케어 습관 트래커 웹앱",
        creator: "M. Lee",
        tags: ["Next.js", "React", "Zustand", "PWA", "Analytics"],
        likes: 203,
        createdAt: "2025-06-01",
        cover: "https://picsum.photos/seed/health/800/500",
        desc: "개인 맞춤 습관 형성 UX를 반영한 웹앱. 오프라인 동기화 지원.",
        link: "https://example.com/health",
    },
    {
        id: "p3",
        title: "브랜드 사이트 – Cafe VERT",
        creator: "A. Park",
        tags: ["Astro", "Tailwind", "SSR", "SEO", "Animation"],
        likes: 96,
        createdAt: "2025-07-10",
        cover: "https://picsum.photos/seed/brand/800/500",
        desc: "친환경 카페 브랜드의 모션 인터랙션과 SEO 최적화.",
        link: "https://example.com/vert",
    },
    {
        id: "p4",
        title: "3D 제품 뷰어(웹)",
        creator: "D. Choi",
        tags: ["Three.js", "WebGL", "React", "Product", "Viewer"],
        likes: 310,
        createdAt: "2025-05-20",
        cover: "https://picsum.photos/seed/3d/800/500",
        desc: "브라우저에서 회전/재질 변경 가능한 3D 제품 뷰어.",
        link: "https://example.com/3d",
    },
    {
        id: "p5",
        title: "에듀테크 랜딩",
        creator: "S. Han",
        tags: ["Next.js", "TypeScript", "A/B Test", "Accessibility", "Landing"],
        likes: 77,
        createdAt: "2025-08-22",
        cover: "https://picsum.photos/seed/edu/800/500",
        desc: "전환율 상승을 위한 메시지 구조와 접근성 개선.",
        link: "https://example.com/edu",
    },
    {
        id: "p6",
        title: "매거진 타이포 시스템(웹)",
        creator: "B. Yoo",
        tags: ["Vue", "Nuxt", "SSR", "Typography", "WebFont"],
        likes: 65,
        createdAt: "2025-02-18",
        cover: "https://picsum.photos/seed/type/800/500",
        desc: "가독성 중심의 모듈형 타이포그래피 시스템.",
        link: "https://example.com/type",
    },
    {
        id: "p7",
        title: "여행 계획 웹 서비스 UX 개편",
        creator: "H. Jung",
        tags: ["React", "Redux", "REST", "i18n", "Admin"],
        likes: 181,
        createdAt: "2025-04-02",
        cover: "https://picsum.photos/seed/travel/800/500",
        desc: "검색→저장→공유 플로우 개선으로 예약 전환율 향상.",
        link: "https://example.com/travel",
    },
    {
        id: "p8",
        title: "러닝 코치 대시보드",
        creator: "R. Kwon",
        tags: ["Svelte", "Node.js", "Express", "MongoDB", "Realtime"],
        likes: 142,
        createdAt: "2025-03-12",
        cover: "https://picsum.photos/seed/run/800/500",
        desc: "센서 데이터 실시간 시각화와 사용자 피드백 모듈.",
        link: "https://example.com/run",
    },
    {
        id: "p9",
        title: "컴포넌트 문서화 시스템",
        creator: "Y. Song",
        tags: ["Storybook", "React", "Testing", "DesignSystem", "CI/CD"],
        likes: 54,
        createdAt: "2025-07-25",
        cover: "https://picsum.photos/seed/illu/800/500",
        desc: "디자인 시스템 문서화와 테스트 자동화 파이프라인.",
        link: "https://example.com/illu",
    },
];

const formatDate = (iso) => new Date(iso).toLocaleDateString("ko-KR");

/* ===== 공통 헤더/테마 ===== */
function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    const btn = $("#btnTheme");
    if (btn) btn.setAttribute("aria-pressed", theme === "dark");
}
function toggleTheme() {
    state.theme = state.theme === "dark" ? "light" : "dark";
    localStorage.setItem("theme", state.theme);
    applyTheme(state.theme);
}
function initHeader() {
    const themeBtn = $("#btnTheme");
    if (themeBtn) themeBtn.addEventListener("click", toggleTheme);
    applyTheme(state.theme);

    const navToggle = $(".nav-toggle");
    const navList = $("#primary-menu");
    if (navToggle && navList) {
        navToggle.addEventListener("click", () => {
            const open = navList.classList.toggle("open");
            navToggle.setAttribute("aria-expanded", open);
        });
    }

    const year = $("#year");
    if (year) year.textContent = new Date().getFullYear();
}

/* ===== URL 파라미터 ===== */
function getQueryParam(name) {
    const params = new URLSearchParams(location.search);
    return params.get(name);
}

/* ===== placeholder 길이에 맞춰 input/컨테이너 너비 조정 ===== */
function measureTextWidth(text, font) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");
    ctx.font = font;
    return Math.ceil(ctx.measureText(text).width);
}
function getFontFor(el) {
    const s = getComputedStyle(el);
    return `${s.fontStyle} ${s.fontVariant} ${s.fontWeight} ${s.fontSize} ${s.fontFamily}`;
}
function fitInputToPlaceholder() {
    const heroInput = $("#searchInputHero");
    const listInput = $("#searchInput");

    [heroInput, listInput].forEach((input) => {
        if (!input) return;
        if (window.matchMedia("(max-width:640px)").matches) {
            input.style.width = "100%";
            return;
        }
        const font = getFontFor(input);
        const text = input.getAttribute("placeholder") || "";
        const w = measureTextWidth(text, font) + 28; // 좌우 패딩 보정
        const min = input.id === "searchInputHero" ? 360 : 320;
        const max = input.id === "searchInputHero" ? 880 : 720;
        input.style.width = Math.max(min, Math.min(max, w)) + "px";

        // 히어로 검색폼 전체 너비도 버튼 폭 포함해 보정
        if (input.id === "searchInputHero") {
            const form = $("#heroSearch");
            const btn = form?.querySelector("button");
            if (form && btn) {
                const gap = 10;
                const total =
                    input.getBoundingClientRect().width +
                    btn.getBoundingClientRect().width +
                    gap;
                form.style.width =
                    Math.min(total, max + btn.offsetWidth + gap) + "px";
            }
        }
    });
}

/* ===== 검색/정렬 (Home 전용) ===== */
function attachToolbarHandlers() {
    const input = $("#searchInput");
    if (input) {
        input.addEventListener("input", (e) => {
            state.query = e.target.value.trim();
            state.page = 1;
            renderGrid();
        });
    }

    const heroForm = $("#heroSearch");
    if (heroForm) {
        heroForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const q = $("#searchInputHero").value.trim();
            applyQuery(q);
        });
    }

    $$(".hero-tags .tag").forEach((tagBtn) => {
        tagBtn.addEventListener("click", () => {
            applyQuery(tagBtn.dataset.tag);
        });
    });

    const sort = $("#sortSelect");
    if (sort) {
        sort.addEventListener("change", (e) => {
            state.sort = e.target.value;
            state.page = 1;
            renderGrid();
        });
    }

    const clear = $("#btnClear");
    if (clear) {
        clear.addEventListener("click", () => {
            state.query = "";
            state.sort = "latest";
            state.page = 1;
            const si = $("#searchInput");
            if (si) si.value = "";
            const ss = $("#sortSelect");
            if (ss) ss.value = "latest";
            renderGrid();
        });
    }
}

function applyQuery(q) {
    state.query = q || "";
    const si = $("#searchInput");
    if (si) si.value = state.query;
    state.page = 1;
    renderGrid();
    const bar = $("#section-projects");
    if (bar) window.scrollTo({ top: bar.offsetTop - 70, behavior: "smooth" });
}

/* ===== 데이터 처리 ===== */
function getFilteredSortedData() {
    let data = [...sampleProjects];
    const q = state.query.toLowerCase();
    if (q) {
        data = data.filter((p) => {
            const inTags = p.tags.some((t) => t.toLowerCase().includes(q));
            return (
                p.title.toLowerCase().includes(q) ||
                p.desc.toLowerCase().includes(q) ||
                p.creator.toLowerCase().includes(q) ||
                inTags
            );
        });
    }

    if (state.sort === "latest") {
        data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (state.sort === "popular") {
        data.sort((a, b) => b.likes - a.likes);
    } else if (state.sort === "title") {
        data.sort((a, b) => a.title.localeCompare(b.title, "ko"));
    }
    return data;
}

/* 태그 렌더링: 최대 3개 + 더보기 개수 */
function renderTagBadges(tags) {
    const show = tags.slice(0, MAX_TAGS_PER_CARD);
    const hidden = Math.max(0, tags.length - MAX_TAGS_PER_CARD);
    const html = show
        .map(
            (t) => `<span class="tag-badge" aria-label="태그 ${t}">${t}</span>`
        )
        .join("");
    const more = hidden
        ? `<span class="tag-badge more" aria-label="추가 태그 ${hidden}개">+${hidden}</span>`
        : "";
    return html + more;
}

/* ===== 카드 렌더링 ===== */
function createCard(project) {
    const el = document.createElement("article");
    el.className = "card";
    el.setAttribute("data-id", project.id);
    el.setAttribute("tabindex", "0");
    el.innerHTML = `
    <figure class="card-media skeleton">
      <img src="${project.cover}" alt="${
        project.title
    } 대표 이미지" loading="lazy" />
    </figure>
    <div class="card-body">
      <h3 class="card-title">${project.title}</h3>
      <div class="card-meta">
        <span>by ${project.creator}</span>
        <span aria-hidden="true">•</span>
        <span>${formatDate(project.createdAt)}</span>
        <span aria-hidden="true">•</span>
        <span>❤ ${project.likes.toLocaleString()}</span>
      </div>
      <div class="card-tags">${renderTagBadges(project.tags)}</div>
    </div>
    <div class="card-actions">
      <button class="btn view">자세히</button>
      <a class="btn ghost" href="${
          project.link
      }" target="_blank" rel="noopener">원본</a>
    </div>
  `;

    const img = $("img", el);
    img.addEventListener("load", () =>
        $(".card-media", el).classList.remove("skeleton")
    );

    $(".view", el).addEventListener("click", () => openModal(project));
    el.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            openModal(project);
        }
    });

    return el;
}

function renderGrid() {
    const grid = $("#grid");
    if (!grid) return;
    grid.setAttribute("aria-busy", "true");
    grid.innerHTML = "";

    const all = getFilteredSortedData();
    const end = state.page * state.pageSize;
    const slice = all.slice(0, end);

    if (slice.length === 0) {
        grid.innerHTML = `<p class="note">조건에 맞는 포트폴리오가 없어요. 검색어를 변경해보세요.</p>`;
        const moreBtn = $("#btnLoadMore");
        if (moreBtn) moreBtn.classList.add("hidden");
    } else {
        slice.forEach((p) => grid.appendChild(createCard(p)));
        const moreBtn = $("#btnLoadMore");
        if (moreBtn) moreBtn.classList.toggle("hidden", end >= all.length);
    }

    grid.setAttribute("aria-busy", "false");
}

/* 더 보기 */
function attachLoadMore() {
    const btn = $("#btnLoadMore");
    if (!btn) return;
    btn.addEventListener("click", () => {
        state.page++;
        renderGrid();
    });
}

/* 상단 이동 */
function attachToTop() {
    const fab = $("#btnToTop");
    if (!fab) return;
    window.addEventListener("scroll", () => {
        const show = window.scrollY > 600;
        fab.classList.toggle("hidden", !show);
    });
    fab.addEventListener("click", () =>
        window.scrollTo({ top: 0, behavior: "smooth" })
    );
}

/* 모달 */
function openModal(project) {
    $("#modalImage")?.setAttribute("src", project.cover);
    $("#modalImage")?.setAttribute("alt", `${project.title} 상세 이미지`);
    $("#modalTitle") && ($("#modalTitle").textContent = project.title);
    $("#modalDesc") && ($("#modalDesc").textContent = project.desc);
    const tags = $("#modalTags");
    if (tags) tags.innerHTML = renderTagBadges(project.tags);
    const link = $("#modalLink");
    if (link) link.href = project.link;

    const dlg = $("#modal");
    if (dlg && typeof dlg.showModal === "function") dlg.showModal();
    else if (dlg) dlg.setAttribute("open", "");
}
function attachModal() {
    const dlg = $("#modal");
    if (!dlg) return;
    $(".modal-close", dlg).addEventListener("click", () => dlg.close());
    dlg.addEventListener("click", (e) => {
        const card = $(".modal-card", dlg);
        if (e.target === dlg && !card.contains(e.target)) dlg.close();
    });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && dlg.open) dlg.close();
    });
}

/* ===== Detail 페이지 전용 (완전 수정 버전) ===== */
function attachDetailPage() {
    if (document.body.dataset.page !== "detail") {
        return;
    }

    const LANG_OPTS = [
        "Java",
        "JS",
        "JavaScript",
        "TypeScript",
        "TS",
        "HTML",
        "CSS",
        "SCSS",
        "Thymeleaf",
        "Spring",
        "SpringBoot",
        "SQL",
        "XML",
        "JSON",
        "NodeJS",
        "Tailwind",
        "React",
        "React-Router-Dom",
        "NextJS",
    ];
    const TOOL_OPTS = [
        "VSCode",
        "SQLDeveloper",
        "Git",
        "GitHub",
        "Postman",
        "React",
        "Spring",
        "SpringBoot",
        "SpringSecurity",
        "SpringDataJPA",
        "JPA",
        "NextJS",
        "TailwindCSS",
        "Lombok",
        "Oracle",
        "GoogleOAuth2",
        "KakaoOAuth2",
        "NaverOAuth2",
        "KakaoMap",
        "NaverMap",
        "Toss",
        "Maven",
        "SourceTree",
        "SpringOAuth2",
        "Brevo",
        "Eclipse",
        "DevTools",
        "Security",
        "OAuth2",
        "Bootstrap",
        "Font-Awesome",
        "Express",
        "Mongoose",
        "Redis",
        "Vite",
        "Monaco",
        "SpringDoc",
        "Thumbnailator",
        "WebSocket",
        "Socket.io",
        "ChakraUI",
        "MongoDB",
        "EmailJS",
        "Tailwind",
        "Chakra",
        "NodeJS",
        "FS",
        "Multer",
    ];

    initBlock(
        "techSelectLang",
        "techSearchLang",
        "techListLang",
        "selectedTechLang",
        "clearTechLang",
        "applyTechLang",
        LANG_OPTS
    );
    initBlock(
        "techSelectTools",
        "techSearchTools",
        "techListTools",
        "selectedTechTools",
        "clearTechTools",
        "applyTechTools",
        TOOL_OPTS
    );

    // 바깥 클릭 시 닫기 (pills 영역과 드롭다운 내부는 제외)
    document.addEventListener("click", (e) => {
        document.querySelectorAll(".multiselect.open").forEach((ms) => {
            const dropdown = ms.querySelector(".ms-dropdown");
            const pillsArea = ms.querySelector(".ms-pills");

            // 클릭한 곳이 multiselect 내부가 아니면 닫기
            if (!ms.contains(e.target)) {
                closeDropdown(ms);
            }
            // multiselect 내부더라도 pills나 dropdown이 아닌 곳 클릭시 닫기 방지
        });
    });
}

function closeDropdown(multiselectElement) {
    multiselectElement.classList.remove("open");
    const dd = multiselectElement.querySelector(".ms-dropdown");
    const trig = multiselectElement.querySelector(".ms-trigger");

    if (dd) {
        dd.hidden = true;
        dd.style.display = "none";
    }
    if (trig) {
        trig.setAttribute("aria-expanded", "false");
    }
}

function openDropdown(multiselectElement) {
    multiselectElement.classList.add("open");
    const dd = multiselectElement.querySelector(".ms-dropdown");
    const trig = multiselectElement.querySelector(".ms-trigger");
    const search = multiselectElement.querySelector("input[type='search']");

    if (dd) {
        dd.hidden = false;
        dd.style.display = "";
    }
    if (trig) {
        trig.setAttribute("aria-expanded", "true");
    }
    if (search) {
        setTimeout(() => search.focus({ preventScroll: true }), 50);
    }
}

function initBlock(
    rootId,
    searchId,
    listId,
    pillsId,
    clearId,
    applyId,
    OPTIONS
) {
    const root = document.getElementById(rootId);
    if (!root) {
        return;
    }

    console.log(`✅ ${rootId} 초기화 중...`);

    const trig = root.querySelector(".ms-trigger");
    const dd = root.querySelector(".ms-dropdown");
    const search = document.getElementById(searchId);
    const list = document.getElementById(listId);
    const pills = document.getElementById(pillsId);
    const btnClr = document.getElementById(clearId);
    const btnApp = document.getElementById(applyId);

    if (!trig || !dd) {
        return;
    }

    // 초기 상태 설정
    closeDropdown(root);

    if (trig) {
        trig.setAttribute("aria-haspopup", "listbox");

        // 기존 이벤트 리스너 제거 후 새로 추가
        const newTrig = trig.cloneNode(true);
        trig.parentNode.replaceChild(newTrig, trig);

        newTrig.addEventListener("click", (e) => {
            console.log(`🖱️ ${rootId} 트리거 클릭됨`);
            e.preventDefault();
            e.stopPropagation();

            const isOpen = root.classList.contains("open");

            // 다른 모든 드롭다운 닫기
            document.querySelectorAll(".multiselect.open").forEach((ms) => {
                if (ms !== root) closeDropdown(ms);
            });

            // 현재 드롭다운 토글
            if (isOpen) {
                closeDropdown(root);
            } else {
                openDropdown(root);
            }
        });
    }

    let selected = [];

    function renderList(filter = "") {
        if (!list) return;
        list.innerHTML = "";
        OPTIONS.filter((o) =>
            o.toLowerCase().includes(filter.toLowerCase())
        ).forEach((opt) => {
            const li = document.createElement("li");
            li.textContent = opt;
            if (selected.includes(opt)) li.style.fontWeight = "600";

            // ⭐ 핵심: 항목 클릭해도 드롭다운 안 닫힘!
            li.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation(); // 이벤트 버블링 방지

                // 선택/해제 처리
                if (selected.includes(opt)) {
                    selected = selected.filter((s) => s !== opt);
                } else {
                    selected = [...selected, opt];
                }

                // UI 업데이트 (드롭다운은 열린 상태 유지)
                renderList(search ? search.value : "");
                renderPills();
                console.log(`선택됨: ${opt}, 드롭다운 계속 열림`);
            });

            list.appendChild(li);
        });
    }

    function renderPills() {
        if (!pills) return;
        pills.innerHTML = "";
        selected.forEach((s) => {
            const pill = document.createElement("span");
            pill.className = "pill";
            pill.innerHTML = `${s} <button type="button" aria-label="삭제">&times;</button>`;

            // ⭐ 핵심: pill 삭제 버튼 클릭해도 드롭다운 안 닫힘!
            pill.querySelector("button").addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation(); // 이벤트 버블링 방지

                selected = selected.filter((x) => x !== s);
                renderList(search ? search.value : "");
                renderPills();
                console.log(`삭제됨: ${s}, 드롭다운 계속 열림`);
            });

            pills.appendChild(pill);
        });
    }

    // 이벤트 리스너 등록
    if (search) {
        search.addEventListener("input", (e) => {
            renderList(e.target.value);
        });

        // 검색창 클릭해도 드롭다운 안 닫힘
        search.addEventListener("click", (e) => {
            e.stopPropagation();
        });
    }

    if (btnClr) {
        btnClr.addEventListener("click", (e) => {
            e.stopPropagation(); // 드롭다운 안 닫힘
            selected = [];
            renderList("");
            renderPills();
            console.log("초기화됨, 드롭다운 계속 열림");
        });
    }

    if (btnApp) {
        btnApp.addEventListener("click", (e) => {
            e.stopPropagation();

            const other = collectOther(rootId);
            const keywords = [...selected, ...other];

            if (typeof state !== "undefined") {
                state.query = keywords.length ? keywords.join(" ") : "";
                state.page = 1;
            }
            if (typeof renderGrid === "function") renderGrid();

            // ⭐ 핵심: 적용 버튼 클릭시에만 드롭다운 닫힘!
            closeDropdown(root);
            console.log("적용됨, 드롭다운 닫힘");
        });
    }

    // pills 영역 클릭 시 드롭다운 안 닫히게
    if (pills) {
        pills.addEventListener("click", (e) => {
            e.stopPropagation();
        });
    }

    // 드롭다운 내부 클릭 시 안 닫히게
    if (dd) {
        dd.addEventListener("click", (e) => {
            e.stopPropagation();
        });
    }

    // 초기 렌더링
    renderList();
    renderPills();

    console.log(`✅ ${rootId} 초기화 완료`);

    // 반대쪽 선택값 수집
    function collectOther(currRootId) {
        const otherPillsId =
            currRootId === "techSelectLang"
                ? "selectedTechTools"
                : "selectedTechLang";
        const spans =
            document.getElementById(otherPillsId)?.querySelectorAll(".pill") ||
            [];
        return Array.from(spans).map((p) => p.firstChild.nodeValue.trim());
    }
}

// 안전한 초기화
function safeInit() {
    console.log("🔄 safeInit 실행됨");

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", () => {
            console.log("📄 DOMContentLoaded 이벤트 발생");
            attachDetailPage();
        });
    } else {
        console.log("📄 DOM이 이미 로드됨, 즉시 실행");
        attachDetailPage();
    }
}

// 실행
safeInit();

/* ===== Signup 전용: 이메일 인증 UX ===== */
function initSignupPage() {
    const form = $("#signupForm");
    const emailEl = $("#email");
    const sendBtn = $("#btnSendCode");
    const codeBlock = $("#codeBlock");
    const codeInput = $("#emailCode");
    const errCode = $("#err-code");
    const hint = $("#codeHint");

    if (!form || !emailEl || !sendBtn) return; // 안전 가드

    const emailRe = /^\S+@\S+\.\S+$/;
    const csrfToken = $('meta[name="_csrf"]')?.content;
    const csrfHeader = $('meta[name="_csrf_header"]')?.content;

    // 이메일 형식 → 버튼 활성/비활성
    function updateSendBtn() {
        const ok = emailRe.test((emailEl.value || "").trim());
        sendBtn.disabled = !ok;
    }
    emailEl.addEventListener("input", updateSendBtn);
    updateSendBtn();

    // 재전송 쿨다운
    let timer = null,
        cooldown = 0;
    function startCooldown(sec = 60) {
        if (timer) clearInterval(timer);
        cooldown = sec;
        sendBtn.disabled = true;
        sendBtn.textContent = `재전송 ${cooldown}s`;
        timer = setInterval(() => {
            cooldown--;
            if (cooldown <= 0) {
                clearInterval(timer);
                sendBtn.textContent = "인증코드 발송";
                updateSendBtn();
            } else {
                sendBtn.textContent = `재전송 ${cooldown}s`;
            }
        }, 1000);
    }

    // 인증코드 발송
    sendBtn.addEventListener("click", async () => {
        if (sendBtn.disabled) return;
        errCode.textContent = "";

        const email = (emailEl.value || "").trim();
        if (!emailRe.test(email)) return;

        // 클릭 즉시 코드칸 표시 & 포커스
        if (codeBlock) codeBlock.hidden = false;
        codeInput?.focus();

        try {
            const res = await fetch("/send-code", {
                method: "POST",
                headers: Object.assign(
                    { "Content-Type": "application/x-www-form-urlencoded" },
                    csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}
                ),
                body: new URLSearchParams({ email }),
            });
            if (!res.ok) throw new Error("인증코드 발송 실패");
            startCooldown(60);
            if (hint)
                hint.textContent =
                    "인증코드를 보냈습니다. 5분 안에 입력해 주세요.";
        } catch (err) {
            errCode.textContent =
                "인증코드 발송에 실패했습니다. 이메일을 확인 후 다시 시도해 주세요.";
            setTimeout(updateSendBtn, 5000);
        }
    });

    // 제출 시: 코드칸이 열려 있다면 코드 필수
    form.addEventListener("submit", (e) => {
        if (!codeBlock?.hidden && !codeInput?.value.trim()) {
            e.preventDefault();
            if (errCode) errCode.textContent = "인증코드를 입력하세요.";
            codeInput?.focus();
        }
    });
}

/* ===== 초기화 ===== */
function init() {
    initHeader();
    fitInputToPlaceholder();
    window.addEventListener("resize", fitInputToPlaceholder);

    const page = document.body.dataset.page;

    if (page === "home") {
        // Home
        state.pageSize = 9;
        attachToolbarHandlers();
        attachLoadMore();
        attachToTop();
        attachModal();

        const initialQ = getQueryParam("q");
        if (initialQ) {
            const ss = $("#sortSelect");
            if (ss) ss.value = "latest";
            applyQuery(initialQ);
        } else {
            renderGrid();
        }
    }

    if (page === "detail") {
        // Detail
        state.pageSize = 9;
        attachDetailPage();
        attachLoadMore();
        attachToTop();
        renderGrid();
    }

    if (page === "signin") {
        initSigninPage();
    }

    if (page === "signup") {
        initSignupPage();
    }
}

document.addEventListener("DOMContentLoaded", init);
