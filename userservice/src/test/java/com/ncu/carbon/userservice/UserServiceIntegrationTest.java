package com.ncu.carbon.userservice;

import com.ncu.carbon.userservice.model.User;
import com.ncu.carbon.userservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository repository;

    @Test
    void createAndCreditUser() {
        User u = new User("Alice", 100.0);
        User saved = repository.save(u);
        Assertions.assertNotNull(saved.getId());

        saved.setCredits(saved.getCredits() + 50.0);
        repository.save(saved);

        User reloaded = repository.findById(saved.getId()).orElseThrow();
        Assertions.assertEquals(150.0, reloaded.getCredits(), 0.0001);
    }
}
