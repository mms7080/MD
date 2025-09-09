/* =========================================================
   KAFOLIO – Global + Page JS (home + detail)
   - Home: 기존 검색/정렬/카드 그대로
   - Detail: Dribbble-like grid, 12개 1차 노출, 썸네일 풀채움 + 하단 정보 상시
   ========================================================= */

/* ===== 공통 유틸 ===== */
const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

/* ===== 상태 ===== */
const state = {
    theme: localStorage.getItem("theme") || "light",
    query: "",
    sort: "latest",
    page: 1,
    pageSize: 9, // home 기본
    filters: { lang: [], tools: [] }, // detail에서 사용
};

const MAX_TAGS_PER_CARD = 4;

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
    {
        id: "p10",
        title: "Mono UI",
        creator: "Mono Co",
        tags: ["React", "Chakra"],
        likes: 510,
        views: 7400,
        createdAt: "2024-12-11",
        cover: "https://images.unsplash.com/photo-1547658719-da2b51169166?w=800&h=500&fit=crop",
    },
    {
        id: "p11",
        title: "Portfolio Grid",
        creator: "Mona",
        tags: ["Vue", "SCSS"],
        likes: 330,
        views: 5000,
        createdAt: "2024-11-02",
        cover: "https://images.unsplash.com/photo-1558655146-364adfc985ee?w=800&h=500&fit=crop",
    },
    {
        id: "p12",
        title: "Neo Cards",
        creator: "Neo",
        tags: ["React", "NextJS"],
        likes: 970,
        views: 13200,
        createdAt: "2024-10-24",
        cover: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=500&fit=crop",
    },
];

