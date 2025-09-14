document.addEventListener('DOMContentLoaded', function() {
    const grid = document.getElementById('folio-grid');
    if (!grid) return;

    const mockData = {
        items: [
            { folioId: "e2f4e5a6", user: { userName: "송준회" }, skills: ["Java", "Spring Boot", "JPA"], thumbnail: "https://picsum.photos/seed/dev1/400/250" },
            { folioId: "a1b2c3d4", user: { userName: "이홍시" }, skills: ["Python", "Django", "React"], thumbnail: "https://picsum.photos/seed/dev2/400/250" },
            { folioId: "f0e9d8c7", user: { userName: "박개발" }, skills: ["TypeScript", "Next.js", "GraphQL"], thumbnail: "https://picsum.photos/seed/dev3/400/250" }
        ]
    };

    grid.innerHTML = '';
    mockData.items.forEach(folio => {
        const skillsHtml = folio.skills.map(skill => `<span class="tag">${skill}</span>`).join('');
        grid.insertAdjacentHTML('beforeend', `
            <div class="card">
                <a href="/folios/detail/${folio.folioId}" class="card-link">
                    <div class="media"><img src="${folio.thumbnail}" alt="${folio.user.userName} 프로필"></div>
                    <div class="body">
                        <h3 class="title">${folio.user.userName}</h3>
                        <div class="tags">${skillsHtml}</div>
                    </div>
                </a>
            </div>
        `);
    });
});