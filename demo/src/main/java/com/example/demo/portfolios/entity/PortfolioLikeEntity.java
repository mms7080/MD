package com.example.demo.portfolios.entity;

import com.example.demo.users.UsersEntity.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "portfolio_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfoliosEntity portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

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
