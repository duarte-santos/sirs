package pt.tecnico.contacttracing.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import pt.tecnico.contacttracing.ble.Constants;
import pt.tecnico.contacttracing.model.NumberKey;


public class ReceivedNumber {
    private long number;
    private Instant firstTimestamp;
    private Instant lastTimestamp;
    private String location;


    public ReceivedNumber(long number, Instant firstTimestamp, Instant lastTimestamp, String location){
        this.number = number;
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        this.location = location;
    }

    private String getLocationString() {
        // Retrieving Latitude and Longitude
        return location.split("gps ")[1].split(" hAcc")[0];
    }

    @Override
    public String toString() {
        long duration = Duration.between(firstTimestamp, lastTimestamp).toMillis() / 1000;
        if (duration == 0) {
            return "number: " + number + "\ndate: " + firstTimestamp + "\nperiod: less than " + Constants.SCAN_PERIOD / 1000 + " seconds\nlocation: " + getLocationString() + "\n";
        }
        return "number: " + number + "\ndate: " + firstTimestamp + "\nperiod: at least " + duration + " seconds\nlocation: " + getLocationString() + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null ) return false;

        if (o instanceof NumberKey) {
            NumberKey nk = (NumberKey) o;
            return number == nk.getNumber();
        }

        if (getClass() != o.getClass()) return false;
        ReceivedNumber receivedNumber = (ReceivedNumber) o;
        return number == receivedNumber.number &&
                Objects.equals(firstTimestamp, receivedNumber.firstTimestamp) &&
                Objects.equals(lastTimestamp, receivedNumber.lastTimestamp) &&
                location.equals(location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, firstTimestamp, lastTimestamp, location);
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public Instant getFirstTimestamp() {
        return firstTimestamp;
    }

    public void setFirstTimestamp(Instant firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public Instant getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Instant lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
