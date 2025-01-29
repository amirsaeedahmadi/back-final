//package com.kalado.authentication;
//
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.mail.host=smtp.gmail.com",
//        "spring.mail.port=587",
//        "spring.mail.username=test@example.com",
//        "spring.mail.password=test"
//})
//@Transactional
//public abstract class BaseIntegrationTest {
//
//    protected void clearDatabase(javax.persistence.EntityManager em) {
//        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
//        em.createNativeQuery("TRUNCATE TABLE verification_tokens").executeUpdate();
//        em.createNativeQuery("TRUNCATE TABLE password_reset_tokens").executeUpdate();
//        em.createNativeQuery("TRUNCATE TABLE authentication_info").executeUpdate();
//        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
//        em.flush();
//    }
//}