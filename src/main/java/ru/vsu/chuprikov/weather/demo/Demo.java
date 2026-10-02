package ru.vsu.chuprikov.weather.demo;

import ru.vsu.chuprikov.weather.model.*;
import ru.vsu.chuprikov.weather.service.WeatherService;

import java.time.*;
import java.util.*;

public class Demo {
    public static void main(String[] args) {
        WeatherService service = new WeatherService();

        Region region = new Region("Центральный");
        ZoneId moscow = ZoneId.of("Europe/Moscow");
        ZoneId novosib = ZoneId.of("Asia/Novosibirsk");

        Station s1 = new Station("MSK-1", "Москва-ВДНХ", region, moscow);
        Station s2 = new Station("NSK-1", "Новосибирск-Центр", region, novosib);
        region.addStation(s1);
        region.addStation(s2);

        List<String> lines = new ArrayList<>();
        Random rnd = new Random(42);
        WindDirection[] dirs = WindDirection.values();
        WeatherPhenomenon[] phs = WeatherPhenomenon.values();

        for (int day = 1; day <= 5; day++) {
            for (int hour = 0; hour < 24; hour += 4) {
                double t = 10 + 8 * Math.sin((hour - 6) / 24.0 * 2 * Math.PI) + rnd.nextDouble() * 3;
                int p = 754 + rnd.nextInt(15);
                double ws = rnd.nextDouble() * 8;
                double azDouble = rnd.nextDouble() * 360;
                int az = (int) Math.round(azDouble) % 360;
                var ph = phs[rnd.nextInt(phs.length)];
                String instant = String.format("2026-09-%02dT%02d:00:00Z", day, hour);
                lines.add(String.format(Locale.ROOT, "%s;%.1f;%d;%.1f;%d;%s",
                        instant, t, p, ws, az, ph.name()));
                if (lines.size() >= 35) break;
            }
            if (lines.size() >= 35) break;
        }

        List<Observation> observations = service.parse(lines.toArray(String[]::new));
        System.out.println("Загружено наблюдений: " + observations.size());

        for (int i = 0; i < observations.size(); i++) {
            (i % 2 == 0 ? s1 : s2).addObservation(observations.get(i));
        }

        var from = Instant.parse("2026-09-01T00:00:00Z");
        var to   = Instant.parse("2026-09-06T00:00:00Z");
        var stats = service.temperatureStats(region, from, to);
        System.out.printf("Температура: min=%.1f max=%.1f avg=%.2f (n=%d)%n",
                stats.min(), stats.max(), stats.average(), stats.count());

        System.out.println("Суточный ход температуры для " + s1.name() + ":");
        service.dailyTemperatureCurve(s1, LocalDate.of(2026, 6, 1))
                .forEach((h, t) -> System.out.printf("  %02d:00 -> %.1f°C%n", h, t));

        System.out.println("Дни с осадками: " + service.precipitationDays(region));

        System.out.println("Одновременные наблюдения:");
        service.observationsAtSameMoment(region).forEach((instant, list) -> {
            System.out.println("  " + instant + " -> " + list.size() + " станций");
        });

        Map<LocalDate, Double> norms = new HashMap<>();
        for (int d = 1; d <= 5; d++) norms.put(LocalDate.of(2026, 6, d), 15.0);
        System.out.println("Аномальные дни (delta=5): " +
                service.anomalousDays(region, norms, 5.0));

        try {
            service.parse(new String[]{
                    "2026-09-01T00:00:00Z;10;1010;1;0;CLEAR",
                    "broken;;;",
                    "2026-09-02T00:00:00Z;11;1011;2;90;RAIN"
            });
        } catch (Exception e) {
            System.out.println("Поймано исключение: " + e.getMessage());
        }
    }
}