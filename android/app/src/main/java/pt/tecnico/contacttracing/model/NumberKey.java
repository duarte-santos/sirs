package pt.tecnico.contacttracing.model;

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
