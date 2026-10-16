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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

/**
 * Demonstration des trois strategies d'heritage JPA/Hibernate.
 *
 * <p>Persiste deux entites par strategie puis interroge chaque hierarchie avec une
 * requete JPQL polymorphique (sur la classe racine), ce qui permet de constater que
 * Hibernate restitue bien les types concrets.</p>
 *
 * <p>Execution : {@code mvn clean compile exec:java -Dexec.mainClass="com.example.App"}</p>
 */
public class App {

    public static void main(String[] args) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            persisterDonnees(em);
            afficherHierarchies(em);
        } finally {
            em.close();
            JpaUtil.close();
        }
    }

    /** Persiste les six entites de demonstration dans une seule transaction. */
    private static void persisterDonnees(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Strategie SINGLE_TABLE
        em.persist(new Voiture("Toyota", 4));
        em.persist(new Moto("Harley", true));

        // Strategie JOINED
        em.persist(new Developpeur("Alice", "Java"));
        em.persist(new Manager("Bob", 3));

        // Strategie TABLE_PER_CLASS
        em.persist(new Livre("Clean Code", "Robert C. Martin"));
        em.persist(new Electronique("Smartphone", 24));

        tx.commit();
    }

    /** Interroge les trois hierarchies racines : le polymorphisme fait le reste. */
    private static void afficherHierarchies(EntityManager em) {
        System.out.println("--- SINGLE_TABLE : Vehicule ---");
        List<Vehicule> vehicules = em.createQuery("SELECT v FROM Vehicule v", Vehicule.class)
                .getResultList();
        for (Vehicule v : vehicules) {
            System.out.println(v.getClass().getSimpleName() + ": " + v.getMarque());
        }

        System.out.println("--- JOINED : Employe ---");
        List<Employe> employes = em.createQuery("SELECT e FROM Employe e", Employe.class)
                .getResultList();
        for (Employe e : employes) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getNom());
        }

        System.out.println("--- TABLE_PER_CLASS : Produit ---");
        List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class)
                .getResultList();
        for (Produit p : produits) {
            System.out.println(p.getClass().getSimpleName() + ": " + p.getNom());
        }
    }
}
