// ===== 공통 유틸 =====
const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

const state = {
  theme: localStorage.getItem("theme") || "light",
  query: "",
  sort: "latest",
  page: 1,
  pageSize: 9, // Home 기본 9, Detail에서는 5로 조정
};

const MAX_TAGS_PER_CARD = 3;

// 데모 데이터
const sampleProjects = [
  { id:"p1", title:"핀테크 대시보드 리디자인", creator:"J. Kim",
    tags:["React","TypeScript","Tailwind","Dashboard","DesignSystem"], likes:128,
    createdAt:"2025-08-15", cover:"https://picsum.photos/seed/fin/800/500",
    desc:"금융 데이터 시각화 중심의 관리자 대시보드 리디자인 프로젝트.", link:"https://example.com/fin" },
  { id:"p2", title:"헬스케어 습관 트래커 웹앱", creator:"M. Lee",
    tags:["Next.js","React","Zustand","PWA","Analytics"], likes:203,
    createdAt:"2025-06-01", cover:"https://picsum.photos/seed/health/800/500",
    desc:"개인 맞춤 습관 형성 UX를 반영한 웹앱. 오프라인 동기화 지원.", link:"https://example.com/health" },
  { id:"p3", title:"브랜드 사이트 – Cafe VERT", creator:"A. Park",
    tags:["Astro","Tailwind","SSR","SEO","Animation"], likes:96,
    createdAt:"2025-07-10", cover:"https://picsum.photos/seed/brand/800/500",
    desc:"친환경 카페 브랜드의 모션 인터랙션과 SEO 최적화.", link:"https://example.com/vert" },
  { id:"p4", title:"3D 제품 뷰어(웹)", creator:"D. Choi",
    tags:["Three.js","WebGL","React","Product","Viewer"], likes:310,
    createdAt:"2025-05-20", cover:"https://picsum.photos/seed/3d/800/500",
    desc:"브라우저에서 회전/재질 변경 가능한 3D 제품 뷰어.", link:"https://example.com/3d" },
  { id:"p5", title:"에듀테크 랜딩", creator:"S. Han",
    tags:["Next.js","TypeScript","A/B Test","Accessibility","Landing"], likes:77,
    createdAt:"2025-08-22", cover:"https://picsum.photos/seed/edu/800/500",
    desc:"전환율 상승을 위한 메시지 구조와 접근성 개선.", link:"https://example.com/edu" },
  { id:"p6", title:"매거진 타이포 시스템(웹)", creator:"B. Yoo",
    tags:["Vue","Nuxt","SSR","Typography","WebFont"], likes:65,
    createdAt:"2025-02-18", cover:"https://picsum.photos/seed/type/800/500",
    desc:"가독성 중심의 모듈형 타이포그래피 시스템.", link:"https://example.com/type" },
  { id:"p7", title:"여행 계획 웹 서비스 UX 개편", creator:"H. Jung",
    tags:["React","Redux","REST","i18n","Admin"], likes:181,
    createdAt:"2025-04-02", cover:"https://picsum.photos/seed/travel/800/500",
    desc:"검색→저장→공유 플로우 개선으로 예약 전환율 향상.", link:"https://example.com/travel" },
  { id:"p8", title:"러닝 코치 대시보드", creator:"R. Kwon",
    tags:["Svelte","Node.js","Express","MongoDB","Realtime"], likes:142,
    createdAt:"2025-03-12", cover:"https://picsum.photos/seed/run/800/500",
    desc:"센서 데이터 실시간 시각화와 사용자 피드백 모듈.", link:"https://example.com/run" },
  { id:"p9", title:"컴포넌트 문서화 시스템", creator:"Y. Song",
    tags:["Storybook","React","Testing","DesignSystem","CI/CD"], likes:54,
    createdAt:"2025-07-25", cover:"https://picsum.photos/seed/illu/800/500",
    desc:"디자인 시스템 문서화와 테스트 자동화 파이프라인.", link:"https://example.com/illu" },
];

const formatDate = iso => new Date(iso).toLocaleDateString('ko-KR');

// ===== 공통 헤더/테마 =====
function applyTheme(theme){
  document.documentElement.setAttribute("data-theme", theme);
  const btn = $("#btnTheme"); if (btn) btn.setAttribute("aria-pressed", theme === "dark");
}
function toggleTheme(){
  state.theme = state.theme === "dark" ? "light" : "dark";
  localStorage.setItem("theme", state.theme);
  applyTheme(state.theme);
}
function initHeader(){
  const themeBtn = $("#btnTheme");
  if (themeBtn) themeBtn.addEventListener("click", toggleTheme);
  applyTheme(state.theme);

  const navToggle = $(".nav-toggle");
  const navList = $("#primary-menu");
  if (navToggle && navList){
    navToggle.addEventListener("click", ()=>{
      const open = navList.classList.toggle("open");
      navToggle.setAttribute("aria-expanded", open);
    });
  }

  const year = $("#year");
  if (year) year.textContent = new Date().getFullYear();
}