const formatDate = (iso) => new Date(iso).toLocaleDateString("ko-KR");
function formatNumber(n) {
    if (n >= 10000) return (n / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    return String(n || 0);
}

/* ===== 헤더/테마 ===== */
function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    $("#btnTheme")?.setAttribute("aria-pressed", theme === "dark");
}
function toggleTheme() {
    state.theme = state.theme === "dark" ? "light" : "dark";
    localStorage.setItem("theme", state.theme);
    applyTheme(state.theme);
}
function initHeader() {
    $("#btnTheme")?.addEventListener("click", toggleTheme);
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

/* ===== 홈 전용(있으면 동작, 없으면 무시) ===== */
function getQueryParam(name) {
    const p = new URLSearchParams(location.search);
    return p.get(name);
}
function measureTextWidth(text, font) {
    const c = document.createElement("canvas");
    const ctx = c.getContext("2d");
    ctx.font = font;
    return Math.ceil(ctx.measureText(text).width);
}
function getFontFor(el) {
    const s = getComputedStyle(el);
    return `${s.fontStyle} ${s.fontVariant} ${s.fontWeight} ${s.fontSize} ${s.fontFamily}`;
}
function fitInputToPlaceholder() {
    const heroInput = $("#searchInputHero"),
        listInput = $("#searchInput");
    [heroInput, listInput].forEach((input) => {
        if (!input) return;
        if (window.matchMedia("(max-width:640px)").matches) {
            input.style.width = "100%";
            return;
        }
        const w =
            measureTextWidth(
                input.getAttribute("placeholder") || "",
                getFontFor(input)
            ) + 28;
        const min = input.id === "searchInputHero" ? 360 : 320,
            max = input.id === "searchInputHero" ? 880 : 720;
        input.style.width = Math.max(min, Math.min(max, w)) + "px";
        if (input.id === "searchInputHero") {
            const form = $("#heroSearch");
            const btn = form?.querySelector("button");
            if (form && btn) {
                const gap = 10,
                    total =
                        input.getBoundingClientRect().width +
                        btn.getBoundingClientRect().width +
                        gap;
                form.style.width =
                    Math.min(total, max + btn.offsetWidth + gap) + "px";
            }
        }
    });
}
function attachToolbarHandlers() {
    if (document.body.dataset.page !== "home") return;
    $("#searchInput")?.addEventListener("input", (e) => {
        state.query = e.target.value.trim();
        state.page = 1;
        renderGrid();
    });
    $("#heroSearch")?.addEventListener("submit", (e) => {
        e.preventDefault();
        const q = $("#searchInputHero").value.trim();
        applyQuery(q);
    });
    $$(".hero-tags .tag").forEach((btn) =>
        btn.addEventListener("click", () => applyQuery(btn.dataset.tag))
    );
    const sort = $("#sortSelect");
    if (sort) {
        sort.addEventListener("change", (e) => {
            state.sort = e.target.value;
            state.page = 1;
            renderGrid();
        });
    }
    $("#btnClear")?.addEventListener("click", () => {
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
function applyQuery(q) {
    state.query = q || "";
    const si = $("#searchInput");
    if (si) si.value = state.query;
    state.page = 1;
    renderGrid();
    const bar = $("#section-projects");
    if (bar) window.scrollTo({ top: bar.offsetTop - 70, behavior: "smooth" });
}
function getFilteredSortedData() {
    let data = [...sampleProjects];
    const q = state.query.toLowerCase();
    if (q) {
        data = data.filter((p) => {
            const inTags = p.tags.some((t) => t.toLowerCase().includes(q));
            return (
                p.title.toLowerCase().includes(q) ||
                p.desc?.toLowerCase().includes(q) ||
                p.creator.toLowerCase().includes(q) ||
                inTags
            );
        });
    }
    if (state.sort === "latest") {
        data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (state.sort === "popular") {
        data.sort(
            (a, b) =>
                (b.likes || 0) +
                (b.views || 0) * 0.05 -
                ((a.likes || 0) + (a.views || 0) * 0.05)
        );
    } else if (state.sort === "title") {
        data.sort((a, b) => a.title.localeCompare(b.title, "ko"));
    }
    return data;
}
function createHomeCard(p) {
    const el = document.createElement("article");
    el.className = "card";
    el.innerHTML = `
    <figure class="card-media skeleton"><img src="${p.cover}" alt="${
        p.title
    } 대표 이미지" loading="lazy"/></figure>
    <div class="card-body">
      <h3 class="card-title">${p.title}</h3>
      <div class="card-meta"><span>by ${
          p.creator
      }</span><span aria-hidden="true">•</span><span>${formatDate(
        p.createdAt
    )}</span><span aria-hidden="true">•</span><span>❤ ${formatNumber(
        p.likes
    )}</span></div>
      <div class="card-tags">${p.tags
          .slice(0, MAX_TAGS_PER_CARD)
          .map((t) => `<span class="tag-badge">${t}</span>`)
          .join("")}</div>
    </div>`;
    const img = $("img", el);
    img?.addEventListener("load", () =>
        $(".card-media", el)?.classList?.remove("skeleton")
    );
    return el;
}
function renderGrid(append = false) {
    if (document.body.dataset.page === "detail")
        return renderGridDetail(append);
    const grid = $("#grid");
    if (!grid) return;
    grid.setAttribute("aria-busy", "true");
    if (!append) grid.innerHTML = "";
    const all = getFilteredSortedData();
    const end = state.page * state.pageSize;
    const slice = all.slice(0, end);
    if (slice.length === 0) {
        grid.innerHTML = `<p class="note">조건에 맞는 포트폴리오가 없어요. 검색어를 변경해보세요.</p>`;
        $("#btnLoadMore")?.classList.add("hidden");
    } else {
        slice.forEach((p) => grid.appendChild(createHomeCard(p)));
        $("#btnLoadMore")?.classList.toggle("hidden", end >= all.length);
    }
    grid.setAttribute("aria-busy", "false");
}

/* ===== Detail: 정렬 드롭다운 ===== */
function initSortDropdown() {
    if (document.body.dataset.page !== "detail") return;
    const sortChip = $("#sortChip"),
        sortToggle = $("#sortToggle"),
        sortDropdown = $("#sortDropdown"),
        sortList = $("#sortList"),
        sortLabel = $("#sortLabel");
    if (!sortChip || !sortToggle || !sortDropdown || !sortList || !sortLabel)
        return;
    sortToggle.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        const isOpen = sortChip.classList.contains("open");
        sortChip.classList.toggle("open", !isOpen);
        sortDropdown.style.display = isOpen ? "none" : "block";
        sortToggle.setAttribute("aria-expanded", String(!isOpen));
    });
    sortList.addEventListener("click", (e) => {
        if (e.target.tagName === "LI") {
            state.sort = e.target.dataset.sort;
            sortLabel.textContent = e.target.textContent;
            sortChip.classList.remove("open");
            sortDropdown.style.display = "none";
            sortToggle.setAttribute("aria-expanded", "false");
            state.page = 1;
            renderGridDetail(false);
        }
    });
    document.addEventListener(
        "click",
        (e) => {
            if (!e.target.closest("#sortChip")) {
                sortChip.classList.remove("open");
                sortDropdown.style.display = "none";
                sortToggle.setAttribute("aria-expanded", "false");
            }
        },
        { capture: true }
    );
}

/* ===== Detail: 멀티셀렉트 ===== */
function openDropdown(ms) {
    ms.classList.add("open");
    ms.querySelector(".ms-dropdown").style.display = "block";
    ms.querySelector(".ms-trigger")?.setAttribute("aria-expanded", "true");
    const search = ms.querySelector("input[type='search']");
    if (search) setTimeout(() => search.focus({ preventScroll: true }), 30);
}
function closeDropdown(ms) {
    ms.classList.remove("open");
    ms.querySelector(".ms-dropdown").style.display = "none";
    ms.querySelector(".ms-trigger")?.setAttribute("aria-expanded", "false");
}
function updateFilterSummary() {
    const filterSummary = $("#filterSummary"),
        langPills = $("#selectedTechLang"),
        toolsPills = $("#selectedTechTools");
    if (!filterSummary || !langPills || !toolsPills) return;
    const hasSelection =
        langPills.children.length > 0 || toolsPills.children.length > 0;
    filterSummary.hidden = !hasSelection;
}
function initBlock(
    rootId,
    searchId,
    listId,
    pillsId,
    clearId,
    applyId,
    OPTIONS,
    key
) {
    if (document.body.dataset.page !== "detail") return;
    const root = $("#" + rootId);
    if (!root) return;
    const trig = root.querySelector(".ms-trigger"),
        dd = root.querySelector(".ms-dropdown"),
        search = $("#" + searchId),
        list = $("#" + listId),
        pills = $("#" + pillsId),
        btnClr = $("#" + clearId),
        btnApp = $("#" + applyId);
    closeDropdown(root);
    trig.setAttribute("aria-haspopup", "listbox");

    const newTrig = trig.cloneNode(true);
    trig.parentNode.replaceChild(newTrig, trig);
    newTrig.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        $$(".multiselect.open").forEach((ms) => {
            if (ms !== root) closeDropdown(ms);
        });
        $$(".chip.sort.open").forEach((chip) => chip.classList.remove("open"));
        root.classList.contains("open")
            ? closeDropdown(root)
            : openDropdown(root);
    });

    let selected = [];
    function renderList(filter = "") {
        list.innerHTML = "";
        OPTIONS.filter((o) =>
            o.toLowerCase().includes(filter.toLowerCase())
        ).forEach((opt) => {
            const li = document.createElement("li");
            li.textContent = opt;
            if (selected.includes(opt)) {
                li.style.fontWeight = "600";
                li.style.backgroundColor = "var(--primary-ink)";
                li.style.color = "var(--primary)";
            }
            li.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                selected = selected.includes(opt)
                    ? selected.filter((s) => s !== opt)
                    : [...selected, opt];
                renderList(search ? search.value : "");
                renderPills();
                updateFilterSummary();
            });
            list.appendChild(li);
        });
    }
    function renderPills() {
        pills.innerHTML = "";
        selected.forEach((s) => {
            const pill = document.createElement("span");
            pill.className = "pill";
            pill.innerHTML = `${s} <button type="button" aria-label="삭제">&times;</button>`;
            pill.querySelector("button").addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                selected = selected.filter((x) => x !== s);
                renderList(search ? search.value : "");
                renderPills();
                updateFilterSummary();
            });
            pills.appendChild(pill);
        });
    }
    search?.addEventListener("input", (e) => renderList(e.target.value));
    search?.addEventListener("click", (e) => e.stopPropagation());
    btnClr?.addEventListener("click", (e) => {
        e.stopPropagation();
        selected = [];
        renderList("");
        renderPills();
        updateFilterSummary();
    });
    btnApp?.addEventListener("click", (e) => {
        e.stopPropagation();
        state.filters[key] = [...selected];
        const all = [...state.filters.lang, ...state.filters.tools];
        state.query = all.join(" ");
        state.page = 1;
        renderGridDetail(false);
        closeDropdown(root);
        updateFilterSummary();
    });
    dd.addEventListener("click", (e) => e.stopPropagation());
    pills.addEventListener("click", (e) => e.stopPropagation());

    renderList();
    renderPills();
    updateFilterSummary();
}

