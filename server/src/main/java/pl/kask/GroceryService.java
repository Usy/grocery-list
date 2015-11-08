package pl.kask;

import pl.kask.model.GroceryDao;
import pl.kask.model.GroceryItem;

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
        groceryDao.openCurrentSessionWithTransaction();
        groceryDao.persist(groceryItem);
        groceryDao.closeCurrentSessionWithTransaction();
    }

    public void update(GroceryItem groceryItem) {
        groceryDao.openCurrentSessionWithTransaction();
        groceryDao.update(groceryItem);
        groceryDao.closeCurrentSessionWithTransaction();
    }

    public void delete(GroceryItem groceryItem) {
        groceryDao.openCurrentSessionWithTransaction();
        groceryDao.delete(groceryItem);
        groceryDao.closeCurrentSessionWithTransaction();
    }

    public GroceryItem findById(long id) {
        groceryDao.openCurrentSession();
        GroceryItem groceryItem = groceryDao.findById(id);
        groceryDao.closeCurrentSession();
        return groceryItem;
    }

    public List<GroceryItem> findAll() {
        groceryDao.openCurrentSession();
        List<GroceryItem> groceryItems = groceryDao.findAll();
        groceryDao.closeCurrentSession();
        return groceryItems;
    }

    public List<GroceryItem> findByOwner(String owner) {
        groceryDao.openCurrentSession();
        List<GroceryItem> groceryItems = groceryDao.findByOwner(owner);
        groceryDao.closeCurrentSession();
        return groceryItems;
    }
}
