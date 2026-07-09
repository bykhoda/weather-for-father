---
name: compose-conventions
description: Конвенции Jetpack Compose для этого проекта. Использовать при написании или изменении любого @Composable, ViewModel или UI-стейта.
---
# Compose
- Логика в ViewModel. В @Composable — только отрисовка.
- Стейт: StateFlow + collectAsStateWithLifecycle(). Не LiveData, не mutableStateOf во ViewModel.
- Каждый экранный @Composable имеет @Preview для всех статусов (CALM/WINDY/HARSH).
- Ноль DI-фреймворков. Граф собирается вручную в AppGraph/EhatApp.
- Частицы фона — один Canvas + withFrameNanos. Не 150 composable.
- Никаких аллокаций внутри кадра отрисовки.
- Анимация угла — по кратчайшей дуге: ((to - from + 540) % 360) - 180
- Числа: TextStyle(fontFeatureSettings = "tnum"). Вес >= Bold, никогда Light.
