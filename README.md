# 🎮 GameTopHelperKMM

Кроссплатформенная игра с расписанием колледжа на Kotlin Multiplatform Mobile

## 📊 Статус сборки

| Платформа | Статус                                                                                                                                                                                               |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Android | [![Build Android](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/build-android.yml/badge.svg)](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/build-android.yml) |
| iOS | [![Build iOS](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/build-ios.yml/badge.svg)](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/build-ios.yml)             |
| Tests | [![Run Tests](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/tests.yml/badge.svg)](https://github.com/roma111-ai/GameTopHelperKMM/actions/workflows/tests.yml)                     |

## 📱 Функциональность

- ✅ Вход для студентов (JSON API)
- ✅ Вход для преподавателей (свой сервер)
- 🎮 C++ игровой движок
- 📅 Расписание в виджете
- 🔄 Кроссплатформенность (Android + iOS)

## 🛠 Технологии

- Kotlin Multiplatform Mobile
- Compose Multiplatform
- Ktor Client
- C++ (игровой движок)
- GitHub Actions CI/CD

## 🚀 Запуск проекта

```bash
# Клонировать
git clone https://github.com/roma111-ai/GameTopHelperKMM.git

# Собрать Android
./gradlew :androidApp:assembleDebug

# Собрать iOS (на Mac)
./gradlew :shared:iosX64Test