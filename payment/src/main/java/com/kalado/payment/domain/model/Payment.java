package com.kalado.payment.domain.model;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "payment")
public class Payment {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @CreationTimestamp
  @Column(name = "create_time")
  private Timestamp createTime;

  @Column(name = "transaction_id")
  private String transactionId;
}
