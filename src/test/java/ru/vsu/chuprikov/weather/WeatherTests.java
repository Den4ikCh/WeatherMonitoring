package ru.vsu.chuprikov.weather;

import org.junit.jupiter.api.Test;
import ru.vsu.chuprikov.weather.exception.InvalidObservationException;
import ru.vsu.chuprikov.weather.exception.WeatherParseException;
import ru.vsu.chuprikov.weather.model.*;
import ru.vsu.chuprikov.weather.service.WeatherService;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WeatherTests {

    private final WeatherService service = new WeatherService();

    @Test 
    void windDirection_byAzimuth_north() {
        assertEquals(WindDirection.N, WindDirection.byAzimuth(0));
        assertEquals(WindDirection.N, WindDirection.byAzimuth(350));
        assertEquals(WindDirection.N, WindDirection.byAzimuth(359.9));
    }

    @Test 
    void windDirection_byAzimuth_east() {
        assertEquals(WindDirection.E, WindDirection.byAzimuth(90));
    }

    @Test 
    void windDirection_byAzimuth_invalid() {
        assertThrows(IllegalArgumentException.class, () -> WindDirection.byAzimuth(-1));
        assertThrows(IllegalArgumentException.class, () -> WindDirection.byAzimuth(360));
    }

    @Test 
    void observation_negativeWindSpeed_throws() {
        assertThrows(InvalidObservationException.class, () ->
                new Observation(Instant.now(), 10, 760, -1, WindDirection.N, WeatherPhenomenon.CLEAR));
    }

    @Test 
    void observation_nonPositivePressure_throws() {
        assertThrows(InvalidObservationException.class, () ->
                new Observation(Instant.now(), 10, 0, 1, WindDirection.N, WeatherPhenomenon.CLEAR));
    }

    @Test 
    void station_equals_hashCode_contract() {
        Region r = new Region("Р1");
        Station a = new Station("S1", "A", r, ZoneId.of("UTC"));
        Station b = new Station("S1", "B", r, ZoneId.of("Europe/Moscow"));
        Station c = new Station("S2", "C", r, ZoneId.of("UTC"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test 
    void parse_valid() {
        String[] lines = {
                "2026-09-01T12:00:00Z;21.5;760;4.2;180;RAIN",
                "2026-09-01T13:00:00Z;22.0;759;3.5;90;CLEAR"
        };
        List<Observation> list = service.parse(lines);
        assertEquals(2, list.size());
        assertEquals(WindDirection.S, list.get(0).windDirection());
        assertEquals(WeatherPhenomenon.RAIN, list.get(0).phenomenon());
    }

    @Test 
    void parse_invalidLineNumberReported() {
        String[] lines = {
                "2026-09-01T12:00:00Z;21.5;760;4.2;180;RAIN",
                "bad-line",
                "2026-09-01T14:00:00Z;22.0;759;3.5;90;CLEAR"
        };
        WeatherParseException ex = assertThrows(WeatherParseException.class,
                () -> service.parse(lines));
        assertEquals(2, ex.lineNumber());
        assertEquals("bad-line", ex.sourceLine());
    }

    @Test 
    void parse_badPhenomenon_throws() {
        String[] lines = {"2026-09-01T12:00:00Z;21.5;760;4.2;180;UNKNOWN"};
        assertThrows(WeatherParseException.class, () -> service.parse(lines));
    }

    @Test 
    void temperatureStats_basic() {
        Region r = buildRegion();
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to   = Instant.parse("2026-09-02T00:00:00Z");
        var stats = service.temperatureStats(r, from, to);
        assertEquals(15.0, stats.min(), 1e-9);
        assertEquals(25.0, stats.max(), 1e-9);
        assertEquals(20.0, stats.average(), 1e-9);
        assertEquals(3, stats.count());
    }

    @Test 
    void temperatureStats_emptyPeriod_throws() {
        Region r = buildRegion();
        assertThrows(IllegalStateException.class, () -> service.temperatureStats(
                r, Instant.parse("2030-01-01T00:00:00Z"), Instant.parse("2030-01-02T00:00:00Z")));
    }

    @Test 
    void precipitationDays_found() {
        Region r = buildRegion();
        Set<LocalDate> days = service.precipitationDays(r);
        assertTrue(days.contains(LocalDate.of(2026, 9, 1)));
    }


    @Test 
    void dailyTemperatureCurve_averagesByHour() {
        Region r = buildRegion();
        Station s = r.stations().get(0);
        Map<Integer, Double> curve = service.dailyTemperatureCurve(s, LocalDate.of(2026, 9, 1));
        assertEquals(15.0, curve.get(15), 1e-9);
        assertEquals(25.0, curve.get(16), 1e-9);
    }

    @Test 
    void observationsAtSameMoment_acrossTimeZones() {
        Region r = buildRegion();
        Map<Instant, List<Observation>> map = service.observationsAtSameMoment(r);

        Instant key = Instant.parse("2026-09-01T12:00:00Z");
        assertTrue(map.containsKey(key));
        assertEquals(2, map.get(key).size());
    }

    @Test 
    void anomalousDays_detectsDeviation() {
        Region r = buildRegion();
        Map<LocalDate, Double> norms = Map.of(
                LocalDate.of(2026, 9, 1), 10.0,
                LocalDate.of(2026, 9, 2), 20.0
        );
        List<LocalDate> bad = service.anomalousDays(r, norms, 5.0);
        assertEquals(List.of(LocalDate.of(2026, 9, 1)), bad);
    }

    private Region buildRegion() {
        Region r = new Region("Тестовый");
        Station s1 = new Station("S1", "Станция 1", r, ZoneId.of("Europe/Moscow"));
        Station s2 = new Station("S2", "Станция 2", r, ZoneId.of("Asia/Novosibirsk"));
        r.addStation(s1);
        r.addStation(s2);

        s1.addObservation(new Observation(Instant.parse("2026-09-01T12:00:00Z"),
                15.0, 760, 3.0, WindDirection.N, WeatherPhenomenon.RAIN));
        s1.addObservation(new Observation(Instant.parse("2026-09-01T13:00:00Z"),
                25.0, 759, 2.0, WindDirection.S, WeatherPhenomenon.CLEAR));
        s2.addObservation(new Observation(Instant.parse("2026-09-01T12:00:00Z"),
                20.0, 757, 5.0, WindDirection.W, WeatherPhenomenon.CLOUDY));
        s2.addObservation(new Observation(Instant.parse("2026-09-02T08:00:00Z"),
                20.0, 758, 1.0, WindDirection.E, WeatherPhenomenon.CLEAR));
        return r;
    }
}