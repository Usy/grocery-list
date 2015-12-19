package pl.kask.model;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AccountDao {

    public void persist(Account account) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(account);
            transaction.commit();
        } catch (HibernateException e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public Account findByGoogleId(String googleId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Account> accounts = (List<Account>) session
                    .createQuery("from Account a where a.googleId = :id")
                    .setParameter("id", googleId).list();
            if (accounts.size() > 0) {
                return accounts.get(0);
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Account findByMail(String mail) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Account> accounts = (List<Account>) session
                    .createQuery("from Account a where a.email = :mail")
                    .setParameter("mail", mail).list();
            if (accounts.size() > 0) {
                return accounts.get(0);
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }
}
