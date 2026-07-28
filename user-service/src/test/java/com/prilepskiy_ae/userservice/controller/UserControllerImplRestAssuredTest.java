package com.prilepskiy_ae.userservice.controller;

import com.prilepskiy_ae.userservice.BaseTest;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.service.user.UserService;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static io.qameta.allure.SeverityLevel.CRITICAL;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("User API REST Assured")
@Feature("UserController ")
@Owner("Prilepskiy Alex")
@ExtendWith(AllureJunit5.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerImplRestAssuredTest extends BaseTest {
    @LocalServerPort
    private int port;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @Story("Создание пользователя средствами REST Assured")
    @Severity(CRITICAL)
    @DisplayName("POST /api/users должен создать пользователя и вернуть 201 Created(Проверка средствами REST  Assured) ")
    @Description("Проверяем, что контроллер принимает UserRequest, вызывает UserService и возвращает UserResponse со статусом 201.")
    void testCreateUserReturns201Created() {
        UserRequest request = createUserRequest(USER_NAME, USER_EMAIL, USER_AGE);
        UserResponse response = new UserResponse(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                USER_AGE,
                CREATED_AT
        );

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(request)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body(
                        "id",  equalTo(USER_ID_INT),
                        "name", equalTo(USER_NAME),
                        "email", equalTo(USER_EMAIL),
                        "age", equalTo(USER_AGE),
                        "createdAt", notNullValue()
                );

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Story("Получение пользователя REST Assured")
    @Severity(CRITICAL)
    @DisplayName("GET /api/users/{id} должен вернуть пользователя по id (Проверка средствами REST  Assured)")
    @Description("Проверяем успешное получение пользователя по идентификатору.")
    @Test
    void testGetUserByIdReturnsUser(){
        UserResponse response = new UserResponse(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                USER_AGE,
                CREATED_AT
        );

        when(userService.getUserById(USER_ID))
                .thenReturn(response);

        given()
                .when()
                .get(BASE_URL + "/{id}", USER_ID)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(USER_ID_INT))
                .body("name", equalTo(USER_NAME))
                .body("email", equalTo(USER_EMAIL))
                .body("age", equalTo(USER_AGE));

        verify(userService, times(1)).getUserById(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Получение списка пользователей")
    @Severity(CRITICAL)
    @DisplayName("GET /api/users должен вернуть список пользователей (Проверка средствами REST  Assured)")
    @Description("Проверяем успешное получение списка пользователей.")
    void testGetUsersReturnsList(){
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

        given()
                .when()
                .get(BASE_URL)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("", hasSize(2))
                .body("[0].id", equalTo(USER_ID_INT))
                .body("[0].name", equalTo(USER_NAME))
                .body("[0].email", equalTo(USER_EMAIL))
                .body("[0].age", equalTo(USER_AGE))
                .body("[1].id", equalTo(SECOND_USER_ID_INT))
                .body("[1].name", equalTo(SECOND_USER_NAME))
                .body("[1].email", equalTo(SECOND_USER_EMAIL))
                .body("[1].age", equalTo(SECOND_USER_AGE));

        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Обновление пользователя")
    @Severity(CRITICAL)
    @DisplayName("PUT /api/users/{id} должен обновить пользователя (Проверка средствами REST  Assured)")
    @Description("Проверяем, что контроллер принимает UserRequest, вызывает обновление в UserService и возвращает обновленного пользователя.")
    void testUpdate(){
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
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_URL + "/{id}", USER_ID)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(USER_ID_INT))
                .body("name", equalTo(UPDATED_USER_NAME))
                .body("email", equalTo(NEW_EMAIL))
                .body("age", equalTo(USER_AGE));

        verify(userService, times(1)).updateUser(eq(USER_ID), any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @Story("Удаление пользователя")
    @Severity(CRITICAL)
    @DisplayName("DELETE /api/users/{id} должен удалить пользователя и вернуть 204 No Content (Проверка средствами REST  Assured)")
    @Description("Проверяем успешное удаление пользователя по идентификатору.")
    void testDelete(){
        doNothing()
                .when(userService)
                .deleteUserById(USER_ID);

        given()
                .when()
                .delete(BASE_URL + "/{id}", USER_ID)
                .then()
                .statusCode(204)
                .body(is(emptyString()));

        verify(userService, times(1)).deleteUserById(USER_ID);
        verifyNoMoreInteractions(userService);
    }
}
