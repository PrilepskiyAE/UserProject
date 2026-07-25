# notification-service

## Назначение сервиса

`notification-service` — микросервис для отправки уведомлений пользователям. Он:
* Слушает события из Kafka (топик `user-events`).
* Извлекает данные (email, тип операции).
* Отправляет email через SMTP (Gmail).

Сервис построен на **Spring Boot + Jakarta Mail**, использует общий модуль `common` для DTO и событий.

---

## Основные зависимости и технологии

* Java 21
* Spring Boot
* Spring Kafka
* Jakarta Mail
* Lombok
* Общий модуль `com.prilepskiy_ae.common` (DTO, enum)

---

## Переменные окружения

Для запуска нужны следующие переменные MAIL_LOGIN, MAIL_PASSWORD


## Как запустить

### Вариант 1: В Docker (вместе с инфраструктурой)

Если у тебя уже запущен `docker-compose` с PostgreSQL, Kafka и т. д.:

1. Убедись, что сервисы инфраструктуры (`PostgreSQL`, `kafka`, `zookeeper`) в статусе `running`.
2. Добавь `notification-service` в `docker-compose.yml` (пример ниже).
3. Запусти:
   ```bash
   docker compose up -d notification-service
   ```

**Пример фрагмента `docker-compose.yml` для сервиса:**
```yaml
services:
  notification-service:
    build: .
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

networks:
  app-network:
    external: true
```

---

### Вариант 2: Локально из IntelliJ IDEA

1. Создай Run Configuration для основного класса (`NotificationServiceApplication`).
2. В поле **Environment variables** пропиши каждую переменную на отдельной строке:
   ```text
   MAIL_LOGIN=exempleg@gmail.com
   MAIL_PASSWORD=abcdefghijklmno
   ```
3. Нажми **Run**.


---

## Архитектура и ключевые компоненты

* `UserEventListener` — слушатель Kafka, обрабатывает события `UserEventDto`.
* `NotificationServiceImpl` — логика отправки email.
* `KafkaConfig` — настройка фабрики слушателей с `ErrorHandlingDeserializer` (чтобы ошибки десериализации не «роняли» контейнер).
* `common` — общий модуль с `UserEventDto` и `OperationType`.

---

## Частые проблемы и решения

| Проблема | Причина | Решение |
|----------|---------|---------|
| `MailAuthenticationException: 535 5.7.8` | Неверный логин/пароль или обычный пароль вместо App Password | Создай App Password в Google Account, проверь переменные окружения |
| `AddressException: Illegal semicolon…` | Переменные склеены в одну строку | Каждая переменная — отдельная строка в настройках запуска |
| Сервис не видит события | Неправильный `bootstrap-servers` | Внутри Docker: `kafka:29092`, локально: `localhost:9092` |
| Ошибка десериализации `UserEventDto` | Класс не на classpath или неверный `trusted.packages` | Проверь зависимость на модуль `common`, убедись, что пакет в конфиге совпадает с реальным |
| Контейнер падает сразу после старта | Проблемы с подключением к Kafka/БД | Проверь `depends_on` и healthcheck, подожди готовности инфраструктуры |

---

## Полезные команды

* Посмотреть логи:
  ```bash
  docker compose logs -f --tail=100 notification-service
  ```
* Перезапустить сервис:
  ```bash
  docker compose restart notification-service
  ```
* Выполнить SQL в БД (для проверок):
  ```bash
  docker exec -it aston_postgres psql -U aston_user -d aston_db
  ```

---
