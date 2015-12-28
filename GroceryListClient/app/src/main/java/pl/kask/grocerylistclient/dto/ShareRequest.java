package pl.kask.grocerylistclient.dto;

import java.io.Serializable;

public class ShareRequest implements Serializable {

    private String itemName;
    private String coOwnerMail;

    public ShareRequest() {
    }

    public ShareRequest(String itemName, String coOwnerMail) {
        this.itemName = itemName;
        this.coOwnerMail = coOwnerMail;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCoOwnerMail() {
        return coOwnerMail;
    }

    public void setCoOwnerMail(String coOwnerMail) {
        this.coOwnerMail = coOwnerMail;
    }

    @Override
    public String toString() {
        return "ShareRequest{" +
                "itemName='" + itemName + '\'' +
                ", coOwnerMail='" + coOwnerMail + '\'' +
                '}';
    }
}

