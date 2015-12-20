package pl.kask.grocerylistclient.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynchronizationRequest implements Serializable {

    private List<String> productsToAdd;
    private List<String> productsToRemove;
    private Map<String, Integer> subSums;
    private Map<String, ShopNameDto> shopNames;

    public SynchronizationRequest() {
        productsToAdd = new ArrayList<>();
        productsToRemove = new ArrayList<>();
        subSums = new HashMap<>();
        shopNames = new HashMap<>();
    }

    public SynchronizationRequest(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> subSums, Map<String, ShopNameDto> shopNames) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.subSums = subSums;
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

    public Map<String, Integer> getSubSums() {
        return subSums;
    }

    public void setSubSums(Map<String, Integer> subSums) {
        this.subSums = subSums;
    }

    public Map<String, ShopNameDto> getShopNames() {
        return shopNames;
    }

    public void setShopNames(Map<String, ShopNameDto> shopNames) {
        this.shopNames = shopNames;
    }

    @Override
    public String toString() {
        return "SynchronizationRequest{" +
                "productsToAdd=" + productsToAdd +
                ", productsToRemove=" + productsToRemove +
                ", subSums=" + subSums +
                ", shopNames=" + shopNames +
                '}';
    }
}
