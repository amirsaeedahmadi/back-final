package com.kalado.user.domain.model;

import java.util.ArrayList;
import java.util.List;
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
@Table(name = "admins")
public class Admin {

  @Id private long id;

  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String address;

  @ElementCollection
  @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
  @Column(name = "permission")
  @Singular
  private List<String> permissions = new ArrayList<>();
}