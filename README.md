# User Project

Это мультисервисный проект на Spring Boot для управления пользователями и отправки уведомлений. Состоит из двух микросервисов:

- **`user-service`** — REST API для CRUD‑операций с пользователями, публикует события в Kafka.
- **`notification-service`** — потребляет события из Kafka и отправляет email‑уведомления.

---

## ⚙️ Стек технологий

| Компонент | Технологии |
|---------|------------|
| Язык и рантайм | Java 21 |
| Фреймворк | Spring Boot |
| Веб / REST | Spring Web, MockMvc, REST Assured |
| ORM / БД | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Брокер сообщений | Apache Kafka (Confluent), Spring Kafka |
| Почта | Jakarta Mail (SMTP) |
| Сборка | Maven |
| Тестирование | JUnit 5, Mockito, Allure Report |
| Контейнеризация | Docker, Docker Compose |
| Прочее | Lombok, Bean Validation,HATEOAS |

---

## 🚀 Возможности

### `user-service`
- CRUD пользователей: создание, чтение (по ID и списком), обновление, удаление.
- Валидация входных данных через Bean Validation.
- Публикация событий в топик Kafka `user-events` (создание/удаление пользователя).
- Интеграция с общим модулем `common` для DTO событий.

### `notification-service`
- Потребление событий из топика `user-events`.
- Отправка email‑уведомлений при создании/удалении пользователя.
- Использование общего модуля `common` для типов событий.

---

## 📦 Требования

- Docker Engine ≥ 20.10
- Docker Compose (v2)
- Java 21 (для сборки и запуска приложений)
- Maven 3.9+
- ОС: Linux / macOS / Windows (с WSL2 или Docker Desktop)

---

## 🐳 Как запустить инфраструктуру

1. Создайте `.env` с переменными.
2. Убедитесь, что `init.sql` лежит рядом с `docker-compose.yml` (если нужна инициализация БД).
3. Запустите окружение:
   ```bash
   docker compose up -d
   ```
4. Проверьте статус:
   ```bash
   docker compose ps
   ```
   Все сервисы должны быть в статусе `running`.
5. Дождитесь готовности:
   - PostgreSQL: ~50–60 секунд.
   - Kafka: до 2–3 минут (синхронизация с Zookeeper).

---

## ☕ Как запустить сервисы

### Вариант 1: Все сервисы в Docker (рекомендуется для end‑to‑end)

Добавьте `notification-service` и `user-service` в `docker-compose.yml` (см. пример ниже) и запустите:

```bash
docker compose up -d user-service notification-service
```

```yaml
  notification-service:
    build: ./notification-service
    container_name: aston_notification_service
    environment:
      - MAIL_LOGIN=${MAIL_LOGIN}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=${SPRING_KAFKA_BOOTSTRAP_SERVERS:-kafka:29092}
      - APP_KAFKA_TOPIC_USER_EVENTS=${APP_KAFKA_TOPIC_USER_EVENTS:-user-events}
    depends_on:
      kafka:
        condition: service_healthy
    networks:
      - app-network
    restart: unless-stopped

  user-service:
    build: ./user-service
    container_name: aston_user_service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${ASTON_POSTGRES_DB}
      - SPRING_DATASOURCE_USERNAME=${ASTON_POSTGRES_USER}
      - SPRING_DATASOURCE_PASSWORD=${ASTON_POSTGRES_PASSWORD}
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy
    networks:
      - app-network
    restart: unless-stopped
    ports:
      - "8080:8080"
```

### Вариант 2: Локально из IDE (для разработки и отладки)

1. Для `user-service`: создайте Run Configuration для `UserServiceApplication`.
2. Для `notification-service`: создайте Run Configuration для `NotificationServiceApplication`.
3. В поле **Environment variables** пропишите переменные по одной на строку:
   ```text
   MAIL_LOGIN=exempleg@gmail.com
   MAIL_PASSWORD=abcdefghijklmno
   ```
4. Запустите сервисы.

> 💡 Для тестов с Testcontainers используйте `localhost:9092`.  
> Для сервисов внутри Docker‑сети используйте `kafka:29092`.

---

## 🌐 Доступ к сервисам

| Сервис | URL / адрес | Логин | Пароль |
|--------|-------------|-------|--------|
| pgAdmin | `http://localhost:5050` | `${ASTON_PGADMIN_DEFAULT_EMAIL}` | `${ASTON_PGADMIN_DEFAULT_PASSWORD}` |
| PostgreSQL | `host=localhost port=5432` | `${ASTON_POSTGRES_USER}` | `${ASTON_POSTGRES_PASSWORD}` |
| Kafka UI | `http://localhost:8083` | — | — |
| Kafka (клиенты с хоста) | `localhost:9092` | — | — |
| Kafka (внутри Docker) | `kafka:29092` | — | — |

---

## 🧠 Особенности архитектуры и интеграции

### Kafka: два слушателя
- `PLAINTEXT://kafka:29092` — для сервисов внутри Docker‑сети.
- `PLAINTEXT_HOST://localhost:9092` — для клиентов с хоста (IntelliJ, локальные тесты).

### Конфигурация Spring Boot

**Для `user-service` (продюсер):**
```properties
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonSerializer
```

**Для `notification-service` (потребитель):**
```properties
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.prilepskiy_ae.common
```

> Если приложение запускается **не в Docker**, а с хоста — используйте `localhost:9092`.

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

