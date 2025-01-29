package com.kalado.authentication.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
public class AdminConfiguration {
    private Set<String> authorizedAdminEmails = new HashSet<>();
    private Set<String> authorizedGodEmails = new HashSet<>();

    @PostConstruct
    public void loadAuthorizedEmails() {
        try {
            Resource resource = new ClassPathResource("admin-emails.properties");
            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            String adminEmailsString = properties.getProperty("authorized.admin.emails", "");
            String[] adminEmails = adminEmailsString.split(",");
            for (String email : adminEmails) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty()) {
                    authorizedAdminEmails.add(trimmedEmail.toLowerCase());
                }
            }

            String godEmailsString = properties.getProperty("authorized.god.emails", "");
            String[] godEmails = godEmailsString.split(",");
            for (String email : godEmails) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty()) {
                    authorizedGodEmails.add(trimmedEmail.toLowerCase());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load authorized emails", e);
        }
    }

    public boolean isEmailAuthorizedForAdmin(String email) {
        return email != null && authorizedAdminEmails.contains(email.toLowerCase());
    }

    public boolean isEmailAuthorizedForGod(String email) {
        return email != null && authorizedGodEmails.contains(email.toLowerCase());
    }

    public Set<String> getAuthorizedAdminEmails() {
        return new HashSet<>(authorizedAdminEmails);
    }

    public Set<String> getAuthorizedGodEmails() {
        return new HashSet<>(authorizedGodEmails);
    }
}