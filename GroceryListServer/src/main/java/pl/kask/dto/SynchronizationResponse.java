package pl.kask.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynchronizationResponse implements Serializable {

    private List<String> productsToAdd;
    private List<String> productsToRemove;
    private Map<String, Integer> totalAmounts;
    private Map<String, ShopNameDto> shopNames;

    public SynchronizationResponse() {
        productsToAdd = new ArrayList<>();
        productsToRemove = new ArrayList<>();
        totalAmounts = new HashMap<>();
        shopNames = new HashMap<>();
    }

    public SynchronizationResponse(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> totalAmounts) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.totalAmounts = totalAmounts;
        shopNames = new HashMap<>();
    }

    public SynchronizationResponse(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> totalAmounts, Map<String, ShopNameDto> shopNames) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.totalAmounts = totalAmounts;
        this.shopNames = shopNames;
    }

    public List<String> getProductsToAdd() {
        return productsToAdd;
    }

    public void setProductsToAdd(List<String> productsToAdd) {
        this.productsToAdd = productsToAdd;
    }

    public List<String> getProductsToRemove() {
        return productsToRemove;
    }

    public void setProductsToRemove(List<String> productsToRemove) {
        this.productsToRemove = productsToRemove;
    }

    public Map<String, Integer> getTotalAmounts() {
        return totalAmounts;
    }

    public void setTotalAmounts(Map<String, Integer> totalAmounts) {
        this.totalAmounts = totalAmounts;
    }

    public Map<String, ShopNameDto> getShopNames() {
        return shopNames;
    }

    public void setShopNames(Map<String, ShopNameDto> shopNames) {
        this.shopNames = shopNames;
    }

    @Override
    public String toString() {
        return "SynchronizationResponse{" +
                "productsToAdd=" + productsToAdd +
                ", productsToRemove=" + productsToRemove +
                ", totalAmounts=" + totalAmounts +
                ", shopNames=" + shopNames +
                '}';
    }
}
