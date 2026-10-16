package com.example.model.tableperclass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Racine de la hierarchie TABLE_PER_CLASS.
 *
 * <p>Chaque classe <strong>concrete</strong> possede sa propre table complete, qui
 * duplique les colonnes heritees. La classe abstraite {@code Produit} n'a
 * <strong>aucune</strong> table : il n'existe donc que {@code Livre} et
 * {@code Electronique}.</p>
 *
 * <ul>
 *   <li>Avantage : aucune jointure, tables totalement independantes.</li>
 *   <li>Inconvenient : colonnes dupliquees, requetes polymorphiques couteuses
 *       (UNION ALL de toutes les tables).</li>
 * </ul>
 *
 * <p>Remarque : avec TABLE_PER_CLASS, Hibernate interdit
 * {@code GenerationType.IDENTITY} (un meme identifiant doit pouvoir exister dans
 * plusieurs tables). On utilise donc un generateur partage.</p>
 */
@Entity
@Table(name = "Produit")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name = "produit_seq", sequenceName = "produit_seq", allocationSize = 1)
public abstract class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "produit_seq")
    private Long id;

    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    protected Produit() {
    }

    protected Produit(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    /** Description du type concret, utilisee par la demo et les tests. */
    public abstract String decrire();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produit)) {
            return false;
        }
        Produit autre = (Produit) o;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", nom='" + nom + "'}";
    }
}
