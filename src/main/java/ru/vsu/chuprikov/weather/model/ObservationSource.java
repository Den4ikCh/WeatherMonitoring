package ru.vsu.chuprikov.weather.model;

import java.util.List;
import java.util.Objects;

public abstract class ObservationSource {
    protected final String id;
    protected final String name;

    protected ObservationSource(String id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
    }

    public String id() {
        return id;
    }
    public String name() {
        return name;
    }

    public abstract List<Observation> observations();
}
