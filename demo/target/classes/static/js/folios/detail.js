// /js/folios/detail.js
(function () {
    // 서버에서 detail 페이지에 folioId를 모델로 안 주고 있다면,
    // URL에서 마지막 세그먼트를 id로 사용
    const pathParts = location.pathname.split("/").filter(Boolean);
    const folioId = pathParts[pathParts.length - 1];

    const $ = (sel) => document.querySelector(sel);
    const profileImg = $("#profile-image");
    const profileName = $("#profile-name");
    const profileIntro = $("#profile-job");
    const profileSkills = $("#profile-skills");

    const introText = $("#introduction-text");
    const projectList = $("#project-list");
    const gallery = $("#photo-gallery");

    function pill(text) {
        const s = document.createElement("span");
        s.className = "pill";
        s.textContent = text;
        return s;
    }

    function liLink(text, href) {
        const a = document.createElement("a");
        a.textContent = text;
        a.href = href || "#";
        const li = document.createElement("div");
        li.className = "project-item";
        li.appendChild(a);
        return li;
    }

    async function load() {
        const res = await fetch(`/api/folios/${folioId}`);
        if (!res.ok) {
            console.error("상세 로드 실패");
            return;
        }
        const d = await res.json();

        // 상단 프로필: 작성자 이름/스킬/한줄소개
        profileImg.src =
            d.thumbnail || "https://picsum.photos/seed/placeholder/300";
        profileName.textContent = d.title || "Untitled";
        profileIntro.textContent = (d.introduction || "").trim();

        profileSkills.innerHTML = "";
        (d.skills || [])
            .slice(0, 8)
            .forEach((s) => profileSkills.appendChild(pill(s)));

        // 소개 섹션
        introText.textContent = (d.introduction || "").trim();

        // 참여 포트폴리오 (있다면)
        projectList.innerHTML = "";
        (d.projects || []).forEach((p) => {
            projectList.appendChild(liLink(p.title, `/folios/detail/${p.id}`));
        });

        // Photos: 세로로 모두 출력
        gallery.innerHTML = "";
        (d.photos || []).forEach((url) => {
            const img = new Image();
            img.src = url;
            img.alt = "photo";
            img.loading = "lazy";
            img.className = "photo";
            gallery.appendChild(img);
        });
    }

    load();
})();
