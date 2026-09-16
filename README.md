
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

> 🔐 **Админ, созданный через KeycloakInitializer**: http://localhost:8084
> **Логин**: `appAdmin` | **Пароль**: `password123`

---

## 🔑 Тестовые аккаунты

Для удобной проверки системы в Keycloak заранее созданы пользователи с соответствующими ролями:

| Пользователь | Логин          | Пароль         | Назначение                                                  |
|--------------|----------------|----------------|-------------------------------------------------------------|
| **Admin**    | `admin-test`   | `admin-test`   | Полный доступ (управление курсами, уроками, пользователями) |
| **Teacher**  | `teacher-test` | `teacher-test` | Просмотр контента и загрузка вложений к учебным материалам  |
| **Student**  | `student-test` | `student-test` | Полный доступ к обучению: просмотр курсов, глав и уроков    |
| **User**     | `user-test`    | `user-test`    | Базовый аккаунт: доступ только к каталогу курсов            |

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
* `POST /api/v1/auth/refresh-token` — Обновление JWT-токена

### 🎓 Курсы, главы и уроки

* `GET /api/v1/courses` — Список всех курсов *(Доступно всем ролям)*
* `GET /api/v1/chapters?courseId={id}` — Список глав курса *(ADMIN, TEACHER, STUDENT)*
* `GET /api/v1/lessons?chapterId={id}` — Список уроков главы *(ADMIN, TEACHER, STUDENT)*

### 👤 Пользователи и администрирование

* `PUT /api/v1/users/update-profile` — Обновление личного профиля *(Все аутентифицированные)*
* `POST /api/v1/users/create` — Создание нового пользователя *(Только ADMIN)*
* `PUT /api/v1/users/{userId}/role` — Изменение роли пользователя *(Только ADMIN)*

### 👤 Вложения

* `POST /api/v1/attachments/upload` — Загрузка файла/вложения *(ADMIN, TEACHER)*
* `GET /api/v1/attachments/download/{attachmentId}` — Скачивание файла по ID*(ADMIN, TEACHER, STUDENT)*
* `DELETE /api/v1/attachments/{attachmentId}` — Удаление вложения *(Только ADMIN)*


---

## 🛡 Матрица доступов

| Эндпоинт / Действие                                        | Публично | USER | STUDENT | TEACHER | ADMIN |
|------------------------------------------------------------|----------|------|---------|---------|-------|
| Аутентификация (`/api/v1/auth/**`)                         | ✅        | ✅    | ✅       | ✅       | ✅     |
| Обновление своего профиля (`PUT /users/update-profile`)    | ❌        | ✅    | ✅       | ✅       | ✅     |
| Просмотр списка курсов (`GET /courses/**`)                 | ❌        | ✅    | ✅       | ✅       | ✅     |
| Просмотр глав и уроков (`GET /chapters/**`, `/lessons/**`) | ❌        | ❌    | ✅       | ✅       | ✅     |
| Скачивание вложений (`GET /attachments/download/**`)       | ❌        | ❌    | ✅       | ✅       | ✅     |
| Загрузка вложений (`POST /attachments/upload`)             | ❌        | ❌    | ❌       | ✅       | ✅     |
| Создание и ред. уроков (`POST/PUT /lessons/**`)            | ❌        | ❌    | ❌       | ❌       | ✅     |
| Управление курсами и главами (`/courses`, `/chapters`)     | ❌        | ❌    | ❌       | ❌       | ✅     |
| Управление пользователями и ролями (`/users/**`)           | ❌        | ❌    | ❌       | ❌       | ✅     |

```

```
