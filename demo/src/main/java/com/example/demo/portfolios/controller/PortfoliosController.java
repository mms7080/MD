package com.example.demo.portfolios.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.service.PortfolioService;

@Controller
public class PortfoliosController {

    private final PortfolioService portfolioService;
    
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
    new TeamMemberEntity("Filmora","송준회","백엔드",List.of("지역 상영관,시간, 좌석 등 예매 핵심 데이터 API"," 예매 데이터 구조 설계 및 API 구축"," 실시간 좌석 정보 제공 및 중복 예매 방지")),
    new TeamMemberEntity("Filmora","박범수","백엔드",List.of("이벤트, 공지","결제,스토어,쿠폰", "예매 알림,안정적인 결제 시스템 연동","서비스 운영을 위한 관리자 페이지 및 사용자 소통 채널")),
    new TeamMemberEntity("Filmora","박채훈","프론트",List.of("영화 목록 페이지","영화관 정보 페이지","영화 목록 페이지", "영화관 정보 및 위치 안내 페이지 구현")),
    new TeamMemberEntity("Filmora","손종현","프론트",List.of("예매 페이지"," 좌석 선택 페이지","영화 결제 페이지 UI", "직관적인 단계별 예매 인터페이스 구현","시각적 좌석 선택 페이지 구현"))

);

private static final List<TeamMemberEntity> team3 = List.of(
    new TeamMemberEntity("ModeMe","박범수","팀장",List.of("회원가입")),
    new TeamMemberEntity("ModeMe","김정민","팀원",List.of("상세페이지")),
    new TeamMemberEntity("ModeMe","송용근","팀원",List.of("장바구니")),
    new TeamMemberEntity("ModeMe","손태웅","팀원",List.of("관리자페이지")),
    new TeamMemberEntity("ModeMe","정채빈","팀원",List.of("마이페이지")),
    new TeamMemberEntity("ModeMe","김은채","팀원",List.of("Q&A"))
);

private static final List<TeamMemberEntity> team4 = List.of(
    new TeamMemberEntity("OSPE","박범수","팀장",List.of("로그인","회원가입","마이페이지")),
    new TeamMemberEntity("OSPE","김정민","팀원",List.of("처방전 작성 및 출력")),
    new TeamMemberEntity("OSPE","송용근","팀원",List.of("의료이력 확인")),
    new TeamMemberEntity("OSPE","손태웅","팀원",List.of("의사와 환자간의 메시지")),
    new TeamMemberEntity("OSPE","정채빈","팀원",List.of("진료 예약 및 예약 조회")),
    new TeamMemberEntity("OSPE","김은채","팀원",List.of("Header","Footer","메인페이지","Q&A 게시판"))
);

private static final List<TeamMemberEntity> team5 = List.of(
    new TeamMemberEntity("NotePad","무명","팀장",List.of("회원가입","로그인","메모장"))
    
);

private static final List<TeamMemberEntity> team6 = List.of(
    new TeamMemberEntity("Planner for U","나호성","팀장",List.of("로그인","로그아웃","유저정보","방(생성,수정,삭제)","메모장")),
    new TeamMemberEntity("Planner for U","정민석","팀원",List.of("게스트(검색, 추가)","채팅","캘린더","계산기","캘린더","검색")),
    new TeamMemberEntity("Planner for U","이준오","팀원",List.of("메인화면","계산기","캘린더","스케쥴","방 생성 화면")),
    new TeamMemberEntity("Planner for U","이욱재","팀원",List.of("방 메인화면","메모장","캘린더","채팅","방 정보수정화면")),
    new TeamMemberEntity("Planner for U","조승우","팀원",List.of("Q&A"))
);

