package com.example.model.singletable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Racine de la hierarchie SINGLE_TABLE.
 *
 * <p>Toute la hierarchie (Vehicule, Voiture, Moto) est stockee dans une seule table
 * nommee {@code Vehicule}. Une colonne discriminante {@code vehicule_type} indique le
 * type concret de chaque ligne.</p>
 *
 * <ul>
 *   <li>Avantage : une seule table a interroger, pas de jointure.</li>
 *   <li>Inconvenient : les colonnes propres aux sous-classes restent NULL pour les autres.</li>
 * </ul>
 */
@Entity
@Table(name = "Vehicule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicule_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "marque", length = 100)
    private String marque;

    protected Vehicule() {
    }

    protected Vehicule(String marque) {
        this.marque = marque;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    /** Description du type concret, utilisee par la demo et les tests. */
    public abstract String decrire();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicule)) {
            return false;
        }
        Vehicule autre = (Vehicule) o;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", marque='" + marque + "'}";
    }
}
