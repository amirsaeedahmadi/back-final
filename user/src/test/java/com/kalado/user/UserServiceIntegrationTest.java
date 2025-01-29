// package com.kalado.user;
// import com.kalado.common.dto.*;
// import com.kalado.common.exception.CustomException;
// import com.kalado.common.feign.authentication.AuthenticationApi;
// import com.kalado.user.adapters.repository.UserRepository;
// import com.kalado.user.domain.model.User;
// import com.kalado.user.service.ImageService;
// import com.kalado.user.service.UserService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.transaction.annotation.Transactional;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.Mockito.when;
//
// @SpringBootTest
// class UserServiceIntegrationTest {
//
//     @Autowired
//     private UserService userService;
//
//     @Autowired
//     private UserRepository userRepository;
//
//     @MockBean
//     private AuthenticationApi authenticationApi;
//
//     @MockBean
//     private ImageService imageService;
//
//     private static final Long TEST_USER_ID = 1L;
//     private static final String TEST_USERNAME = "test@example.com";
//
//     private User testUser;
//     private UserProfileUpdateDto updateDto;
//
//     @BeforeEach
//     void setUp() {
//         userRepository.deleteAll();
//
//         testUser = User.builder()
//                 .id(TEST_USER_ID)
//                 .firstName("John")
//                 .lastName("Doe")
//                 .phoneNumber("1234567890")
//                 .address("123 Test St")
//                 .build();
//
//         updateDto = UserProfileUpdateDto.builder()
//                 .id(TEST_USER_ID)
//                 .firstName("John Updated")
//                 .lastName("Doe Updated")
//                 .phoneNumber("0987654321")
//                 .address("456 New St")
//                 .build();
//
//         when(authenticationApi.getUsername(anyLong())).thenReturn(TEST_USERNAME);
//     }
//
//     @Test
//     @Transactional
//     @DisplayName("Should handle concurrent profile updates")
//     void concurrentProfileUpdates() {
//         // Create initial user
//         userRepository.save(testUser);
//
//         // Create two different update DTOs
//         UserProfileUpdateDto updateDto1 = UserProfileUpdateDto.builder()
//                 .id(TEST_USER_ID)
//                 .firstName("Update 1")
//                 .lastName("Doe 1")
//                 .build();
//
//         UserProfileUpdateDto updateDto2 = UserProfileUpdateDto.builder()
//                 .id(TEST_USER_ID)
//                 .firstName("Update 2")
//                 .lastName("Doe 2")
//                 .build();
//
//         // Perform updates in quick succession
//         ProfileUpdateResponseDto response1 = userService.modifyUserProfile(updateDto1, null);
//         ProfileUpdateResponseDto response2 = userService.modifyUserProfile(updateDto2, null);
//
//         // Verify final state
//         assertTrue(response1.isSuccess());
//         assertTrue(response2.isSuccess());
//
//         User finalUser = userRepository.findById(TEST_USER_ID).orElseThrow();
//         assertEquals("Update 2", finalUser.getFirstName());
//         assertEquals("Doe 2", finalUser.getLastName());
//     }
//
//     @Test
//     @Transactional
//     @DisplayName("Should handle profile update with invalid data")
//     void invalidProfileUpdate() {
//         // Save initial user
//         userRepository.save(testUser);
//
//         // Attempt update with invalid phone number
//         UserProfileUpdateDto invalidDto = UserProfileUpdateDto.builder()
//                 .id(TEST_USER_ID)
//                 .firstName("John")
//                 .lastName("Doe")
//                 .phoneNumber("invalid")
//                 .build();
//
//         // Verify that original data remains unchanged
//         User unchangedUser = userRepository.findById(TEST_USER_ID).orElseThrow();
//         assertEquals(testUser.getPhoneNumber(), unchangedUser.getPhoneNumber());
//     }
//
//     @Test
//     @Transactional
//     @DisplayName("Should handle profile image operations correctly")
//     void profileImageOperations() {
//         // Save initial user
//         userRepository.save(testUser);
//
//         // Create multiple profile images
//         MockMultipartFile image1 = new MockMultipartFile(
//                 "profileImage1",
//                 "test1.jpg",
//                 "image/jpeg",
//                 "test image content 1".getBytes()
//         );
//
//         MockMultipartFile image2 = new MockMultipartFile(
//                 "profileImage2",
//                 "test2.jpg",
//                 "image/jpeg",
//                 "test image content 2".getBytes()
//         );
//
//         String imageUrl1 = "http://example.com/test1.jpg";
//         String imageUrl2 = "http://example.com/test2.jpg";
//
//         when(imageService.storeProfileImage(image1)).thenReturn(imageUrl1);
//         when(imageService.storeProfileImage(image2)).thenReturn(imageUrl2);
//
//         // Update profile with first image
//         UserProfileUpdateDto updateDto1 = updateDto.toBuilder().build();
//         ProfileUpdateResponseDto response1 = userService.modifyUserProfile(updateDto1, image1);
//         assertTrue(response1.isSuccess());
//         assertEquals(imageUrl1, userRepository.findById(TEST_USER_ID).orElseThrow().getProfileImageUrl());
//
//         // Update profile with second image
//         UserProfileUpdateDto updateDto2 = updateDto.toBuilder().build();
//         ProfileUpdateResponseDto response2 = userService.modifyUserProfile(updateDto2, image2);
//         assertTrue(response2.isSuccess());
//         assertEquals(imageUrl2, userRepository.findById(TEST_USER_ID).orElseThrow().getProfileImageUrl());
//     }
//
//     @Test
//     @Transactional
//     @DisplayName("Should handle user address operations")
//     void userAddressOperations() {
//         // Save initial user
//         userRepository.save(testUser);
//
//         // Test getting user address
//         String address = userService.getUserAddress(TEST_USER_ID);
//         assertEquals(testUser.getAddress(), address);
//
//         // Update address
//         UserProfileUpdateDto addressUpdateDto = updateDto.toBuilder()
//                 .address("789 New Address St")
//                 .build();
//
//         ProfileUpdateResponseDto response = userService.modifyUserProfile(addressUpdateDto, null);
//         assertTrue(response.isSuccess());
//
//         // Verify updated address
//         String updatedAddress = userService.getUserAddress(TEST_USER_ID);
//         assertEquals("789 New Address St", updatedAddress);
//
//         // Test getting address for non-existent user
//         assertThrows(CustomException.class, () -> userService.getUserAddress(999L));
//     }
// }