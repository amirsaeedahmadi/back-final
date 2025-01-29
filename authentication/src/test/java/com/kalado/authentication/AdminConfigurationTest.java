package com.kalado.authentication;

import com.kalado.authentication.configuration.AdminConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class AdminConfigurationTest {

    private AdminConfiguration adminConfiguration;
    private static final String TEST_ADMIN_EMAIL = "testadmin@example.com";
    private static final String TEST_GOD_EMAIL = "testgod@example.com";

    @BeforeEach
    void setUp() throws Exception {
        // Create a test properties file in memory
        Properties testProperties = new Properties();
        testProperties.setProperty("authorized.admin.emails",
                String.format("%s,another.admin@example.com", TEST_ADMIN_EMAIL));
        testProperties.setProperty("authorized.god.emails", TEST_GOD_EMAIL);

        adminConfiguration = new AdminConfiguration();

        // Use reflection to set the private fields that would normally be populated from the properties file
        Set<String> adminEmails = Set.of(TEST_ADMIN_EMAIL, "another.admin@example.com");
        Set<String> godEmails = Set.of(TEST_GOD_EMAIL);

        ReflectionTestUtils.setField(adminConfiguration, "authorizedAdminEmails", adminEmails);
        ReflectionTestUtils.setField(adminConfiguration, "authorizedGodEmails", godEmails);
    }

    @Test
    void isEmailAuthorizedForAdmin_ShouldReturnTrue_ForAuthorizedEmail() {
        assertTrue(adminConfiguration.isEmailAuthorizedForAdmin(TEST_ADMIN_EMAIL));
    }

    @Test
    void isEmailAuthorizedForAdmin_ShouldReturnTrue_ForCaseInsensitiveEmail() {
        assertTrue(adminConfiguration.isEmailAuthorizedForAdmin(TEST_ADMIN_EMAIL.toUpperCase()));
    }

    @Test
    void isEmailAuthorizedForAdmin_ShouldReturnFalse_ForUnauthorizedEmail() {
        assertFalse(adminConfiguration.isEmailAuthorizedForAdmin("unauthorized@example.com"));
    }

    @Test
    void isEmailAuthorizedForAdmin_ShouldReturnFalse_ForNullEmail() {
        assertFalse(adminConfiguration.isEmailAuthorizedForAdmin(null));
    }

    @Test
    void isEmailAuthorizedForGod_ShouldReturnTrue_ForAuthorizedEmail() {
        assertTrue(adminConfiguration.isEmailAuthorizedForGod(TEST_GOD_EMAIL));
    }

    @Test
    void isEmailAuthorizedForGod_ShouldReturnTrue_ForCaseInsensitiveEmail() {
        assertTrue(adminConfiguration.isEmailAuthorizedForGod(TEST_GOD_EMAIL.toUpperCase()));
    }

    @Test
    void isEmailAuthorizedForGod_ShouldReturnFalse_ForUnauthorizedEmail() {
        assertFalse(adminConfiguration.isEmailAuthorizedForGod("unauthorized@example.com"));
    }

    @Test
    void isEmailAuthorizedForGod_ShouldReturnFalse_ForNullEmail() {
        assertFalse(adminConfiguration.isEmailAuthorizedForGod(null));
    }

    @Test
    void getAuthorizedAdminEmails_ShouldReturnCopy() {
        Set<String> emails = adminConfiguration.getAuthorizedAdminEmails();
        assertNotNull(emails);
        assertTrue(emails.contains(TEST_ADMIN_EMAIL));

        emails.add("new@example.com");
        assertFalse(adminConfiguration.isEmailAuthorizedForAdmin("new@example.com"));
    }

    @Test
    void getAuthorizedGodEmails_ShouldReturnCopy() {
        Set<String> emails = adminConfiguration.getAuthorizedGodEmails();
        assertNotNull(emails);
        assertTrue(emails.contains(TEST_GOD_EMAIL));

        emails.add("new@example.com");
        assertFalse(adminConfiguration.isEmailAuthorizedForGod("new@example.com"));
    }
}