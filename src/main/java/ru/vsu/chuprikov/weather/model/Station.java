package ru.vsu.chuprikov.weather.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Station extends ObservationSource {
    private final Region region;
    private final ZoneId zone;
    private final List<Observation> observations = new ArrayList<>();

    public Station(String id, String name, Region region, ZoneId zone) {
        super(id, name);
        this.region = Objects.requireNonNull(region);
        this.zone = Objects.requireNonNull(zone);
    }

    public Region region() {
        return region;
    }

    public ZoneId zone() {
        return zone;
    }

    public List<Observation> observations() {
        return Collections.unmodifiableList(observations);
    }

    public void addObservation(Observation observation) {
        observations.add(Objects.requireNonNull(observation));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Station s)) return false;
        return id.equals(s.id);
    }

    @Override
    public String toString() {
        return "Station{" + id + ", " + name + ", zone=" + zone + "}";
    }
}
