package ru.vsu.chuprikov.weather.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import ru.vsu.chuprikov.weather.exception.WeatherParseException;
import ru.vsu.chuprikov.weather.model.*;

public class WeatherService {
    public List<Observation> parse(String[] lines) {
        List<Observation> observations = new ArrayList<Observation>();
        for (int i = 0; i < lines.length; i++) {
            observations.add(parseLine(lines[i], i + 1));
        }
        return observations;
    }

    private Observation parseLine(String line, int lineNumber) {
        if (line == null || line.isBlank()) {
            throw new WeatherParseException(lineNumber, String.valueOf(line), "Пустая строка");
        }
        String[] parts = line.split(";");
        if (parts.length != 6) {
            throw new WeatherParseException(lineNumber, line, "Ожидалось 6 полей, получено " + parts.length);
        }
        try {
            Instant moment = Instant.parse(parts[0].trim());
            double temp = Double.parseDouble(parts[1].trim());
            int pressure = Integer.parseInt(parts[2].trim());
            double windSpeed = Double.parseDouble(parts[3].trim());
            double azimuth = Double.parseDouble(parts[4].trim());
            WeatherPhenomenon phenomenon = WeatherPhenomenon.valueOf(parts[5].trim().toUpperCase());
            WindDirection dir = WindDirection.byAzimuth(azimuth);
            return new Observation(moment, temp, pressure, windSpeed, dir, phenomenon);
        } catch (WeatherParseException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherParseException(lineNumber, line, "Ошибка разбора: " + e.getMessage());
        }
    }

    public TempStats temperatureStats(Region region, Instant from, Instant to) {
        List<Observation> filtered = filterByPeriod(region.allObservations(), from, to);
        if (filtered.isEmpty()) throw new IllegalStateException("Нет наблюдений в периоде");
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, sum = 0;
        for (Observation o : filtered) {
            min = Math.min(min, o.temperature());
            max = Math.max(max, o.temperature());
            sum += o.temperature();
        }
        return new TempStats(min, max, sum / filtered.size(), filtered.size());
    }

    public record TempStats(double min, double max, double average, int count) {}

    public Map<Integer, Double> dailyTemperatureCurve(Station station, LocalDate date) {
        Map<Integer, List<Double>> byHour = new TreeMap<>();
        for (Observation o : station.observations()) {
            ZonedDateTime local = o.moment().atZone(station.zone());
            if (local.toLocalDate().equals(date)) {
                byHour.computeIfAbsent(local.getHour(), k -> new ArrayList<>())
                        .add(o.temperature());
            }
        }
        Map<Integer, Double> result = new LinkedHashMap<>();
        byHour.forEach((hour, list) ->
                result.put(hour, list.stream().mapToDouble(Double::doubleValue).average().orElseThrow()));
        return result;
    }

    public Set<LocalDate> precipitationDays(Region region) {
        Set<LocalDate> result = new TreeSet<>();
        for (Station s : region.stations()) {
            for (Observation o : s.observations()) {
                if (o.phenomenon().isPrecipitation()) {
                    result.add(o.moment().atZone(s.zone()).toLocalDate());
                }
            }
        }
        return result;
    }

    public Map<Instant, List<Observation>> observationsAtSameMoment(Region region) {
        Map<Instant, List<Observation>> result = new TreeMap<>();
        for (Observation o : region.allObservations()) {
            Instant key = o.moment().truncatedTo(ChronoUnit.SECONDS);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(o);
        }
        result.values().removeIf(list -> list.size() < 2);
        return result;
    }

    public List<LocalDate> anomalousDays(Region region, Map<LocalDate, Double> normByDay, double delta) {
        Map<LocalDate, List<Double>> byDay = new TreeMap<>();
        for (Station s : region.stations()) {
            for (Observation o : s.observations()) {
                LocalDate day = o.moment().atZone(s.zone()).toLocalDate();
                byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(o.temperature());
            }
        }
        List<LocalDate> result = new ArrayList<>();
        byDay.forEach((day, temps) -> {
            Double norm = normByDay.get(day);
            if (norm == null) return;
            double avg = temps.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
            if (Math.abs(avg - norm) > delta) result.add(day);
        });
        return result;
    }

    private List<Observation> filterByPeriod(List<Observation> src, Instant from, Instant to) {
        List<Observation> result = new ArrayList<>();
        for (Observation o : src) {
            if (!o.moment().isBefore(from) && !o.moment().isAfter(to)) result.add(o);
        }
        return result;
    }
}
