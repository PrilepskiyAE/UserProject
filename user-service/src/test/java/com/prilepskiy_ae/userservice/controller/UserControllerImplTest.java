package com.prilepskiy_ae.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prilepskiy_ae.userservice.BaseTest;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.service.user.UserService;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Epic("User API")
@Feature("UserController ")
@Owner("Prilepskiy Alex")
@ExtendWith(AllureJunit5.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerImplTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    @Story("Создание пользователя")
    @Severity(CRITICAL)
    @DisplayName("POST /api/users должен создать пользователя и вернуть 201 Created")
    @Description("Проверяем, что контроллер принимает UserRequest, вызывает UserService и возвращает UserResponse со статусом 201.")
    void testCreateUserReturns201Created() throws Exception {
        UserRequest request = createUserRequest(USER_NAME, USER_EMAIL, USER_AGE);
        UserResponse response = new UserResponse(USER_ID, USER_NAME, USER_EMAIL, USER_AGE, CREATED_AT);

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.age").value(USER_AGE))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T12:00:00"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/users/" + USER_ID))
                .andExpect(jsonPath("$._links.collection.href").value("http://localhost/api/users"))
                .andExpect(header().string("Location", "http://localhost/api/users/" + USER_ID));

        verify(userService, times(1)).createUser(any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Получение пользователя")
    @Severity(CRITICAL)
    @DisplayName("GET /api/users/{id} должен вернуть пользователя по id")
    @Description("Проверяем успешное получение пользователя по идентификатору.")
    void testGetUserByIdReturnsUser() throws Exception {
        UserResponse response = new UserResponse(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                USER_AGE,
                CREATED_AT
        );

        when(userService.getUserById(USER_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.age").value(USER_AGE))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T12:00:00"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/users/" + USER_ID))
                .andExpect(jsonPath("$._links.collection.href").value("http://localhost/api/users"));

        verify(userService, times(1)).getUserById(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Получение списка пользователей")
    @Severity(CRITICAL)
    @DisplayName("GET /api/users должен вернуть список пользователей")
    @Description("Проверяем успешное получение списка пользователей.")
    void testGetUsersReturnsList() throws Exception {
        List<UserResponse> response = List.of(
                new UserResponse(
                        USER_ID,
                        USER_NAME,
                        USER_EMAIL,
                        USER_AGE,
                        CREATED_AT
                ),
                new UserResponse(
                        SECOND_USER_ID,
                        SECOND_USER_NAME,
                        SECOND_USER_EMAIL,
                        SECOND_USER_AGE,
                        SECOND_CREATED_AT
                )
        );

        when(userService.getAllUsers())
                .thenReturn(response);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andExpect(jsonPath("$._embedded.userResponseList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.userResponseList[0].id").value(USER_ID))
                .andExpect(jsonPath("$._embedded.userResponseList[0].name").value(USER_NAME))
                .andExpect(jsonPath("$._embedded.userResponseList[0].email").value(USER_EMAIL))
                .andExpect(jsonPath("$._embedded.userResponseList[0].age").value(USER_AGE))
                .andExpect(jsonPath("$._embedded.userResponseList[0].createdAt").value("2026-01-01T12:00:00"))
                .andExpect(jsonPath("$._embedded.userResponseList[0]._links.self.href").value("http://localhost/api/users/" + USER_ID))
                .andExpect(jsonPath("$._embedded.userResponseList[1].id").value(SECOND_USER_ID))
                .andExpect(jsonPath("$._embedded.userResponseList[1].name").value(SECOND_USER_NAME))
                .andExpect(jsonPath("$._embedded.userResponseList[1].email").value(SECOND_USER_EMAIL))
                .andExpect(jsonPath("$._embedded.userResponseList[1].age").value(SECOND_USER_AGE))
                .andExpect(jsonPath("$._embedded.userResponseList[1].createdAt").value("2026-01-02T12:00:00"))
                .andExpect(jsonPath("$._embedded.userResponseList[1]._links.self.href").value("http://localhost/api/users/" + SECOND_USER_ID))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Обновление пользователя")
    @Severity(CRITICAL)
    @DisplayName("PUT /api/users/{id} должен обновить пользователя")
    @Description("Проверяем, что контроллер принимает UserRequest, вызывает обновление в UserService и возвращает обновленного пользователя.")
    void testUpdate() throws Exception {
        UserRequest request = createUserRequest(
                UPDATED_USER_NAME,
                NEW_EMAIL,
                USER_AGE
        );

        UserResponse response = new UserResponse(
                USER_ID,
                UPDATED_USER_NAME,
                NEW_EMAIL,
                USER_AGE,
                CREATED_AT
        );

        when(userService.updateUser(eq(USER_ID), any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(UPDATED_USER_NAME))
                .andExpect(jsonPath("$.email").value(NEW_EMAIL))
                .andExpect(jsonPath("$.age").value(USER_AGE))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T12:00:00"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/users/" + USER_ID))
                .andExpect(jsonPath("$._links.collection.href").exists()); // collection может быть, а может и не быть — лучше exists(), чем value()


        verify(userService, times(1)).updateUser(eq(USER_ID), any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Удаление пользователя")
    @Severity(CRITICAL)
    @DisplayName("DELETE /api/users/{id} должен удалить пользователя и вернуть 204 No Content")
    @Description("Проверяем успешное удаление пользователя по идентификатору.")
    void testDelete() throws Exception {
        doNothing()
                .when(userService)
                .deleteUserById(USER_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", USER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService, times(1)).deleteUserById(USER_ID);
        verifyNoMoreInteractions(userService);
    }

}




