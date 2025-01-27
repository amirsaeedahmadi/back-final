package com.kalado.authentication.domain.model;

import com.kalado.common.enums.Role;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "authentication_info")
public class AuthenticationInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long userId;

  private String username;
  private String password;

  @Column(name = "role", length = 32, columnDefinition = "varchar(32) default 'USER' ")
  @Enumerated(EnumType.STRING)
  private Role role;
}
