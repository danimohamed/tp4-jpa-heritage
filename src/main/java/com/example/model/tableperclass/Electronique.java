package com.example.model.tableperclass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Sous-classe TABLE_PER_CLASS : possede sa propre table {@code Electronique}, qui
 * contient les colonnes heritees ({@code id}, {@code nom}) en plus de
 * {@code garantie}.
 */
@Entity
@Table(name = "Electronique")
public class Electronique extends Produit {

    /** Duree de garantie en mois. */
    @Column(name = "garantie")
    private int garantie;

    public Electronique() {
    }

    public Electronique(String nom, int garantie) {
        super(nom);
        this.garantie = garantie;
    }

    public int getGarantie() {
        return garantie;
    }

    public void setGarantie(int garantie) {
        this.garantie = garantie;
    }

    @Override
    public String decrire() {
        return "Electronique " + getNom() + " (garantie " + garantie + " mois)";
    }
}
