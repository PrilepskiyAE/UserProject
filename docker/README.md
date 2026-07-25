# Docker‑окружения (PostgreSQL, pgAdmin, Kafka, Kafka UI)

## Описание

Этот `docker-compose.yml` поднимает локальное окружение для разработки микросервисов:

- **PostgreSQL 16** — база данных с инициализацией через `init.sql`.
- **pgAdmin** — веб‑интерфейс для управления БД.
- **Kafka + Zookeeper (Confluent)** — брокер сообщений для обмена событиями между сервисами.
- **Kafka UI** — веб‑панель для просмотра топиков, сообщений и настроек кластера.

Все сервисы объединены в общую сеть `app-network` и запускаются с проверками работоспособности (healthcheck).

---

## Требования

- Docker Engine ≥ 20.10
- Docker Compose (v2)
- Linux / macOS / Windows с WSL2 или Docker Desktop

---

## Переменные окружения

Для запуска нужны следующие переменные (в файле `.env`):

```env
ASTON_POSTGRES_DB=aston_db
ASTON_POSTGRES_USER=aston_user
ASTON_POSTGRES_PASSWORD=aston_secret

ASTON_PGADMIN_DEFAULT_EMAIL=admin@example.com
ASTON_PGADMIN_DEFAULT_PASSWORD=pgadmin_secret
```

> ⚠️ **Важно:** не храните реальные пароли в репозитории. Добавьте `.env` в `.gitignore`.

---

## Как запустить

1. Создайте файл `.env` с переменными из раздела выше.
2. Положите `init.sql` в ту же директорию, что и `docker-compose.yml` (если нужна инициализация БД).
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
   - PostgreSQL: healthcheck срабатывает после ~50–60 секунд.
   - Kafka: стартует дольше (до 2–3 минут) из‑за синхронизации с Zookeeper.

---

## Доступ к сервисам

| Сервис | URL / адрес | Логин | Пароль |
|--------|-------------|-------|--------|
| pgAdmin | `http://localhost:5050` | `${ASTON_PGADMIN_DEFAULT_EMAIL}` | `${ASTON_PGADMIN_DEFAULT_PASSWORD}` |
| PostgreSQL | `host=localhost port=5432` | `${ASTON_POSTGRES_USER}` | `${ASTON_POSTGRES_PASSWORD}` |
| Kafka UI | `http://localhost:8083` | — | — |
| Kafka (для приложений) | `localhost:9092` (внешние клиенты), `kafka:29092` (внутри сети Docker) | — | — |

---

## Особенности настройки Kafka

- **Два слушателя**:
  - `PLAINTEXT://kafka:29092` — для сервисов внутри Docker‑сети.
  - `PLAINTEXT_HOST://localhost:9092` — для клиентов с хоста (например, IntelliJ, локальные тесты).
- **Автосоздание топиков**: включено (`KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`) — удобно для локальной разработки.
- **Безопасность**: протокол `PLAINTEXT` без SSL/SASL — только для локальной среды. Не использовать в production.

> Для Spring‑приложений в Docker используйте: `spring.kafka.bootstrap-servers=kafka:29092`.  
> Для тестов с хоста: `spring.kafka.bootstrap-servers=localhost:9092`.

---

## Инициализация базы данных

Скрипт `init.sql` автоматически выполняется при первом запуске контейнера PostgreSQL (если база пуста). Пример содержимого:

```sql
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## Полезные команды

- Посмотреть логи:
  ```bash
  docker compose logs -f --tail=50 kafka
  docker compose logs -f --tail=50 PostgreSQL
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

---

## Интеграция с Java‑приложениями (Spring Boot)

### Для `userservice` (продюсер событий)

В `application.properties`:
```properties
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonSerializer
```

### Для `notification-service` (потребитель событий)

В `application.properties`:
```properties
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.prilepskiy_ae.common
```

> Если приложение запускается **не в Docker**, а с хоста — используйте `localhost:9092`.

---

## Частые проблемы и решения

| Проблема | Причина | Решение |
|----------|---------|---------|
| pgAdmin не пускает | Неверные логин/пароль | Проверьте `.env` и перезапустите сервис: `docker compose restart aston_pgAdmin` |
| Приложение не видит Kafka | Неправильный `bootstrap-servers` | Внутри Docker: `kafka:29092`, с хоста: `localhost:9092` |
| Топики не создаются | Kafka ещё не готов | Дождитесь статуса `running` и healthcheck; можно вручную создать топик через Kafka UI |
| Ошибки подключения к БД | Инициализация ещё идёт | Подождите 1–2 минуты; проверьте логи PostgreSQL |
| `init.sql` не выполнился | База уже создана ранее | Пересоздайте volume: `docker compose down -v`, затем `up -d` |

---

