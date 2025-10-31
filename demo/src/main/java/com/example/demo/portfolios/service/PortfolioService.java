package com.example.demo.portfolios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.portfolios.dto.PortfolioFormDto;
import com.example.demo.portfolios.dto.TeamMemberDto;
import com.example.demo.portfolios.entity.PortfolioComment;
import com.example.demo.portfolios.entity.PortfolioLikeEntity;
import com.example.demo.portfolios.entity.PortfoliosEntity;
import com.example.demo.portfolios.entity.TeamMemberEntity;
import com.example.demo.portfolios.repository.PortfolioCommentRepository;
import com.example.demo.portfolios.repository.PortfolioLikeRepository;
import com.example.demo.portfolios.repository.PortfoliosRepository;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfoliosRepository repository;
    private final PortfolioCommentRepository commentRepository;
    private final PortfolioLikeRepository likeRepository;
    private final UsersRepository usersRepository;

    /**
     * ✅ 리스트 페이지용 — LazyInitialization 방지용 트랜잭션 유지
     */
    @Transactional(readOnly = true)
    public List<PortfoliosEntity> getAllPortfolios(org.springframework.data.domain.Pageable pageable) {
        return repository.findAllBasic(pageable).getContent(); // ✅ EntityGraph로 미리 로드됨
    }

    /**
     * 파일 저장 (이미지/ZIP 구분)
     */
    public String saveFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) return null;

        long maxSize = type.equals("zip") ? 50 * 1024 * 1024 : 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IOException("파일 용량 초과: " + file.getOriginalFilename());
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

        if (type.equals("image")) {
            if (!(extension.equals("jpg") || extension.equals("jpeg")
                    || extension.equals("png") || extension.equals("webp"))) {
                throw new IOException("이미지 파일만 업로드 가능: " + originalName);
            }
        } else if (type.equals("zip")) {
            if (!extension.equals("zip")) {
                throw new IOException("ZIP 파일만 업로드 가능: " + originalName);
            }
        }

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + filename;
    }

    @Transactional
    public void saveFromDto(PortfolioFormDto dto, String coverPath, String iconPath, String downloadPath) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
    
        // ✅ 폴더명 자동 변환 (한글 → 영어 대응)
        String folderName = (dto.getTeamName() != null && !dto.getTeamName().isBlank())
                ? normalizeFolderName(dto.getTeamName())
                : normalizeFolderName(dto.getTitle());
    
        /* 1️⃣ 대표 이미지 (cover) */
        if (coverPath == null || coverPath.isBlank()) {
            String staticCover = findStaticFile(folderName, "cover");
            if (staticCover != null) coverPath = staticCover;
        }
    
        /* 2️⃣ 아이콘 (icon) */
        if (iconPath == null || iconPath.isBlank()) {
            String staticIcon = findStaticFile(folderName, "icon");
            if (staticIcon != null) iconPath = staticIcon;
        }
    
       /* ======================================================
   ✅ 3️⃣ 스크린샷 (screenshots)
   ====================================================== */
List<String> screenshotPaths = new ArrayList<>();

boolean hasUploadedScreenshots = (dto.getScreenshots() != null) &&
        dto.getScreenshots().stream().anyMatch(f -> f != null && !f.isEmpty());

