# ЕХАТЬ? — контекст проекта

## Что это
Погодное приложение для головного устройства Denza N9 (BYD DiLink).
Единственный пользователь — мужчина 55 лет, Актау, Казахстан.
Отвечает на вопрос «ехать на дачу?», а не показывает погоду.
Полная спецификация: `docs/spec.md`. Читать перед изменениями.

## Жёсткие ограничения
- Google Play Services ОТСУТСТВУЮТ. Нет Firebase, нет FusedLocation, нет GMS вообще.
- minSdk 24. Только landscape. Ноль runtime-permissions (INTERNET и ACCESS_NETWORK_STATE — normal).
- Сеть ненадёжна. Оффлайн — штатный режим, не исключение.
- `domain/` не импортирует ничего из `android.*` и `androidx.*`.

## Стек
Kotlin, Compose Material3, OkHttp, kotlinx.serialization, DataStore.
БЕЗ: Hilt, Koin, Retrofit, Coil, WorkManager, Room.

## Правила
- Ровно ШЕСТЬ факторов погоды (WIND, HEAT, UV, PRECIP, DUST, SEA). Седьмой добавляется только удалением одного из шести.
- Никаких диалогов и тостов при ошибке сети. Только штамп свежести.
- FontWeight.Light запрещён. Минимум — Bold для цифр.
- Цифры: fontFeatureSettings = "tnum".
- Красно-зелёная пара как единственный индикатор запрещена (палитра teal/amber/coral + текст + иконка).
- Время берётся из инжектируемого `Clock`, НЕ из `Instant.now()`.

## Архитектура
- `domain/` — чистый Kotlin, вся тестируемая логика (WindMath, StatusMapper, FactorEngine, ArrivalForecast, CalmWindow). Тесты — JVM, без Robolectric.
- `data/` — OpenMeteoClient (3 endpoint'а), ForecastRepository (single-flight, ретраи, кэш), FetchError, DataStore.
- `ui/` — Compose. Логика только в `MainViewModel` (StateFlow), в @Composable — отрисовка.
- DI собирается вручную в `AppGraph`, создаётся в `EhatApp`.

## Известные TODO для сборочной машины (нет сети/SDK у автора)
- `gradle/wrapper/gradle-wrapper.jar` не закоммичен (бинарь). Сгенерировать: `./scripts/init-wrapper.sh` или `gradle wrapper --gradle-version 8.9`.
- Тестовые фикстуры в `app/src/test/resources/*.json` собраны по схеме Open-Meteo, не сняты вживую. Обновить реальным ответом (см. resources/README.md).
- Marine API по Каспию, вероятно, отдаёт null для волн — фактор SEA тогда сам отключается (relevant=false), эвристику НЕ подставлять. Проверить на реальном устройстве.
- Координаты дачи: положить в `local.properties` (DACHA_LAT/DACHA_LON/ROUTE_BEARING). По умолчанию — Актау.

## Проверка перед коммитом
./gradlew assembleDebug testDebugUnitTest
./gradlew :app:dependencies | grep gms   # должно быть пусто

## Версии
Не полагайся на версии из памяти — они устарели. Всё в `gradle/libs.versions.toml`.
При ошибке компиляции читай сообщение и правь, не подставляй «похожий» API.
