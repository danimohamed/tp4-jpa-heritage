# TP 4 — Héritage avec Hibernate : stratégies de mapping

Projet Maven autonome illustrant les **trois stratégies d'héritage JPA/Hibernate** :
`SINGLE_TABLE`, `JOINED` et `TABLE_PER_CLASS`. Le projet persiste deux entités par
hiérarchie, puis interroge chaque racine avec une requête JPQL polymorphique afin de
constater que Hibernate restitue bien les types concrets et que le schéma diffère
radicalement d'une stratégie à l'autre.

Base de données **H2 en mémoire** — aucune installation externe n'est requise.

---

## Prérequis

| Outil | Version testée |
|-------|----------------|
| JDK   | 21 (le code cible Java 11) |
| Maven | 3.9.9 |

---

## Démarrage rapide

```bash
# Compiler et lancer les tests
mvn test

# Construire le jar
mvn package

# Exécuter la démonstration console
mvn clean compile exec:java -Dexec.mainClass="com.example.App"
```

Le jar produit est `target/hibernate-inheritance-1.0.0.jar`.

---

## Structure du projet

```
tp4-jpa-heritage/
├── pom.xml
└── src
    ├── main
    │   ├── java/com/example
    │   │   ├── App.java                              # démonstration console (3 hiérarchies)
    │   │   ├── model
    │   │   │   ├── singletable
    │   │   │   │   ├── Vehicule.java                 # @Inheritance(SINGLE_TABLE) + @DiscriminatorColumn
    │   │   │   │   ├── Voiture.java                  # @DiscriminatorValue("VOITURE")
    │   │   │   │   └── Moto.java                     # @DiscriminatorValue("MOTO")
    │   │   │   ├── joined
    │   │   │   │   ├── Employe.java                  # @Inheritance(JOINED) + @DiscriminatorColumn
    │   │   │   │   ├── Developpeur.java              # @PrimaryKeyJoinColumn
    │   │   │   │   └── Manager.java                  # @PrimaryKeyJoinColumn
    │   │   │   └── tableperclass
    │   │   │       ├── Produit.java                  # @Inheritance(TABLE_PER_CLASS) + @SequenceGenerator
    │   │   │       ├── Livre.java
    │   │   │       └── Electronique.java
    │   │   └── util
    │   │       └── JpaUtil.java                      # EntityManagerFactory singleton
    │   └── resources/META-INF
    │       └── persistence.xml                       # unité de persistance "hibernate-inheritance"
    └── test/java/com/example
        ├── TestSupport.java                          # introspection de schéma + remise à zéro
        ├── SingleTableInheritanceTest.java           # 6 tests
        ├── JoinedInheritanceTest.java                # 7 tests
        ├── TablePerClassInheritanceTest.java         # 6 tests
        └── AppDemoTest.java                          # 3 tests
```

---

## Dépendances (`pom.xml`)

| Dépendance | Version | Portée | Rôle |
|------------|---------|--------|------|
| `javax.persistence-api` | 2.2 | compile | API JPA (annotations) |
| `hibernate-core` | 5.6.15.Final | compile | Implémentation JPA |
| `com.h2database:h2` | 2.1.214 | runtime | Base en mémoire |
| `junit:junit` | 4.13.2 | test | Tests unitaires |
| `net.bytebuddy:byte-buddy` | 1.14.18 | compile | **Correctif JDK 21** (voir ci-dessous) |

> **Note sur les versions.** L'énoncé prescrit `hibernate-core 5.4.30.Final` et `h2 1.4.200`.
> Ces versions embarquent une version de Byte Buddy incapable de lire les fichiers de classe
> du JDK 21 (`Unsupported class file major version 65`). Hibernate est donc passé en
> `5.6.15.Final`, H2 en `2.1.214`, et `byte-buddy 1.14.18` est épinglé explicitement.
> Toutes les annotations, les packages `com.example.model.*`, le nom de l'unité de persistance
> `hibernate-inheritance` et l'URL JDBC `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` restent
> conformes à l'énoncé.

---

## Configuration JPA (`META-INF/persistence.xml`)

```xml
<persistence-unit name="hibernate-inheritance" transaction-type="RESOURCE_LOCAL">
  <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

  <class>com.example.model.singletable.Vehicule</class>
  <class>com.example.model.singletable.Voiture</class>
  <class>com.example.model.singletable.Moto</class>

  <class>com.example.model.joined.Employe</class>
  <class>com.example.model.joined.Developpeur</class>
  <class>com.example.model.joined.Manager</class>

  <class>com.example.model.tableperclass.Produit</class>
  <class>com.example.model.tableperclass.Livre</class>
  <class>com.example.model.tableperclass.Electronique</class>

  <properties>
    <property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"/>
    <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
    <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
    <property name="hibernate.show_sql" value="true"/>
    <property name="hibernate.format_sql" value="true"/>
  </properties>
</persistence-unit>
```

