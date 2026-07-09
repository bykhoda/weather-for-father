# ЕХАТЬ?

Погода для головного устройства Denza N9. Отвечает на вопрос, а не показывает
таблицу. Один экран, landscape, тёмная тема. Взгляд < 2 секунд — и ответ:
ехать на дачу или нет.

## Быстрый старт

    git clone <url> && cd ehat
    ./scripts/init-wrapper.sh      # один раз: создаёт gradle-wrapper.jar (нужен системный gradle)
    ./gradlew assembleDebug

APK: `app/build/outputs/apk/debug/app-debug.apk`

> `gradle-wrapper.jar` не лежит в репозитории (бинарь не удалось приложить из
> окружения без сети). Если у вас Android Studio — он сгенерируется сам при
> открытии проекта, и `init-wrapper.sh` не нужен.

## Своя дача

    cp gradle.properties.example local.properties   # или допишите в существующий local.properties
    # вписать DACHA_LAT, DACHA_LON, ROUTE_BEARING

Без этого файла обе карточки показывают Актау. Приложение соберётся и запустится.

## Эмулятор

Нужен образ **AOSP** (в списке просто «Android 14», без иконки Play Store).
Не «Google APIs» и не «Google Play» — головное устройство их не имеет, и
зависимость от GMS должна падать здесь, а не в машине.
Профиль: landscape, 1920×1080, 240 dpi.

## Тесты

    ./gradlew testDebugUnitTest

Вся логика живёт в `domain/` и тестируется на JVM за секунды, без эмулятора.

## Данные

[Open-Meteo](https://open-meteo.com) — forecast, air quality, marine. Без ключа.

Шесть факторов решения: ветер, ощущаемая жара, УФ, осадки/гроза, пыль (PM10),
море (волна). Приложение находит один ограничивающий фактор и называет его.

## Спецификация

`docs/spec.md` — единственный источник истины. Читать перед изменениями.