if (hasUploadedScreenshots) {
    // ✅ 새로 업로드된 스크린샷이 있는 경우
    screenshotPaths = dto.getScreenshots().stream()
        .filter(file -> file != null && !file.isEmpty())
        .map(file -> {
            try {
                return saveFile(file, "image");
            } catch (IOException e) {
                throw new RuntimeException("스크린샷 저장 실패", e);
            }
        })
        .toList();
} else {
    // ✅ 업로드가 아예 없을 경우 → 정적 이미지 자동 불러오기
    List<String> staticScreens = loadStaticScreenshots(folderName);
    if (!staticScreens.isEmpty()) {
        screenshotPaths = staticScreens;
        System.out.println("✅ 정적 스크린샷 자동 로드: " + staticScreens.size() + "개");
    } else {
        System.out.println("⚠️ 정적 스크린샷 없음: " + folderName);
    }
}

    
        /* 4️⃣ 태그 (null-safe) */
        Set<String> safeTags = (dto.getTags() != null) ? dto.getTags() : new LinkedHashSet<>();
    
        /* 5️⃣ 엔티티 생성 */
        PortfoliosEntity entity = PortfoliosEntity.builder()
            .title(dto.getTitle())
            .creator(currentUser)
            .tags(safeTags)
            .cover(coverPath)
            .desc(dto.getDesc())
            .screenshots(screenshotPaths)
            .icon(iconPath)
            .link(dto.getLink())
            .download(downloadPath)
            .likes(new LinkedHashSet<>())
            .createdAt(LocalDateTime.now())
            .teamName(dto.getTeamName())
            .build();
    
        /* 6️⃣ 팀원 */
        List<TeamMemberEntity> team = new ArrayList<>();
        if (dto.getTeam() != null) {
            for (TeamMemberDto t : dto.getTeam()) {
                TeamMemberEntity member = new TeamMemberEntity(
                    t.getMemberName(), t.getMemberRole(), t.getParts()
                );
                member.setPortfolio(entity);
                team.add(member);
            }
        }
    
        entity.setTeam(team);
        repository.save(entity);
    }
    
    

    @Transactional(readOnly = true)
    public PortfoliosEntity getPortfolioWithTeam(Long id) {
        return repository.findDetailById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));
    }

    @Transactional
    public void deletePortfolio(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 존재하지 않습니다. id=" + id));

        commentRepository.deleteByPortfolioId(id);
        repository.delete(portfolio);
    }

    @Transactional
    public PortfoliosEntity increaseViewCount(Long id) {
        PortfoliosEntity portfolio = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

        if (portfolio.getViewCount() == null) {
            portfolio.setViewCount(0);
        }

        portfolio.setViewCount(portfolio.getViewCount() + 1);
        return repository.save(portfolio);
    }

    @Transactional
public void updatePortfolio(Long id, PortfolioFormDto dto) throws IOException {
    PortfoliosEntity portfolio = repository.findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));

    portfolio.setTitle(dto.getTitle());
    portfolio.setDesc(dto.getDesc());
    portfolio.setLink(dto.getLink());
    portfolio.setTeamName(dto.getTeamName());

    // ✅ 폴더명 자동 변환
    String folderName = (dto.getTeamName() != null && !dto.getTeamName().isBlank())
            ? normalizeFolderName(dto.getTeamName())
            : normalizeFolderName(dto.getTitle());

    /* 대표 이미지 */
    if (dto.getCover() != null && !dto.getCover().isEmpty()) {
        portfolio.setCover(saveFile(dto.getCover(), "image"));
    } else if (portfolio.getCover() == null || portfolio.getCover().isBlank()) {
        String staticCover = findStaticFile(folderName, "cover");
        if (staticCover != null) portfolio.setCover(staticCover);
    }

    /* 아이콘 */
    if (dto.getIcon() != null && !dto.getIcon().isEmpty()) {
        portfolio.setIcon(saveFile(dto.getIcon(), "image"));
    } else {
        String staticIcon = findStaticFile(folderName, "icon");
        if (staticIcon != null) portfolio.setIcon(staticIcon);
    }

    /* ZIP 파일 */
    if (dto.getDownload() != null && !dto.getDownload().isEmpty()) {
        portfolio.setDownload(saveFile(dto.getDownload(), "zip"));
    }

    /* 스크린샷 */
    if (dto.getScreenshots() != null) {
        List<MultipartFile> nonEmptyScreenshots = dto.getScreenshots().stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        if (!nonEmptyScreenshots.isEmpty()) {
            List<String> screenshotPaths = new ArrayList<>();
            for (MultipartFile file : nonEmptyScreenshots) {
                screenshotPaths.add(saveFile(file, "image"));
            }
            portfolio.setScreenshots(screenshotPaths);
        } else {
            List<String> staticScreens = loadStaticScreenshots(folderName);
            if (!staticScreens.isEmpty()) portfolio.setScreenshots(staticScreens);
        }
    }

    /* 태그 */
    Set<String> safeTags = (dto.getTags() != null && !dto.getTags().isEmpty())
            ? dto.getTags()
            : (portfolio.getTags() != null ? portfolio.getTags() : new LinkedHashSet<>());
    portfolio.setTags(safeTags);

    /* 팀원 */
    if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {
        List<TeamMemberEntity> existing = portfolio.getTeam();
        existing.clear();
        for (TeamMemberDto t : dto.getTeam()) {
            TeamMemberEntity member = new TeamMemberEntity(
                t.getMemberName(), t.getMemberRole(), t.getParts()
            );
            member.setPortfolio(portfolio);
            existing.add(member);
        }
    }

    repository.save(portfolio);
}


