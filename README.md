# Weather Monitoring

Модель предметной области «Метеонаблюдения по районам» (вариант 14).

## Модель
- `Region` — район, агрегирует станции.
- `Station` — метеостанция с часовым поясом (`ZoneId`).
- `Observation` (record) — момент времени (`Instant`), температура (°C),
  давление (мм рт. ст.), скорость ветра (м/с), направление, явление.
- `WindDirection` — 8 румбов, метод `byAzimuth(double)`.
- `WeatherPhenomenon` — явления погоды с признаком осадков.

## Единицы измерения
- температура — °C;
- давление — **мм рт. ст.** (диапазон 600–850);
- скорость ветра — м/с (диапазон 0–120);
- время — `Instant` (UTC), локальность через `ZoneId` станции.

## Сборка и запуск
- `mvn clean test` — запуск тестов;
- `mvn compile exec:java` — запуск демонстрации;
- `mvn javadoc:javadoc` — генерация документации.

## Методы сервиса
- `parse(String[])` — разбор наблюдений из строк;
- `temperatureStats(Region, Instant, Instant)` — min/max/avg;
- `dailyTemperatureCurve(Station, LocalDate)` — суточный ход;
- `precipitationDays(Region)` — дни с осадками;
- `observationsAtSameMoment(Region)` — одновременные наблюдения;
- `anomalousDays(Region, NormProvider, double)` — аномалии.

## История коммитов
Разбита по слоям: `chore → exception → model → service → test → demo → docs`.