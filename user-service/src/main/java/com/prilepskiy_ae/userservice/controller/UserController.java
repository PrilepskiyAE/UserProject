package com.prilepskiy_ae.userservice.controller;

import com.prilepskiy_ae.userservice.dto.error.ErrorResponse;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import com.prilepskiy_ae.userservice.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.EntityModel;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController  {

    private final UserService userService;

    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя. Если email уже занят — вернёт 409"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Произошла внутренняя ошибка сервиса. Попробуйте позже или сообщите оператору Correlation-ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @PostMapping
    public ResponseEntity<EntityModel<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        EntityModel<UserResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(response.getId()))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));

        URI location = linkTo(methodOn(UserController.class).getUserById(response.getId())).toUri();
        return ResponseEntity.created(location).body(resource);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя. Если пользователь не найден — вернёт 404."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Произошла внутренняя ошибка сервиса. Попробуйте позже или сообщите оператору Correlation-ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUserById(
            @NotNull @Positive(message = "ID должен быть положительным") @PathVariable Long id
    ) {
        UserResponse response = userService.getUserById(id);

        EntityModel<UserResponse> resource = EntityModel.of(response);

        resource.add(linkTo(methodOn(UserController.class).getUserById(id))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, null))
                .withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id))
                .withRel("delete"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список пользователей."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        var resources = responses.stream()
                .map(u -> {
                    var r = EntityModel.of(u);
                    r.add(linkTo(methodOn(UserController.class).getUserById(u.getId()))
                            .withRel("self"));
                    return r;
                })
                .collect(Collectors.toList());

        var collection = CollectionModel.of(resources);

        collection.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("self"));


        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные пользователя. Проверяет валидность полей. Если пользователь не найден — 404."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлён",
                    content = @Content(schema = @Schema(implementation = EntityModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь с ID не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Произошла внутренняя ошибка сервиса. Попробуйте позже или сообщите оператору Correlation-ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>>updateUser(
            @NotNull @Positive(message = "ID должен быть положительным") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        EntityModel<UserResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id))
                .withRel("self"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers())
                .withRel("collection"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по ID. Если пользователь не найден — вернёт 404."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Произошла внутренняя ошибка сервиса. Попробуйте позже или сообщите оператору Correlation-ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@NotNull @Positive(message = "ID должен быть положительным") @PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
