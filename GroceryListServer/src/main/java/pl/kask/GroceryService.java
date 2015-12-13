package pl.kask;

import pl.kask.dto.SynchronizationRequest;
import pl.kask.dto.SynchronizationResponse;
import pl.kask.model.GroceryDao;
import pl.kask.model.GroceryItem;

import java.util.ArrayList;
import java.util.List;

public class GroceryService {

    private static GroceryDao groceryDao;

    public GroceryService() {
        groceryDao = new GroceryDao();
    }

    public GroceryDao getGroceryDao() {
        return groceryDao;
    }

    public void persist(GroceryItem groceryItem) {
        groceryDao.persist(groceryItem);
    }

    public void update(GroceryItem groceryItem) {
        groceryDao.update(groceryItem);
    }

    public void delete(GroceryItem groceryItem) {
        groceryDao.delete(groceryItem);
    }

    public GroceryItem findById(long id) {
        return groceryDao.findById(id);
    }

    public List<GroceryItem> findAll() {
        return groceryDao.findAll();
    }

    public List<GroceryItem> findByOwner(String owner) {
        return groceryDao.findByOwner(owner);
    }

    public SynchronizationResponse synchronize(String userId, String deviceId, SynchronizationRequest request) {

        List<GroceryItem> items = findByOwner(userId);

        List<String> productsToRemove = request.getProductsToRemove();
        List<GroceryItem> itemsToRemove = new ArrayList<>();
        items.stream().filter(item -> productsToRemove.contains(item.getItemName())).forEach(item -> {
            itemsToRemove.add(item);
            delete(item);
        });
        items.removeAll(itemsToRemove);

        for (String productName : request.getProductsToAdd()) {
            if (items.stream().anyMatch(item -> item.getItemName().equals(productName))) {
                break;
            }

            GroceryItem newItem = new GroceryItem(userId, productName);
            persist(newItem);
            items.add(newItem);
        }

        SynchronizationResponse result = new SynchronizationResponse();

        for (GroceryItem item : items) {
            if (!request.getSubSums().containsKey(item.getItemName())) {
                result.getProductsToAdd().add(item.getItemName());
            } else {
                int newSubSum = request.getSubSums().get(item.getItemName());
                item.getSubSums().put(deviceId, newSubSum);
                update(item);
            }
            Integer totalAmount = item.getSubSums().values().stream().reduce(0, Integer::sum);
            result.getTotalAmounts().put(item.getItemName(), totalAmount);
        }

        for (String productName : request.getSubSums().keySet()) {
            if (items.stream().noneMatch(item -> item.getItemName().equals(productName))) {
                result.getProductsToRemove().add(productName);
            }
        }

        return result;
    }
}
