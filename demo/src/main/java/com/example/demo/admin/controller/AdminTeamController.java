package com.example.demo.admin.controller;

import com.example.demo.admin.service.AdminTeamService;
import com.example.demo.users.UsersEntity.DeleteStatus;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTeamController {

    private final AdminTeamService teamService;
    private final UsersRepository usersRepository;

    // ✅ 기본 유저 목록 (팀 편성용)
    @GetMapping("/users/basic")
    public List<Map<String, Object>> listUsersBasic() {
        List<Users> users = teamService.getActiveUsers();

        return users.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ✅ 팀 목록
    @GetMapping("/teams")
    public List<Map<String, Object>> listTeams() {
        return teamService.getTeams().stream()
                .map(t -> Map.of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "members", t.getMembers().stream()
                                .map(m -> m.getUserName())
                                .toList(),
                        "createdAt", t.getCreatedAt().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ))
                .toList();
    }

    // ✅ 팀 생성
    @PostMapping("/teams")
    public Map<String, Object> createTeam(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        List<Integer> memberIdsRaw = (List<Integer>) body.get("memberIds");
        List<Long> memberIds = memberIdsRaw.stream().map(Integer::longValue).toList();

        Long id = teamService.createTeam(name, memberIds);
        return Map.of("id", id);
    }

    // ✅ 팀 삭제
    @DeleteMapping("/teams/{id}")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

    // ✅ 유저 목록 (페이징 + 나이 미입력 처리)
    @GetMapping("/users")
    public Map<String, Object> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 페이지 요청 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Users> userPage = usersRepository.findAll(pageable);

        // 탈퇴 제외 필터 + 매핑
        List<Map<String, Object>> userList = userPage.getContent().stream()
                .filter(u -> u.getDeleteStatus() == DeleteStatus.N)
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    map.put("name", u.getName());

                    // 나이 미입력 처리
                    if (u.getAge() == null || u.getAge() == 0) {
                        map.put("age", "미입력");
                    } else {
                        map.put("age", u.getAge() + "세");
                    }

                    map.put("deleteStatus", u.getDeleteStatus().name());
                    return map;
                })
                .toList();

        // 결과 구성 (페이징 정보 포함)
        Map<String, Object> result = new HashMap<>();
        result.put("users", userList);
        result.put("currentPage", page + 1);
        result.put("totalPages", userPage.getTotalPages());
        result.put("totalItems", userPage.getTotalElements());
        return result;
    }

    // ✅ 회원 탈퇴 (Soft Delete)
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Optional<Users> opt = usersRepository.findById(id);
        if (opt.isEmpty()) return Map.of("success", false, "message", "유저 없음");

        Users u = opt.get();
        u.setDeleteStatus(DeleteStatus.Y);
        u.setDeletedAt(java.time.LocalDateTime.now());
        usersRepository.save(u);

        return Map.of("success", true);
    }
}
