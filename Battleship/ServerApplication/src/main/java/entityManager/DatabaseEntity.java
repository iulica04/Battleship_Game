package entityManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseEntity {
    private static EntityManagerFactory entityManagerFactory = null;
    private static EntityManager entityManager = null;

    private DatabaseEntity() {

    }

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("BattleShipPU");
            entityManager = entityManagerFactory.createEntityManager();
        }
        return entityManager;
    }
}
