package com.kalado.user.adapters.repository;

import com.kalado.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByPhoneNumber(String phoneNumber);

  @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName, " +
          "u.address = :address, u.phoneNumber = :phoneNumber, u.blocked = :blocked " +
          "WHERE u.id = :id")
  @Modifying
  @Transactional
  void modify(
          @Param("firstName") String firstName,
          @Param("lastName") String lastName,
          @Param("address") String address,
          @Param("phoneNumber") String phoneNumber,
          @Param("id") Long id,
          @Param("blocked") boolean blocked);
}