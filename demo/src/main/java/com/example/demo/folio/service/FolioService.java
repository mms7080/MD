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
    private final PortfolioService portfolioService; // ✅ 컨트롤러 주입 제거
    private final UsersService usersService;

    /** 공개 목록: PUBLISHED만 (썸네일/제목/작성일/유저) */
    public Page<FoliosSummaryDto> getFolioSummaries(Pageable pageable) {
        Page<Folio> page = folioRepository.findByStatusOrderByCreatedAtDesc(Folio.Status.PUBLISHED, pageable);
        return page.map(FoliosSummaryDto::new);
    }

    /** 상세: 사진/스킬/프로젝트까지 페치 */
    public FolioDetailDto getFolioDetail(String id) {
        Folio folio = folioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Folio를 찾을 수 없습니다. id=" + id));

        List<PortfolioInFolioDto> projects = folio.getProjectIds().stream()
                .map(pid -> {
                    try {
                        PortfoliosEntity e = portfolioService.getPortfolioWithTeam(Long.valueOf(pid));
                        return (e != null) ? new PortfolioInFolioDto(e.getId(), e.getTitle()) : null;
                    } catch (NumberFormatException nfe) {
                        return null; // projectIds에 숫자가 아닐 수 있음 → 무시
                    }
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        return new FolioDetailDto(folio, projects);
    }

    /** 최초 생성(또는 새로운 버전 생성) – 기본 DRAFT */
    @Transactional
    public Folio createOrUpdateFolio(FolioRequestDto req, Principal principal) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users currentUser = usersRepository
                .findByUsernameAndDeleteStatus(principal.getName(), DeleteStatus.N)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Folio folio = new Folio();
        folio.setUser(currentUser);

        // 제목/소개/태그/사진/프로젝트
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            folio.setTitle(req.getTitle());
        }
        folio.setIntroduction(req.getIntroduction());
        folio.setSkills(req.getSkills());
        folio.setPhotos(req.getPhotos());
        folio.setProjectIds(req.getProjectIds());

        // 썸네일: 사진 첫 장 또는 디폴트
        if (req.getPhotos() != null && !req.getPhotos().isEmpty()) {
            folio.setThumbnail(req.getPhotos().get(0));
        } else if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        } else {
            folio.setThumbnail("https://picsum.photos/seed/default/300");
        }

        // 템플릿/상태/에디터JSON
        if (req.getTemplate() != null && !req.getTemplate().isBlank()) {
            folio.setTemplate(req.getTemplate());
        }
        // 기본 DRAFT (엔티티 디폴트)
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");

        return folioRepository.save(folio);
    }

    /** PPT 에디터 전체 상태 저장(JSON 통짜) – 템플릿별 최신본 유지 */
    @Transactional
    public Folio saveState(Principal principal, FolioStateSaveRequest req) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users user = usersService.getUserByUsername(principal.getName());

        String tpl = (req.getTemplate() != null && !req.getTemplate().isBlank())
                ? req.getTemplate() : "dev-basic";

        Folio folio = folioRepository
                .findTopByUserAndTemplateOrderByUpdatedAtDesc(user, tpl)
                .orElseGet(Folio::new);

        folio.setUser(user);
        folio.setTemplate(tpl);
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            folio.setTitle(req.getTitle());
        }
        folio.setContentJson(req.getContentJson() != null ? req.getContentJson() : "{}");

        if (req.getThumbnail() != null && !req.getThumbnail().isBlank()) {
            folio.setThumbnail(req.getThumbnail());
        }
        if (req.getStatus() != null) {
            folio.setStatus(req.getStatus()); // DRAFT ↔ PUBLISHED 전환
        }

        return folioRepository.save(folio);
    }
    public int countMyPublished(Principal principal) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users user = usersService.getUserByUsername(principal.getName());
        return folioRepository.countByUserAndStatus(user, Folio.Status.PUBLISHED);
    }

    public List<String> getMyRecentPublishedTitles(Principal principal, int limit) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users user = usersService.getUserByUsername(principal.getName());
        return folioRepository
                .findTop3ByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.PUBLISHED)
                .stream()
                .limit(limit)
                .map(Folio::getTitle)
                .toList();
    }

    /** 로그인 사용자의 최신 저장본(템플릿별) */
    public Optional<Folio> getMyLatest(Principal principal, String template) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users user = usersService.getUserByUsername(principal.getName());
        String tpl = (template != null && !template.isBlank()) ? template : "dev-basic";
        return folioRepository.findTopByUserAndTemplateOrderByUpdatedAtDesc(user, tpl);
    }

    /** 내 공개 목록 (마이페이지) – PUBLISHED만 */
    public Page<FoliosSummaryDto> getMyPublished(Principal principal, Pageable pageable) {
        if (principal == null) throw new AccessDeniedException("로그인이 필요합니다.");
        Users user = usersService.getUserByUsername(principal.getName());
        Page<Folio> page = folioRepository.findByUserAndStatusOrderByUpdatedAtDesc(user, Folio.Status.PUBLISHED, pageable);
        return page.map(FoliosSummaryDto::new);
    }
}