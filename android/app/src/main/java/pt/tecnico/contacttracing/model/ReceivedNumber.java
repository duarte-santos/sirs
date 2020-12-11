package pt.tecnico.contacttracing.model;

import java.time.Instant;
import java.util.Objects;
import pt.tecnico.contacttracing.model.NumberKey;


public class ReceivedNumber {
    private int number;
    private Instant timestamp;
    private String location;


    public ReceivedNumber(int number, Instant timestamp, String location){
        this.number = number;
        this.timestamp = timestamp;
        this.location = location;
    }

    @Override
    public String toString() {
        return "ReceivedNumber{" +
                "number='" + number + '\'' +
                ", timestamp=" + timestamp +
                ", location=" + location +
                '}';
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
                Objects.equals(timestamp, receivedNumber.timestamp) &&
                location.equals(location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, timestamp, location);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
