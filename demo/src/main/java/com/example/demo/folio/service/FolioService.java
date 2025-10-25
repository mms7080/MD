package com.example.demo.folio.service;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FolioPublishRequest;
import com.example.demo.folio.dto.PortfolioInFolioDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.portfolios.service.PortfolioService;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;
import com.example.demo.folio.dto.FoliosSummaryDto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolioService {
    
    private final ObjectMapper objectMapper;
    private final FolioRepository folioRepository;
    private final UsersRepository usersRepository;
    private final PortfolioService portfolioService;
    private final UsersService usersService;

    public FolioDetailDto getFolioDetail(String id) {
        Folio folio = folioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 Folio를 찾을 수 없습니다. id=" + id));

        // projectIds -> projects 매핑 (생략 가능: 기존 로직)
        List<PortfolioInFolioDto> projects = folio.getProjectIds().stream()
            .map(pid -> {
                var ent = portfolioService.getPortfolioWithTeam(Long.valueOf(pid));
                return ent != null ? new PortfolioInFolioDto(ent.getId(), ent.getTitle()) : null;
            })
            .filter(p -> p != null)
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

    @Transactional 
    public Folio createOrUpdateFolio(FolioRequestDto requestDto, Principal principal) {
        
        // UsersRepository 수정해서 현재 로그인했으면서 탈퇴를 하지 않은 사람 찾아오게 수정해놨습니다.
        Users currentUser = usersRepository.findByUsernameAndDeleteStatus(principal.getName(), DeleteStatus.N)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // --- 수정된 부분: 덮어쓰지 않고 항상 새로 생성하도록 변경 ---
        Folio folio = new Folio(); 
        // ---------------------------------------------------

        folio.setUser(currentUser);
        folio.setIntroduction(requestDto.getIntroduction());
        folio.setSkills(requestDto.getSkills());
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

    /** PPT 에디터 전체 상태 저장(JSON 통짜) */
    @Transactional
    public Folio saveState(Principal principal, FolioStateSaveRequest req) {
        Users user = usersService.getUserByUsername(principal.getName());

        // 유저당 1개 저장 전략 (그대로 유지)
        Folio folio = folioRepository.findByUserId(user.getId())
                .orElse(new Folio());

        folio.setUser(user);
        folio.setTemplate(req.getTemplate() != null ? req.getTemplate() : "dev-basic");
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");

        // ✅ status 기본값
        folio.setStatus(req.getStatus() != null ? req.getStatus() : Folio.Status.DRAFT);

        // ✅ thumbnail 기본값
        if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        } else if (folio.getThumbnail() == null || folio.getThumbnail().isBlank()) {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }

        return folioRepository.save(folio);
    }

    @Transactional
    public Folio publishAsImages(Principal principal, FolioPublishRequest req) {
        Users user = usersService.getUserByUsername(principal.getName());

        // 유저당 1개 전략 유지: 최신 DRAFT 없으면 새로 생성
        Folio folio = folioRepository.findTopByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.DRAFT)
                .orElse(new Folio());
        folio.setUser(user);
        folio.setTemplate(req.getTemplate() != null ? req.getTemplate() : "dev-basic");
        folio.setTitle(req.getTitle() != null && !req.getTitle().isBlank() ? req.getTitle() : user.getName());
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");
        folio.setStatus(Folio.Status.PUBLISHED);

        // 먼저 저장해서 id 확보
        folio = folioRepository.save(folio);

        // uploads/folios/{id}/slides/slide-001.png ...
        var slides = saveBase64Images(folio.getId(), req.getImages());
        folio.setPhotos(slides);

        // 썸네일
        if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        } else if (!slides.isEmpty()) {
            folio.setThumbnail(slides.get(0));
        } else {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }

        return folioRepository.save(folio);
    }

    private List<String> saveBase64Images(String folioId, List<String> dataUrls) {
        if (dataUrls == null || dataUrls.isEmpty()) return List.of();

        try {
            Path root = Paths.get("uploads/folios", folioId, "slides");
            Files.createDirectories(root);

            int idx = 1;
            var out = new java.util.ArrayList<String>();
            for (String dataUrl : dataUrls) {
                // data:image/png;base64,xxxx...
                String base64 = dataUrl.substring(dataUrl.indexOf(",") + 1);
                byte[] bytes = Base64.getDecoder().decode(base64);

                String name = String.format("slide-%03d.png", idx++);
                Path file = root.resolve(name);
                Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                // 정적 리소스 매핑이 file:uploads/ 로 되어 있으니 URL은 /uploads/부터 시작
                String url = "/uploads/folios/" + folioId + "/slides/" + name;
                out.add(url);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("슬라이드 이미지 저장 실패", e);
        }
    }

     // ① 에디터 불러오기 데이터 (edit.js가 기대하는 구조)
    public Optional<Map<String, Object>> getMyDevBasicState(Principal principal) {
        Users user = usersService.getUserByUsername(principal.getName());
        // 우선순위: DRAFT 최신 → 없으면 최신 하나
        Folio folio = folioRepository.findTopByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.DRAFT)
                .or(() -> folioRepository.findTopByUserOrderByUpdatedAtDesc(user))
                .orElse(null);
        if (folio == null) return Optional.empty();

        Map<String, Object> res = new HashMap<>();
        res.put("id", folio.getId());                 // ⚠ dto.id로 쓰니 id 키 제공
        res.put("template", folio.getTemplate());
        res.put("contentJson", folio.getContentJson());
        res.put("status", folio.getStatus());
        res.put("thumbnail", folio.getThumbnail());
        res.put("updatedAt", folio.getUpdatedAt());
        return Optional.of(res);
    }

    // ② 마이페이지 요약 데이터
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
    @Transactional(readOnly = true)
    public Page<FoliosSummaryDto> getFolioSummaries(Pageable pageable) {
        return folioRepository.findAll(pageable).map(FoliosSummaryDto::new);
    }
}