/* ===== Detail: 데이터/렌더 ===== */
function getDetailData() {
    let data = sampleProjects.slice();
    // 멀티셀렉트 키워드 AND 필터
    const keywords = (state.query || "")
        .split(" ")
        .map((s) => s.trim())
        .filter(Boolean);
    if (keywords.length) {
        data = data.filter((p) =>
            keywords.every((k) =>
                p.tags.map((t) => t.toLowerCase()).includes(k.toLowerCase())
            )
        );
    }
    // 정렬
    if (state.sort === "latest") {
        data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (state.sort === "popular") {
        const score = (x) => (x.likes || 0) + (x.views || 0) * 0.05;
        data.sort((a, b) => score(b) - score(a));
    } else if (state.sort === "title") {
        data.sort((a, b) => a.title.localeCompare(b.title, "ko"));
    }
    return data;
}

function createShotCard(p) {
    const card = document.createElement("article");
    card.className = "shot-card";

    card.innerHTML = `
    <a class="shot-thumb" href="${p.link || "#"}" aria-label="${
        p.title
    }" target="_blank" rel="noopener">
      <img src="${p.cover}" alt="${p.title}">
    </a>

    <div class="shot-caption">
      <h3 class="shot-title" title="${p.title}">${p.title}</h3>
      <div class="shot-stats">
        <span class="stat">👁 ${formatNumber(p.views || 0)}</span>
        <span class="stat">❤️ ${formatNumber(p.likes || 0)}</span>
      </div>
    </div>
  `;
    return card;
}

function renderGridDetail(append = false) {
    const grid = $("#grid");
    if (!grid) return;
    grid.setAttribute("aria-busy", "true");
    if (!append) grid.innerHTML = "";
    const all = getDetailData();
    const end = state.page * state.pageSize; // detail에서는 12로 세팅
    const slice = all.slice(0, end);
    if (slice.length === 0) {
        grid.innerHTML = `<p class="note">조건에 맞는 포트폴리오가 없어요. 검색어나 필터를 조정해보세요.</p>`;
    } else {
        slice.forEach((p) => grid.appendChild(createShotCard(p)));
    }
    const moreBtn = $("#btnLoadMore");
    if (moreBtn) moreBtn.classList.toggle("hidden", end >= all.length);
    grid.setAttribute("aria-busy", "false");
}

/* ===== 공통: 더보기 / 맨위로 / 캐러셀 ===== */
function attachLoadMore() {
    const btn = $("#btnLoadMore");
    if (!btn) return;
    btn.addEventListener("click", () => {
        state.page += 1;
        if (document.body.dataset.page === "detail") renderGridDetail(true);
        else renderGrid(true);
    });
}
function attachToTop() {
    const fab = $("#btnToTop");
    if (!fab) return;
    window.addEventListener("scroll", () =>
        fab.classList.toggle("hidden", window.scrollY <= 600)
    );
    fab.addEventListener("click", () =>
        window.scrollTo({ top: 0, behavior: "smooth" })
    );
}
function initCarouselAutoplay() {
    if (document.body.dataset.page !== "detail") return;
    const track = $("#carousel");
    if (!track) return;
    let timer = null;
    const step = () => {
        track.scrollLeft += 2;
        if (track.scrollLeft + track.clientWidth >= track.scrollWidth - 1)
            track.scrollLeft = 0;
    };
    const start = () => {
        if (!timer) timer = setInterval(step, 20);
    };
    const stop = () => {
        if (timer) {
            clearInterval(timer);
            timer = null;
        }
    };
    start();
    track.addEventListener("mouseenter", stop);
    track.addEventListener("mouseleave", start);
}

/* ===== Detail 초기화 ===== */
function attachDetailPage() {
    if (document.body.dataset.page !== "detail") return;

    // 정렬 드롭다운
    initSortDropdown();

    // 멀티셀렉트 옵션
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
        LANG_OPTS,
        "lang"
    );
    initBlock(
        "techSelectTools",
        "techSearchTools",
        "techListTools",
        "selectedTechTools",
        "clearTechTools",
        "applyTechTools",
        TOOL_OPTS,
        "tools"
    );

    // 전체 초기화 버튼(있으면)
    const reset = $("#resetFilters");
    if (reset) {
        reset.addEventListener("click", (e) => {
            e.preventDefault();
            $$(".multiselect.open").forEach((ms) => closeDropdown(ms));
            $$(".chip.sort.open").forEach((chip) =>
                chip.classList.remove("open")
            );
            $("#clearTechLang")?.click();
            $("#clearTechTools")?.click();
            state.filters = { lang: [], tools: [] };
            state.query = "";
            state.page = 1;
            renderGridDetail(false);
            updateFilterSummary();
        });
    }

    // 캐러셀, 맨위로
    initCarouselAutoplay();
}

