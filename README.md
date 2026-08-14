# User Project


Мультисервисный проект на Spring Boot для управления пользователями и отправки уведомлений.

Проект состоит из следующих микросервисов:

- **`user-service`** — REST API для CRUD-операций с пользователями, публикует события в Kafka.
- **`notification-service`** — потребляет события из Kafka и отправляет email-уведомления.
- **`gateway-service`** — API-шлюз, единая точка входа для внешних запросов.
- **`eureka-server`** — Service Discovery для регистрации и поиска сервисов.
- **`config-server`** — централизованный сервис конфигурации микросервисов.

---

## ⚙️ Стек технологий

| Компонент | Технологии |
|---|---|
| Язык и рантайм | Java 21 |
| Фреймворк | Spring Boot, Spring Cloud |
| Веб / REST | Spring Web, Spring Cloud Gateway Server WebMVC, MockMvc, REST Assured |
| ORM / БД | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Брокер сообщений | Apache Kafka, Spring Kafka |
| Почта | Jakarta Mail SMTP |
| Service Discovery | Spring Cloud Netflix Eureka |
| Config Server | Spring Cloud Config |
| Сборка | Maven 3.9+ |
| Тестирование | JUnit 5, Mockito, Allure Report |
| Контейнеризация | Docker Engine ≥ 20.10, Docker Compose v2 |
| Прочее | Lombok, Bean Validation, HATEOAS, Actuator, Resilience4j |

---


### `user-service`
- CRUD пользователей: создание, чтение (по ID и списком), обновление, удаление.
- Валидация входных данных через Bean Validation.
- Публикация событий в топик Kafka `user-events` (создание/удаление пользователя).
- Интеграция с общим модулем `common` для DTO событий.

### `notification-service`
- Потребление событий из топика `user-events`.
- Отправка email‑уведомлений при создании/удалении пользователя.
- Использование общего модуля `common` для типов событий.

### `gateway-service`
- Единая точка входа для всех внешних запросов.
- Маршрутизация запросов к микросервисам через Service Discovery (Eureka).
- Проксирование запросов с использованием `@LoadBalanced RestTemplate` (или Spring Cloud Gateway).

### `eureka-server`
- Регистрация микросервисов по имени (`spring.application.name`).
- Обнаружение сервисов для маршрутизации и балансировки.

### `config-server`
- Централизованное хранение конфигураций (Git / локальная папка).
- Раздача конфигов клиентским сервисам по имени сервиса и профилю.
- Поддержка динамического обновления конфигурации через Actuator `/actuator/refresh`.

---

## 📦 Требования

- Docker Engine ≥ 20.10
- Docker Compose (v2)
- Java 21 (для сборки и запуска приложений)
- Maven 3.9+
- ОС: Linux / macOS / Windows (с WSL2 или Docker Desktop)

---

## 🐳 Как запустить инфраструктуру

### Подготовка


1. Создайте файл `.env` в корне проекта:
   ```env
   
ASTON_POSTGRES_DB=platform_db
ASTON_POSTGRES_USER=admin
ASTON_POSTGRES_PASSWORD=astonpassword
ASTON_PGADMIN_DEFAULT_EMAIL=admin@aston.com
ASTON_PGADMIN_DEFAULT_PASSWORD=password
ASTON_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:///config/
MAIL_LOGIN=example@gmail.com
MAIL_PASSWORD=app-password-here

2. Убедитесь, что `init.sql` лежит рядом с `docker-compose.yml` (если нужна инициализация БД).
3. Добавьте `.env` и `target/` в `.gitignore`.


## ☕ Как запустить сервисы

### Вариант 1: Все сервисы в Docker (end‑to‑end)


Запуск:
В корне проекта лежит скрипт build.sh, который корректно поднимает инфраструктуру с учётом зависимостей и времени на прогрев сервисов.

```
#!/bin/bash

COMPOSE_FILE="docker-compose.yml"

echo "...........................Build..........................."
mvn clean package -DskipTests
docker compose -f "$COMPOSE_FILE" config >/dev/null
docker compose -f "$COMPOSE_FILE" build
docker compose -f "$COMPOSE_FILE" down --remove-orphans
docker compose -f "$COMPOSE_FILE" up -d

echo "...........................Start..........................."
sleep 10

docker compose -f "$COMPOSE_FILE" ps

echo "...........................Success..........................."
```
---

### Вариант 2: Локально из IDE (для разработки и отладки)

1. В IntelliJ IDEA создайте Run Configuration для каждого сервиса (`UserServiceApplication`, `NotificationServiceApplication`, `GatewayServiceApplication`).
2. В поле **Environment variables** укажите переменные из `.env`.
3. В **VM options** добавьте: `-Dspring.profiles.active=local`.
4. Запустите сервисы в порядке: `gateway-service`, `user-service`, `notification-service`.

> Для сервисов из IDE используйте `localhost` для подключения к инфраструктуре:
> - Kafka: `localhost:9092`
> - Eureka: `http://localhost:8761/eureka/`
> - Postgres: `jdbc:postgresql://localhost:5432/aston_db`

