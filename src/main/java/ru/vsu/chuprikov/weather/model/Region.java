package ru.vsu.chuprikov.weather.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Region {
    private final String name;
    private final List<Station> stations = new ArrayList<>();

    public Region(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public List<Station> stations() {
        return Collections.unmodifiableList(stations);
    }

    public void addStation(Station station) {
        stations.add(Objects.requireNonNull(station));
    }

    public List<Observation> allObservations() {
        List<Observation> observations = new ArrayList<>();
        for (Station station : stations) {
            observations.addAll(station.observations());
        }
        return observations;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Region r)) return false;
        return name.equals(r.name);
    }

    @Override
    public String toString() {
        return "Region{" + name + "}";
    }
}


