package com.example.demo.folio;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Project;
import com.example.demo.TeamMember;

import java.util.*;

@Controller
public class PortfolioController {


    private static final List<TeamMember> team1 = List.of(
    new TeamMember("PAWPLE","김민정", "팀장) 프론트+백엔드", List.of("회원", "스토어", "지도", "유기동물")),
    new TeamMember("PAWPLE","노홍래", "프론트", List.of("건강관리", "관리자 페이지")),
    new TeamMember("PAWPLE","김훈희", "프론트+백엔드", List.of("관리자 페이지", "스토어", "지도")),
    new TeamMember("PAWPLE","윤휘원","백엔드",List.of("건강관리")),
    new TeamMember("PAWPLE","최소현","프론트+백엔드",List.of("커뮤니티","스토어")),
    new TeamMember("PAWPLE","송용근","프론트",List.of("회원관리","커뮤니티"))
    
    // 필요한 만큼 추가
);
    // 🔹 임시 더미 데이터 (DB 대신 메모리에 저장)
    private static final Map<String, Project> PROJECTS = new LinkedHashMap<>();

static {
    PROJECTS.put("1", new Project("1", "PAWPLE", "J. Kim",
            List.of("REACT","SPRINGBOOT","JPA","VSCode","LOMBOK"),
            10, "2025-08-15",
            "https://kafolio.kr/static/requests/2025/7/9/boards/ZWY1....png",
            "금융 데이터 시각화 중심의 관리자 대시보드 리디자인 프로젝트.",
            List.of(
                "https://kafolio.kr/static/requests/2025/7/9/boards/ZWY1ZDE0NjY2MjM0Yzg5MDY0MDAzZWVkZTM5Y2E1ZWJjNmYyMDg0YjI0MDc1ODY5OTg5OTljMTY1YTBlODI4Nw==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/NmI2MTk3MzEwZTFkODBmMTRhMTI1NWRlMmE1ODI2NWZkZTg0OWJkNzJlOTM5ZDg3MGFlMjc5Yzc4NzViOThhMw==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/YWFmYmFhOTM1OTI1MWVjMzU1N2Q2NmZjYTc5OTg0MjliYTI4YzdmZDRhMDE0YWNjMTM1OTQzZjQ0ZGUxMDNmMg==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/Y2RmNzg2ZmUyZjRlZDdjYjU5ZTVhMGFlZDBjZThhOTExZDgwMmFiZjRjMjRlNGFkMzE5ZTIzMDQ3NTk5ZWZhNg==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/MTdkZjgyMzc1YjQ5ZDMzMzBjNDdmZjRmNjQzNTRiNWUwNWEyYmVkNDdjOGE1NDQ3ZGU3MDQwZGYzM2YzMjhiZQ==.png"
            ),team1
    ));

    PROJECTS.put("2", new Project("2", "헬스케어 습관 트래커 웹앱", "M. Lee",
            List.of("Next.js","React","Zustand","PWA","Analytics"),
            2, "2025-06-01",
            "https://kafolio.kr/static/requests/2025/7/9/boards/NmI2....png",
            "개인 맞춤 습관 형성 UX를 반영한 웹앱. 오프라인 동기화 지원.",
            List.of(
                "https://picsum.photos/seed/health1/300/200",
                "https://picsum.photos/seed/health2/300/200",
                "https://picsum.photos/seed/health3/300/200"
            ),team1
    ));

    PROJECTS.put("3", new Project("3", "브랜드 사이트 – Cafe VERT", "A. Park",
            List.of("Astro","Tailwind","SSR","SEO","Animation"),
            2, "2025-07-10",
            "https://kafolio.kr/static/requests/2025/7/9/boards/YWFm....png",
            "친환경 카페 브랜드의 모션 인터랙션과 SEO 최적화.",
            List.of(
                "https://picsum.photos/seed/vert1/300/200",
                "https://picsum.photos/seed/vert2/300/200",
                "https://picsum.photos/seed/vert3/300/200"
            ),team1
    ));

        // ... 필요하면 4~9까지 추가
    }

    @GetMapping("/folio/{id}")
    public String getPortfolio(@PathVariable String id, Model model) {
        Project project = PROJECTS.get(id);
        if (project == null) {
            model.addAttribute("notFound", true);
        } else {
            model.addAttribute("project", project);
        }
        return "detail/portfolio-detail"; // templates/detail/portfolio-detail.html
    }
}
