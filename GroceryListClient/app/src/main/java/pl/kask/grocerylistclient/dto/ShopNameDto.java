package pl.kask.grocerylistclient.dto;

public class ShopNameDto {

    private String name;
    private long timestamp;

    public ShopNameDto() {
        name = "";
        timestamp = 0;
    }

    public ShopNameDto(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ShopNameDto{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
