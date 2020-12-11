package pt.tecnico.contacttracing.model;

import java.util.Objects;


public class NumberKey {
    private String key;
    private int number;

    public NumberKey(String key, int number){
        this.key = key;
        this.number = number;
    }

    @Override
    public String toString() {
        return "NumberKey{" +
                "key='" + key + '\'' +
                ", number=" + number +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null ) return false;
        if (o instanceof ReceivedNumber) {
            ReceivedNumber rn = (ReceivedNumber) o;
            return number == rn.getNumber();
        }
        if (getClass() != o.getClass()) return false;
        NumberKey numberKey = (NumberKey) o;
        return number == numberKey.number &&
                Objects.equals(key, numberKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, number);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
