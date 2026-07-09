# Test fixtures

These JSON files mirror the Open-Meteo response schema (forecast, air-quality,
marine) and are used to unit-test parsing and merging without touching the
network.

They were authored to the documented schema, NOT captured live (the authoring
environment had no outbound network). Per spec §8 they should be refreshed with
a real capture when possible:

    curl "https://api.open-meteo.com/v1/forecast?latitude=43.6481,44.12&longitude=51.1722,51.30&hourly=wind_speed_10m,wind_gusts_10m,wind_direction_10m,apparent_temperature,uv_index,precipitation_probability,weather_code,is_day&current=wind_speed_10m,wind_gusts_10m,wind_direction_10m,temperature_2m&wind_speed_unit=ms&timezone=Asia/Aqtau&forecast_days=3" > forecast_aktau.json

`marine_null.json` deliberately has all-null wave heights: the Caspian is poorly
covered by global wave models (spec §14.4). Confirm on real hardware whether the
Marine API returns usable data for the dacha coordinates; if it stays null,
drop the SEA factor rather than inventing a heuristic.
