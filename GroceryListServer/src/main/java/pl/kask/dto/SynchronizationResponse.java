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
    private List<String> sharedProducts;

    public SynchronizationResponse() {
        productsToAdd = new ArrayList<>();
        productsToRemove = new ArrayList<>();
        totalAmounts = new HashMap<>();
        shopNames = new HashMap<>();
        sharedProducts = new ArrayList<>();
    }

    public SynchronizationResponse(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> totalAmounts) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.totalAmounts = totalAmounts;
        shopNames = new HashMap<>();
        sharedProducts = new ArrayList<>();
    }

    public SynchronizationResponse(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> totalAmounts, Map<String, ShopNameDto> shopNames, List<String> sharedProducts) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.totalAmounts = totalAmounts;
        this.shopNames = shopNames;
        this.sharedProducts = sharedProducts;
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

    public List<String> getSharedProducts() {
        return sharedProducts;
    }

    public void setSharedProducts(List<String> sharedProducts) {
        this.sharedProducts = sharedProducts;
    }

    @Override
    public String toString() {
        return "SynchronizationResponse{" +
                "productsToAdd=" + productsToAdd +
                ", productsToRemove=" + productsToRemove +
                ", totalAmounts=" + totalAmounts +
                ", shopNames=" + shopNames +
                ", sharedProducts=" + sharedProducts +
                '}';
    }
}
