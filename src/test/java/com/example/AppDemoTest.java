package com.example;

import com.example.model.joined.Developpeur;
import com.example.model.joined.Employe;
import com.example.model.joined.Manager;
import com.example.model.singletable.Moto;
import com.example.model.singletable.Vehicule;
import com.example.model.singletable.Voiture;
import com.example.model.tableperclass.Electronique;
import com.example.model.tableperclass.Livre;
import com.example.model.tableperclass.Produit;
import com.example.util.JpaUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifie le scenario complet de la demonstration : les six entites sont persistees
 * puis restituees par les trois requetes polymorphiques, avec les types concrets
 * attendus.
 */
public class AppDemoTest {

    @Before
    public void setUp() {
        TestSupport.cleanDatabase();
    }

    @AfterClass
    public static void tearDown() {
        JpaUtil.close();
    }

    @Test
    public void lesSixEntitesSontPersisteesDansLeursTroisHierarchies() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            em.persist(new Livre("Clean Code", "Robert C. Martin"));
            em.persist(new Electronique("Smartphone", 24));
            tx.commit();
            em.clear();

            assertEquals(2L, TestSupport.count(em, "SELECT COUNT(*) FROM Vehicule"));
            assertEquals(2L, TestSupport.count(em, "SELECT COUNT(*) FROM Employe"));
            assertEquals(1L, TestSupport.count(em, "SELECT COUNT(*) FROM Livre"));
            assertEquals(1L, TestSupport.count(em, "SELECT COUNT(*) FROM Electronique"));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void chaqueRequetePolymorphiqueRetourneLesTypesConcretsAttendus() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Voiture("Toyota", 4));
            em.persist(new Moto("Harley", true));
            em.persist(new Developpeur("Alice", "Java"));
            em.persist(new Manager("Bob", 3));
            em.persist(new Livre("Clean Code", "Robert C. Martin"));
            em.persist(new Electronique("Smartphone", 24));
            tx.commit();
            em.clear();

            List<Vehicule> vehicules = em
                    .createQuery("SELECT v FROM Vehicule v ORDER BY v.id", Vehicule.class)
                    .getResultList();
            assertEquals("Voiture: Toyota", libelle(vehicules.get(0)));
            assertEquals("Moto: Harley", libelle(vehicules.get(1)));

            List<Employe> employes = em
                    .createQuery("SELECT e FROM Employe e ORDER BY e.id", Employe.class)
                    .getResultList();
            assertEquals("Developpeur: Alice", libelleEmploye(employes.get(0)));
            assertEquals("Manager: Bob", libelleEmploye(employes.get(1)));

            List<Produit> produits = em
                    .createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                    .getResultList();
            assertEquals("Livre: Clean Code", libelleProduit(produits.get(0)));
            assertEquals("Electronique: Smartphone", libelleProduit(produits.get(1)));
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Test
    public void laSortieDeLaDemonstrationContientLesSixLignesAttendues() {
        PrintStream sortieOrigine = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));
            App.main(new String[0]);
        } finally {
            System.setOut(sortieOrigine);
        }

        String sortie = new String(capture.toByteArray(), StandardCharsets.UTF_8);
        for (String ligne : new String[]{
                "Voiture: Toyota",
                "Moto: Harley",
                "Developpeur: Alice",
                "Manager: Bob",
                "Livre: Clean Code",
                "Electronique: Smartphone"}) {
            assertTrue("Ligne attendue absente de la sortie : " + ligne,
                    sortie.contains(ligne));
        }
    }

    private static String libelle(Vehicule v) {
        return v.getClass().getSimpleName() + ": " + v.getMarque();
    }

    private static String libelleEmploye(Employe e) {
        return e.getClass().getSimpleName() + ": " + e.getNom();
    }

    private static String libelleProduit(Produit p) {
        return p.getClass().getSimpleName() + ": " + p.getNom();
    }
}
