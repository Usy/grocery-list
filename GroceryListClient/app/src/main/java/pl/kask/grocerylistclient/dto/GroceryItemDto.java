package pl.kask.grocerylistclient.dto;

import java.io.Serializable;

public class GroceryItemDto implements Serializable, Comparable<GroceryItemDto> {

    private String owner;
    private String itemName;
    private int amount;
    private int localAmount;
    private ShopNameDto shopName;
    private boolean shared;

    public GroceryItemDto() {
    }

    public GroceryItemDto(String owner, String itemName, int amount, int localAmount, ShopNameDto shopName, boolean shared) {
        this.owner = owner;
        this.itemName = itemName;
        this.amount = amount;
        this.localAmount = localAmount;
        this.shopName = shopName;
        this.shared = shared;
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

    public int getLocalAmount() {
        return localAmount;
    }

    public void setLocalAmount(int localAmount) {
        this.localAmount = localAmount;
    }

    public ShopNameDto getShopName() {
        return shopName;
    }

    public void setShopName(ShopNameDto shopName) {
        this.shopName = shopName;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    @Override
    public String toString() {
        String shop = "";
        if (shopName != null && shopName.getName() != null && !shopName.getName().isEmpty()) {
            shop = " - " + shopName.getName();
        }
        String sharedTag = (shared ? "[shared] " : "");
        return sharedTag + itemName + " (" + amount + ")" + shop;
    }

    @Override
    public int compareTo(GroceryItemDto another) {
        if (this.shared == another.shared) {
            return this.getItemName().compareTo(another.getItemName());
        } else {
            return shared ? 1 : -1;
        }
    }
}