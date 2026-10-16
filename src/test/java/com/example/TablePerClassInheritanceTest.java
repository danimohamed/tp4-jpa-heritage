package com.example;

import com.example.model.tableperclass.Electronique;
import com.example.model.tableperclass.Livre;
import com.example.model.tableperclass.Produit;
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
 * Verifie la strategie {@code TABLE_PER_CLASS} : chaque classe concrete possede sa
 * propre table complete et la classe racine abstraite n'a aucune table.
 */
public class TablePerClassInheritanceTest {

    @Before
    public void setUp() {
        TestSupport.cleanDatabase();
    }

    @AfterClass
    public static void tearDown() {
        JpaUtil.close();
    }

    @Test
    public void chaqueClasseConcretePossedeSaTable() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            assertTrue(TestSupport.tableExists(em, "Livre"));
            assertTrue(TestSupport.tableExists(em, "Electronique"));
        } finally {
            em.close();
        }
    }

    @Test
    public void laClasseRacineAbstraiteNaAucuneTable() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            assertFalse("Produit est abstraite : aucune table ne doit etre creee",
                    TestSupport.tableExists(em, "Produit"));
        } finally {
            em.close();
        }
    }

    @Test
    public void lesColonnesHeriteesSontDupliqueesDansChaqueTable() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Livre("Clean Code", "Robert C. Martin"));
            em.persist(new Electronique("Smartphone", 24));
            tx.commit();

            // Chaque table concrete porte bien sa propre colonne nom.
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Livre WHERE nom = 'Clean Code'"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Electronique WHERE nom = 'Smartphone'"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void lesAttributsSpecifiquesRestentDansLeurTable() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Livre("Clean Code", "Robert C. Martin"));
            em.persist(new Electronique("Smartphone", 24));
            tx.commit();

            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Livre WHERE auteur = 'Robert C. Martin'"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Electronique WHERE garantie = 24"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void lesIdentifiantsProviennentDUnGenerateurPartage() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Livre livre = new Livre("Clean Code", "Robert C. Martin");
            Electronique electronique = new Electronique("Smartphone", 24);
            em.persist(livre);
            em.persist(electronique);
            tx.commit();

            // Un generateur unique pour toute la hierarchie : pas de collision d'identifiants.
            assertTrue(livre.getId() != null && electronique.getId() != null);
            assertFalse("Les identifiants doivent etre distincts",
                    livre.getId().equals(electronique.getId()));
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
            em.persist(new Livre("Clean Code", "Robert C. Martin"));
            em.persist(new Electronique("Smartphone", 24));
            tx.commit();
            em.clear();

            // Hibernate emule le polymorphisme par un UNION ALL des tables concretes.
            List<Produit> resultat = em
                    .createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                    .getResultList();

            assertEquals(2, resultat.size());
            assertTrue(resultat.get(0) instanceof Livre);
            assertTrue(resultat.get(1) instanceof Electronique);

            Livre livre = (Livre) resultat.get(0);
            assertEquals("Clean Code", livre.getNom());
            assertEquals("Robert C. Martin", livre.getAuteur());

            Electronique electronique = (Electronique) resultat.get(1);
            assertEquals("Smartphone", electronique.getNom());
            assertEquals(24, electronique.getGarantie());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
