package com.example.model.joined;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Sous-classe JOINED : ses attributs specifiques vivent dans la table
 * {@code Developpeur}, dont la cle primaire est aussi cle etrangere vers
 * {@code Employe}.
 */
@Entity
@Table(name = "Developpeur")
@DiscriminatorValue("DEVELOPPEUR")
@PrimaryKeyJoinColumn(name = "id")
public class Developpeur extends Employe {

    @Column(name = "langage", length = 50)
    private String langage;

    public Developpeur() {
    }

    public Developpeur(String nom, String langage) {
        super(nom);
        this.langage = langage;
    }

    public String getLangage() {
        return langage;
    }

    public void setLangage(String langage) {
        this.langage = langage;
    }

    @Override
    public String decrire() {
        return "Developpeur " + getNom() + " (" + langage + ")";
    }
}
