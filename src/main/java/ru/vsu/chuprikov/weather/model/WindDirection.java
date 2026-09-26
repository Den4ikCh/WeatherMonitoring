package ru.vsu.chuprikov.weather.model;

public enum WindDirection {
    N(337.5, 22.5, "Северный"),
    NE(22.5, 67.5, "Северо-восточный"),
    E(67.5, 112.5, "Восточный"),
    SE(112.5, 157.5, "Юго-восточный"),
    S(157.5, 202.5, "Южный"),
    SW(202.5, 247.5, "Юго-западный"),
    W(247.5, 292.5, "Западный"),
    NW(292.5, 337.5, "Северо-западный");

    private final double from;
    private final double to;
    private final String name;

    WindDirection(double from, double to, String name) {
        this.from = from;
        this.to = to;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static WindDirection byAzimuth(double azimuth) {
        if (azimuth < 0 || azimuth >= 360) {
            throw new IllegalArgumentException("Азимут должен быть в [0, 360): " + azimuth);
        }
        for (WindDirection windDirection : values()) {
            if (windDirection == N) {
                if (azimuth >= windDirection.from || azimuth <= windDirection.to) {
                    return windDirection;
                }
            } else if (azimuth >= windDirection.from && azimuth <= windDirection.to) {
                return windDirection;
            }
        }
        throw new IllegalArgumentException("Не удалось определить румб");
    }
}
