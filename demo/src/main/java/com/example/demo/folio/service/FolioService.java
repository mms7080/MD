package com.example.demo.folio.service;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.dto.PortfolioInFolioDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.portfolios.entity.PortfoliosEntity; // 임시 데이터용
import com.example.demo.portfolios.service.PortfolioService;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolioService {
    
    private final FolioRepository folioRepository;
    private final UsersRepository usersRepository;
    private final PortfolioService portfolioService;
    private final UsersService usersService;

    public Page<FoliosSummaryDto> getFolioSummaries(Pageable pageable) {
        Page<Folio> folioPage = folioRepository.findAll(pageable);
        return folioPage.map(FoliosSummaryDto::new);
    }
    
    public FolioDetailDto getFolioDetail(String id) {
        Folio folio = folioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Folio를 찾을 수 없습니다. id=" + id));
        
        List<PortfolioInFolioDto> projects = folio.getProjectIds().stream()
            .map(projectId -> {
                PortfoliosEntity entity = portfolioService.getPortfolioWithTeam(Long.valueOf(projectId));
                return (entity != null) ? new PortfolioInFolioDto(entity.getId(), entity.getTitle()) : null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());

        return new FolioDetailDto(folio, projects);
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
}