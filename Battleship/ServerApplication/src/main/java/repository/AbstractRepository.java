package repository;

import jakarta.persistence.EntityManager;

import java.util.List;

import entityManager.DatabaseEntity;

public abstract class AbstractRepository<T> {
    private Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void create(T entity) {
        EntityManager em = DatabaseEntity.getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
    }

    public T findById(Integer id) {
        EntityManager em = DatabaseEntity.getEntityManager();
        return em.find(entityClass, id);
    }

    public List<T> findByName(String name) {
        EntityManager em = DatabaseEntity.getEntityManager();
        String query = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.name LIKE :name";
        return em.createQuery(query, entityClass)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    public List<T> findAll() {
        EntityManager em = DatabaseEntity.getEntityManager();
        String query = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(query, entityClass)
                .getResultList();
    }

    public void update(T entity) {
        EntityManager em = DatabaseEntity.getEntityManager();
        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
    }


    public void deleteById(Integer id) {
        EntityManager em = DatabaseEntity.getEntityManager();
        T entity = em.find(entityClass, id);
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

    public void deleteByName(String name) {
        EntityManager em = DatabaseEntity.getEntityManager();
        List<T> entities = findByName(name);
        em.getTransaction().begin();
        for (T entity : entities) {
            em.remove(entity);
        }
        em.getTransaction().commit();
    }

}

