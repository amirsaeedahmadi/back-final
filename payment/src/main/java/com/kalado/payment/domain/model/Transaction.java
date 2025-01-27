package com.kalado.payment.domain.model;

import com.kalado.common.Price;
import com.kalado.common.enums.TransactionStatus;
import java.util.UUID;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "transaction")
public class Transaction {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "status", length = 32, columnDefinition = "varchar(32) default 'NOT_PAID' ")
  @Enumerated(EnumType.STRING)
  private TransactionStatus status;

  @Embedded private Price price;
  private long orderId;
}
