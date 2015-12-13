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

    public SynchronizationResponse() {
        productsToAdd = new ArrayList<>();
        productsToRemove = new ArrayList<>();
        totalAmounts = new HashMap<>();
    }

    public SynchronizationResponse(List<String> productsToAdd, List<String> productsToRemove, Map<String, Integer> totalAmounts) {
        this.productsToAdd = productsToAdd;
        this.productsToRemove = productsToRemove;
        this.totalAmounts = totalAmounts;
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
}
