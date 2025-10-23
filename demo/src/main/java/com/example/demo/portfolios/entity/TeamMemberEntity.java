package com.example.demo.portfolios.entity;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 포트폴리오와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="portfolio_id")
    private PortfoliosEntity portfolio;


    @Column(name = "member_name")
    private String memberName;

    @Column(name = "member_role")
    private String memberRole;

    // ✅ 담당 파트 (순서 유지)
    @Column(name = "part")
    
    private String parts;

    // 생성자 (portfolio 없이)
    public TeamMemberEntity( String memberName, String memberRole, String parts) {
        
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.parts = parts;
    }

    @Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TeamMemberEntity)) return false;
    return id != null && id.equals(((TeamMemberEntity) o).getId());
}

@Override
public int hashCode() {
    return getClass().hashCode();
}


}
