package ru.vsu.chuprikov.weather.model;

public enum WeatherPhenomenon {
    CLEAR("Ясно", false),
    CLOUDY("Облачно", false),
    RAIN("Дождь", true),
    SNOW("Снег", true),
    FOG("Туман", false),
    STORM("Гроза", true),
    HAIL("Град", true);

    private final String name;
    private final boolean precipitation;

    WeatherPhenomenon(String name, boolean precipitation) {
        this.name = name;
        this.precipitation = precipitation;
    }

    public String getName() {
        return name;
    }

    public boolean isPrecipitation() {
        return precipitation;
    }
}
