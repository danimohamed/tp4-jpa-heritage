package com.example.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Point d'acces unique a l'{@link EntityManagerFactory} de l'unite de persistance
 * {@code hibernate-inheritance}.
 *
 * <p>L'{@code EntityManagerFactory} est couteux a creer : on le construit une seule
 * fois (singleton paresseux, thread-safe) et on le reutilise pour toute la duree de
 * vie de l'application.</p>
 */
public final class JpaUtil {

    /** Nom de l'unite de persistance declaree dans {@code META-INF/persistence.xml}. */
    public static final String PERSISTENCE_UNIT = "hibernate-inheritance";

    private static volatile EntityManagerFactory entityManagerFactory;

    private JpaUtil() {
    }

    /**
     * Retourne l'{@code EntityManagerFactory} partage, en le creant au premier appel.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory local = entityManagerFactory;
        if (local == null) {
            synchronized (JpaUtil.class) {
                local = entityManagerFactory;
                if (local == null) {
                    local = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
                    entityManagerFactory = local;
                }
            }
        }
        return local;
    }

    /**
     * Cree un nouvel {@code EntityManager}. L'appelant reste responsable de sa
     * fermeture.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Ferme l'{@code EntityManagerFactory} et remet le singleton a zero, ce qui permet
     * a un appel ulterieur de le reconstruire (utile dans les tests JUnit).
     */
    public static synchronized void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }
}
