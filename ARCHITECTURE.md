# Архитектура приложения "Книга рецептов"

## Обзор

Приложение "Книга рецептов" построено на основе современной Android архитектуры с использованием Jetpack Compose и следует принципам Clean Architecture и MVVM паттерна.

## Архитектурные слои

### 1. Presentation Layer (UI)

#### Screens (Экраны)
- **HomeScreen** - главный экран с поиском и категориями
- **CatalogScreen** - каталог всех рецептов
- **RecipeDetailScreen** - детальный просмотр рецепта
- **AddRecipeScreen** - форма добавления нового рецепта
- **ProfileScreen** - профиль пользователя и авторизация

#### ViewModels
Каждый экран имеет собственную ViewModel:
- `HomeViewModel` - управление состоянием главного экрана
- `CatalogViewModel` - управление списком рецептов
- `RecipeDetailViewModel` - управление деталями рецепта
- `AddRecipeViewModel` - управление формой добавления

#### Custom Components
- **AnimatedFavoriteButton** - кнопка с анимацией для добавления в избранное
- **ProgressBadge** - круговой индикатор сложности рецепта
- **RecipeCard** - переиспользуемая карточка рецепта

#### Navigation
- Navigation Compose для управления навигацией
- Bottom Navigation Bar для основных разделов
- Deep linking поддержка для переходов к деталям рецепта

### 2. Domain Layer (Бизнес-логика)

#### Models
- **Recipe** - основная модель рецепта с полями:
  - id, name, description
  - ingredients, instructions
  - category, difficulty, cookingTime, servings
  - imageUrl, isFavorite, isLocal
  
- **User** - модель пользователя
- **Category** - модель категории рецептов

#### Repository
- **RecipeRepository** - единая точка доступа к данным:
  - Предоставляет данные из локальной БД
  - Синхронизирует с REST API
  - Управляет кэшированием

### 3. Data Layer (Данные)

#### Local Storage
- **Room Database**
  - `RecipeDatabase` - конфигурация БД
  - `RecipeDao` - DAO для работы с рецептами
  - Автоматическая миграция схемы
  
- **DataStore**
  - `UserPreferencesManager` - хранение настроек пользователя
  - Сохранение токена авторизации
  - Реактивное получение данных через Flow

#### Remote Data Source
- **Retrofit**
  - `RecipeApiService` - интерфейс API
  - `RetrofitClient` - конфигурация клиента
  - Поддержка logging interceptor
  - Обработка ошибок сети

## Поток данных

```
UI (Compose) ←→ ViewModel ←→ Repository ←→ [Local DB / Remote API]
```

1. UI отображает данные из StateFlow в ViewModel
2. ViewModel запрашивает данные через Repository
3. Repository решает откуда брать данные (БД или API)
4. Данные возвращаются через Flow и автоматически обновляют UI

## Реактивность

### Использование Flow
- Все данные из БД возвращаются как `Flow<List<Recipe>>`
- UI автоматически обновляется при изменении данных
- StateFlow используется для состояния экрана

### Корутины
- ViewModelScope для управления жизненным циклом
- suspend функции для асинхронных операций
- Structured concurrency

## Хранение данных

### Room Database
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    ...
)
```

### DataStore Preferences
```kotlin
val userToken: Flow<String?> = dataStore.data.map { 
    preferences -> preferences[USER_TOKEN] 
}
```

## Сетевое взаимодействие

### API Endpoints
- GET /recipes - получить все рецепты
- GET /recipes/{id} - получить рецепт по ID
- POST /recipes - создать новый рецепт
- PUT /recipes/{id} - обновить рецепт
- DELETE /recipes/{id} - удалить рецепт

### Стратегия загрузки данных
1. Сначала показываем данные из кэша (Room)
2. Параллельно загружаем актуальные данные с сервера
3. Обновляем кэш и UI автоматически обновляется

## UI/UX особенности

### Material Design 3
- Современный дизайн
- Адаптивная цветовая схема
- Поддержка темной темы

### Анимации
- Плавные переходы между экранами
- Анимация добавления в избранное
- Прогресс-индикатор сложности с анимацией

### Пользовательский опыт
- Валидация форм
- Обработка ошибок
- Placeholder изображения
- Pull-to-refresh (при необходимости)

## Безопасность

### ProGuard/R8
- Обфускация кода в release сборке
- Уменьшение размера APK
- Защита от reverse engineering

### Правила ProGuard
```proguard
-keep class com.example.recipes.data.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
```

## Производительность

### Оптимизации
- LazyColumn для эффективного отображения списков
- Coil для эффективной загрузки изображений
- Room для быстрого доступа к данным
- ViewModelScope для управления памятью

### Кэширование
- Изображения кэшируются Coil
- Данные кэшируются в Room
- Умная стратегия обновления

## Тестирование

### Unit Tests
- ViewModels тестирование бизнес-логики
- Repository тестирование работы с данными
- Utils функции

### UI Tests (Compose)
- Тестирование навигации
- Тестирование взаимодействия с UI
- Screenshot тесты

## Расширяемость

### Добавление новых фич
1. Создать новый Screen в ui/screens/
2. Создать ViewModel если нужно
3. Добавить в навигацию
4. Обновить Repository при необходимости

### Интеграция с новыми API
1. Обновить RecipeApiService
2. Добавить новые модели в data/model/
3. Обновить Repository

## Best Practices

1. **Single Responsibility** - каждый класс имеет одну ответственность
2. **Dependency Injection** - готово к интеграции с Hilt/Dagger
3. **SOLID принципы** - следование принципам ООП
4. **Clean Code** - читаемый и поддерживаемый код
5. **Reactive Programming** - использование Flow и StateFlow

## Будущие улучшения

- [ ] Добавить Hilt для DI
- [ ] Реализовать WorkManager для фоновой синхронизации
- [ ] Добавить пагинацию для больших списков
- [ ] Реализовать push-уведомления
- [ ] Добавить поддержку офлайн-режима с синхронизацией
- [ ] Реализовать sharing рецептов
- [ ] Добавить фильтры и сортировку
- [ ] Поддержка множественных языков
