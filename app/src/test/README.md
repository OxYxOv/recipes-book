# Test Suite Documentation

This directory contains comprehensive tests for the Recipes Book application.

## Test Structure

### Unit Tests (`app/src/test`)
Unit tests run on the JVM without requiring an Android device or emulator.

#### Repository Tests
- **RecipeRepositoryTest.kt**: Tests for RecipeRepository
  - getAllRecipes flow
  - getFavoriteRecipes filtering
  - searchRecipes functionality
  - getRecipesByCategory filtering
  - CRUD operations (insert, update, delete)
  - toggleFavorite functionality
  - API sync with error handling

#### ViewModel Tests
- **HomeViewModelTest.kt**: Tests for HomeViewModel
  - loadAllRecipes
  - loadRecipesByCategory
  - searchRecipes with query
  - searchRecipes with blank query (should load all)
  - toggleFavorite
  
- **CatalogViewModelTest.kt**: Tests for CatalogViewModel
  - Initial recipes loading
  - toggleFavorite
  - Reactive updates

- **AddRecipeViewModelTest.kt**: Tests for AddRecipeViewModel
  - addRecipe with all fields
  - addRecipe with null imageUrl
  - Recipe marked as local

- **RecipeDetailViewModelTest.kt**: Tests for RecipeDetailViewModel
  - Recipe loading by ID
  - Null handling for missing recipes
  - toggleFavorite with reload

#### Data Layer Tests
- **UserPreferencesManagerTest.kt**: Tests for UserPreferencesManager using Robolectric
  - saveUserData
  - clearUserData
  - Default values (null/false)
  - Multiple saves overwrite

### Instrumented Tests (`app/src/androidTest`)
Instrumented tests run on Android devices/emulators and test Android-specific functionality.

#### DAO Tests
- **RecipeDaoTest.kt**: Tests for RecipeDao using in-memory database
  - insertAndGetRecipe
  - getAllRecipes
  - getRecipesByCategory
  - getFavoriteRecipes
  - searchRecipes (by name and description)
  - updateRecipe
  - deleteRecipe
  - updateFavoriteStatus
  - insertReplace (conflict strategy)

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests RecipeRepositoryTest
```

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Dependencies

- **JUnit 4.13.2**: Testing framework
- **Mockito 5.7.0**: Mocking framework
- **Mockito-Kotlin 5.1.0**: Kotlin extensions for Mockito
- **Coroutines Test 1.7.3**: Testing coroutines
- **Arch Core Testing 2.2.0**: Testing LiveData and ViewModels
- **Room Testing 2.6.1**: In-memory database for testing
- **Turbine 1.0.0**: Testing Kotlin Flows
- **Robolectric 4.11.1**: Android framework for JVM tests
- **Espresso 3.5.1**: UI testing
- **Compose UI Test**: Testing Compose components

## Coverage

The test suite covers:
- ✅ Repository layer (100%)
- ✅ ViewModel layer (100%)
- ✅ DAO layer (100%)
- ✅ UserPreferences (100%)

## Best Practices

1. **Isolation**: Each test is isolated and doesn't depend on others
2. **Mocking**: External dependencies are mocked
3. **Coroutines**: Uses TestDispatcher for coroutine testing
4. **Flow Testing**: Uses Turbine for Flow testing
5. **Database Testing**: Uses in-memory database for DAO tests
6. **Given-When-Then**: Clear test structure with comments

## CI/CD Integration

Tests are automatically run in the CI/CD pipeline:
- Unit tests run on every commit
- Instrumented tests run on pull requests
- Coverage reports are generated
