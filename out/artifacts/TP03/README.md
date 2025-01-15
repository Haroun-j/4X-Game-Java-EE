# Projet TP03 - Servlets Java EE 🌐

_Poursuite des concepts fondamentaux des servlets Java EE : gestion des requêtes HTTP, génération de contenu dynamique, authentification, résolution d'équations et gestion de matrices._

---

## Table des Matières
- [Introduction](#introduction)
- [Exercice 1 : com.projet.controller.LoginServlet - Authentification](#exercice-1-loginservlet)
- [Exercice 2 : EquationServlet - Résolution d'Équations](#exercice-2-equationservlet)
- [Exercice 3 : MatriceServlet - Gestion de Matrice](#exercice-3-matriceservlet)
- [Structure du Projet](#structure-du-projet)
- [Instructions d'Exécution](#instructions-dexécution)
- [Auteur](#auteur)

---

## Introduction

Dans le cadre du TP03, nous avons continué notre exploration des servlets et JSP dans Java EE, en mettant en œuvre des fonctionnalités interactives telles que l'authentification utilisateur, la résolution d'équations du second degré et la gestion d'une matrice 10x10.

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/0_landing_page.png)

---
## Exercice 1 : com.projet.controller.LoginServlet - Authentification

### Objectif
La servlet `com.projet.controller.LoginServlet` gère l'authentification de base de l'utilisateur en vérifiant les identifiants fournis via la page `Login.jsp`. En cas de succès, l'utilisateur est redirigé vers une page de bienvenue, sinon un message d'erreur est affiché.

### Étapes Réalisées
1. **Création du Formulaire (Login.jsp)** :
   - Champs pour le nom d'utilisateur et le mot de passe.
   - Bouton de soumission pour envoyer les informations d'identification à `com.projet.controller.LoginServlet`.

2. **Validation dans la Servlet** :
   - Vérification des paramètres `username` et `password`.
   - Affichage de `Welcome to your First JSP page !` en cas de succès, ou `Identification Failure !` en cas d'échec.

3. **Affichage du Message** :
   - Utilisation des attributs de requête pour transmettre le message à `resultat.jsp`.
   
   `(Accès direct via ${Clé} au lieu de request.getAttribute(Clé) )`

#### Capture d’écran pour Login

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/1_login.png)

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/2_login_error.png)

Essaye avec `username=password='J2EE'`

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/3_login_success.png)

---

## Exercice 2 : EquationServlet - Résolution d'Équations

### Objectif
La servlet `EquationServlet` résout des équations du second degré. L'utilisateur entre les coefficients `a`, `b` et `c` et la servlet calcule le discriminant pour déterminer la solution.

### Étapes Réalisées
1. **Création du Formulaire (Formulaire.jsp)** :
   - Champs pour les coefficients `a`, `b` et `c`.
   - Bouton de soumission pour envoyer les coefficients à `EquationServlet`.

2. **Calcul dans la Servlet** :
   - Calcul du discriminant `d = b^2 - 4*a*c`.
   - Affichage des solutions sur `resultat.jsp` :
      - Rouge si pas de racines réelles.
      - Bleu s'il y a une seule racine réelle.
      - Vert s'il y a deux racines réelles.

3. **Affichage du Résultat** :
   - Affichage coloré du résultat dans `resultat.jsp` en fonction de la valeur du discriminant.

#### Capture d’écran pour Equation

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/4_equation_form.png)

`a = 1, b = -3, c = 2; Delta > 0`

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/5_result_two_solutions.png)

`a = 1, b = 2, c = 1 ; Delta = 0`

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/6_result_one_double_solution.png)

`a = 1, b = 3, c = 5; Delta < 0`

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/7_result_no_real_solution.png)

---

## Exercice 3 : MatriceServlet - Gestion de Matrice

### Objectif
La servlet `MatriceServlet` permet de gérer une matrice 10x10 avec quatre opérations principales : affichage de la matrice complète, consultation de la valeur d'une cellule, mise à jour d'une cellule et réinitialisation de la matrice.

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/8_matrice_form.png)

### Étapes Réalisées
1. **Affichage de la Matrice Complète (GET sans paramètres)** :
   - Lorsque la servlet est accédée par une requête GET sans paramètres, elle renvoie et affiche la matrice complète sous forme de table dans `Resultat.jsp`.
   - La matrice est envoyée sous forme de tableau HTML généré directement dans la servlet pour un affichage structuré dans `resultat.jsp`.

   ![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/9_result_matricice_full.png)

2. **Consultation de la Valeur d’une Cellule (GET avec x et y)** :
   - Si les paramètres `x` et `y` sont fournis, la servlet récupère et affiche la valeur de la cellule spécifiée.
   - Affiche un message d'erreur si les coordonnées sont hors limites.

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/10_result_matrice_x2_y3.png)
  
![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/11_result_matrice_hors_limit.png)

3. **Mise à Jour d’une Cellule (POST avec x, y et val)** :
   - Met à jour la cellule `(x, y)` avec une nouvelle valeur `val` reçue via une requête POST.
   - Affiche un message de confirmation ou un message d'erreur si les coordonnées sont incorrectes.
     
![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/13_result_matrice_replace.png)

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/13_result_matrice_replace_show.png)

4. **Réinitialisation de la Matrice (POST sans paramètres)** :
   - Réinitialise la matrice en mettant toutes les cellules à zéro lorsque la requête POST est envoyée sans paramètres.

![0__](https://github.com/Haroun-j/TP03/blob/master/screenshots/12_result_matrice_init.png)

---

## Structure du Projet

TP03/

├── src/

│   ├── com.projet.controller.LoginServlet.java       # Gère l'authentification

│   ├── EquationServlet.java    # Résout l'équation du second degré

│   ├── MatriceServlet.java     # Gère la matrice

├── WEB-INF/

│   ├── web.xml                 # Configuration des servlets

│   ├── Resultat.jsp           # Page de résultat pour afficher les messages et les tables

├── Login.jsp                   # Page de connexion HTML

├── Formulaire.jsp              # Formulaire pour les équations

├── matrice.jsp                

├── styles/

│   ├── login.css               # Styles pour le formulaire de connexion
│   │  
│   ├── matrice.css             # Styles pour la page de matrice
│   │  
│   └── resultat.css            # Styles pour les résultats et affichages dynamiques
   
    └── formulaire.css            # Styles pour les résultats et affichages dynamiques
      
    └── clock.css          

---

## Instructions d'Exécution

### Serveur et IDE utilisés
- Apache Tomcat 11.0
- IntelliJ IDEA

### Étapes pour Exécuter
1. **Déployer le Projet** : Copiez `TP03` dans le dossier `webapps` de Tomcat.
2. **Démarrer le Serveur** : Lancez Tomcat.
3. **Accéder aux Pages** :
   - **com.projet.controller.LoginServlet** : `http://localhost:8080/TP03/Login.jsp`
   - **EquationServlet** : `http://localhost:8080/TP03/Formulaire.jsp`
   - **MatriceServlet** : `http://localhost:8080/TP03/Matrice.jsp`
---

## Auteur

- [@HarounJoudi](https://github.com/Haroun-j)

---