/* ===== 초기화 ===== */
function init() {
    initHeader();
    fitInputToPlaceholder();
    window.addEventListener("resize", fitInputToPlaceholder);

    const page = document.body.dataset.page;

    if (page === "home") {
        state.pageSize = 9;
        attachToolbarHandlers();
        attachLoadMore();
        attachToTop();
        const initialQ = getQueryParam("q");
        if (initialQ) {
            const ss = $("#sortSelect");
            if (ss) ss.value = "latest";
            applyQuery(initialQ);
        } else {
            renderGrid(false);
        }
    }

    if (page === "detail") {
        state.pageSize = 12; // ✅ 첫 로드 12개
        attachDetailPage();
        attachLoadMore();
        attachToTop();
        buildMarqueeFromProjects();
        renderGridDetail(false); // ✅ 드리블 스타일 렌더링
    }
}

document.addEventListener("DOMContentLoaded", init);

function buildMarqueeFromProjects() {
    const viewport = document.getElementById("carouselMarquee");
    const track = document.getElementById("marqueeTrack");
    if (!viewport || !track) return;

    // 1) 데이터 → DOM
    const items = sampleProjects.slice(0, 12); // 원하는 개수만큼
    const makeNode = (p) => {
        const a = document.createElement("a");
        a.className = "marquee-item";
        a.href = p.link || "#";
        a.target = "_blank";
        a.rel = "noopener";
        a.innerHTML = `
      <div class="mi-thumb"><img src="${p.cover}" alt="${p.title}"></div>
      <div class="mi-title">${p.title}</div>
    `;
        return a;
    };

    track.innerHTML = "";
    items.forEach((p) => track.appendChild(makeNode(p))); // 원본
    items.forEach((p) => track.appendChild(makeNode(p))); // 복제 1세트(무한 루프용)

    // 2) 속도/거리 계산 → CSS 변수로 주입
    const updateMetrics = () => {
        // 전체를 2세트로 만들었으므로 절반 길이가 실제 루프 거리
        const halfWidth = track.scrollWidth / 2;
        const pxPerSec = 80; // 속도(px/s) — 60~120 사이로 취향 조절
        const duration = halfWidth / pxPerSec;

        track.style.setProperty("--marquee-distance", `${halfWidth}px`);
        track.style.setProperty("--marquee-duration", `${duration}s`);

        // 애니메이션 리셋(리사이즈 시 깔끔하게)
        track.style.animation = "none";
        // reflow
        void track.offsetWidth;
        track.style.animation = "";
    };

    requestAnimationFrame(updateMetrics);
    window.addEventListener("resize", updateMetrics, { passive: true });

    // 3) hover 제어(추가 보장)
    viewport.addEventListener(
        "mouseenter",
        () => (track.style.animationPlayState = "paused")
    );
    viewport.addEventListener(
        "mouseleave",
        () => (track.style.animationPlayState = "running")
    );
}
