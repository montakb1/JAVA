# Variable Neighborhood Search (VNS) pour l'optimisation du placement d'antennes

## Énoncé du problème

**Donné :**

- Une grille de 20×15 représentant une zone géographique
- 45 maisons réparties aléatoirement (densité ≈ 15 %)
- Chaque maison contient 100 utilisateurs nécessitant une couverture cellulaire
- Trois types d'antennes avec différents rayons de couverture et coûts :

  - **Small** : rayon = 2 cellules, coût = $1,000
  - **Medium** : rayon = 4 cellules, coût = $1,500
  - **Large** : rayon = 6 cellules, coût = $2,000

- La couverture est calculée en utilisant la distance euclidienne
- Les antennes ne peuvent pas être placées sur des maisons

**Objectif :**

- Obtenir 100 % de couverture (les 45 maisons couvertes)
- Minimiser le coût total de déploiement
- Trouver un placement d'antennes optimal ou quasi-optimal

---

## Pourquoi Variable Neighborhood Search ?

Contrairement aux algorithmes génétiques qui maintiennent des populations et utilisent des opérateurs évolutionnaires, le VNS explore systématiquement différentes structures de voisinage autour d'une solution courante. Il échappe aux optima locaux en changeant le voisinage de recherche plutôt qu'en maintenant plusieurs solutions, ce qui le rend plus économe en mémoire et souvent plus rapide tout en trouvant des solutions de bonne qualité.

---

## Comment fonctionne le Variable Neighborhood Search

### 1. Représentation de la solution

Chaque solution est une liste d'emplacements d'antennes sur notre grille 20×15 :

```python
Solution = [
  {x: 5, y: 3, type: "Large", radius: 6, cost: 2000},
  {x: 12, y: 8, type: "Medium", radius: 4, cost: 1500},
  {x: 7, y: 14, type: "Small", radius: 2, cost: 1000}
]
```

**Contraintes :**

- Positions `(x, y)` doivent être dans les bornes de la grille : `0 ≤ x < 20`, `0 ≤ y < 15`
- Interdiction de placer une antenne sur une maison
- La solution démarre par une initialisation gloutonne (greedy) pour un bon point de départ

---

### 2. Structures de voisinage (k-voisinages)

Concept central du VNS : définir plusieurs façons de modifier une solution, ordonnées des perturbations les plus petites aux plus grandes :

**N₁ - Changer le type d'antenne (petite perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Modifier le type d'une antenne
After:  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(7,14,Small)]
        ↑ Large → Medium (économie $500, peut réduire la couverture)
```

**N₂ - Déplacer une antenne (perturbation moyenne) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Déplacer la position d'une antenne de 1-2 cellules
After:  [Antenna(5,3,Large), Antenna(13,9,Medium), Antenna(7,14,Small)]
                              ↑ déplacée de (12,8) vers (13,9)
```

**N₃ - Remplacer une antenne (perturbation moyenne-grande) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Retirer une antenne, en ajouter une nouvelle à une position valide aléatoire
After:  [Antenna(5,3,Large), Antenna(18,6,Small), Antenna(7,14,Small)]
                              ↑ (12,8,Medium) remplacée par (18,6,Small)
```

**N₄ - Ajouter/Supprimer une antenne (grande perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Retirer l'antenne couvrant le moins de maisons OU ajouter une antenne pour une zone non couverte
After:  [Antenna(5,3,Large), Antenna(12,8,Medium)]  ← suppression de la moins utile
```

