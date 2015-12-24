package pl.kask.model;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class GroceryDao {

    public void persist(GroceryItem groceryItem) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(groceryItem);
            transaction.commit();
        } catch (HibernateException e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void update(GroceryItem groceryItem) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(groceryItem);
            transaction.commit();
        } catch (HibernateException e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public GroceryItem findById(long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            GroceryItem groceryItem = (GroceryItem) session.get(GroceryItem.class, id);
            return groceryItem;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    public void delete(GroceryItem groceryItem) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.delete(groceryItem);
            transaction.commit();
        } catch (HibernateException e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<GroceryItem> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<GroceryItem> groceryItems = (List<GroceryItem>) session.createQuery("from GroceryItem").list();
            return groceryItems;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<GroceryItem> findByOwner(String owner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<GroceryItem> groceryItems = (List<GroceryItem>) session
                    .createQuery("from GroceryItem item where item.owner = :o")
                    .setParameter("o", owner).list();
            return groceryItems;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<GroceryItem> findByCoOwner(String coOwner) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<GroceryItem> groceryItems = (List<GroceryItem>) session
                    .createQuery("from GroceryItem item where item.owner = :o or :o member of item.coOwners")
                    .setParameter("o", coOwner).list();
            return groceryItems;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }
}
