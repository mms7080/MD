package com.example.demo.admin.service;

import com.example.demo.admin.entity.AdminTeam;
import com.example.demo.admin.entity.AdminTeamMember;
import com.example.demo.admin.repository.AdminTeamRepository;
import com.example.demo.admin.repository.AdminTeamMemberRepository;
import com.example.demo.users.UsersEntity.Users;
import com.example.demo.users.UsersRepository.UsersRepository;
import com.example.demo.users.UsersEntity.DeleteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTeamService {
    private final AdminTeamRepository teamRepository;
    private final AdminTeamMemberRepository teamMemberRepository;
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public List<Users> getActiveUsers() {
        return usersRepository.findAll()
                .stream()
                .filter(u -> u.getDeleteStatus() == DeleteStatus.N)
                .toList();
    }

    @Transactional
    public Long createTeam(String name, List<Long> memberIds) {
        AdminTeam team = AdminTeam.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
        teamRepository.save(team);

        List<Users> users = usersRepository.findAllById(memberIds);
        for (Users u : users) {
            teamMemberRepository.save(AdminTeamMember.builder()
                    .team(team)
                    .userId(u.getId())
                    .userName(u.getName())
                    .build());
        }
        return team.getId();
    }

    @Transactional(readOnly = true)
    public List<AdminTeam> getTeams() {
        return teamRepository.findAll();
    }

    @Transactional
    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }
}