/* ======================================================
   ✅ 정적 폴더 스크린샷 자동 로드 함수 (확장자 자동 인식)
====================================================== */
private List<String> loadStaticScreenshots(String folderName) {
    List<String> images = new ArrayList<>();
    try {
        Path dirPath = Paths.get("src/main/resources/static/images", folderName);
        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            Files.list(dirPath)
                .filter(path -> path.getFileName().toString().matches("^[0-9]+\\.(png|jpg|jpeg|webp)$"))
                .sorted((a, b) -> {
                    // 숫자 순 정렬 (1,2,3,10 순서 유지)
                    String aNum = a.getFileName().toString().replaceAll("\\D+", "");
                    String bNum = b.getFileName().toString().replaceAll("\\D+", "");
                    return Integer.compare(
                        aNum.isEmpty() ? 0 : Integer.parseInt(aNum),
                        bNum.isEmpty() ? 0 : Integer.parseInt(bNum)
                    );
                })
                .forEach(path -> {
                    String relative = "/images/" + folderName + "/" + path.getFileName().toString();
                    images.add(relative);
                });
        }
    } catch (IOException e) {
        System.err.println("⚠️ 정적 스크린샷 로드 실패: " + e.getMessage());
    }
    return images;
}


    

@Transactional(readOnly = true)
public PortfolioFormDto getPortfolioForm(Long id) {
    // ✅ 기존: findDetailById() → comments, screenshots 둘 다 fetch
    // ❌ 변경: 수정용으로 fetch 줄이기
    PortfoliosEntity entity = repository.findForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));

    return PortfolioFormDto.formEntityDto(entity);
}


@Transactional
public int toggleLike(Long id, Principal principal) {
    Users user = usersRepository.findByUsername(principal.getName())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    PortfoliosEntity portfolio = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));

    Optional<PortfolioLikeEntity> existingLike = likeRepository.findByPortfolioAndUser(portfolio, user);
    if (existingLike.isPresent()) {
        likeRepository.delete(existingLike.get());
    } else {
        PortfolioLikeEntity like = PortfolioLikeEntity.builder()
            .user(user)
            .portfolio(portfolio)
            .build();
        likeRepository.save(like);
    }

    return likeRepository.countByPortfolioId(id);
}


    

@Transactional(readOnly = true)
public PortfoliosEntity getPortfolioDetail(Long id) {
    PortfoliosEntity portfolio = repository.findDetailById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 포트폴리오가 없습니다. id=" + id));

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    boolean isPublic = Boolean.TRUE.equals(portfolio.getIsPublic());

    if (!isPublic && !isAdmin) {
        throw new AccessDeniedException("비공개 포트폴리오입니다.");
    }

    Hibernate.initialize(portfolio.getTags());
    Hibernate.initialize(portfolio.getTeam());
    Hibernate.initialize(portfolio.getLikes());
    Hibernate.initialize(portfolio.getScreenshots());
    Hibernate.initialize(portfolio.getComments());
    portfolio.getComments().forEach(c -> {
        if (c.getUser() != null) Hibernate.initialize(c.getUser());
    });

    portfolio.setTeam(new ArrayList<>(new LinkedHashSet<>(portfolio.getTeam())));
    portfolio.setTags(new LinkedHashSet<>(portfolio.getTags()));
    portfolio.setLikes(new LinkedHashSet<>(portfolio.getLikes()));
    portfolio.setComments(new ArrayList<>(new LinkedHashSet<>(portfolio.getComments())));

    return portfolio;
}




    

/* ======================================================
   ✅ 확장자 무관 자동 탐색 (cover, icon 전용)
====================================================== */
private String findStaticFile(String folderName, String baseName) {
    String[] exts = {"png", "jpg", "jpeg", "webp"};
    for (String ext : exts) {
        Path path = Paths.get("src/main/resources/static/images", folderName, baseName + "." + ext);
        if (Files.exists(path)) {
            return "/images/" + folderName + "/" + baseName + "." + ext;
        }
    }
    return null;
}


/* ======================================================
   ✅ 공통: 폴더명 정규화 + 한글 → 영문 매핑
====================================================== */
private String normalizeFolderName(String rawName) {
    if (rawName == null || rawName.isBlank()) return "default";

    // 1️⃣ 공백, 특수문자 제거
    String folder = rawName.toLowerCase().replaceAll("[^a-z0-9ㄱ-ㅎ가-힣]", "");

    // 2️⃣ 한글 제목 매핑
    switch (folder) {
        case "취미존중" -> folder = "hobbyrespect";
        case "오오커뮤니티" -> folder = "oo";
        case "플레너포유" -> folder = "plannerforu";
        case "필모라" -> folder = "filmora";
        case "모드미" -> folder = "modeme";
        
        // ✅ 필요할 때마다 추가
    }

    return folder;
}


