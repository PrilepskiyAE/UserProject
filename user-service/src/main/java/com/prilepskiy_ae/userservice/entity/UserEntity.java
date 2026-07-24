package com.prilepskiy_ae.userservice.entity;

import com.prilepskiy_ae.userservice.dto.user.UserResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@Table(name = "users")
public class UserEntity {
    /**
     * Уникальный идентификатор пользователя.
     *
     * <p>
     * Значение генерируется базой данных автоматически с использованием
     * стратегии {@link GenerationType#IDENTITY}.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя.
     *
     * <p>
     * Поле является обязательным и соответствует колонке {@code name}
     * в таблице {@code users}. Максимальная длина значения — 100 символов.
     * </p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email пользователя.
     *
     * <p>
     * Поле является обязательным, должно быть уникальным и соответствует
     * колонке {@code email} в таблице {@code users}. Максимальная длина
     * значения — 255 символов.
     * </p>
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Возраст пользователя.
     *
     * <p>
     * Поле является обязательным и соответствует колонке {@code age}
     * в таблице {@code users}.
     * </p>
     */
    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserEntity(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = LocalDateTime.now();
    }

    public UserResponse toResponse(){
        return new UserResponse(this.id,this.name,this.email,this.age,this.createdAt);
    }


}