Les neuf entités sont déclarées **explicitement** : Hibernate ne balaye pas le classpath
lorsqu'une unité de persistance est déclarée en XML.

---

## Points clés du TP

### 1. Les trois stratégies en une table

| Stratégie | Schéma généré | Discriminant | Requête polymorphique |
|-----------|---------------|--------------|-----------------------|
| `SINGLE_TABLE` | **1** table `Vehicule` pour les 3 classes | colonne `vehicule_type` (`VOITURE` / `MOTO`) | `SELECT` simple, pas de jointure |
| `JOINED` | **3** tables `Employe`, `Developpeur`, `Manager` | colonne `employe_type` + clés étrangères | `LEFT JOIN` sur les sous-tables |
| `TABLE_PER_CLASS` | **2** tables `Livre`, `Electronique` (pas de table racine) | aucun | `UNION ALL` des tables concrètes |

### 2. SINGLE_TABLE

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicule_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Vehicule { ... }

@Entity
@DiscriminatorValue("VOITURE")
public class Voiture extends Vehicule { private int nombrePortes; }
```

Une seule table contient donc `nombrePortes` **et** `sidecar`. Chaque ligne laisse nulles les
colonnes des sous-types auxquels elle n'appartient pas — c'est le prix de la lecture la plus
rapide. Avantage : performances optimales et schéma minimal. Inconvénient : colonnes nulles et
table qui s'élargit à chaque nouveau sous-type.

### 3. JOINED

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "employe_type")
public abstract class Employe { ... }

@Entity
@DiscriminatorValue("DEVELOPPEUR")
@PrimaryKeyJoinColumn(name = "id")
public class Developpeur extends Employe { private String langage; }
```

`nom` (commun) vit dans `Employe`, `langage` dans `Developpeur`, `nombreEquipes` dans `Manager` ;
les sous-tables partagent l'identifiant de la racine. Avantage : aucune colonne nulle, modèle
normalisé. Inconvénient : chaque lecture polymorphique coûte des jointures.

