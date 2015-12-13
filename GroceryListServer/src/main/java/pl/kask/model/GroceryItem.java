package pl.kask.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
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

    public Map<String, Integer> getSubSums() {
        return subSums;
    }

    public void setSubSums(Map<String, Integer> subSums) {
        this.subSums = subSums;
    }
}
