package pl.kask.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

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
    private int amount;

    public GroceryItem() {
    }

    public GroceryItem(String owner, String itemName, int amount) {
        this.owner = owner;
        this.itemName = itemName;
        this.amount = amount;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
