package com.kalado.user;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.user.adapters.repository.UserRepository;
import com.kalado.user.domain.model.User;
import com.kalado.user.service.ImageService;
import com.kalado.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationApi authenticationApi;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private UserProfileUpdateDto validUpdateDto;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .address("123 Test St")
                .build();

        validUpdateDto = UserProfileUpdateDto.builder()
                .id(TEST_USER_ID)
                .firstName("John Updated")
                .lastName("Doe Updated")
                .phoneNumber("0987654321")
                .address("456 New St")
                .build();
    }

    @Nested
    @DisplayName("Profile Update Tests")
    class ProfileUpdateTests {

        @Test
        @DisplayName("Should successfully update profile without image")
        void modifyUserProfile_Success() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(authenticationApi.getUsername(TEST_USER_ID)).thenReturn(TEST_USERNAME);

            ProfileUpdateResponseDto response = userService.modifyUserProfile(validUpdateDto, null);

            assertTrue(response.isSuccess());
            assertNotNull(response.getUpdatedProfile());
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(validUpdateDto.getFirstName(), savedUser.getFirstName());
            assertEquals(validUpdateDto.getLastName(), savedUser.getLastName());
            assertEquals(validUpdateDto.getPhoneNumber(), savedUser.getPhoneNumber());
            assertEquals(validUpdateDto.getAddress(), savedUser.getAddress());
        }

        @Test
        @DisplayName("Should fail when user not found")
        void modifyUserProfile_UserNotFound() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> userService.modifyUserProfile(validUpdateDto, null));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should successfully update profile with image")
        void modifyUserProfile_WithImage_Success() {
            MultipartFile mockFile = mock(MultipartFile.class);
            String imageUrl = "http://example.com/image.jpg";

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(imageService.storeProfileImage(mockFile)).thenReturn(imageUrl);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(authenticationApi.getUsername(TEST_USER_ID)).thenReturn(TEST_USERNAME);

            ProfileUpdateResponseDto response = userService.modifyUserProfile(validUpdateDto, mockFile);

            assertTrue(response.isSuccess());
            verify(imageService).storeProfileImage(mockFile);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(imageUrl, savedUser.getProfileImageUrl());
        }
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {

        @Test
        @DisplayName("Should successfully create user")
        void createUser_Success() {
            UserDto userDto = UserDto.builder()
                    .id(TEST_USER_ID)
                    .username(TEST_USERNAME)
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("1234567890")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> userService.createUser(userDto));

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals(userDto.getFirstName(), savedUser.getFirstName());
            assertEquals(userDto.getLastName(), savedUser.getLastName());
            assertEquals(userDto.getPhoneNumber(), savedUser.getPhoneNumber());
        }
    }

    @Nested
    @DisplayName("User Query Tests")
    class UserQueryTests {

        @Test
        @DisplayName("Should successfully get user profile")
        void getUserProfile_Success() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(authenticationApi.getUsername(TEST_USER_ID)).thenReturn(TEST_USERNAME);

            UserDto result = userService.getUserProfile(TEST_USER_ID);

            assertNotNull(result);
            assertEquals(TEST_USERNAME, result.getUsername());
            assertEquals(testUser.getFirstName(), result.getFirstName());
            assertEquals(testUser.getLastName(), result.getLastName());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserProfile_UserNotFound() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> userService.getUserProfile(TEST_USER_ID));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("User Blocking Tests")
    class UserBlockingTests {

        @Test
        @DisplayName("Should successfully block user")
        void blockUser_Success() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            boolean result = userService.blockUser(TEST_USER_ID);

            assertTrue(result);
            verify(userRepository).modify(
                    testUser.getFirstName(),
                    testUser.getLastName(),
                    testUser.getAddress(),
                    testUser.getPhoneNumber(),
                    testUser.getId(),
                    true
            );
        }

        @Test
        @DisplayName("Should throw exception when blocking non-existent user")
        void blockUser_UserNotFound() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> userService.blockUser(TEST_USER_ID));

            assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
            verify(userRepository, never()).modify(any(), any(), any(), any(), any(), anyBoolean());
        }
    }
}