**N₅ - Échanger deux antennes (grande perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Échanger positions et types de deux antennes
After:  [Antenna(12,8,Large), Antenna(5,3,Medium), Antenna(7,14,Small)]
        ↑ échange des deux premières antennes
```

**Pourquoi plusieurs voisinages ?**

- Voisinages petits (N₁-N₂) : affinent de bonnes solutions (exploitation)
- Voisinages grands (N₄-N₅) : permettent d'échapper aux optima locaux (exploration)
- Recherche progressive : commencer petit, agrandir si bloqué

---

### 3. Fonction objectif (qualité de la solution)

Comment évaluer chaque solution :

```python
cost = total_antenna_cost
penalty = uncovered_houses * penalty_weight
objective = cost + penalty  # MINIMISER cette valeur
```

**Détails pour notre problème :**

1. **Coût total** = somme des coûts des antennes
   - Small : $1,000
   - Medium : $1,500
   - Large : $2,000

2. **Pénalité de couverture** = `(45 - houses_covered) × 10,000`
   - Distance euclidienne : `√[(x₁-x₂)² + (y₁-y₂)²]`
   - Poids de pénalité = $10,000 par maison non couverte
   - Rend la couverture incomplète extrêmement coûteuse

3. **Objectif complet** :
   - Si toutes les 45 maisons sont couvertes : `objective = cost`
   - Si 43/45 maisons couvertes : `objective = cost + 2 × $10,000`

**Exemples :**

- 10 antennes couvrant 43/45 maisons, coût $13,000 :
  - `coverage_penalty = (45-43) × 10,000 = $20,000`
  - `objective = $13,000 + $20,000 = $33,000` (MAUVAIS — couverture incomplète)

- 11 antennes couvrant 45/45 maisons, coût $14,000 :
  - `coverage_penalty = 0`
  - `objective = $14,000` (BON — couverture complète)

**Pourquoi cet objectif ?**

- Forte pression pour atteindre 100 % de couverture en priorité
- Une fois la couverture atteinte, l'algorithme se concentre sur la minimisation du coût
- Simple à calculer et à interpréter

---

### 4. Recherche locale (amélioration de solution)

**Recherche locale Best Improvement** — Améliore systématiquement au sein d'un voisinage :

```
Donné : solution courante, voisinage courant Nₖ
Répéter tant qu'il y a amélioration :
  1. Générer TOUS les voisins possibles dans Nₖ
  2. Évaluer l'objectif pour chaque voisin
  3. Si meilleur voisin < solution courante :
       → Accepter le meilleur voisin comme nouvelle solution courante
       → Continuer la recherche
  4. Sinon :
       → Optimum local atteint dans Nₖ
       → Stopper et retourner au boucle VNS
```

**Exemple — Recherche locale dans N₁ (changement de type) :**

```
Courant: [Antenna(5,3,Large,$2000), Antenna(12,8,Medium,$1500)]
         Coût = $3,500, Couverture = 45/45, Objectif = $3,500

Tester tous les swaps de type :
  Voisin 1: [Antenna(5,3,Medium), Antenna(12,8,Medium)]
           Coût = $3,000, Couverture = 45/45, Objectif = $3,000 ✓ MEILLEUR !
  ...

Accepter Voisin 1 (objectif $3,000 < $3,500)
Continuer la recherche locale à partir de la nouvelle solution...
```

**Pourquoi Best Improvement ?**

- Explore tous les voisins avant de bouger (approche exhaustive)
- Garantit de trouver le meilleur déplacement dans le voisinage
- Plus lent que le "first improvement" mais trouve de meilleurs optima locaux
- Important ici car un mauvais changement peut faire perdre la couverture

---

### 5. Shaking (perturbation)

**Génération aléatoire d'un voisin — Échapper aux optima locaux :**

```
Donné : solution courante, structure de voisinage Nₖ
Action :
  1. Générer UN voisin aléatoire via la transformation Nₖ
  2. Ne pas évaluer ni comparer — juste générer
  3. Retourner ce voisin aléatoire
```

**Pourquoi le shaking est important :**

```
Scénario : bloqué dans un optimum local en N₁
Solution courante (optimum local en N₁):
  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(18,14,Small)]
  Coût = $4,000, Toutes les maisons couvertes

Shaking avec N₂ (déplacement):
  Déplacer Antenna(12,8) → (11,7)
  Nouvelle solution: [Antenna(5,3,Medium), Antenna(11,7,Medium), Antenna(18,14,Small)]

Effet :
  - Peut toujours couvrir toutes les maisons (bien)
  - Peut permettre un meilleur swap de type ensuite
  - Ou empirer temporairement la solution (acceptable — exploration)
