# ExploreWithMe - Микросервисная архитектура

## Обзор проекта

ExploreWithMe - это сервис для поиска и организации мероприятий, преобразованный из монолитной архитектуры в микросервисную.

## Архитектура

Проект разделен на три основные группы модулей:

### 1. Core (Бизнес-логика)

#### `category-service`
- Управление категориями мероприятий
- **Контроллеры:**
    - CategoryControllerAdmin
    - CategoryControllerPublic
    - InternalCategoryController

#### `event-service`
- Управление мероприятиями и связанными сущностями
- **Контроллеры:**
    - EventControllerAdmin
    - EventControllerPrivate
    - EventControllerPublic
    - InternalEventController
    - CommentControllerAdmin/Private/Public
    - CompilationControllerAdmin/Public
    - InternalLocationController
    - SubscriptionController

#### `interaction-service`
- Централизованное хранение сущностей и моделей данных

#### `request-service`
- Управление заявками на участие
- **Контроллеры:**
    - RequestControllerPrivate
    - InternalRequestController

#### `user-service`
- Управление пользователями
- **Контроллеры:**
    - UserControllerAdmin
    - InternalUserController

### 2. Infra (Инфраструктура)

#### `config-server`
- Централизованное управление конфигурациями

#### `discovery-server`
- Service Discovery на основе Eureka

#### `gateway-server`
- API Gateway на основе Spring Cloud Gateway

### 3. Stats (Статистика)

#### `stats-server`
- Сбор и хранение статистики просмотров
- **Контроллер:** StatsController

#### `stats-client`
- Клиентская библиотека для stats-server

#### `stats-dto`
- Общие DTO и модели для статистики

## Взаимодействие сервисов

### Внутреннее API (Internal Controllers)

1. **event-service → user-service**
   GET /internal/user/{userId}

2. **event-service → category-service**
   GET /internal/category/{categoryId}

3. **event-service → request-service**
   POST /internal/request/confirmed
   POST /internal/request/confirmeds

4. **user-service → event-service**
   GET /internal/event/{eventId} 
   GET /internal/event/all

5. **request-service → event-service**
   GET /internal/event/{eventId}/user/{userId}
   GET /internal/event/{eventId}

6. **request-service → user-service**
   GET /internal/user/{userId}

7. **category-service → event-service**
   GET /internal/event/exists/{categoryId}
   DELETE /internal/event/{eventId} 

### Технологии межсервисной коммуникации

- OpenFeign - декларативный REST-клиент
- Eureka - обнаружение сервисов
- Spring Cloud Gateway - маршрутизация
- Config Server - централизованная конфигурация

## Конфигурация

### Основные настройки

Все сервисы получают конфигурацию из `config-server`.

## Маршрутизация в API Gateway

### Публичные маршруты
GET /events/** → event-service
GET /categories/** → category-service
GET /compilations/** → event-service
POST /hit → stats-service
GET /stats → stats-service

### Приватные маршруты
GET/POST/PATCH/DELETE /users/{userId}/** → соответствующие сервисы

### Административные маршруты
/admin/** → соответствующие сервисы

## Базы данных

1. **event-service** - мероприятия, комментарии, подборки, локации
2. **user-service** - пользователи
3. **category-service** - категории
4. **request-service** - заявки на участие
5. **stats-service** - статистика просмотров

### Требования
- Java 17+
- Maven 3.6+
- Docker и Docker Compose
- PostgreSQL

## Тестирование
Postman коллекции
Основной функционал - тесты через Gateway (порт 8080)

Дополнительный функционал - комментарии, подписки

Статистика - сбор и получение статистики

## Внешний API

1. https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json
2. https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json