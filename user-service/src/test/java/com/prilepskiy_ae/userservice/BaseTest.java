package com.prilepskiy_ae.userservice;

import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.entity.UserEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseTest {
    protected  final Long USER_ID = 1L;
    protected  final Integer USER_ID_INT = 1;
    protected  final Long SECOND_USER_ID = 2L;
    protected  final Integer SECOND_USER_ID_INT = 2;
    protected  final Long NOT_FOUND_USER_ID = 999L;

    protected  final String USER_NAME = "Alex";
    protected  final String UPDATED_USER_NAME = "Alex Updated";
    protected  final String OLD_USER_NAME = "Old Name";
    protected  final String SECOND_USER_NAME = "Bob";

    protected final String USER_EMAIL = "alex@mail.com";
    protected final String OLD_EMAIL = "old@mail.com";
    protected final String NEW_EMAIL = "new@mail.com";
    protected final String BUSY_EMAIL = "busy@mail.com";
    protected final String FREE_EMAIL = "free@mail.com";
    protected final String SECOND_USER_EMAIL = "bob@mail.com";

    protected final int USER_AGE = 25;
    protected final int OLD_AGE = 20;
    protected final int SECOND_USER_AGE = 30;


    protected final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
    protected final LocalDateTime SECOND_CREATED_AT = LocalDateTime.of(2026, 1, 2, 12, 0);

    protected final String BASE_URL = "/api/users";

    protected UserEntity createUserEntity(
            Long id,
            String name,
            String email,
            int age
    ) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity.setAge(age);
        return userEntity;
    }

    protected UserRequest createUserRequest(
            String name,
            String email,
            int age
    ) {
        return new UserRequest(name, email, age);
    }

    protected void assertUserResponse(
            UserResponse actual,
            Long expectedId,
            String expectedName,
            String expectedEmail,
            int expectedAge
    ) {
        assertEquals(expectedId, actual.getId());
        assertEquals(expectedName, actual.getName());
        assertEquals(expectedEmail, actual.getEmail());
        assertEquals(expectedAge, actual.getAge());
    }
}
