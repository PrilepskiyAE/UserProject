package com.prilepskiy_ae.userservice.dto.user;

import com.prilepskiy_ae.userservice.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.prilepskiy_ae.userservice.util.ValidationConstants.MAX_NAME_LEN;
import static com.prilepskiy_ae.userservice.util.ValidationConstants.MIN_NAME_LEN;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "Ответ с данными пользователя", name = "UserResponse")
public class UserResponse {
    @Schema(
            description = "Уникальный идентификатор пользователя в системе",
            example = "123",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    @Schema(
            description = "Имя пользователя",
            example = "Alex",
            minLength = MIN_NAME_LEN,
            maxLength = MAX_NAME_LEN
    )
    private String name;
    @Schema(
            description = "Email пользователя",
            example = "alex@aston.com",
            format = "email"
    )
    private String email;
    @Schema(
            description = "Возраст пользователя в годах",
            example = "25",
            minimum = "0",
            maximum = "100"
    )
    private int age;

    @Schema(
            description = "Дата и время создания/обновления",
            example = "2024-06-15T10:30:00",
            format = "date-time"
    )
    private LocalDateTime createdAt;

    public UserEntity toEntity(){
        return new UserEntity(this.id,this.name,this.email,this.age,this.createdAt);
    }

    public static UserResponse fromEntity(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getAge(),
                userEntity.getCreatedAt()
        );
    }
}
