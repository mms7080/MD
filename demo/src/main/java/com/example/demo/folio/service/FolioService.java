package com.example.demo.folio.service;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.dto.PortfolioInFolioDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.portfolios.controller.PortfoliosController; // 임시 데이터용
import com.example.demo.portfolios.entity.PortfoliosEntity; // 임시 데이터용
import com.example.demo.portfolios.service.PortfolioService;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersService.UsersService;
import com.example.demo.folio.dto.FolioRequestDto;
import com.example.demo.folio.dto.FolioStateSaveRequest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolioService {
    
    private final FolioRepository folioRepository;
    private final UsersRepository usersRepository;
    // 임시로 PortfoliosController의 데이터를 사용
    private final PortfoliosController portfoliosController;
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
        if (principal == null) throw new AccessDeniedException("로그인 필요");

        Users user = usersService.getUserByUsername(principal.getName()); // ★ 정적호출 금지

        // 사용자+템플릿 최신본 조회 후 없으면 신규
        String tpl = (req.getTemplate() != null && !req.getTemplate().isBlank())
                ? req.getTemplate() : "dev-basic";

        Folio folio = folioRepository
                .findTopByUserAndTemplateOrderByUpdatedAtDesc(user, tpl)
                .orElseGet(Folio::new);

        folio.setUser(user);
        folio.setTemplate(tpl);

        // contentJson/thumbnail/status 반영
        folio.setContentJson(
                req.getContentJson() != null ? req.getContentJson() : "{}"
        );
        if (req.getThumbnail() != null) folio.setThumbnail(req.getThumbnail());
        if (req.getStatus() != null) folio.setStatus(req.getStatus());

        return folioRepository.save(folio);
    }

    /** 로그인 사용자의 최신 저장본(템플릿별) */
    public Optional<Folio> getMyLatest(Principal principal, String template) {
        if (principal == null) throw new AccessDeniedException("로그인 필요");
        Users user = usersService.getUserByUsername(principal.getName());
        String tpl = (template != null && !template.isBlank()) ? template : "dev-basic";
        return folioRepository.findTopByUserAndTemplateOrderByUpdatedAtDesc(user, tpl);
    }
}