### 4. TABLE_PER_CLASS

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SequenceGenerator(name = "produit_seq", sequenceName = "produit_seq", allocationSize = 1)
public abstract class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "produit_seq")
    private Long id;
    ...
}
```

Chaque table concrète duplique `id` et `nom` ; `Produit` n'existe pas en base. Avantage :
aucune jointure. Inconvénient : redondance des colonnes et requêtes polymorphiques coûteuses.

> **Décision d'implémentation.** L'énoncé prescrit `GenerationType.AUTO` sur `Produit`.
> Avec cette stratégie, Hibernate **interdit** `IDENTITY` (un même identifiant peut apparaître
> dans plusieurs tables, il doit donc provenir d'une source partagée) et `AUTO` retombe
> précisément sur `IDENTITY` avec `H2Dialect`. Un générateur de séquence partagé
> (`produit_seq`, `allocationSize = 1`) est donc utilisé : les identifiants restent uniques
> entre `Livre` et `Electronique`, ce que vérifie
> `TablePerClassInheritanceTest.lesIdentifiantsProviennentDUnGenerateurPartage`.

### 5. Polymorphisme : la racine suffit

```java
// Une seule requête sur la classe racine, trois stratégies
em.createQuery("SELECT v FROM Vehicule v", Vehicule.class).getResultList();
em.createQuery("SELECT e FROM Employe e",  Employe.class).getResultList();
em.createQuery("SELECT p FROM Produit p",  Produit.class).getResultList();
```

`getClass().getSimpleName()` sur les objets retournés affiche bien `Voiture`, `Moto`,
`Developpeur`, `Manager`, `Livre`, `Electronique` : le type concret est restauré par
Hibernate, jamais par la requête.

### 6. Requêtes SQL observées

| Stratégie | SQL de `SELECT` polymorphique |
|-----------|------------------------------|
| SINGLE_TABLE | `select ... from Vehicule where vehicule_type in ('VOITURE','MOTO')` |
| JOINED | `from Employe left outer join Developpeur ... left outer join Manager ...` |
| TABLE_PER_CLASS | `from Livre ... union all from Electronique ...` |

### 7. Bonnes pratiques retenues

1. **Éviter `TABLE_PER_CLASS`** dès que des requêtes polymorphiques sont fréquentes : le
   `UNION ALL` dégrade les performances et rend le tri/pagination coûteux.
2. **Privilégier `SINGLE_TABLE`** pour les hiérarchies simples avec peu d'attributs
   spécifiques, et **indexer la colonne discriminante**.
3. **Utiliser `JOINED`** quand la normalisation prime et que la base est volumineuse.
4. **Tester chaque mapping** en analysant le SQL généré : c'est la seule façon de vérifier
   qu'on a bien obtenu le schéma attendu.
5. **Anticiper les migrations** : l'ajout d'un sous-type en `JOINED` crée une table, en
   `SINGLE_TABLE` une colonne. `create-drop` est réservé aux tests.

---

## Tests

```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
```

Les tests **introspectent le schéma réel** via `INFORMATION_SCHEMA.TABLES` et comptent les
lignes en SQL natif : ils valident donc le mapping produit par Hibernate, pas seulement le
comportement Java.

**`SingleTableInheritanceTest` (6 tests)**

| Test | Vérifie |
|------|---------|
| `uneSeuleTableContientTouteLaHierarchie` | une seule table `Vehicule` existe pour les 3 classes |
| `lesDeuxSousTypesPartagentLaMemeTable` | la ligne d'une `Voiture` et celle d'une `Moto` sont dans `Vehicule` |
| `laColonneDiscriminanteDistingueLesSousTypes` | `vehicule_type` vaut `VOITURE` / `MOTO` |
| `lesColonnesSpecifiquesRestentNullesPourLesAutresSousTypes` | `nombrePortes` est `NULL` sur la ligne `MOTO` et inversement |
| `laRequetePolymorphiqueRestaureLesTypesConcrets` | `SELECT v FROM Vehicule v` rend des `Voiture` et des `Moto` |
| `uneSousClassePeutEtreInterrogeeSeule` | `SELECT v FROM Voiture v` filtre sur le discriminant |

**`JoinedInheritanceTest` (7 tests)**

| Test | Vérifie |
|------|---------|
| `chaqueClasseDeLaHierarchiePossedeSaTable` | `Employe`, `Developpeur` et `Manager` existent |
| `lesAttributsCommunsSontStockesDansLaTableRacine` | `nom` n'apparaît que dans `Employe` |
| `lesAttributsSpecifiquesSontStockesDansLesSousTables` | `langage` dans `Developpeur`, `nombreEquipes` dans `Manager` |
| `lesSousTablesSontRelieesALaTableRacineParCleEtrangere` | contraintes FK de `Developpeur` / `Manager` vers `Employe` |
| `aucunIdentifiantNEstDupliqueEntreDeuxSousTables` | l'identifiant d'un `Manager` n'existe pas dans `Developpeur` |
| `laRequetePolymorphiqueRestaureLesTypesConcrets` | la jointure rend les bons types concrets |
| `laTableRacineNestPasUneTableConcrete` | la racine abstraite ne contient aucune ligne |

**`TablePerClassInheritanceTest` (6 tests)**

| Test | Vérifie |
|------|---------|
| `chaqueClasseConcretePossedeSaTable` | `Livre` et `Electronique` existent |
| `laClasseRacineAbstraiteNaAucuneTable` | la table `Produit` n'existe pas |
| `lesColonnesHeriteesSontDupliqueesDansChaqueTable` | `id` et `nom` présents dans les deux tables |
| `lesAttributsSpecifiquesRestentDansLeurTable` | `auteur` seulement dans `Livre`, `garantie` seulement dans `Electronique` |
| `lesIdentifiantsProviennentDUnGenerateurPartage` | aucun identifiant commun entre les deux tables |
| `laRequetePolymorphiqueRestaureLesTypesConcrets` | `SELECT p FROM Produit p` rend `Livre` et `Electronique` |

**`AppDemoTest` (3 tests)**

| Test | Vérifie |
|------|---------|
| `lesSixEntitesSontPersisteesDansLeursTroisHierarchies` | les 6 entités de démonstration sont en base |
| `chaqueRequetePolymorphiqueRetourneLesTypesConcretsAttendus` | chaque racine rend ses 2 sous-types |
| `laSortieDeLaDemonstrationContientLesSixLignesAttendues` | la sortie console contient exactement les 6 lignes attendues |

`TestSupport.cleanDatabase()` vide les tables avant chaque test dans l'ordre
`Developpeur`, `Manager`, `Employe`, `Vehicule`, `Livre`, `Electronique` : les sous-tables
d'abord, sinon les contraintes de clé étrangère bloquent la remise à zéro.
`DB_CLOSE_DELAY=-1` maintenant la base H2 en mémoire vivante pendant toute la JVM, cette
remise à zéro est indispensable malgré `create-drop`.

---

## Résultat de la démonstration

```
--- SINGLE_TABLE : Vehicule ---
Voiture: Toyota
Moto: Harley
--- JOINED : Employe ---
Developpeur: Alice
Manager: Bob
--- TABLE_PER_CLASS : Produit ---
Livre: Clean Code
Electronique: Smartphone
```

---

## Auteur

Mohamed Dani — [@danimohamed](https://github.com/danimohamed)
