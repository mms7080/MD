package com.example.demo.folio.service;

import com.example.demo.folio.dto.FolioDetailDto;
import com.example.demo.folio.dto.FoliosSummaryDto;
import com.example.demo.folio.entity.Folio;
import com.example.demo.folio.repository.FolioRepository;
import com.example.demo.users.UsersEntity.Role;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.folio.dto.FolioRequestDto;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolioService {
    
    private final FolioRepository folioRepository;
    private final UsersRepository usersRepository;

    @PostConstruct
    @Transactional
    public void init() {
        if (folioRepository.count() == 0) {
            Users user1 = new Users(); user1.setUsername("song"); user1.setName("송준회"); user1.setPassword("password"); user1.setEmail("song@example.com"); user1.setAge(25); user1.setGender("M"); user1.setRole(Role.USER);
            usersRepository.save(user1);
            Users user2 = new Users(); user2.setUsername("hong"); user2.setName("이홍시"); user2.setPassword("password"); user2.setEmail("hong@example.com"); user2.setAge(28); user2.setGender("W"); user2.setRole(Role.USER);
            usersRepository.save(user2);
            
            Folio folio1 = new Folio(); folio1.setUser(user1); folio1.setSkills(List.of("Java", "Spring Boot", "JPA")); folio1.setIntroduction("백엔드 개발자 송준회입니다."); folio1.setThumbnail("https://picsum.photos/seed/dev1/300");
            folioRepository.save(folio1);
            Folio folio2 = new Folio(); folio2.setUser(user2); folio2.setSkills(List.of("Python", "Django", "React")); folio2.setIntroduction("풀스택 개발자 이홍시입니다."); folio2.setThumbnail("https://picsum.photos/seed/dev2/300");
            folioRepository.save(folio2);
        }
    }

    public Page<FoliosSummaryDto> getFolioSummaries(Pageable pageable) {
        Page<Folio> folioPage = folioRepository.findAll(pageable);
        return folioPage.map(FoliosSummaryDto::new);
    }


    
    /**
     * ID로 Folio를 찾아 상세 DTO로 변환하여 반환
     * @param id Folio의 UUID
     * @return FolioDetailDto
     */
    public FolioDetailDto getFolioDetail(String id) {
        Folio folio = folioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 Folio를 찾을 수 없습니다. id=" + id));
        return new FolioDetailDto(folio);
    }

    @Transactional 
    public Folio createOrUpdateFolio(FolioRequestDto requestDto, Principal principal) {
        // 1. 현재 로그인한 사용자 정보 조회
        Users currentUser = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 해당 사용자의 Folio가 이미 있는지 확인 (없으면 새로 생성)
        Folio folio = folioRepository.findByUserId(currentUser.getId())
                .orElse(new Folio());

        // 3. DTO의 데이터로 Folio 엔티티 속성 업데이트
        folio.setUser(currentUser);
        folio.setIntroduction(requestDto.getIntroduction());
        folio.setSkills(requestDto.getSkills());
        folio.setThumbnail(requestDto.getThumbnail()); // 썸네일 정보 저장

        // 4. 데이터베이스에 저장 (새로운 경우 insert, 기존 경우 update)
        return folioRepository.save(folio);
    }
}
