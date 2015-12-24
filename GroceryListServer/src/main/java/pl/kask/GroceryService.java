package pl.kask;

import pl.kask.dto.ShopNameDto;
import pl.kask.dto.SynchronizationRequest;
import pl.kask.dto.SynchronizationResponse;
import pl.kask.model.Account;
import pl.kask.model.AccountDao;
import pl.kask.model.GroceryDao;
import pl.kask.model.GroceryItem;

import java.util.ArrayList;
import java.util.List;

public class GroceryService {

    private static GroceryDao groceryDao;
    private static AccountDao accountDao;

    public GroceryService(AccountDao accountDao) {
        groceryDao = new GroceryDao();
        GroceryService.accountDao = accountDao;
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

    public List<GroceryItem> findByCoOwner(String owner) {
        return groceryDao.findByCoOwner(owner);
    }

    public List<GroceryItem> findByOwner(String owner) {
        return groceryDao.findByOwner(owner);
    }

    public SynchronizationResponse synchronize(String userId, String deviceId, SynchronizationRequest request) {

        List<GroceryItem> items = findByCoOwner(userId);

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
            String itemName = item.getItemName();
            if (!request.getSubSums().containsKey(itemName)) {
                result.getProductsToAdd().add(itemName);
            } else {
                ShopNameDto shopName = request.getShopNames().get(itemName);
                if (shopName != null && shopName.getTimestamp() > item.getTimestamp()) {
                    item.setShopName(shopName.getName());
                    item.setTimestamp(shopName.getTimestamp());
                }
                int newSubSum = request.getSubSums().get(itemName);
                item.getSubSums().put(deviceId, newSubSum);
                update(item);
            }
            Integer totalAmount = item.getSubSums().values().stream().reduce(0, Integer::sum);
            result.getTotalAmounts().put(itemName, totalAmount);
            result.getShopNames().put(itemName, new ShopNameDto(item.getShopName(), item.getTimestamp()));
            if (!item.getOwner().equals(userId)) {
                result.getSharedProducts().add(itemName);
            }
        }

        for (String productName : request.getSubSums().keySet()) {
            if (items.stream().noneMatch(item -> item.getItemName().equals(productName))) {
                result.getProductsToRemove().add(productName);
            }
        }

        return result;
    }

    public boolean share(String userId, String itemName, String coOwnerMail) {
        List<GroceryItem> items = findByOwner(userId);

        Account account = accountDao.findByMail(coOwnerMail);
        if (account == null) {
            return false;
        }
        System.out.println("Found: " + account);
        String coOwnerName = account.getGoogleId();

        items.stream()
                .filter(i -> i.getItemName().equals(itemName))
                .filter(i -> !i.getCoOwners().contains(coOwnerName))
                .forEach(i -> {
                    i.getCoOwners().add(coOwnerName);
                    System.out.println("Persisting: " + i);
                    update(i);
                });
        return true;
    }
}