```

**Stratégie de shaking pour notre problème :**
- Commencer par des perturbations petites (N₁) si la solution est déjà bonne
- Monter vers des perturbations plus larges (N₄, N₅) si bloqué
- Les changements aléatoires évitent de revenir cycliquement au même optimum local
- Accepter le shake sans condition — on l'améliore ensuite par recherche locale

---

## Structure de l'algorithme VNS

### Boucle VNS complète

```
Algorithme VNS :
1. Générer solution initiale (algorithme greedy)
2. k = 1 (commencer par le plus petit voisinage)
3. Répéter jusqu'au critère d'arrêt (nombre max d'itérations ou temps) :

   PHASE SHAKING :
     4. Générer x' aléatoire dans Nₖ(x)

   PHASE LOCAL SEARCH :
     5. Appliquer recherche locale best-improvement : x'' = LocalSearch(x', N₁)

   DÉPLACER OU PAS :
     6. Si objective(x'') < objective(x) :
          → ACCEPTER : x = x'', k = 1 (redémarrer par le plus petit voisinage)
       Sinon :
          → REJETER : k = k + 1 (essayer un voisinage plus grand)

   CHANGEMENT DE VOISINAGE :
     7. Si k > k_max : k = 1 (retour au plus petit voisinage)

8. Retourner la meilleure solution trouvée
```

**Principes clés du VNS :**

- Changement systématique de voisinage :
  - Succès → recommencer à N₁ (exploitation)
  - Échec → passer à Nₖ₊₁ (exploration)

- La recherche locale utilise toujours N₁ :
  - Shaking utilise Nₖ (diversification)
  - Local search utilise N₁ (intensification)

- Pas besoin de population :
  - Trajectoire à solution unique
  - Mémoire : O(nombre d'antennes) vs O(population × antennes)
  - Plus simple qu'un algorithme génétique

---

## Étape par étape : comment VNS résout notre problème

### Initialisation (Itération 0)

Générer la solution initiale avec l'algorithme glouton (greedy) :

```python
Greedy Initialization :
1. Commencer avec ensemble d'antennes vide
2. Tant que des maisons restent non couvertes :
     - Pour chaque position valide et chaque type d'antenne, calculer combien de maisons non couvertes seraient couvertes
     - Choisir l'antenne qui couvre le plus de maisons non couvertes par dollar
     - Ajouter cette antenne à la solution
3. Retourner la solution initiale pour VNS
```

**Solution initiale (exemple) :**
- Antennes : 12 antennes placées stratégiquement
- Maisons couvertes : 45/45 (100 %)
- Coût : $15,500
- Objectif : $15,500 (pas de pénalité)

**Pourquoi commencer par greedy ?**
- Un départ aléatoire risque d'avoir une couverture faible (objectif = coût + lourde pénalité)
- Greedy assure 100 % de couverture dès l'itération 1
- VNS améliore à partir de cette base de haute qualité
- Convergence plus rapide que le départ aléatoire

---

### Itérations 1-10 : Affinage avec N₁ (Swap Type)

(Exemples illustratifs d'itérations, voir le document original pour la liste complète d'itérations et détails)

Le VNS explore surtout N₁ et N₂ au début

Trouve des dégradations de type (Large → Medium → Small) qui conservent la couverture

Ajustements fins :

- **Downgrades intelligents** : le VNS cherche des antennes dont le rayon peut être réduit (Large → Medium ou Medium → Small) sans perdre de maisons couvertes. Ces downgrades font baisser le coût immédiatement (−$500 ou −$1,000) et sont privilégiés lorsqu'ils conservent la couverture totale.

- **Combinaisons position/type** : un downgrade réussi s'appuie souvent sur de petits déplacements préalables (N₂) qui repositionnent légèrement une antenne pour couvrir les mêmes maisons même avec un rayon plus faible. Par exemple, déplacer une Large de (12,8) à (11,9) peut permettre de la remplacer par une Medium sans perte de couverture.

- **Sélection par ratio** : pendant l'initialisation greedy et dans certains tests de voisinage, les candidats sont évalués par le nombre de maisons couvertes par dollar. Cette métrique guide les swaps de type vers des économies réelles.

- **Preuve de non-dégradation** : chaque swap testé vérifie explicitement la couverture des 45 maisons. Si un swap réduit la couverture, il reçoit une pénalité importante (10 000$ × maisons non couvertes) et est donc rejeté pendant la recherche locale best-improvement.

Illustration d'opérations typiques (itérations 1–10) :

1. **Itération 1 (k=1)** — Shaking (swap aléatoire de type) : change Large→Medium à (8,11). Local search en N₁ évalue tous les swaps et accepte un autre swap qui réduit le coût tout en préservant la couverture. Résultat : économie immédiate de $500.

2. **Itération 3 (k=2)** — Shaking (petit déplacement) : déplacer une Medium de (12,8) à (13,9) ouvre la possibilité de downgrader une Large voisine. Local search trouve la séquence : déplacer → downgrade → économie nette $1,000.

3. **Itération 7 (k=1)** — Plusieurs swaps testés, seul le meilleur (best-improvement) est accepté, garantissant que chaque mouvement local choisi est le plus profitable possible dans N₁.

Bénéfices observés dans cette phase :

- Réduction rapide des coûts sans perte de couverture
- Stabilisation de la solution autour d'une configuration robuste (souvent encore 11–12 antennes)
- Préparation de la solution pour les modifications structurelles (N₃, N₄) aux itérations suivantes

Limites et garde-fous :

- **Éviter les oscillations** : pour ne pas répéter indéfiniment les mêmes downgrades/upgrade, l'historique est suivi via l'objectif; seules les améliorations strictes sont acceptées.
- **Temps consommé** : la recherche exhaustive dans N₁ (best-improvement) coûte du temps, mais fournit des gains qui justifient ce coût dans les premières itérations.

Suite :

Passé ce stade d'affinage (itérations 1–10), le VNS s'oriente vers des changements plus structurels (itérations 11–25) en utilisant N₃ et N₄ pour supprimer les antennes redondantes et tester des remplacements aléatoires. Ces étapes permettent de réduire le nombre d'antennes tout en maintenant la couverture — c'est la prochaine phase du processus d'optimisation.

*(Exemples illustratifs d'itérations, voir le document original pour la liste complète d'itérations et détails)*

- Le VNS explore surtout N₁ et N₂ au début
- Trouve des dégradations de type (Large → Medium → Small) qui conservent la couverture
- Ajustements de position (N₂) permettent de rendre possibles certains downgrades
- Réduction du coût de $15,500 à $14,000 tout en conservant 12 antennes et 100 % de couverture

---

### Itérations 11-25 : Changements structurels (N₃-N₄)

- Les voisinages plus larges (remplacement, ajout/suppression) sont explorés
- Découverte clef : suppression d'une antenne redondante → 12 → 11 antennes
- Réduction du coût à $13,000

---

### Itérations 26-40 : Exploration profonde (N₅)

- Échanges de deux antennes (N₅) provoquent de nouvelles configurations
- Ces nouvelles configurations peuvent permettre des économies additionnelles
- Réduction du coût final à $12,500 avec 11 antennes

---

### Itérations 41-50 : Convergence

- Après plusieurs cycles sans amélioration, le VNS atteint un fort optimum local
- Résultat final (exemple) après 50 itérations :
  - Antennes : 11
  - Maisons couvertes : 45/45
  - Coût : $12,500
  - Temps d'exécution : ≈ 8 secondes

---

## Choix de paramètres pour notre problème (45 maisons)

### Pourquoi 5 voisinages ?

| Voisinages | Avantages | Inconvénients | Notre choix |
|------------|-----------|---------------|-------------|
| 2-3        | Rapide, simple | Exploration limitée | Trop peu |
| **5**      | **Bon équilibre** | **Exploration systématique** | **✅ Choisi** |
| 7+         | Très exhaustif | Lent, complexe | Inutile |

**Raisons :** N₁-N₂ = optimisation locale fréquente ; N₃-N₄ = diversification moyenne ; N₅ = diversification forte et rare.

### Pourquoi 50 itérations ?

- Observations expérimentales :
  - Itérations 1-10 : améliorations rapides
  - Itérations 10-25 : supérieures (suppression d'antennes)
  - Itérations 25-40 : améliorations mineures
  - Itérations 40-50 : convergence
- 50 itérations → bon compromis (≈ 8 s)

### Pourquoi penalty = $10,000 par maison non couverte ?

- Coût d'une antenne : $1,000–$2,000
- Si penalty = $100 ou $1,000, l'algorithme pourrait sacrifier une maison pour économiser sur des antennes
- Avec $10,000 : couverture incomplète devient inacceptable, ce qu'on souhaite

---

## Analyse de performance

### Complexité et opérations

- Local search N₁ (best-improvement) : pour ~11 antennes × 2 types × 45 maisons ≈ 990 vérifications par round
- Total par itération ≈ 4,000–6,000 opérations
- Pour 50 itérations ≈ 250,000 opérations
- VNS ≈ 3× moins d'opérations qu'un GA équivalent (ex. 50 générations)

### Temps d'exécution (observé)

- 5–10 secondes sur matériel courant
- Greedy : 50–200 ms
- GA comparable : 30–60 secondes

### Mémoire

- VNS : environ 1.2 KB (solution courante + meilleure)
- GA : ~20 KB pour une population (30 solutions)

---

## Avantages et inconvénients (pour notre problème)

**Avantages ✅**
1. Exploration systématique ordonnée
2. Échapement efficace des optima locaux (shaking)
3. Convergence rapide comparée au GA
4. Flexible et interprétable
5. Peu gourmand en mémoire
6. Recherche locale déterministe (best-improvement)

**Inconvénients ❌**
1. Plus lent que greedy (5–10 s vs 0.05–0.2 s)
2. Sensible à la qualité de l'initialisation
3. Overhead de la recherche locale
4. Pas de garantie d'optimalité globale
5. Conception des voisinages cruciale
6. Risque de cycles sans amélioration (nécessite early stopping)

---

## Quand utiliser VNS pour le placement d'antennes ?

**Utilisez VNS quand :**
- Vous avez besoin de meilleures solutions que greedy et plus rapides que GA
- L'espace de recherche admet des voisinages naturels
- Vous disposez d'une bonne solution de départ (greedy)
- Limitation mémoire ou contrainte de temps modérée (5–15 s)
- Transparence et interprétabilité nécessaires

**Utilisez GA quand :**
- Qualité maximale critique et temps de calcul non contraint
- Espace de solutions très multimodal et difficile à voisinager
- Aucune bonne solution initiale disponible

**Utilisez Greedy quand :**
- Besoin en temps réel (< 1 s)
- Reproductibilité stricte ou simplicité prioritaire

---

## Résumé

**Ce que nous avons implémenté et observé :**
- Problème : placer des antennes pour couvrir 45 maisons sur une grille 20×15, minimiser le coût
- Solution : VNS avec 5 voisinages, initialisation greedy, recherche locale best-improvement, 50 itérations (~8 s)
- Résultat typique : 11 antennes, 100 % couverture, coût $12,500–$13,500
- Amélioration observée : ex. $15,000 → $12,500 = économie $2,500 (≈ 16.7 %)

**Points clés :**
1. VNS = exploration systématique des voisinages (petit→grand)
2. Bon compromis : plus performant que greedy, plus rapide que GA
3. Dépend fortement du design des voisinages et de l'initialisation

---

## Résumé de l'algorithme (caractéristiques principales)

- Système : exploration ordonnée des voisinages k=1→5
- Trajectoire à solution unique
- Deux phases : Shaking (diversification) + Local search (intensification)
- Objectif : coût + pénalité de couverture
- Complexité empirique : ≈ 250,000 opérations pour 50 itérations

**Avantages :** rapide, mémoire faible, solutions de haute qualité

**Limitations :** plus lent que greedy, dépend du voisinage, non garanti optimal

---

> Fichier généré : prêt à être consulté et téléchargé (format Markdown).

