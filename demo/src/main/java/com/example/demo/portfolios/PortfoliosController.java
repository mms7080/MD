package com.example.demo.portfolios;

import java.util.List;

import org.springframework.stereotype.Controller;

@Controller
public class PortfoliosController {
    
    
    private static final List<TeamMemberEntity> team1 = List.of(
    new TeamMemberEntity("PAWPLE","김민정", "팀장) 프론트+백엔드", List.of("회원", "스토어", "지도", "유기동물")),
    new TeamMemberEntity("PAWPLE","노홍래", "프론트", List.of("건강관리", "관리자 페이지")),
    new TeamMemberEntity("PAWPLE","김훈희", "프론트+백엔드", List.of("관리자 페이지", "스토어", "지도")),
    new TeamMemberEntity("PAWPLE","윤휘원","백엔드",List.of("건강관리")),
    new TeamMemberEntity("PAWPLE","최소현","프론트+백엔드",List.of("커뮤니티","스토어")),
    new TeamMemberEntity("PAWPLE","송용근","프론트",List.of("회원관리","커뮤니티"))
    
    // 필요한 만큼 추가
);

private static final List<TeamMemberEntity> team2 = List.of(
    new TeamMemberEntity("Filmora","길광재","팀장",List.of("로그인","회원가입","홈페이지", "상세페이지,사용자 인증 시스템 구축예매의 시작점","영화 상세페이지 구현")),
    new TeamMemberEntity("Filmora","송준회","백엔드",List.of("지역 상영관,시간,좌석 등 예매 핵심 데이터 API","예매 데이터 구조 설계 및 API 구축","실시간 좌석 정보 제공 및 중복 예매 방지")),
    new TeamMemberEntity("Filmora","박범수","백엔드",List.of("이벤트, 공지","결제,스토어,쿠폰", "예매 알림,안정적인 결제 시스템 연동","서비스 운영을 위한 관리자 페이지 및 사용자 소통 채널")),
    new TeamMemberEntity("Filmora","박채훈","프론트",List.of("영화 목록 페이지","영화관 정보 페이지","영화 목록 페이지", "영화관 정보 및 위치 안내 페이지 구현")),
    new TeamMemberEntity("Filmora","손종현","프론트",List.of("예매 페이지"," 좌석 선택 페이지","영화 결제 페이지 UI", "직관적인 단계별 예매 인터페이스 구현","시각적 좌석 선택 페이지 구현"))

);

    private static final Map<String,PortfoliosEntity> = new LinkedHashMap<>();

        static{
            PortfoliosEntity.put("1",new PortfoliosEntity("1","Pawple","H.kim",
                                List.of("React","Next.js","SpringBoot","JPA","LomBok"),10,"2025-08-15",
                                "https://kafolio.kr/static/requests/2025/7/9/boards/ZWY1....png","반려동물 건강 관리 웹사이트 프로젝트",
                                List.of("https://kafolio.kr/static/requests/2025/7/9/boards/ZWY1ZDE0NjY2MjM0Yzg5MDY0MDAzZWVkZTM5Y2E1ZWJjNmYyMDg0YjI0MDc1ODY5OTg5OTljMTY1YTBlODI4Nw==.png",
                                        "https://kafolio.kr/static/requests/2025/7/9/boards/NmI2MTk3MzEwZTFkODBmMTRhMTI1NWRlMmE1ODI2NWZkZTg0OWJkNzJlOTM5ZDg3MGFlMjc5Yzc4NzViOThhMw==.png",
                                        "https://kafolio.kr/static/requests/2025/7/9/boards/YWFmYmFhOTM1OTI1MWVjMzU1N2Q2NmZjYTc5OTg0MjliYTI4YzdmZDRhMDE0YWNjMTM1OTQzZjQ0ZGUxMDNmMg==.png",
                                        "https://kafolio.kr/static/requests/2025/7/9/boards/Y2RmNzg2ZmUyZjRlZDdjYjU5ZTVhMGFlZDBjZThhOTExZDgwMmFiZjRjMjRlNGFkMzE5ZTIzMDQ3NTk5ZWZhNg==.png",
                                        "https://kafolio.kr/static/requests/2025/7/9/boards/MTdkZjgyMzc1YjQ5ZDMzMzBjNDdmZjRmNjQzNTRiNWUwNWEyYmVkNDdjOGE1NDQ3ZGU3MDQwZGYzM2YzMjhiZQ==.png",
                                        "https://kafolio.kr/static/requests/2025/7/9/boards/YzNlY2JhNDU4MGVkM2ZkYmU0ZDdlMDZlMGQxZGNmMzA1NGEyOGJjZTg5ODM5NDEyOGJlODQ2MjJkNWRmZDhlMw==.png"
            ),team1,"https://kafolio.kr/static/requests/2025/7/9/icon/MDg4NzVmYTM5MWYyOTE3OGQ3ZmY0NWZkYjA4YjQwZjg5NzAxOTdlYTlhZjhkZmY4MTU4NDgwMzViNzRlMGYxYg==.png",

            "https://pawple.kafolio.kr/","pawpleRe.zip"
        ));
    
    
    
    }



}
