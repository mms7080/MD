package com.example.demo.folio.service;

import com.example.demo.folio.dto.*;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.portfolios.service.PortfolioService;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolioService {

    private final ObjectMapper objectMapper;
    private final FolioRepository folioRepository;
    private final UsersRepository usersRepository;
    private final PortfolioService portfolioService;
    private final UsersService usersService;

    /** 상세 */
    public FolioDetailDto getFolioDetail(String id) {
        Folio folio = folioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Folio를 찾을 수 없습니다. id=" + id));

        List<PortfolioInFolioDto> projects = folio.getProjectIds().stream()
                .map(pid -> {
                    var ent = portfolioService.getPortfolioWithTeam(Long.valueOf(pid));
                    return ent != null ? new PortfolioInFolioDto(ent.getId(), ent.getTitle()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> state = Map.of();
        try {
            String cj = folio.getContentJson();
            if (cj != null && !cj.isBlank()) {
                state = objectMapper.readValue(cj, Map.class);
            }
        } catch (Exception ignored) {}

        return new FolioDetailDto(folio, projects, state);
    }

    /** 새 Folio 생성 (폼 제출) */
    @Transactional
    public Folio createOrUpdateFolio(FolioRequestDto requestDto, Principal principal) {
        Users currentUser = usersRepository
                .findByUsernameAndDeleteStatus(principal.getName(), DeleteStatus.N)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Folio folio = new Folio(); // 항상 새로 생성
        folio.setUser(currentUser);
        folio.setIntroduction(requestDto.getIntroduction());
        folio.setSkills(requestDto.getSkills());        // 가변 리스트 세팅
        folio.setPhotos(requestDto.getPhotos());
        folio.setProjectIds(requestDto.getProjectIds());

        if (requestDto.getPhotos() != null && !requestDto.getPhotos().isEmpty()) {
            folio.setThumbnail(requestDto.getPhotos().get(0));
        } else {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }

        folio.setContentJson("{}");
        return folioRepository.save(folio);
    }

    /** 에디터 상태 저장 (임시저장) */
    @Transactional
    public Folio saveState(Principal principal, FolioStateSaveRequest req) {
        Users user = usersService.getUserByUsername(principal.getName());

        Folio folio;
        if (req.getFolioId() != null && !req.getFolioId().isBlank()) {
            folio = folioRepository.findByIdAndUser(req.getFolioId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("권한 없거나 없는 folioId"));
        } else {
            folio = new Folio();
        }

        folio.setUser(user);
        folio.setTemplate(req.getTemplate() != null ? req.getTemplate() : "dev-basic");
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");
        folio.setStatus(req.getStatus() != null ? req.getStatus() : Folio.Status.DRAFT);

        if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        } else if (folio.getThumbnail() == null || folio.getThumbnail().isBlank()) {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            folio.setTitle(req.getTitle());
        }

        return folioRepository.save(folio);
    }

    /** 발행 + 슬라이드 이미지 저장 */
    @Transactional
    public Folio publishAsImages(Principal principal, FolioPublishRequest req) {
        Users user = usersService.getUserByUsername(principal.getName());

        Folio folio = (req.getFolioId() != null && !req.getFolioId().isBlank())
                ? folioRepository.findByIdAndUser(req.getFolioId(), user).orElse(new Folio())
                : new Folio();

        folio.setUser(user);
        folio.setTemplate(req.getTemplate() != null ? req.getTemplate() : "dev-basic");
        folio.setTitle((req.getTitle() != null && !req.getTitle().isBlank()) ? req.getTitle() : user.getName());
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");
        folio.setStatus(Folio.Status.PUBLISHED);

        // ID 확보
        folio = folioRepository.save(folio);

        // 가변 리스트로 교체
        List<String> slides = saveBase64Images(folio.getId(), req.getImages());
        folio.setPhotos(slides);

        if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        } else if (!slides.isEmpty()) {
            folio.setThumbnail(slides.get(0));
        } else {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }

        return folioRepository.save(folio);
    }

    /** 빈 경우에도 가변 리스트 반환(불변 리스트 금지) */
    private List<String> saveBase64Images(String folioId, List<String> dataUrls) {
        if (dataUrls == null || dataUrls.isEmpty()) return new ArrayList<>();

        try {
            Path root = Paths.get("uploads/folios", folioId, "slides");
            Files.createDirectories(root);

            int idx = 1;
            var out = new ArrayList<String>();
            for (String dataUrl : dataUrls) {
                String base64 = dataUrl.substring(dataUrl.indexOf(",") + 1);
                byte[] bytes = java.util.Base64.getDecoder().decode(base64);

                String name = String.format("slide-%03d.png", idx++);
                Path file = root.resolve(name);
                Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                out.add("/uploads/folios/" + folioId + "/slides/" + name);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("슬라이드 이미지 저장 실패", e);
        }
    }

    // ===== 마이페이지/목록용 =====

    /** 에디터가 처음 열 때 불러가는 최신 상태 */
    public Optional<Map<String, Object>> getMyDevBasicState(Principal principal) {
        Users user = usersService.getUserByUsername(principal.getName());
        Folio folio = folioRepository.findTopByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.DRAFT)
                .or(() -> folioRepository.findTopByUserOrderByUpdatedAtDesc(user))
                .orElse(null);
        if (folio == null) return Optional.empty();

        Map<String, Object> res = new HashMap<>();
        res.put("id", folio.getId());
        res.put("template", folio.getTemplate());
        res.put("contentJson", folio.getContentJson());
        res.put("status", folio.getStatus());
        res.put("thumbnail", folio.getThumbnail());
        res.put("updatedAt", folio.getUpdatedAt());
        return Optional.of(res);
    }

    /** 마이페이지 카드용 요약 */
    public Map<String, Object> getMyFoliosSummary(Principal principal) {
        Users user = usersService.getUserByUsername(principal.getName());
        Map<String, Object> res = new HashMap<>();
        folioRepository.findTopByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.DRAFT)
                .ifPresent(f -> res.put("draft", Map.of(
                        "id", f.getId(),
                        "updatedAt", f.getUpdatedAt(),
                        "thumbnail", f.getThumbnail()
                )));
        folioRepository.findTopByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.PUBLISHED)
                .ifPresent(f -> res.put("published", Map.of(
                        "id", f.getId(),
                        "updatedAt", f.getUpdatedAt(),
                        "thumbnail", f.getThumbnail()
                )));
        return res;
    }

    /** 내 목록(페이지) */
    @Transactional(readOnly = true)
    public Page<FoliosSummaryDto> getMyFolioSummaries(Principal principal, Pageable pageable) {
        var user = usersService.getUserByUsername(principal.getName());
        var page = folioRepository.findAllByUser(user, pageable);
        return page.map(FoliosSummaryDto::from);   // 엔티티 → DTO 즉시 변환
    }

    /** 전체 목록(페이지) */
    public Page<FoliosSummaryDto> getFolioSummaries(Pageable pageable) {
        return folioRepository.findAll(pageable).map(FoliosSummaryDto::from);
    }

    /** 모달용 버킷(임시저장/업로드) */
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getMyFoliosBuckets(Principal principal) {
        Users user = usersService.getUserByUsername(principal.getName());
        List<Folio> drafts    = folioRepository.findAllByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.DRAFT);
        List<Folio> published = folioRepository.findAllByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.PUBLISHED);

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("DRAFT", drafts.stream().map(this::toBucketItem).collect(Collectors.toList()));
        result.put("PUBLISHED", published.stream().map(this::toBucketItem).collect(Collectors.toList()));
        return result;
    }

    /** 상태별 페이지 */
    @Transactional(readOnly = true)
    public Page<FoliosSummaryDto> getMyListByStatus(Principal principal, String status, Pageable pageable) {
        Users user = usersService.getUserByUsername(principal.getName());

        Page<Folio> page;
        if (status == null || status.isBlank()) {
            page = folioRepository.findAllByUser(user, pageable);
        } else {
            Folio.Status st = Folio.Status.valueOf(status); // "DRAFT" / "PUBLISHED"만 허용
            page = folioRepository.findAllByUserAndStatus(user, st, pageable);
        }
        return page.map(FoliosSummaryDto::from);
    }

    /** 소유자 삭제 */
    @Transactional
    public void deleteIfOwner(Principal principal, String id) {
        var user = usersService.getUserByUsername(principal.getName());
        var folio = folioRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("권한 없거나 없는 folio"));
        try {
            Path root = Paths.get("uploads/folios", folio.getId());
            if (Files.exists(root)) deleteRecursively(root);
        } catch (Exception ignore) {}
        folioRepository.delete(folio);
    }

    private void deleteRecursively(Path path) throws Exception {
        if (!Files.exists(path)) return;
        try (var s = Files.walk(path)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignore) {}
            });
        }
    }

    // ===== 내부 유틸 =====

    /** 모달/카드용 최소 필드만 꺼내는 아이템(컬렉션 접근 없음) */
    private Map<String, Object> toBucketItem(Folio f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.getId());
        m.put("status", f.getStatus() != null ? f.getStatus().name() : "DRAFT");
        m.put("template", f.getTemplate());
        m.put("updatedAt", f.getUpdatedAt());
        m.put("title", safeTitle(f));
        return m;
    }

    /** 제목 안전 추출(없으면 contentJson.intro.name → 최종 “제목 없음”) */
    private String safeTitle(Folio folio) {
        if (folio.getTitle() != null && !folio.getTitle().isBlank()) {
            return folio.getTitle().trim();
        }
        try {
            String cj = folio.getContentJson();
            if (cj != null && !cj.isBlank()) {
                var root = objectMapper.readTree(cj);
                var nameNode = root.path("intro").path("name");
                if (!nameNode.isMissingNode() && !nameNode.isNull()) {
                    String name = nameNode.asText();
                    if (name != null && !name.isBlank()) return name;
                }
            }
        } catch (Exception ignore) {}
        return "제목 없음";
    }
}
