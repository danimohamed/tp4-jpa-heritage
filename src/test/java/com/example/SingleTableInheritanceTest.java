package com.example;

import com.example.model.singletable.Moto;
import com.example.model.singletable.Vehicule;
import com.example.model.singletable.Voiture;
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
 * Verifie la strategie {@code SINGLE_TABLE} : une seule table pour toute la
 * hierarchie, differenciee par une colonne discriminante.
 */
public class SingleTableInheritanceTest {

    @Before
    public void setUp() {
        TestSupport.cleanDatabase();
    }

    @AfterClass
    public static void tearDown() {
        JpaUtil.close();
    }

    @Test
    public void uneSeuleTableContientTouteLaHierarchie() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            assertTrue("La table Vehicule doit exister",
                    TestSupport.tableExists(em, "Vehicule"));
            assertFalse("Aucune table dediee a Voiture en SINGLE_TABLE",
                    TestSupport.tableExists(em, "Voiture"));
            assertFalse("Aucune table dediee a Moto en SINGLE_TABLE",
                    TestSupport.tableExists(em, "Moto"));
        } finally {
            em.close();
        }
    }

    @Test
    public void lesDeuxSousTypesPartagentLaMemeTable() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            tx.commit();

            assertEquals(2L, TestSupport.count(em, "SELECT COUNT(*) FROM Vehicule"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void laColonneDiscriminanteDistingueLesSousTypes() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            tx.commit();

            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Vehicule WHERE vehicule_type = 'VOITURE'"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Vehicule WHERE vehicule_type = 'MOTO'"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void lesColonnesSpecifiquesRestentNullesPourLesAutresSousTypes() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            tx.commit();

            // La voiture n'a pas de sidecar : la colonne est nulle.
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Vehicule WHERE sidecar IS NULL"));
            assertEquals(1L, TestSupport.count(em,
                    "SELECT COUNT(*) FROM Vehicule WHERE nombre_portes = 4"));
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
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            tx.commit();
            em.clear();

            List<Vehicule> resultat = em
                    .createQuery("SELECT v FROM Vehicule v ORDER BY v.id", Vehicule.class)
                    .getResultList();

            assertEquals(2, resultat.size());
            assertTrue("Premier resultat attendu : Voiture",
                    resultat.get(0) instanceof Voiture);
            assertTrue("Second resultat attendu : Moto",
                    resultat.get(1) instanceof Moto);

            Voiture voiture = (Voiture) resultat.get(0);
            assertEquals("Toyota", voiture.getMarque());
            assertEquals(4, voiture.getNombrePortes());

            Moto moto = (Moto) resultat.get(1);
            assertEquals("Harley", moto.getMarque());
            assertTrue(moto.isSidecar());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void uneSousClassePeutEtreInterrogeeSeule() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            tx.commit();
            em.clear();

            List<Voiture> voitures = em
                    .createQuery("SELECT v FROM Voiture v", Voiture.class)
                    .getResultList();

            assertEquals(1, voitures.size());
            assertEquals("Toyota", voitures.get(0).getMarque());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
