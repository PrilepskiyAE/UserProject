package com.prilepskiy_ae.userservice.controller;

import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Users", description = "API для управления пользователями")
@RequestMapping("/api/users")
public interface UserController {

   @Operation(summary = "Создать пользователя")
   @PostMapping
   ResponseEntity<EntityModel<UserResponse>> createUser(
           @Valid @RequestBody UserRequest request
   );

   @Operation(summary = "Получить пользователя по ID")
   @GetMapping("/{id}")
   ResponseEntity<EntityModel<UserResponse>> getUserById(
           @Positive(message = "ID должен быть положительным")
           @PathVariable Long id
   );

   @Operation(summary = "Получить всех пользователей")
   @GetMapping
   ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers();

   @Operation(summary = "Обновить пользователя")
   @PutMapping("/{id}")
   ResponseEntity<EntityModel<UserResponse>> updateUser(
           @Positive(message = "ID должен быть положительным")
           @PathVariable Long id,
           @Valid @RequestBody UserRequest request
   );

   @Operation(summary = "Удалить пользователя")
   @DeleteMapping("/{id}")
   ResponseEntity<Void> deleteUser(
           @Positive(message = "ID должен быть положительным")
           @PathVariable Long id
   );
}
