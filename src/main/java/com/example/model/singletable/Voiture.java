package com.example.model.singletable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Sous-classe SINGLE_TABLE : elle est stockee dans la meme table que {@link Vehicule},
 * avec la valeur de discriminant {@code VOITURE} dans la colonne {@code vehicule_type}.
 */
@Entity
@DiscriminatorValue("VOITURE")
public class Voiture extends Vehicule {

    @Column(name = "nombre_portes")
    private int nombrePortes;

    public Voiture() {
    }

    public Voiture(String marque, int nombrePortes) {
        super(marque);
        this.nombrePortes = nombrePortes;
    }

    public int getNombrePortes() {
        return nombrePortes;
    }

    public void setNombrePortes(int nombrePortes) {
        this.nombrePortes = nombrePortes;
    }

    @Override
    public String decrire() {
        return "Voiture " + getMarque() + " (" + nombrePortes + " portes)";
    }
}
