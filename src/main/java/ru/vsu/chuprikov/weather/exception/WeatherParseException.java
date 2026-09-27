package ru.vsu.chuprikov.weather.exception;

public class WeatherParseException extends RuntimeException {
    private final int lineNumber;
    private final String sourceLine;

    public WeatherParseException(int lineNumber, String sourceLine, String message) {
        super("Строка " + lineNumber + ": " + message + " | '" + sourceLine + "'");
        this.lineNumber = lineNumber;
        this.sourceLine = sourceLine;
    }

    public int lineNumber() {
        return lineNumber;
    }

    public String sourceLine() {
        return sourceLine;
    }
}
