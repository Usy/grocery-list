package pl.kask.grocerylistclient.dto;

import java.io.Serializable;

public class GroceryItemDto implements Serializable {

    private String owner;
    private String itemName;
    private int amount;

    public GroceryItemDto() {
    }

    public GroceryItemDto(String owner, String itemName, int amount) {
        this.owner = owner;
        this.itemName = itemName;
        this.amount = amount;
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

    @Override
    public String toString() {
        return itemName + " (" + amount + ")";
    }
}