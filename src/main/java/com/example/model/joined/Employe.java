package com.example.model.joined;

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
 * Racine de la hierarchie JOINED.
 *
 * <p>Chaque classe de la hierarchie possede sa propre table. La table de la classe
 * parente {@code Employe} contient les attributs communs, et chaque table de
 * sous-classe ne contient que ses attributs specifiques plus une cle etrangere
 * (et cle primaire) vers {@code Employe}.</p>
 *
 * <ul>
 *   <li>Avantage : modele normalise, aucune colonne nulle inutile.</li>
 *   <li>Inconvenient : les requetes polymorphiques necessitent des jointures.</li>
 * </ul>
 */
@Entity
@Table(name = "Employe")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "employe_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    protected Employe() {
    }

    protected Employe(String nom) {
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
        if (!(o instanceof Employe)) {
            return false;
        }
        Employe autre = (Employe) o;
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
