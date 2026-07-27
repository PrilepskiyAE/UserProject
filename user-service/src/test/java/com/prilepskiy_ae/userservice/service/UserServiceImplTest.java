package com.prilepskiy_ae.userservice.service;

import com.prilepskiy_ae.userservice.BaseTest;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.entity.UserEntity;
import com.prilepskiy_ae.userservice.exception.UserAlreadyExistsException;
import com.prilepskiy_ae.userservice.exception.UserNotFoundException;
import com.prilepskiy_ae.userservice.repository.UserRepository;
import com.prilepskiy_ae.userservice.service.user.UserServiceImpl;
import com.prilepskiy_ae.userservice.service.userEvent.UserEventProducer;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Epic("Service Layer Tests")
@Feature("UserServiceImpl Business Logic")

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest extends BaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserEventProducer producer;

    @Test
    @Story("Create user")
    @Severity(CRITICAL)
    @DisplayName("Create user: should save user and return response")
    @Description("""
            Проверяет успешное создание пользователя.
            Сервис должен сохранить пользователя через repository.save()
            и вернуть UserResponse с корректными id, name, email и age.
            """)
    @Tag("create")
    void test1() {
        UserRequest request = createUserRequest(USER_NAME, USER_EMAIL, USER_AGE);
        UserEntity savedEntity = createUserEntity(USER_ID, USER_NAME, USER_EMAIL, USER_AGE);

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertUserResponse(result, USER_ID, USER_NAME, USER_EMAIL, USER_AGE);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @Story("Get user by id")
    @Severity(CRITICAL)
    @DisplayName("Get user by id: existing user should return response")
    @Description("""
            Проверяет получение пользователя по существующему id.
            Repository возвращает Optional с UserEntity,
            сервис должен вернуть UserResponse с корректными данными.
            """)
    @Tag("get-by-id")
    @Tag("positive")
    void test2() {
        UserEntity entity = createUserEntity(USER_ID, USER_NAME, USER_EMAIL, USER_AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(entity));

        UserResponse result = userService.getUserById(USER_ID);

        assertNotNull(result);
        assertUserResponse(result, USER_ID, USER_NAME, USER_EMAIL, USER_AGE);

        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    @Story("Get user by id")
    @Severity(CRITICAL)
    @DisplayName("Get user by id: not found user should throw UserNotFoundException")
    @Description("""
            Проверяет сценарий, когда пользователь по id не найден.
            Repository возвращает Optional.empty(),
            сервис должен выбросить UserNotFoundException.
            """)
    @Tag("get-by-id")
    @Tag("negative")
    void test3() {
        when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(NOT_FOUND_USER_ID)
        );

        verify(userRepository, times(1)).findById(NOT_FOUND_USER_ID);
    }

    @Test
    @Story("Get all users")
    @Severity(CRITICAL)
    @DisplayName("Get all users: should return list of user responses")
    @Description("""
            Проверяет получение списка пользователей.
            Repository возвращает список из двух UserEntity,
            сервис должен вернуть список UserResponse такого же размера
            и с корректными данными.
            """)
    @Tag("get-all")
    @Tag("positive")
    void test4() {
        UserEntity firstUser = createUserEntity(USER_ID, USER_NAME, USER_EMAIL, USER_AGE);
        UserEntity secondUser = createUserEntity(
                SECOND_USER_ID,
                SECOND_USER_NAME,
                SECOND_USER_EMAIL,
                SECOND_USER_AGE
        );

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertUserResponse(result.get(0), USER_ID, USER_NAME, USER_EMAIL, USER_AGE);
        assertUserResponse(result.get(1), SECOND_USER_ID, SECOND_USER_NAME, SECOND_USER_EMAIL, SECOND_USER_AGE);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @Story("Get all users")
    @Severity(CRITICAL)
    @DisplayName("Get all users: empty repository should return empty list")
    @Description("""
            Проверяет получение пользователей при пустом репозитории.
            Repository возвращает пустой список,
            сервис должен вернуть пустой список без исключений.
            """)
    @Tag("get-all")
    @Tag("empty")
    @Tag("positive")
    void test5() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @Story("Update user")
    @Severity(CRITICAL)
    @DisplayName("Update user: existing user with same email should be updated")
    @Description("""
            Проверяет обновление пользователя, когда email не изменился.
            Сервис не должен проверять existsByEmail(),
            так как email остался прежним.
            """)
    @Tag("update")
    @Tag("positive")
    void test6() {
        UserEntity existingEntity = createUserEntity(USER_ID, OLD_USER_NAME, USER_EMAIL, OLD_AGE);
        UserRequest request = createUserRequest(UPDATED_USER_NAME, USER_EMAIL, USER_AGE);
        UserEntity updatedEntity = createUserEntity(USER_ID, UPDATED_USER_NAME, USER_EMAIL, USER_AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(updatedEntity);

        UserResponse result = userService.updateUser(USER_ID, request);

        assertNotNull(result);
        assertUserResponse(result, USER_ID, UPDATED_USER_NAME, USER_EMAIL, USER_AGE);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(existingEntity);
    }

    @Test
    @Story("Update user")
    @Severity(CRITICAL)
    @DisplayName("Update user: changed free email should update user")
    @Description("""
            Проверяет обновление пользователя, когда email изменился и новый email свободен.
            Сервис должен проверить existsByEmail(newEmail), получить false,
            сохранить обновлённого пользователя и вернуть UserResponse.
            """)
    @Tag("update")
    @Tag("email")
    @Tag("positive")
    void test7() {
        UserEntity existingEntity = createUserEntity(USER_ID, OLD_USER_NAME, OLD_EMAIL, OLD_AGE);
        UserRequest request = createUserRequest(UPDATED_USER_NAME, NEW_EMAIL, USER_AGE);
        UserEntity updatedEntity = createUserEntity(USER_ID, UPDATED_USER_NAME, NEW_EMAIL, USER_AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(userRepository.save(existingEntity)).thenReturn(updatedEntity);

        UserResponse result = userService.updateUser(USER_ID, request);

        assertNotNull(result);
        assertUserResponse(result, USER_ID, UPDATED_USER_NAME, NEW_EMAIL, USER_AGE);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).existsByEmail(NEW_EMAIL);
        verify(userRepository, times(1)).save(existingEntity);
    }

    @Test
    @Story("Update user")
    @Severity(CRITICAL)
    @DisplayName("Update user: not found user should throw UserNotFoundException")
    @Description("""
            Проверяет обновление несуществующего пользователя.
            Repository возвращает Optional.empty(),
            сервис должен выбросить UserNotFoundException.
            Проверки email и сохранения быть не должно.
            """)
    @Tag("update")
    @Tag("negative")
    void test8() {
        UserRequest request = createUserRequest(USER_NAME, USER_EMAIL, USER_AGE);

        when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(NOT_FOUND_USER_ID, request)
        );

        verify(userRepository, times(1)).findById(NOT_FOUND_USER_ID);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @Story("Update user")
    @Severity(CRITICAL)
    @DisplayName("Update user: changed busy email should throw UserAlreadyExistsException")
    @Description("""
            Проверяет обновление пользователя, когда новый email уже занят.
            Сервис должен вызвать existsByEmail(),
            получить true и выбросить UserAlreadyExistsException.
            Сохранение пользователя выполняться не должно.
            """)
    @Tag("update")
    @Tag("email")
    @Tag("negative")
    void test9() {
        UserEntity existingEntity = createUserEntity(USER_ID, OLD_USER_NAME, OLD_EMAIL, OLD_AGE);
        UserRequest request = createUserRequest(UPDATED_USER_NAME, BUSY_EMAIL, USER_AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));
        when(userRepository.existsByEmail(BUSY_EMAIL)).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(USER_ID, request)
        );

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).existsByEmail(BUSY_EMAIL);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @Story("Update user")
    @Severity(CRITICAL)
    @DisplayName("Update user: should trim name and email before saving")
    @Description("""
            Проверяет нормализацию входных данных при обновлении пользователя.
            Сервис должен убрать пробелы в начале и конце name и email
            перед сохранением entity.
            """)
    @Tag("update")
    @Tag("trim")
    @Tag("positive")
    void test10() {
        UserEntity existingEntity = createUserEntity(USER_ID, OLD_USER_NAME, OLD_EMAIL, OLD_AGE);

        UserRequest request = createUserRequest(
                "  " + UPDATED_USER_NAME + "  ",
                "  " + NEW_EMAIL + "  ",
                USER_AGE
        );

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingEntity));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
        when(userRepository.save(existingEntity)).thenReturn(existingEntity);

        UserResponse result = userService.updateUser(USER_ID, request);

        assertNotNull(result);
        assertEquals(UPDATED_USER_NAME, existingEntity.getName());
        assertEquals(NEW_EMAIL, existingEntity.getEmail());
        assertEquals(USER_AGE, existingEntity.getAge());

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).existsByEmail(NEW_EMAIL);
        verify(userRepository, times(1)).save(existingEntity);
    }

    @Test
    @Story("Delete user")
    @Severity(CRITICAL)
    @DisplayName("Delete user by id: existing user should be deleted")
    @Description("""
            Проверяет удаление существующего пользователя.
            Repository возвращает пользователя по id,
            сервис должен вызвать repository.delete(entity).
            """)
    @Tag("delete")
    @Tag("positive")
    void test11() {
        UserEntity entity = createUserEntity(USER_ID, USER_NAME, USER_EMAIL, USER_AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(entity));

        userService.deleteUserById(USER_ID);

        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).delete(entity);
    }

    @Test
    @Story("Delete user")
    @Severity(CRITICAL)
    @DisplayName("Delete user by id: not found user should throw UserNotFoundException")
    @Description("""
            Проверяет удаление несуществующего пользователя.
            Repository возвращает Optional.empty(),
            сервис должен выбросить UserNotFoundException.
            Удаление выполняться не должно.
            """)
    @Tag("delete")
    @Tag("negative")
    void test12() {
        when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUserById(NOT_FOUND_USER_ID)
        );

        verify(userRepository, times(1)).findById(NOT_FOUND_USER_ID);
        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    @Story("Check email existence")
    @Severity(CRITICAL)
    @DisplayName("Check email existence: existing email should return true")
    @Description("""
            Проверяет метод isEmailExists() для существующего email.
            Repository возвращает true,
            сервис должен вернуть true.
            """)
    @Tag("email")
    @Tag("positive")
    void test13() {
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        boolean result = userService.isEmailExists(USER_EMAIL);

        assertTrue(result);

        verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
    }

    @Test
    @Story("Check email existence")
    @Severity(CRITICAL)
    @DisplayName("Check email existence: free email should return false")
    @Description("""
            Проверяет метод isEmailExists() для свободного email.
            Repository возвращает false,
            сервис должен вернуть false.
            """)
    @Tag("email")
    @Tag("negative")
    void test14() {
        when(userRepository.existsByEmail(FREE_EMAIL)).thenReturn(false);

        boolean result = userService.isEmailExists(FREE_EMAIL);

        assertFalse(result);

        verify(userRepository, times(1)).existsByEmail(FREE_EMAIL);
    }

}
