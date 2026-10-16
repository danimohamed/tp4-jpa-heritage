package com.example;

import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

/**
 * Utilitaires partages par les tests : nettoyage des tables entre deux tests et
 * introspection du schema genere par Hibernate.
 */
final class TestSupport {

    private TestSupport() {
    }

    /**
     * Vide toutes les tables de la hierarchie. La base etant en memoire avec
     * {@code DB_CLOSE_DELAY=-1}, le schema survit d'un test a l'autre : on supprime
     * donc les lignes pour que chaque test demarre d'un etat connu.
     */
    static void cleanDatabase() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Les sous-tables JOINED d'abord (contraintes de cle etrangere).
            em.createNativeQuery("DELETE FROM Developpeur").executeUpdate();
            em.createNativeQuery("DELETE FROM Manager").executeUpdate();
            em.createNativeQuery("DELETE FROM Employe").executeUpdate();
            em.createNativeQuery("DELETE FROM Vehicule").executeUpdate();
            em.createNativeQuery("DELETE FROM Livre").executeUpdate();
            em.createNativeQuery("DELETE FROM Electronique").executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Execute une requete de comptage et retourne le resultat sous forme de {@code long}. */
    static long count(EntityManager em, String sql) {
        Object result = em.createNativeQuery(sql).getSingleResult();
        return ((Number) result).longValue();
    }

    /** Liste les tables du schema {@code PUBLIC} dont le nom figure dans {@code noms}. */
    static List<String> existingTables(EntityManager em, String... noms) {
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < noms.length; i++) {
            if (i > 0) {
                in.append(", ");
            }
            in.append("'").append(noms[i].toUpperCase()).append("'");
        }
        @SuppressWarnings("unchecked")
        List<String> tables = em.createNativeQuery(
                        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME IN (" + in + ") "
                                + "ORDER BY TABLE_NAME")
                .getResultList();
        return tables;
    }

    /** {@code true} si la table existe dans le schema {@code PUBLIC}. */
    static boolean tableExists(EntityManager em, String nom) {
        return !existingTables(em, nom).isEmpty();
    }
}
