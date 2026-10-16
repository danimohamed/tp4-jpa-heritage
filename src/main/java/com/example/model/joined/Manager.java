package com.example.model.joined;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Sous-classe JOINED : ses attributs specifiques vivent dans la table
 * {@code Manager}, dont la cle primaire est aussi cle etrangere vers
 * {@code Employe}.
 */
@Entity
@Table(name = "Manager")
@DiscriminatorValue("MANAGER")
@PrimaryKeyJoinColumn(name = "id")
public class Manager extends Employe {

    @Column(name = "nombre_equipes")
    private int nombreEquipes;

    public Manager() {
    }

    public Manager(String nom, int nombreEquipes) {
        super(nom);
        this.nombreEquipes = nombreEquipes;
    }

    public int getNombreEquipes() {
        return nombreEquipes;
    }

    public void setNombreEquipes(int nombreEquipes) {
        this.nombreEquipes = nombreEquipes;
    }

    @Override
    public String decrire() {
        return "Manager " + getNom() + " (" + nombreEquipes + " equipes)";
    }
}
