package com.example;

import com.example.model.joined.Developpeur;
import com.example.model.joined.Employe;
import com.example.model.joined.Manager;
import com.example.util.JpaUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Verifie la strategie {@code JOINED} : une table par classe, reliees par cle
 * etrangere, avec les attributs communs stockes dans la table racine.
 */
public class JoinedInheritanceTest {

    @Before
    public void setUp() {
        TestSupport.cleanDatabase();
    }

    @AfterClass
    public static void tearDown() {
        JpaUtil.close();
    }

    @Test
    public void chaqueClasseDeLaHierarchiePossedeSaTable() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            assertTrue(TestSupport.tableExists(em, "Employe"));
            assertTrue(TestSupport.tableExists(em, "Developpeur"));
            assertTrue(TestSupport.tableExists(em, "Manager"));
        } finally {
            em.close();
        }
    }

    @Test
    public void lesAttributsCommunsSontStockesDansLaTableRacine() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            tx.commit();

            assertEquals(2L, TestSupport.count(em, "SELECT COUNT(*) FROM Employe"));
            // Le nom n'est present que dans la table racine, pas dans les sous-tables.
            assertEquals(2L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Employe WHERE nom IN ('Alice', 'Bob')"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void lesAttributsSpecifiquesSontStockesDansLesSousTables() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            tx.commit();

            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Developpeur WHERE langage = 'Java'"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Manager WHERE nombre_equipes = 3"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void lesSousTablesSontRelieesALaTableRacineParCleEtrangere() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Developpeur("Alice", "Java"));
            tx.commit();

            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Developpeur d JOIN Employe e ON d.id = e.id"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Developpeur d JOIN Employe e ON d.id = e.id "
                            + "WHERE e.nom = 'Alice'"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void aucunIdentifiantNEstDupliqueEntreDeuxSousTables() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            tx.commit();

            // Chaque sous-table ne porte que sa propre ligne.
            assertEquals(1L, TestSupport.count(em, "SELECT COUNT(*) FROM Developpeur"));
            assertEquals(1L, TestSupport.count(em, "SELECT COUNT(*) FROM Manager"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void laRequetePolymorphiqueRestaureLesTypesConcrets() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            tx.commit();
            em.clear();

            List<Employe> resultat = em
                    .createQuery("SELECT e FROM Employe e ORDER BY e.id", Employe.class)
                    .getResultList();

            assertEquals(2, resultat.size());
            assertTrue(resultat.get(0) instanceof Developpeur);
            assertTrue(resultat.get(1) instanceof Manager);

            Developpeur dev = (Developpeur) resultat.get(0);
            assertEquals("Alice", dev.getNom());
            assertEquals("Java", dev.getLangage());

            Manager manager = (Manager) resultat.get(1);
            assertEquals("Bob", manager.getNom());
            assertEquals(3, manager.getNombreEquipes());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void laTableRacineNestPasUneTableConcrete() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            // Employe est abstraite : aucune ligne ne doit pouvoir y etre inseree seule.
            assertFalse("La table Employe ne doit pas contenir de colonnes propres aux sous-types",
                    TestSupport.tableExists(em, "employe_langage"));
        } finally {
            em.close();
        }
    }
}
