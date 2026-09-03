
# 📚 Course Management Main Service

Backend-сервис для управления курсами, главами, уроками и пользователями с разграничением прав доступа на базе **Keycloak** и **Spring Security**.

---

## 🛠 Технологический стек

* **Java**: 17 / 21
* **Framework**: Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
* **Auth & IAM**: Keycloak (OAuth2 / Resource Server / JWT)
* **Database**: PostgreSQL
* **Documentation**: OpenAPI 3.0 / Swagger UI
* **Containerization**: Docker, Docker Compose
* **Tools**: Lombok, Maven / Gradle

---

## 🚀 Быстрый запуск

### 1. Клонирование репозитория

```bash
git clone [https://github.com/temirlan113/Internship-project.git](https://github.com/temirlan113/Internship-project.git)
cd Internship-project

```

### 2. Запуск контейнеров (Сервис + База данных + Keycloak)

Запустите приложение и всю необходимую инфраструктуру в Docker:

```bash
docker compose up --build -d

```

> 🔐 **Админка Keycloak**: http://localhost:8084
> **Логин**: `admin` | **Пароль**: `admin`

---

## 🔑 Тестовые аккаунты

Для удобной проверки системы в Keycloak заранее созданы пользователи с соответствующими ролями:

| Пользователь | Логин | Пароль | Назначение |
| --- | --- | --- | --- |
| **Admin** | `admin-test` | `admin-test` | Полный доступ (управление курсами, уроками, пользователями) |
| **Teacher** | `teacher-test` | `teacher-test` | Управление уроками (создание, редактирование) |
| **User** | `user-test` | `user-test` | Просмотр материалов и редактирование своего профиля |

---

## 📖 Документация API (Swagger UI)

После успешного запуска документация доступна по адресу:

👉 **http://localhost:8083/swagger-ui/index.html**

### 💡 Как протестировать в Swagger:

1. Выполните запрос `POST /api/v1/auth/login`, используя данные одного из тестовых аккаунтов.
2. Скопируйте полученный `access_token` из ответа.
3. Нажмите кнопку **Authorize** в правом верхнем углу Swagger UI.
4. Вставьте токен и нажмите **Authorize**.

---

## 📌 Основные эндпоинты API

### 🔐 Аутентификация

* `POST /api/v1/auth/login` — Вход в систему (получение JWT)
* `POST /api/v1/auth/refresh-token` — Обновление JWT токена

### 🎓 Курсы, главы и уроки

* `GET /api/v1/courses` — Список всех курсов (с поддержкой пагинации и сортировки)
* `GET /api/v1/chapters?courseId={id}` — Получение списка глав курса
* `GET /api/v1/lessons?chapterId={id}` — Получение списка уроков главы

### 👤 Пользователи и администрирование

* `PUT /api/v1/users/update-profile` — Обновление личного профиля *(Все авторизованные)*
* `POST /api/v1/users/create` — Создание нового пользователя *(Только ADMIN)*
* `PUT /api/v1/users/{userId}/role` — Изменение роли пользователя *(Только ADMIN)*

---

## 🛡 Матрица доступов

| Эндпоинт / Действие                                    | Публично | USER | TEACHER | ADMIN |
|--------------------------------------------------------|----------|------|---------|-------|
| Аутентификация (`/api/v1/auth/**`)                     | ✅        | ✅    | ✅       | ✅     |
| Просмотр контента (`GET`)                              | ❌        | ✅    | ✅       | ✅     |
| Обновление своего профиля (`PUT`)                      | ❌        | ✅    | ✅       | ✅     |
| Создание / ред. уроков (`POST/PUT /lessons`)           | ❌        | ❌    | ✅       | ✅     |
| Управление курсами и главами (`/courses`, `/chapters`) | ❌        | ❌    | ❌       | ✅     |
| Управление пользователями и ролями (`/users/**`)       | ❌        | ❌    | ❌       | ✅     |



