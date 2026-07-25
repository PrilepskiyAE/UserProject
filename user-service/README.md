# User Service

`User Service` — это REST API-приложение для управления пользователями, реализованное на Spring Boot.

Проект является переработанной версией [консольного приложения](https://github.com/PrilepskiyAE/user-service). на Hibernate. Вместо ручного управления `SessionFactory`, `Session` и транзакциями теперь используется Spring Boot, Spring Web и Spring Data JPA.


## Стек технологий

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- JUnit 5
- Mockito
- MockMvc
- Bean Validation
- REST Assured
- Allure report
- Kafka

## Возможности приложения

Приложение предоставляет базовые CRUD-операции для работы с пользователями:

- создание пользователя
- получение пользователя по id
- получение списка всех пользователей
- обновление данных пользователя
- удаление пользователя

![Image alt](https://github.com/PrilepskiyAE/user-service-backend/blob/main/screen/screen1.png)