// ===== URL 파라미터 =====
function getQueryParam(name){
  const params = new URLSearchParams(location.search);
  return params.get(name);
}

// ===== placeholder 길이에 맞춰 input/컨테이너 너비 조정 =====
function measureTextWidth(text, font){
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  ctx.font = font;
  return Math.ceil(ctx.measureText(text).width);
}
function getFontFor(el){
  const s = getComputedStyle(el);
  return `${s.fontStyle} ${s.fontVariant} ${s.fontWeight} ${s.fontSize} ${s.fontFamily}`;
}
function fitInputToPlaceholder(){
  const heroInput = $("#searchInputHero");
  const listInput = $("#searchInput");

  [heroInput, listInput].forEach(input=>{
    if (!input) return;
    if (window.matchMedia("(max-width:640px)").matches){
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
    if (input.id === "searchInputHero"){
      const form = $("#heroSearch");
      const btn = form?.querySelector("button");
      if (form && btn){
        const gap = 10;
        const total = input.getBoundingClientRect().width + btn.getBoundingClientRect().width + gap;
        form.style.width = Math.min(total, max + btn.offsetWidth + gap) + "px";
      }
    }
  });
}

// ===== 검색/정렬 (Home 전용) =====
function attachToolbarHandlers(){
  const input = $("#searchInput");
  if (input){
    input.addEventListener("input", (e)=>{
      state.query = e.target.value.trim();
      state.page = 1;
      renderGrid();
    });
  }

  const heroForm = $("#heroSearch");
  if (heroForm){
    heroForm.addEventListener("submit", (e)=>{
      e.preventDefault();
      const q = $("#searchInputHero").value.trim();
      applyQuery(q);
    });
  }

  $$(".hero-tags .tag").forEach(tagBtn=>{
    tagBtn.addEventListener("click", ()=>{
      applyQuery(tagBtn.dataset.tag);
    });
  });

  const sort = $("#sortSelect");
  if (sort){
    sort.addEventListener("change", (e)=>{
      state.sort = e.target.value;
      state.page = 1;
      renderGrid();
    });
  }

  const clear = $("#btnClear");
  if (clear){
    clear.addEventListener("click", ()=>{
      state.query = ""; state.sort = "latest"; state.page = 1;
      const si = $("#searchInput"); if (si) si.value = "";
      const ss = $("#sortSelect"); if (ss) ss.value = "latest";
      renderGrid();
    });
  }
}

function applyQuery(q){
  state.query = q || "";
  const si = $("#searchInput");
  if (si) si.value = state.query;
  state.page = 1;
  renderGrid();
  const bar = $("#section-projects");
  if (bar) window.scrollTo({ top: bar.offsetTop - 70, behavior: "smooth" });
}

// ===== 데이터 처리 =====
function getFilteredSortedData(){
  let data = [...sampleProjects];
  const q = state.query.toLowerCase();
  if (q){
    data = data.filter(p => {
      const inTags = p.tags.some(t => t.toLowerCase().includes(q));
      return (
        p.title.toLowerCase().includes(q) ||
        p.desc.toLowerCase().includes(q) ||
        p.creator.toLowerCase().includes(q) ||
        inTags
      );
    });
  }

  if (state.sort === "latest"){
    data.sort((a,b)=> new Date(b.createdAt) - new Date(a.createdAt));
  } else if (state.sort === "popular"){
    data.sort((a,b)=> b.likes - a.likes);
  } else if (state.sort === "title"){
    data.sort((a,b)=> a.title.localeCompare(b.title, "ko"));
  }
  return data;
}

// 태그 렌더링: 최대 3개 + 더보기 개수
function renderTagBadges(tags){
  const show = tags.slice(0, MAX_TAGS_PER_CARD);
  const hidden = Math.max(0, tags.length - MAX_TAGS_PER_CARD);
  const html = show.map(t => `<span class="tag-badge" aria-label="태그 ${t}">${t}</span>`).join("");
  const more = hidden ? `<span class="tag-badge more" aria-label="추가 태그 ${hidden}개">+${hidden}</span>` : "";
  return html + more;
}

// ===== 카드 렌더링 =====
function createCard(project){
  const el = document.createElement("article");
  el.className = "card";
  el.setAttribute("data-id", project.id);
  el.setAttribute("tabindex", "0");
  el.innerHTML = `
    <figure class="card-media skeleton">
      <img src="${project.cover}" alt="${project.title} 대표 이미지" loading="lazy" />
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
      <a class="btn ghost" href="${project.link}" target="_blank" rel="noopener">원본</a>
    </div>
  `;

  const img = $("img", el);
  img.addEventListener("load", ()=> $(".card-media", el).classList.remove("skeleton"));

  $(".view", el).addEventListener("click", ()=> openModal(project));
  el.addEventListener("keydown", (e)=>{ if(e.key==="Enter"){ openModal(project); } });

  return el;
}

function renderGrid(){
  const grid = $("#grid");
  if (!grid) return;
  grid.setAttribute("aria-busy","true");
  grid.innerHTML = "";

  const all = getFilteredSortedData();
  const end = state.page * state.pageSize;
  const slice = all.slice(0, end);

  if (slice.length === 0){
    grid.innerHTML = `<p class="note">조건에 맞는 포트폴리오가 없어요. 검색어를 변경해보세요.</p>`;
    const moreBtn = $("#btnLoadMore"); if (moreBtn) moreBtn.classList.add("hidden");
  } else {
    slice.forEach(p => grid.appendChild(createCard(p)));
    const moreBtn = $("#btnLoadMore");
    if (moreBtn) moreBtn.classList.toggle("hidden", end >= all.length);
  }

  grid.setAttribute("aria-busy","false");
}

// 더 보기
function attachLoadMore(){
  const btn = $("#btnLoadMore");
  if (!btn) return;
  btn.addEventListener("click", ()=>{
    state.page++;
    renderGrid();
  });
}

// 상단 이동
function attachToTop(){
  const fab = $("#btnToTop");
  if (!fab) return;
  window.addEventListener("scroll", ()=>{
    const show = window.scrollY > 600;
    fab.classList.toggle("hidden", !show);
  });
  fab.addEventListener("click", ()=> window.scrollTo({ top: 0, behavior: "smooth" }));
}

// 모달
function openModal(project){
  $("#modalImage")?.setAttribute("src", project.cover);
  $("#modalImage")?.setAttribute("alt", `${project.title} 상세 이미지`);
  $("#modalTitle") && ($("#modalTitle").textContent = project.title);
  $("#modalDesc") && ($("#modalDesc").textContent = project.desc);
  const tags = $("#modalTags"); if (tags) tags.innerHTML = renderTagBadges(project.tags);
  const link = $("#modalLink"); if (link) link.href = project.link;

  const dlg = $("#modal");
  if (dlg && typeof dlg.showModal === "function") dlg.showModal();
  else if (dlg) dlg.setAttribute("open", "");
}
function attachModal(){
  const dlg = $("#modal");
  if (!dlg) return;
  $(".modal-close", dlg).addEventListener("click", ()=> dlg.close());
  dlg.addEventListener("click", (e)=>{
    const card = $(".modal-card", dlg);
    if (e.target === dlg && !card.contains(e.target)) dlg.close();
  });
  document.addEventListener("keydown", (e)=>{ if (e.key==="Escape" && dlg.open) dlg.close(); });
}

// ===== Detail 페이지 전용 =====
function attachDetailPage(){
  // detail에서 기술 클릭 -> /home?q=TECH 로 이동
  $$(".tech").forEach(btn=>{
    btn.addEventListener("click", ()=>{
      const tech = btn.dataset.tech;
      const url = new URL("/home", location.origin); // ✅ /home 으로 변경
      url.searchParams.set("q", tech);
      location.href = url.toString();
    });
    btn.setAttribute("aria-label", `${btn.textContent}로 검색`);
  });
}


// ===== 초기화 =====
function init(){
  initHeader();
  fitInputToPlaceholder();
  window.addEventListener("resize", fitInputToPlaceholder);

  const page = document.body.dataset.page;

  if (page === "home"){
    // Home: 9개씩
    state.pageSize = 9;
    attachToolbarHandlers();
    attachLoadMore();
    attachToTop();
    attachModal();

    const initialQ = getQueryParam("q");
    if (initialQ) {
      const ss = $("#sortSelect"); if (ss) ss.value = "latest";
      applyQuery(initialQ);
    } else {
      renderGrid();
    }
  }

  if (page === "detail"){
    // Detail: 5개씩, 기술 리스트 + 그리드 표시
    state.pageSize = 9;
    attachDetailPage();
    attachLoadMore();
    attachToTop();
    renderGrid();
  }
}

document.addEventListener("DOMContentLoaded", init);
