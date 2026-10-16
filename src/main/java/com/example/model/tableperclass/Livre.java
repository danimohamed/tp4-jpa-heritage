package com.example.model.tableperclass;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Sous-classe TABLE_PER_CLASS : possede sa propre table {@code Livre}, qui contient
 * les colonnes heritees ({@code id}, {@code nom}) en plus de {@code auteur}.
 */
@Entity
@Table(name = "Livre")
public class Livre extends Produit {

    @Column(name = "auteur", length = 100)
    private String auteur;

    public Livre() {
    }

    public Livre(String nom, String auteur) {
        super(nom);
        this.auteur = auteur;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    @Override
    public String decrire() {
        return "Livre " + getNom() + " de " + auteur;
    }
}