private static final List<TeamMemberEntity> team7 = List.of(
    new TeamMemberEntity("취미존중","김의령","팀장",List.of("메인 페이지","클래스 등록","클래스 수정","마이페이지")),
    new TeamMemberEntity("취미존중","이용현","팀원",List.of("클래스 등록","클래스 수정","로그인 및 DB 구조화")),
    new TeamMemberEntity("취미존중","이도경","팀원",List.of("메인"," 마이페이지","클래스 예약","결제 페이지")),
    new TeamMemberEntity("취미존중","문준혁","팀원",List.of("검색 페이지","클래스 예약","결제 페이지","마이페이지"))
);

private static final List<TeamMemberEntity> team8 = List.of(
    new TeamMemberEntity("O_O 커뮤니티","장가은","팀장",List.of("토스 API","상점 페이지","프론트")),
    new TeamMemberEntity("O_O 커뮤니티","정동혁","팀원",List.of("유저 관리","관리자 페이지","프론트")),
    new TeamMemberEntity("O_O 커뮤니티","우준영","팀원",List.of("투표기능","채팅기능","프로젝트 전체 조율")),
    new TeamMemberEntity("O_O 커뮤니티","양승환","팀원",List.of("게시판","신고기능","프론트"))
);

private static final List<TeamMemberEntity> team9 = List.of(
    new TeamMemberEntity("Pickup","무명","팀장",List.of("모든 것"))
    
);

    private static final Map<String,PortfoliosEntity> PORTFOLIOS = new LinkedHashMap<>();

        static{
            PORTFOLIOS.put("1",new PortfoliosEntity("Pawple","H.kim",
                                List.of("React","Next.js","SpringBoot","JPA","LomBok"),10,LocalDateTime.of(2025,7,10,21,50),
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
            
        PORTFOLIOS.put("2", new PortfoliosEntity( "Filmora", "S.Han",
            List.of("Next.js","SpringBoot","React","JPA","VSCode"),
            2, LocalDateTime.of(2025,7,10,21,50),
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
    
    
        PORTFOLIOS.put("3", new PortfoliosEntity( "ModeMe", "A. Park",
            List.of("Java","Thymeleaf","HTML","CSS","SpringBoot"),
            2, LocalDateTime.of(2025,7,10,21,50),
            "https://kafolio.kr/static/requests/2025/7/9/boards/YWFm....png",
            "20대 남성 전문 의류 쇼핑몰",
            List.of(
                "https://kafolio.kr/static/requests/2025/3/27/boards/OTZjOTNiYWJkMjNhMjA0ZTI2MGI1YWZmMmNiYTlhNmVmOGZlODJlYjlhODBkNmU0OTdjZmY3ZTM1MmNmMTI2Mg==.jpg",
                "https://kafolio.kr/static/requests/2025/3/27/boards/NzQ2MTIwOTFlMmY1ZTY0ZjVjNDg3MzY3NzRiNzhhNDlhZTFlOTUxMWEwYTBjNjQ5YmFkMjE3ODY1OTM5NDdmMg==.jpg",
                "https://kafolio.kr/static/requests/2025/3/27/boards/MjcyN2QzM2EzNmY4YjQyZTkwYzM3NjU3NjlmZDIxMzY2ODEwMGNjMmYxNTZiMjgxZjk5YjYyNGYwYjA4OTlmMQ==.jpg",
                "https://kafolio.kr/static/requests/2025/3/27/boards/M2NjODQzOWI3MDVkNWU1OTJmMWI0NzMyMWFmMDQ2NDI5Yjg3YTdhMDg0Nzk5ZWEwYzM2N2NhNTJiNjZkMWVhZA==.jpg",
                "https://kafolio.kr/static/requests/2025/3/27/boards/OTMwMzhkOTE1ZWU5YzY3MzAwODFjNzFmZGU5YWMyZTlkYjg1MDdiNDZkNTdiOTc2YzE5YjYzNjBiYjQxM2E4Nw==.jpg",
                "https://kafolio.kr/static/requests/2025/3/27/boards/OGM5Y2U2YTA0MjMzNjg3Y2U5ZGIxYTBlY2ExMTg2MWYzYzA3ZmJmYzZkNzFmZDA5OWI5Yjk1ZGQ0NWFhZmRlOQ==.jpg"
            ),team3,"https://kafolio.kr/static/requests/2025/3/27/icon/ZDZhMTIwNjYxMTgzMjYxMGJjYmViNDY1OTJjNzNlNGZmMmFiNjQ3NzJmOWY4MDJhYjI0MGYyZjhlNTBjYzliMQ==.png",
            "https://modeme.kafolio.kr","ModeMe.zip"
    ));


    PORTFOLIOS.put("4", new PortfoliosEntity( "OSPE", "A. Park",
    List.of("Java","Thymeleaf","HTML","JavaScript","SQL"),
    2, LocalDateTime.of(2025,7,10,21,50),
    "https://kafolio.kr/static/requests/2025/7/9/boards/YWFm....png",
    "진료 예약 및 처방전 출력",
    List.of(
        "https://kafolio.kr/static/requests/2024/12/30/boards/MGFiYzI1YmM1MzBlYmM2M2FjYzFkYzgxZmIyNWVkMzBkN2Q0MWEzODVmYzM0OTZlNzY4MjA0NjFiYTk2MDljOA==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/NzRiMTFlMjY5NmY3ZGY5MmM4MWQxODkyYjUwOGRmMDMzMzkyYjNmZGUwZDM5ZjhmNzQ3Y2YwZDlhZWUzZjI5Ng==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/ZDhiNjcxMTg0MjhkNWMyZmIzNjBlNmE5YmY3NGU2YjAxYzRhYjQwMzI1NWZkZTc1ZTVkNGY4ZjEzNGI0MjBlNA==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/ZTdmYmM0YWQyOWRhOTg3NDQxYzE5YjkzNjAxYTE0NjdkNTQzMDQ4NjhhNDI4OGZhN2ZmY2Y5NTRjYWQzNzQzNQ==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/ZmM1YmY4ZWM0OThiMWJhOGUwYjNmOGUyNTk1MWMwYzA1YTA4ZjU1YWZhNWJiM2U5ZmE2ZWZmY2QzYmVkMDYwNg==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/ZjA3Y2NlMmNjNWQzYzYzMzMxY2VhNWEyMGU4NTJhNjcyNWFhOWVhOGFiY2YyMjlmMjgwZTQ3MGUxZjY5ZWFiYg==.png"
    ),team4,"https://kafolio.kr/static/requests/2024/12/30/icon/ZjczMjc0YzYxZGZjMGRhOTY4ZDlmZjdlMDRlMTEzYjg4Y2ZlNTI2ZWY1Mjc2NzNlMGVjODg3ZDljMzNlMDgxZA==.png",
    "https://ospe.kafolio.kr","OSPE.zip"
));

    
    PORTFOLIOS.put("5", new PortfoliosEntity("NotePad", "A. Park",
    List.of("JavaScript","Tailwind","HTML","CSS","NodeJS"),
    2, LocalDateTime.of(2025,7,10,21,50),
    "https://kafolio.kr/static/requests/2024/9/5/boards/OGRiMjA2YmEyYWMxY2VmOGMxNDJkNjI2ZGM0YTZmMjVlN2VlNzdjYjZlOGE1ZDQzYWI3YTJiNzIxMzgwZjMzOQ==.jpg",
    "진료 예약 및 처방전 출력",
    List.of(
        "https://kafolio.kr/static/requests/2024/9/5/boards/OGRiMjA2YmEyYWMxY2VmOGMxNDJkNjI2ZGM0YTZmMjVlN2VlNzdjYjZlOGE1ZDQzYWI3YTJiNzIxMzgwZjMzOQ==.jpg",
        "https://kafolio.kr/static/requests/2024/9/5/boards/NDA1MzBlZTUyNzZlNzI2MzE5YzM0MTNlZGNjN2NjZDFhZjM4NTVkMDFiY2VhMmM1YzAxOWFlNTNjYWY2NzFmNA==.jpg",
        "https://kafolio.kr/static/requests/2024/9/5/boards/ZDIwNGUxMzY2MmVkOGVmZDgyMmYzMmJjOTE1NzIxNjY0NTA0MDM3ZWQ5NzJmYTliNzRiZjIyNjA2YzMyYWQ2Mg==.jpg",
        "https://kafolio.kr/static/requests/2024/9/5/boards/MTZlMWY4YmJhMDZiYmQ2YTQxOTMxYjdjNmExYTgxODIxZjVmYTE1ODIxZTM1NDRjZDkwOGI2M2RmMGY4Njc2Ng==.jpg",
        "https://kafolio.kr/static/requests/2024/9/5/boards/MzZhZDEyMTZlODhjZDJlYThmM2UxOTA4N2JiYzc4YzgzMDY0YWViZWNiNjFmNGFjMDMwNTIxNjk3NjFhZWUxMw==.jpg",
        "https://kafolio.kr/static/requests/2024/9/5/boards/NzkyMWU3YzM3NWNmZjEwZTU5NGI1NGM3ODIzYWVhYzE0MTE4ZjFlZDg1YzJlN2E3MWJkNzBiOTdhZTRjMjU2MA==.jpg"
    ),team5,"https://kafolio.kr/static/requests/2024/9/5/icon/MzQ1MjBkMGJmNDE3ZTE3MDk5NzYyYjc2MTNmYTQ5Yzg4OGQ1NTU5ZjBlODg4Njg0MmEwYWZjNDJhMzQwMjZmZA==.webp",
    "https://notepad.kafolio.kr","notepad.7z"
    ));


    PORTFOLIOS.put("6",new PortfoliosEntity("Planner for U","H.kim",
                                List.of("Java","SCSS","JavaScript","TS","HTML","CSS","Thymeleaf"),
                                10,LocalDateTime.of(2025,7,10,21,50),
                                "https://kafolio.kr/static/requests/2024/9/4/boards/NjRlNGMwMzJkZDc0OTJlYzZjYTdiYTk3ZTE5YjEyOTEyZmYwODhhZDE4NmM5MjExZDNkYzgzZmE4ZThmZjhkOA==.png",
                                "공유 캘린더 및 팀채팅",
                                List.of("https://kafolio.kr/static/requests/2024/9/4/boards/NjRlNGMwMzJkZDc0OTJlYzZjYTdiYTk3ZTE5YjEyOTEyZmYwODhhZDE4NmM5MjExZDNkYzgzZmE4ZThmZjhkOA==.png",
                                        "https://kafolio.kr/static/requests/2024/9/4/boards/YzcxZDg2MmVhMzE3NWY4MDJiZGVmOGJlM2U5NWQwYWZkMWUzYzY0YzEyYjMxZGU3ZTYzNjg4NjVlMjBlZDVkMw==.png",
                                        "https://kafolio.kr/static/requests/2024/9/4/boards/NTJlMjNkMzIxYzAyZTE1OTA0MjAxYmRkZjFkN2IyZTI0Y2YzN2M1MzJiYTJiZjc3MjU2MjRiMTVmZDllY2UzMg==.png",
                                        "https://kafolio.kr/static/requests/2024/9/4/boards/NWZiNjgyYzc1NzAwNDRjYmQyZGFiMThlNTc5NmI2Y2JhMmQwYjQ2NjI1NjI3MWVkNmU1YjRlNTdiZjEyM2Q3NQ==.png",
                                        "https://kafolio.kr/static/requests/2024/9/4/boards/MWIwYTNjY2RjYjJiNWI5YjhiOTRiMWUzMDM0NWUxNzgzZTVmNWM3ZjliYWQ4MzQ1MjUzMjYxMWIzY2ZjMDkxZg==.png",
                                        "https://kafolio.kr/static/requests/2024/9/4/boards/OTZhNDU0N2NiMTMwZGZjNjQxMGY5MTEzOTQzMWNjM2NiNGU3NDY2NmY0ODQxMmI2OTllZjg2ZGViOGI0NzBlZA==.png"
            ),team6,"https://kafolio.kr/static/requests/2024/9/4/icon/OWZmZGFiZWE3MWQ4ZDMxN2YxN2RhZTdlMWJkNTExOWY0YjRkNDE2YTUyZTQ4NGFiZTc1YjAzNDhmZDQxOGM3ZA==.png",

            "https://plan4u.kafolio.kr/app/login","Plan_4_U.zip"
        ));


        PORTFOLIOS.put("7", new PortfoliosEntity( "취미존중", "A. Park",
    List.of("Java","Thymeleaf","HTML","JavaScript","CSS","SCSS","TypeScript"),
    2, LocalDateTime.of(2025,7,10,21,50),
    "https://kafolio.kr/static/requests/2024/9/5/boards/NmQ4OTU1OGMwZTQ4ODcyZTFmMTExY2M3ZTEwOTA4ZjBlNzAxMDBlMTJkZGEyZDg5MDBmMjM3NzA4ZmY2ZDYxMw==.png",
    "원데이 클래스 예약 사이트",
    List.of(
        "https://kafolio.kr/static/requests/2024/9/5/boards/NmQ4OTU1OGMwZTQ4ODcyZTFmMTExY2M3ZTEwOTA4ZjBlNzAxMDBlMTJkZGEyZDg5MDBmMjM3NzA4ZmY2ZDYxMw==.png",
        "https://kafolio.kr/static/requests/2024/9/5/boards/ZTM1NTUwNjBkM2FhMTIwMzE4YjlmYjY3MTE3NzY1NzZiZjYyNWM4NjM2N2VmMjhiMThmMTE5YjlkOTY4NjEyOA==.png",
        "https://kafolio.kr/static/requests/2024/12/30/boards/ZDhiNjcxMTg0MjhkNWMyZmIzNjBlNmE5YmY3NGU2YjAxYzRhYjQwMzI1NWZkZTc1ZTVkNGY4ZjEzNGI0MjBlNA==.png",
        "https://kafolio.kr/static/requests/2024/9/5/boards/YzA4ZDUyMDZjMjU5MmMyZmFlOWY5Njc3MTJjNmIxNTdiMDU5ZTVhMzNkMWE3ZmYzZDc0NjVkZDk5MjU3ZDY3MA==.png",
        "https://kafolio.kr/static/requests/2024/9/5/boards/Y2IyMTcyNTUzOTZhZmNmNTJlYWNiMGYzZTNhMmI3ZjJkMWZkZGM0ZTA2OGQwZTYxYmM3NzhhZDYwMDRlZjI4NQ==.png",
        "https://kafolio.kr/static/requests/2024/9/5/boards/ZDY1YWQ0NDdjNTQ5ZTBkZWZjZGQyOTc0ZTFkNzU5ZTYzYWQ2YTNiODJkZDllYjhkNGZlODc4NjZmNGI5ZmE0Yw==.png"
    ),team7,"https://kafolio.kr/static/requests/2024/9/4/icon/ZGMzYmE5Y2Y1MWFlNjE3MjE2ZGFlMDYxODA0Y2FmOTA2ZjI0MzY5MzhlNDg3MjE5OTc5MDQ5MjBjYjI3NjBjMA==.png",
    "https://cmjj.kafolio.kr/main","cmjj.zip"
));


PORTFOLIOS.put("8", new PortfoliosEntity( "O_O 커뮤니티", "A. Park",
    List.of("JavaScript","HTML","CSS","React"),
    2,LocalDateTime.of(2025,7,10,21,50),
    "https://kafolio.kr/static/requests/2024/5/3/boards/MjFhOWE5YzMxZmYxMjFmN2E0MzBlZDk1NmRmMDU2MGE5Y2ZkYzZkMmI1ZDIyMzBlNzRhYWE5Nzc5NzE0ODY1Yw==.png",
    "커뮤니티 사이트",
    List.of(
        "https://kafolio.kr/static/requests/2024/5/3/boards/MjFhOWE5YzMxZmYxMjFmN2E0MzBlZDk1NmRmMDU2MGE5Y2ZkYzZkMmI1ZDIyMzBlNzRhYWE5Nzc5NzE0ODY1Yw==.png",
        "https://kafolio.kr/static/requests/2024/5/3/boards/OGU4NGU5YzZlZDFhMDQ2OTQyNzE4MWE5MTgyYWY3Y2QyOWEwYzc5Mjg4NzNkNjQ4NmVlZDA1MTExYzM1OGFmMQ==.png",
        "https://kafolio.kr/static/requests/2024/5/3/boards/MGY5MTZhYjYzYjk0MzU1MTZkNTZjNjYxYThjODJiODcxZGNjMzYxYjQyYmNjNDA0ODZlOTEzNzUyNGJhZWIxOA==.png",
        "https://kafolio.kr/static/requests/2024/5/3/boards/YmU5ZmY1ZThkZmMzYTlkNDJhNGE5YWY3MDliMGZkM2Y2YTJjZjEwOWZmOTJhZDE2MzM0NTZkN2U0ZTk3ZjJkZg==.png",
        "https://kafolio.kr/static/requests/2024/5/3/boards/ZTQyMDZkNTVhOWUzZWZjNjY5ZGZiZDJmMTVhYWNiNjA4ZDEyM2M3OTg4MDQxOWUwNDM5YzYxNGQ2ZTA0OGY3ZQ==.png",
        "https://kafolio.kr/static/requests/2024/5/3/boards/NzFhZTRmYTdlMDVjY2NkOWYwM2YwNmUxOTg0MWIxYjllNjk0OWI3OTVhNDg0YWZjNjhmZDQ1MmIwZjRhYjExNQ==.png"
    ),team8,"https://kafolio.kr/static/requests/2024/5/3/icon/MGY4ZGRhNTc0NjRjZmM3ZmM5NDM5OTFkOGUzMTQ3NDA3YjFiM2FiMjkyN2Y5NGNjMGRiMWQxNTk4M2Y2ZGU0Zg==.png",
    "https://o-o.kafolio.kr/app","O-O.kafolio.kr.zip"
));

PORTFOLIOS.put("9", new PortfoliosEntity( "Pickup", "A. Park",
    List.of("JavaScript","Express","React"),
    2, LocalDateTime.of(2025,7,10,21,50),
    "https://kafolio.kr/static/requests/2024/4/26/boards/MTkyMzEwZTdiOTM2Mjk0NjQ3NDdhMjBiNGQ4OGEwNjdmYWI3YzlkMDk1MGQ5ODg5ZmIyMTBiNzg4NTBjOGIxZA==.jpg",
    "리그오브레전드 아이템 계산기",
    List.of(
        "https://kafolio.kr/static/requests/2024/4/26/boards/MTkyMzEwZTdiOTM2Mjk0NjQ3NDdhMjBiNGQ4OGEwNjdmYWI3YzlkMDk1MGQ5ODg5ZmIyMTBiNzg4NTBjOGIxZA==.jpg",
        "https://kafolio.kr/static/requests/2024/4/26/boards/NmQ5MDM0NzVjYjMyZGJiY2JhMDkwYzgzODgwMzI4ZGMwMTlmZmI5M2M3YWUwY2M3NGRjYjY1M2M3YmYxYmI0Nw==.jpg",
        "https://kafolio.kr/static/requests/2024/4/26/boards/ZGJhYTc2MWM1ZjFkODY5N2RhZmMwYjYyYTA0ZGI1YzE3NmI4YmViNzZhODU0YjdkYTIxNzNkMjQyYzI5MDM1MQ==.jpg",
        "https://kafolio.kr/static/requests/2024/4/26/boards/YzZhNTM2MGFlYzA4Y2EzMTk0MjQyNGZkZjZmNjg1NDNhN2RiNzBkMTk3ODljMjU5MDFhYWE5ZGZkNzQ3YmU2Mg==.jpg",
        "https://kafolio.kr/static/requests/2024/4/26/boards/MTUxM2ZjZmMwYzQ4YTQwNmE0Mjc5MWJjYmIwNjM3NjBiMTRkYTRhMGM4ODM5OTMyYzViMmY0YmRmNDU4NTk4YQ==.jpg",
        "https://kafolio.kr/static/requests/2024/4/26/boards/OGRjYzhjMjQ3MzkzNDAyMmVlMWI1MDdkYjE3NjE3NDRmZDgwN2Y0MTEzYTk1ZWI2ODkwODY1ZWQ5ZGQyNzJjNg==.jpg"
    ),team9,"https://kafolio.kr/static/requests/2024/4/26/icon/YWQ4YzkxODZmMzM0MWIwYmQ2OWFhNjg4MGI5NDM3MjI2M2QyYjM2OGM1NDQ0YmM4NDA4NWMzMjBlNDQ3YWU1OQ==.jpg",
    "https://pickup.kafolio.kr","ChlDmnGur09.zip"
));


    }

    PortfoliosController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolios")
    public String list(Model model) {
        model.addAttribute("items", PORTFOLIOS.values());
        return "portfolios/list"; // templates/portfolios/list.html
    }


    @GetMapping("/portfolios/{id}")
    public String getPortfolio(@PathVariable String id, Model model) {
        PortfoliosEntity portfolios = PORTFOLIOS.get(id);
        if (portfolios == null) {
            model.addAttribute("notFound", true);
        } else {
            model.addAttribute("portfolios", portfolios);
        }
        return "portfolios/detail"; // templates/detail/portfolio-detail.html
    }

    /**
     * ID로 포트폴리오 엔티티를 반환하는 public 메서드 (임시)(준회)
     */
    public PortfoliosEntity getPortfolioById(String id) {
        return PORTFOLIOS.get(id);
    }

    /**
     * 모든 포트폴리오 엔티티 목록을 반환하는 public 메서드 (임시)(준회)
     */
    public Collection<PortfoliosEntity> getAllPortfolios() {
        return PORTFOLIOS.values();
    }


    @GetMapping("/portfolios/create")
    public String createForm(Model model){
        model.addAttribute("portfolioFormDto", new PortfolioFormDto());
        return "portfolios/create";
    }

            // 생성 처리
            @PostMapping("/portfolios/create")
            public String create(@ModelAttribute PortfolioFormDto dto,
                                 @RequestParam("images") List<MultipartFile> images,
                                 @RequestParam("icon") MultipartFile icon,
                                 @RequestParam(value = "download", required = false) MultipartFile download
            ) throws IOException {
            
                // 📂 업로드 폴더 생성
                String uploadDir = System.getProperty("user.dir") + "/uploads";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
            
                // 📌 저장 코드 (transferTo)
                if (icon != null && !icon.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + icon.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, filename);
                    icon.transferTo(filePath.toFile());
                    dto.setIconPath("/uploads/" + filename);   // DB에 저장할 경로
                }
            
                if (download != null && !download.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + download.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, filename);
                    download.transferTo(filePath.toFile());
                    dto.setDownloadPath("/uploads/" + filename);
                }
            
                // 👉 DB 저장 로직 호출
                portfolioService.saveFromDto(dto);
            
                return "redirect:/portfolios";
            }
            
            


}
