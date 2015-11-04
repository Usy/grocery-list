package pl.kask.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class GroceryDao {

    private Session currentSession;
    private Transaction currentTransaction;

    private static SessionFactory getSessionFactory() {
        Configuration configuration = new Configuration().configure();
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties());
        SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());
        return sessionFactory;
    }

    public Session openCurrentSession() {
        currentSession = getSessionFactory().openSession();
        return currentSession;
    }

    public Session openCurrentSessionWithTransaction() {
        currentSession = getSessionFactory().openSession();
        currentTransaction = currentSession.beginTransaction();
        return currentSession;
    }

    public void closeCurrentSession() {
        currentSession.close();
    }

    public void closeCurrentSessionWithTransaction() {
        currentTransaction.commit();
        currentSession.close();
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public Transaction getCurrentTransaction() {
        return currentTransaction;
    }

    public void setCurrentTransaction(Transaction currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    public void persist(GroceryItem groceryItem) {
        getCurrentSession().save(groceryItem);
    }

    public void update(GroceryItem groceryItem) {
        getCurrentSession().update(groceryItem);
    }

    public GroceryItem findById(long id) {
        GroceryItem groceryItem = (GroceryItem) getCurrentSession().get(GroceryItem.class, id);
        return groceryItem;
    }

    public void delete(GroceryItem groceryItem) {
        getCurrentSession().delete(groceryItem);
    }

    @SuppressWarnings("unchecked")
    public List<GroceryItem> findAll() {
        List<GroceryItem> groceryItems = (List<GroceryItem>) getCurrentSession().createQuery("from GroceryItem").list();
        return groceryItems;
    }

    @SuppressWarnings("unchecked")
    public List<GroceryItem> findByOwner(String owner) {
        List<GroceryItem> groceryItems = (List<GroceryItem>) getCurrentSession()
                .createQuery("from GroceryItem item where item.owner = :o")
                .setParameter("o", owner).list();
        return groceryItems;
    }
}
