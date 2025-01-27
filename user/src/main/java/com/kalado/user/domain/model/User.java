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
@Table(name = "users")
public class User {

  @Id private long id;

  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String address;
  private boolean blocked;

  @ElementCollection
  @CollectionTable(name = "user_orders", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "order_id")
  @Singular
  private List<String> orderIds = new ArrayList<>();

  @Column(name = "profile_image_url")
  private String profileImageUrl;
}