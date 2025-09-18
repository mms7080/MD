package com.example.demo.portfolios.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDto {
    
    private String teamName;
    private String memberName;
    private String memberRole;
    private List<String> parts;
}
