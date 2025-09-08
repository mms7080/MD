package com.example.demo.folio;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Project;
import com.example.demo.TeamMember;

import java.util.*;

@Controller
public class PortfoliosController {


    private static final List<TeamMember> team1 = List.of(
    new TeamMember("PAWPLE","김민정", "팀장) 프론트+백엔드", List.of("회원", "스토어", "지도", "유기동물")),
    new TeamMember("PAWPLE","노홍래", "프론트", List.of("건강관리", "관리자 페이지")),
    new TeamMember("PAWPLE","김훈희", "프론트+백엔드", List.of("관리자 페이지", "스토어", "지도")),
    new TeamMember("PAWPLE","윤휘원","백엔드",List.of("건강관리")),
    new TeamMember("PAWPLE","최소현","프론트+백엔드",List.of("커뮤니티","스토어")),
    new TeamMember("PAWPLE","송용근","프론트",List.of("회원관리","커뮤니티"))
    
    // 필요한 만큼 추가
);

    private static final List<TeamMember> team2 = List.of(
        new TeamMember("Filmora","길광재","팀장",List.of("로그인","회원가입","홈페이지", "상세페이지,사용자 인증 시스템 구축예매의 시작점","영화 상세페이지 구현")),
        new TeamMember("Filmora","송준회","백엔드",List.of("지역 상영관,시간,좌석 등 예매 핵심 데이터 API","예매 데이터 구조 설계 및 API 구축","실시간 좌석 정보 제공 및 중복 예매 방지")),
        new TeamMember("Filmora","박범수","백엔드",List.of("이벤트, 공지","결제,스토어,쿠폰", "예매 알림,안정적인 결제 시스템 연동","서비스 운영을 위한 관리자 페이지 및 사용자 소통 채널")),
        new TeamMember("Filmora","박채훈","프론트",List.of("영화 목록 페이지","영화관 정보 페이지","영화 목록 페이지", "영화관 정보 및 위치 안내 페이지 구현")),
        new TeamMember("Filmora","손종현","프론트",List.of("예매 페이지"," 좌석 선택 페이지","영화 결제 페이지 UI", "직관적인 단계별 예매 인터페이스 구현","시각적 좌석 선택 페이지 구현"))
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
                "https://kafolio.kr/static/requests/2025/7/9/boards/MTdkZjgyMzc1YjQ5ZDMzMzBjNDdmZjRmNjQzNTRiNWUwNWEyYmVkNDdjOGE1NDQ3ZGU3MDQwZGYzM2YzMjhiZQ==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/YzNlY2JhNDU4MGVkM2ZkYmU0ZDdlMDZlMGQxZGNmMzA1NGEyOGJjZTg5ODM5NDEyOGJlODQ2MjJkNWRmZDhlMw==.png"
            ),team1,"https://kafolio.kr/static/requests/2025/7/9/icon/MDg4NzVmYTM5MWYyOTE3OGQ3ZmY0NWZkYjA4YjQwZjg5NzAxOTdlYTlhZjhkZmY4MTU4NDgwMzViNzRlMGYxYg==.png",

            "https://pawple.kafolio.kr/","pawpleRe.zip"
    ));

    PROJECTS.put("5", new Project("5", "Filmora", "S.Han",
            List.of("Next.js","SpringBoot","React","JPA","VSCode"),
            2, "2025-08-22",
            "https://kafolio.kr/static/requests/2025/7/9/boards/NmI2....png",
            "영화 예매 사이트",
            List.of(
                "https://kafolio.kr/static/requests/2025/7/9/boards/ZTI0YmNkZTMxMjIzY2VkNmRlYmFkZDYyY2M4M2Q2NDJlNTE2MTdmZGUzZDE3MWVkMmE1ZTMxYTc0YjBmNWQ4Yw==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/YjZlODczYTY5ZjM2YjIxZGU0N2IxYjYwN2M0NTQ4ZGE5N2M2YTczYjkzNWYxMDJlYzY2ZTg3YzhjZjlmMGJkOA==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/MDc1NzdhZmQwZTdhODFjNjZkZDQ1ZjgwMmZmNjI1NmIwNWMzNzQwOWRlYmE4ZDQzNGEyYjk1OWM4ZGFkYzI3MA==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/OWZiZmZjZjI1ZTQyY2RkMmFiMmIxMmMxM2IzY2MzN2E0MWU5MzI5M2VlZjc3ODAyZDk0NjE1YTgwOTA3ZjExOQ==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/MzRlMTQwNjQxMWU4ZTQyYTg1YTA3NzYxOWY3NTMyNzYzYjA5MmEyZTllNWY2YTg0YzY3OTdlY2M0YjBhMzFjZg==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/MTM0MWM5NDVlODIxM2EyZmFjNmFlYjU2MDUzNzYxOTM1MWQ5ODE4ZTdkNjk3YTU5ZDc1NTc2MjlhNjc5ZDE4MQ==.png",
                "https://kafolio.kr/static/requests/2025/7/9/boards/ZDIwN2NjNDk1MjJjOTJiMjlkM2M3YjhiNjA4ZjllMmMzMjAzY2UxMzk5YTQ2YzkzNjMwY2FmNjA3NzRmZTc3NQ==.png"
            ),team2,"https://kafolio.kr/static/requests/2025/7/9/icon/YTNlOTI0MGZmM2IyYTcyZjRiOGE0NmVkMTA1Mzk2YzljMmRkYzJmMmVmZWNlMjFmMzRhMjQxYTVkYjE3YmEwNQ==.png"
            ,"https://filmora.kafolio.kr","Filmora.zip"
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
            ),team1,"https://kafolio.kr/static/requests/2025/3/27/icon/ZDZhMTIwNjYxMTgzMjYxMGJjYmViNDY1OTJjNzNlNGZmMmFiNjQ3NzJmOWY4MDJhYjI0MGYyZjhlNTBjYzliMQ==.png",
            "https://modeme.kafolio.kr","ModeMe.zip"
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
