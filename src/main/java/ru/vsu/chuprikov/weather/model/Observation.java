package ru.vsu.chuprikov.weather.model;

import ru.vsu.chuprikov.weather.exception.InvalidObservationException;

import java.time.Instant;
import java.util.Objects;

public record Observation(
    Instant moment,
    double temperature,
    int pressure,
    double windSpeed,
    WindDirection windDirection,
    WeatherPhenomenon phenomenon
) {
    public static final double MIN_TEMPERATURE = -90.0;
    public static final double MAX_TEMPERATURE = 60.0;
    public static final int    MIN_PRESSURE    = 600;
    public static final int    MAX_PRESSURE    = 850;
    public static final double MIN_WIND_SPEED  = 0.0;
    public static final double MAX_WIND_SPEED  = 120.0;

    public Observation {
        Objects.requireNonNull(moment, "moment");
        Objects.requireNonNull(windDirection, "windDirection");
        Objects.requireNonNull(phenomenon, "phenomenon");

        if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
            throw new InvalidObservationException("temperature", temperature,
                    "должна быть в [" + MIN_TEMPERATURE + ", " + MAX_TEMPERATURE + "]");
        }
        if (pressure < MIN_PRESSURE || pressure > MAX_PRESSURE) {
            throw new InvalidObservationException("pressure", pressure,
                    "должно быть в [" + MIN_PRESSURE + ", " + MAX_PRESSURE + "]");
        }
        if (windSpeed < MIN_WIND_SPEED || windSpeed > MAX_WIND_SPEED) {
            throw new InvalidObservationException("windSpeed", windSpeed,
                    "должна быть в [" + MIN_WIND_SPEED + ", " + MAX_WIND_SPEED + "]");
        }
    }
}
