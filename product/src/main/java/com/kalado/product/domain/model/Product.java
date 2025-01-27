package com.kalado.product.domain.model;

import com.kalado.common.Price;
import com.kalado.common.enums.ProductStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Embedded
  private Price price;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "image_url")
  @Builder.Default
  private List<String> imageUrls = new ArrayList<>();

  @Column(nullable = false)
  private String category;

  private Integer productionYear;

  private String brand;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @CreationTimestamp
  private Timestamp createdAt;

  @Column(nullable = false)
  private Long sellerId;
}