---

## 🌐 Доступ к сервисам

| Сервис | URL / адрес | Примечание |
|--------|-------------|------------|
| pgAdmin | `http://localhost:5050` | Логин/пароль из `.env` |
| PostgreSQL | `host=localhost port=5432` | Для сервисов в IDE; в Docker: `postgres:5432` |
| Kafka UI | `http://localhost:8083` | Просмотр топиков и сообщений |
| Kafka (клиенты с хоста) | `localhost:9092` | Только для IDE и локальных тестов |
| Kafka (внутри Docker) | `kafka:29092` | Для контейнеров |
| Eureka Server | `http://localhost:8761` | Панель Service Discovery |
| Config Server | `http://localhost:8888` | API выдачи конфигов |

---

## 🧠 Особенности архитектуры и интеграции

### Kafka: два слушателя

В `docker-compose.yml` для Kafka настроены два слушателя:
- `PLAINTEXT://kafka:29092` — для сервисов внутри Docker‑сети.
- `PLAINTEXT_HOST://localhost:9092` — для клиентов с хоста (IntelliJ, локальные тесты).

**Важно:** не смешивайте адреса. Если сервис в Docker — используйте `kafka:29092`. Если сервис запускается из IDE — `localhost:9092`.

### Eureka и Service Discovery

Все микросервисы регистрируются в Eureka по `spring.application.name`. Шлюз использует `@LoadBalanced RestTemplate` для проксирования запросов по имени сервиса (например, `http://user-service/api/users`).

### Config Server

Config Server читает конфиги из Git (или локальной папки) и раздаёт их клиентским сервисам. Пример URL для получения конфига:
```bash
curl http://localhost:8888/user-service/local
```

Клиентские сервисы подключаются так:
```properties
spring.config.import=optional:configserver:http://localhost:8888
```

Конфиги хранятся в репозитории в виде файлов:
- `user-service-local.properties`
- `notification-service-local.properties`
- `gateway-service-local.properties`

> Секреты (пароли) не храните в Git. Используйте переменные окружения.

---

## 🧪 Тестирование

- **Юнит‑тесты**: JUnit 5 + Mockito.
- **Интеграционные тесты**: Testcontainers (KafkaContainer, PostgreSQLContainer), MockMvc.
- **Отчётность**: Allure Report.
- **API‑тесты**: REST Assured.

Запуск тестов:
```bash
mvn test
```

Генерация Allure‑отчёта:
```bash
mvn allure:serve
```

---

## 🛠️ Полезные команды

- Посмотреть логи:
  ```bash
  docker compose logs -f --tail=50 kafka
  docker compose logs -f --tail=100 notification-service
  ```
- Пересоздать окружение (с очисткой данных):
  ```bash
  docker compose down -v
  docker compose up -d
  ```
- Выполнить SQL‑запрос напрямую:
  ```bash
  docker exec -it aston_postgres psql -U aston_user -d aston_db
  ```
- Собрать проект:
  ```bash
  mvn clean package
  ```

---

## ⚠️ Частые проблемы и решения

| Проблема | Причина | Решение |
|----------|---------|---------|
| pgAdmin не пускает | Неверные логин/пароль | Проверьте `.env`, перезапустите: `docker compose restart aston_pgAdmin` |
| Приложение не видит Kafka | Неправильный `bootstrap-servers` | Внутри Docker: `kafka:29092`, с хоста: `localhost:9092` |
| Топики не создаются | Kafka ещё не готов | Дождитесь healthcheck; можно создать топик вручную через Kafka UI |
| Ошибки подключения к БД | Инициализация ещё идёт | Подождите 1–2 минуты; проверьте логи PostgreSQL |
| `init.sql` не выполнился | База уже создана ранее | Пересоздайте volume: `docker compose down -v`, затем `up -d` |
| `MailAuthenticationException: 535 5.7.8` | Неверный пароль или обычный пароль вместо App Password | Создайте App Password в Google Account, проверьте переменные |
| Ошибка десериализации `UserEventDto` | Класс не на classpath или неверный `trusted.packages` | Проверьте зависимость на модуль `common`, убедитесь, что пакет в конфиге совпадает с реальным |
| Контейнер падает сразу после старта | Проблемы с подключением к Kafka/БД | Проверьте `depends_on` и healthcheck, подождите готовности инфраструктуры |
| Сервисы не видны в Eureka | Неверный URL в `defaultZone` | Проверьте переменную `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` и соответствие адресов (Docker vs IDE) |
| Конфигурация не подтягивается из Config Server | Не настроен `spring.config.import` | Убедитесь, что зависимость `spring-cloud-starter-config` подключена и импорт указан правильно |

---

## 📜 Инициализация базы данных

Скрипт `init.sql` автоматически выполняется при первом запуске контейнера PostgreSQL (если база пуста). Пример содержимого:

```sql
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---
