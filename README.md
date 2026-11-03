# RequestDesk 🎫

Небольшой help-desk для приёма и обработки обращений. Идея похожа на класс систем заявок, но проект **не копирует vsDesk**: простой интерфейс и минимальный набор ролей/процессов.

## Стек
Java 21 · Spring Boot 3.5 · Spring Security · JWT · PostgreSQL · JPA · Flyway · Swagger · JUnit/Mockito · HTML/CSS/JavaScript · Docker Compose.

## Роли и сценарий
- **USER** регистрируется, создаёт обращение (тема, описание, приоритет), видит **только свои** заявки и пишет комментарии.
- **AGENT** видит все обращения, берёт свободную заявку в работу, меняет статус `IN_PROGRESS` / `RESOLVED`, отвечает в комментариях.
- Статусы: `OPEN → IN_PROGRESS → RESOLVED`. Закрытые заявки нельзя комментировать или брать в работу.
- Другой оператор не может перехватить уже назначенную заявку; доступ пользователя к чужим заявкам запрещён.
- `@Version` + optimistic locking защищают от одновременного назначения заявки двум операторам.

## Быстрый запуск
Требуются Docker Desktop и Docker Compose. В корне проекта:

```bash
cp .env.example .env
# PowerShell: Copy-Item .env.example .env
# Измените тестовые пароли в .env

docker compose up --build
```

Откройте:
- UI: http://localhost:8082
- Swagger: http://localhost:8082/swagger-ui.html

На первом старте создаётся тестовый оператор из переменных `DEMO_AGENT_EMAIL` и `DEMO_AGENT_PASSWORD` (по умолчанию в .env.example: `agent@example.com` / `local-demo-agent-password`). **Это локальные тестовые данные — перед публичным развёртыванием замените их.**

Для проверки сценария:
1. Зарегистрируйте обычного пользователя через UI.
2. Создайте заявку.
3. Выйдите и войдите под агентом.
4. Откройте заявку, возьмите в работу и ответьте.
5. Вернитесь под пользователем и посмотрите комментарий/статус.

## API
| Метод | URL | Доступ |
|---|---|---|
| POST | `/api/auth/register` | Все, регистрация USER |
| POST | `/api/auth/login` | Все |
| GET | `/api/me` | Авторизованные |
| GET | `/api/tickets?page=0&size=20` | USER (свои), AGENT (все) |
| POST | `/api/tickets` | USER |
| GET | `/api/tickets/{id}` | USER (своя), AGENT |
| POST | `/api/tickets/{id}/take` | AGENT |
| PATCH | `/api/tickets/{id}/status` | AGENT, назначенный оператор |
| POST | `/api/tickets/{id}/comments` | Автор и AGENT (пока заявка открыта) |

Пример создания:
```http
POST /api/tickets
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{"title":"Не открывается кабинет","description":"После входа вижу пустую страницу","priority":"NORMAL"}
```

## Архитектура
`security` — JWT, BCrypt и SecurityFilterChain; `user` — учётные записи и роли; `ticket` — заявки, сервис доступа и комментарии; `common` — HTTP-ошибки. Миграция схемы: `src/main/resources/db/migration/V1__init.sql`.

## Тесты
`mvn test` — небольшие unit-тесты бизнес-правил. Если запускать backend вне Docker, понадобятся PostgreSQL, Java 21, Maven и переменные `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `DEMO_AGENT_PASSWORD`.

## Ограничения
- Только две роли и один назначенный оператор на заявку.
- Нет файловых вложений, уведомлений, SLA, полнотекстового поиска и массовых операций.
- JWT живёт 2 часа, refresh-токенов нет.
- Демонстрационный frontend хранит JWT в `sessionStorage`. Для продакшена нужно отдельно спроектировать сессии, XSS/CSRF-защиту и наблюдаемость.
- Проект предназначен для изучения Java/Spring, не для продуктивного help-desk.