@Transactional(readOnly = true)
public List<PortfoliosEntity> searchPortfolios(String keyword, Pageable pageable) {
    return repository.searchByKeyword(keyword, pageable).getContent();
}

@Transactional(readOnly = true)
public List<PortfoliosEntity> searchByTitle(String keyword, Pageable pageable) {
    return repository.searchByTitle(keyword, pageable).getContent();
}

@Transactional(readOnly = true)
public List<PortfoliosEntity> searchByTags(List<String> tags, Pageable pageable) {
    return repository.searchByTags(tags, pageable).getContent();
}

@Transactional(readOnly = true)
public List<PortfoliosEntity> searchByTitleAndTags(String keyword, List<String> tags, Pageable pageable) {
    return repository.searchByTitleAndTags(keyword, tags, pageable).getContent();
}


 /**
     * ✅ 공개 포트폴리오 전체 조회 (Lazy 초기화 포함)
     */
    @Transactional(readOnly = true)
    public List<PortfoliosEntity> getPublicPortfolios() {
        List<PortfoliosEntity> portfolios = repository.findAllWithScreenshots();
    
        portfolios.forEach(p -> {
            if (p.getScreenshots() != null) p.getScreenshots().size(); // ✅ 이미지만 초기화
        });
    
        return portfolios;
    }
    
    
    @Transactional(readOnly = true)
public List<PortfoliosEntity> searchPortfolios(String keyword) {
    // 대소문자 무시를 위해 keyword를 소문자로 변환
    String lowerKeyword = keyword.toLowerCase();

    // 제목, 태그 둘 다 검색
    return repository.findByTitleContainingIgnoreCaseOrTagsContaining(lowerKeyword);
}

@Transactional(readOnly = true)
public List<PortfoliosEntity> filterByTags(List<String> tags) {
    // 소문자로 통일
    List<String> lowerTags = tags.stream()
            .map(String::toLowerCase)
            .toList();

    // 모든 포트폴리오 로드 (tags와 likes 미리 fetch)
    List<PortfoliosEntity> all = repository.findAllWithTagsAndLikes();

    // ✅ 포함 여부 필터링 (부분 일치)
    return all.stream()
            .filter(p -> p.getTags().stream()
                    .map(String::toLowerCase)
                    .anyMatch(tag ->
                        lowerTags.stream().anyMatch(tag::contains)
                    )
            )
            .toList();
}




@Transactional(readOnly = true)
public List<PortfoliosEntity> searchPortfoliosWithTags(String keyword, List<String> tags) {
    String lowerKeyword = keyword.toLowerCase();
    List<String> lowerTags = tags.stream()
            .map(String::toLowerCase)
            .toList();

    return repository.findByKeywordAndTags(lowerKeyword, lowerTags);
}


@Transactional(readOnly = true)
public List<PortfoliosEntity> sortPortfolios(List<PortfoliosEntity> list, String sort) {

    if (list == null || list.isEmpty()) return list;

    // createdAt이나 likeCount 실제 필드명에 맞춰서 수정해줘야 해
    // 예시로: portfolio.getCreatedAt(), portfolio.getLikeCount()

    if ("likes".equalsIgnoreCase(sort)) {
        // 좋아요 많은 순 (내림차순)
        return list.stream()
                .sorted((a, b) -> {
                    Integer aLikes = safeLikes(a.getLikeCount());
                    Integer bLikes = safeLikes(b.getLikeCount());
                    return bLikes.compareTo(aLikes); // b 먼저 -> desc
                })
                .toList();
    }

    // 기본: 최신순 (createdAt desc)
    return list.stream()
            .sorted((a, b) -> {
                // createdAt null 안전 처리
                var aDate = a.getCreatedAt();
                var bDate = b.getCreatedAt();
                if (aDate == null && bDate == null) return 0;
                if (aDate == null) return 1;  // null은 뒤로
                if (bDate == null) return -1;
                return bDate.compareTo(aDate); // 최신 먼저
            })
            .toList();
}

private Integer safeLikes(Integer likeCount) {
    return likeCount == null ? 0 : likeCount;
}





}
