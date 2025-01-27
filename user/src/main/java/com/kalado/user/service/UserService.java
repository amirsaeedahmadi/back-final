package com.kalado.user.service;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.dto.UserProfileUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.user.domain.mapper.UserMapper;
import java.util.Optional;
import com.kalado.user.adapters.repository.UserRepository;

import com.kalado.user.domain.model.Admin;
import com.kalado.user.domain.model.User;
import com.kalado.user.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final AuthenticationApi authenticationApi;
  private final AdminRepository adminRepository;
  private final ImageService imageService;


  @Transactional
  public Boolean modifyUserProfile(UserProfileUpdateDto profileUpdateDto, MultipartFile profileImage) {
    User user = userRepository.findById(profileUpdateDto.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "User not found"));

    try {
      if (profileImage != null) {
        String imageUrl = imageService.storeProfileImage(profileImage);
        user.setProfileImageUrl(imageUrl);
      }

      user.setFirstName(profileUpdateDto.getFirstName());
      user.setLastName(profileUpdateDto.getLastName());
      user.setPhoneNumber(profileUpdateDto.getPhoneNumber());
      user.setAddress(profileUpdateDto.getAddress());

      userRepository.save(user);
      return true;
    } catch (Exception e) {
      log.error("Failed to modify user profile: {}", e.getMessage(), e);
      throw new CustomException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              "Failed to modify user profile: " + e.getMessage()
      );
    }
  }

  public void createUser(UserDto userDto) {
    log.info("Creating a new user with id: {}", userDto.getId());
    User newuser = UserMapper.INSTANCE.dtoTouser(userDto);
    userRepository.save(newuser);
    log.info("Successfully created user with ID: {}", newuser.getId());
  }

  public String getUserAddress(long userID) {
    log.info("Retrieving address for user ID: {}", userID);
    return userRepository
        .findById(userID)
        .map(User::getAddress)
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userID);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
  }

  public UserDto getUserProfile(long userId) {
    log.info("Retrieving user profile for user ID: {}", userId);
    String username = authenticationApi.getUsername(userId);
    var userDb = userRepository
        .findById(userId)
        .map(
            user -> {
              UserDto userDto = UserMapper.INSTANCE.userToDto(user);
              userDto.setUsername(username);
              return userDto;
            })
        .orElseThrow(
            () -> {
              log.error("user ID: {} not found", userId);
              return new CustomException(ErrorCode.NOT_FOUND, "user not found");
            });
    return userDb;
  }

  public void createAdmin(AdminDto adminDto) {
    log.info("Creating a new admin with id: {}", adminDto.getId());
    Admin newAdmin = UserMapper.INSTANCE.dtoToAdmin(adminDto);
    adminRepository.save(newAdmin);
    log.info("Successfully created admin with ID: {}", newAdmin.getId());
  }

  public boolean blockUser(Long id) {
    Optional<User> userOptional = userRepository.findById(id);

    if (userOptional.isPresent()) {
      log.info("Blocking user with ID: {}", id);
      User user = userOptional.get();

      userRepository.modify(
              user.getFirstName(),
              user.getLastName(),
              user.getAddress(),
              user.getPhoneNumber(),
              user.getId(),
              true
      );

      log.info("User blocked successfully. User ID: {}", id);
      return true;
    } else {
      log.warn("Attempted to block non-existent user with ID: {}", id);
      throw new CustomException(ErrorCode.NOT_FOUND, "User not found");
    }
  }
}
