package pl.kask.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "grocery_item", indexes = {
        @Index(columnList = "id"),
        @Index(columnList = "owner")
})
public class GroceryItem {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private long id;

    private String owner;
    private String itemName;
    private String shopName;
    private long timestamp;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "grocery_items_coowners", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "accountId")
    private List<String> coOwners = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "grocery_items_amounts", joinColumns = @JoinColumn(name = "id"))
    @MapKeyColumn(name = "device_id")
    @Column(name = "amount")
    private Map<String, Integer> subSums = new HashMap<>();

    public GroceryItem() {
    }

    public GroceryItem(String owner, String itemName) {
        this.owner = owner;
        this.itemName = itemName;
    }

    public GroceryItem(String owner, String itemName, String shopName, long timestamp) {
        this.owner = owner;
        this.itemName = itemName;
        this.shopName = shopName;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Integer> getSubSums() {
        return subSums;
    }

    public void setSubSums(Map<String, Integer> subSums) {
        this.subSums = subSums;
    }

    public List<String> getCoOwners() {
        return coOwners;
    }

    public void setCoOwners(List<String> coOwners) {
        this.coOwners = coOwners;
    }

    @Override
    public String toString() {
        return "GroceryItem{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", itemName='" + itemName + '\'' +
                ", shopName='" + shopName + '\'' +
                ", timestamp=" + timestamp +
                ", coOwners=" + coOwners +
                ", subSums=" + subSums +
                '}';
    }
}
