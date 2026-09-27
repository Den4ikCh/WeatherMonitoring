package ru.vsu.chuprikov.weather.exception;

public class InvalidObservationException extends RuntimeException {

    private final String field;
    private final Object value;

    public InvalidObservationException(String field, Object value, String message) {
        super("Недопустимое значение поля '" + field + "': " + value + " (" + message + ")");
        this.field = field;
        this.value = value;
    }

    public String field() { return field; }
    public Object value() { return value; }
}