package com.example.model.singletable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Sous-classe SINGLE_TABLE : stockee dans la table {@code Vehicule} avec la valeur
 * de discriminant {@code MOTO}.
 */
@Entity
@DiscriminatorValue("MOTO")
public class Moto extends Vehicule {

    @Column(name = "sidecar")
    private boolean sidecar;

    public Moto() {
    }

    public Moto(String marque, boolean sidecar) {
        super(marque);
        this.sidecar = sidecar;
    }

    public boolean isSidecar() {
        return sidecar;
    }

    public void setSidecar(boolean sidecar) {
        this.sidecar = sidecar;
    }

    @Override
    public String decrire() {
        return "Moto " + getMarque() + (sidecar ? " avec sidecar" : " sans sidecar");
    }
}
