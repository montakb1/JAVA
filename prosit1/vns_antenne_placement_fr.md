# Variable Neighborhood Search (VNS) pour l'optimisation du placement d'antennes

## Énoncé du problème

Étant donné :

- Une grille de **20×15** représentant une zone géographique
- **45 maisons** réparties aléatoirement (densité ≈ 15%)
- Chaque maison contient **100 utilisateurs** nécessitant une couverture cellulaire
- Trois types d'antennes avec des rayons de couverture et coûts différents :

  - **Small** : rayon = 2 cellules, coût = $1,000
  - **Medium** : rayon = 4 cellules, coût = $1,500
  - **Large** : rayon = 6 cellules, coût = $2,000

- La couverture est calculée en utilisant la distance euclidienne
- Les antennes **ne peuvent pas** être placées sur des positions de maisons

### Objectif :

- Atteindre **100% de couverture** (toutes les 45 maisons couvertes)
- **Minimiser** le coût total de déploiement
- Trouver un placement d'antennes optimal ou quasi-optimal

---

## Pourquoi Variable Neighborhood Search ?

Contrairement aux algorithmes génétiques qui maintiennent des populations et utilisent des opérateurs évolutifs, le VNS explore systématiquement différentes structures de voisinage autour d'une solution courante. Il sort des optima locaux en changeant la structure de voisinage plutôt qu'en maintenant plusieurs solutions, ce qui le rend plus économe en mémoire et souvent plus rapide tout en trouvant des solutions de haute qualité.

---

## Comment fonctionne le Variable Neighborhood Search

### 1. Représentation de la solution

Chaque solution est une liste de placements d'antennes sur notre grille 20×15 :

```python
Solution = [
  {x: 5, y: 3, type: "Large", radius: 6, cost: 2000},
  {x: 12, y: 8, type: "Medium", radius: 4, cost: 1500},
  {x: 7, y: 14, type: "Small", radius: 2, cost: 1000}
]
```

**Contraintes :**

- Les positions (x, y) doivent être dans les limites de la grille : 0 ≤ x < 20, 0 ≤ y < 15
- On ne peut pas placer d'antennes sur les emplacements des maisons
- La solution démarre par une initialisation gloutonne (greedy) pour un point de départ de qualité


### 2. Structures de voisinage (k-voisinages)

**Concept central du VNS :** définir plusieurs façons de modifier une solution, ordonnées des perturbations petites vers grandes :

- **N₁ - Échanger le type d'antenne (petite perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Changer le type d'une antenne
After:  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(7,14,Small)]
         ↑ Large → Medium (économise $500, peut réduire la couverture)
```

- **N₂ - Déplacer une antenne (perturbation moyenne) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Déplacer la position d'une antenne de 1-2 cases
After:  [Antenna(5,3,Large), Antenna(13,9,Medium), Antenna(7,14,Small)]
                              ↑ déplacée de (12,8) à (13,9)
```

- **N₃ - Remplacer une antenne (perturbation moyenne-grande) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Retirer une antenne et en ajouter une nouvelle à un emplacement valide aléatoire
After:  [Antenna(5,3,Large), Antenna(18,6,Small), Antenna(7,14,Small)]
                              ↑ remplacement (12,8,Medium) par (18,6,Small)
```

- **N₄ - Ajouter/Supprimer une antenne (grande perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Supprimer l'antenne qui couvre le moins de maisons OU ajouter une antenne pour une zone non couverte
After:  [Antenna(5,3,Large), Antenna(12,8,Medium)]  ← suppression de la moins utile
```

- **N₅ - Échanger deux antennes (grande perturbation) :**

```python
Before: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Échanger positions et types de deux antennes
After:  [Antenna(12,8,Large), Antenna(5,3,Medium), Antenna(7,14,Small)]
        ↑ échange des deux premières antennes
```

**Pourquoi plusieurs voisinages ?**

- **Voisinages petits (N₁-N₂)** : fine optimisation des bonnes solutions (exploitation)
- **Voisinages grands (N₄-N₅)** : échapper aux optima locaux (exploration)
- **Recherche progressive** : commencer petit et agrandir si bloqué

---

### 3. Fonction objectif (qualité de la solution)

Évaluation d'une solution :

```python
cost = total_antenna_cost
penalty = uncovered_houses × penalty_weight
objective = cost + penalty  # MINIMISER
```

**Décomposition pour notre problème :**

1. **Coût total** = somme des coûts des antennes
   - Small : $1,000
   - Medium : $1,500
   - Large : $2,000

2. **Pénalité de couverture** = (45 - houses_covered) × 10,000
   - Distance euclidienne : √[(x₁-x₂)² + (y₁-y₂)²]
   - Poids de pénalité = $10,000 par maison non couverte
   - Rend la couverture incomplète extrêmement coûteuse

3. **Objectif complet** :
   - Si les 45 maisons sont couvertes : objectif = coût uniquement
   - Si 43/45 maisons couvertes : objectif = coût + 2×$10,000

**Exemple de calcul :**

- Solution avec 10 antennes couvrant 43/45 maisons, coût $13,000 :
  - coverage_penalty = (45-43) × 10,000 = $20,000
  - objective = $13,000 + $20,000 = **$33,000** (MAUVAIS - couverture incomplète)

- Meilleure solution avec 11 antennes couvrant 45/45 maisons, coût $14,000 :
  - coverage_penalty = (45-45) × 10,000 = $0
  - objective = $14,000 + $0 = **$14,000** (BON - couverture complète)

**Pourquoi cet objectif ?**

- Forte pression pour atteindre 100% de couverture d'abord
- Une fois la couverture atteinte, l'algorithme se concentre uniquement sur la minimisation du coût
- Simple à calculer et à interpréter

---

### 4. Recherche locale (amélioration de solution)

**Recherche locale Best Improvement** - Améliore systématiquement dans un seul voisinage :

```
Donné : Solution courante, Voisinage Nₖ
Répéter jusqu'à absence d'amélioration :
  1. Générer **TOUS** les voisins possibles dans Nₖ
  2. Évaluer l'objectif pour chaque voisin
  3. Si meilleur voisin < solution courante :
       → Accepter le meilleur voisin comme nouvelle solution courante
       → Continuer la recherche
  4. Sinon :
       → Optimum local atteint dans Nₖ
       → Retourner à la boucle principale de VNS
```

**Exemple - Recherche locale en N₁ (Échange de type) :**

```
Current: [Antenna(5,3,Large,$2000), Antenna(12,8,Medium,$1500)]
         Cost = $3,500, Coverage = 45/45, Objective = $3,500

Try all type swaps:
  Neighbor 1: [Antenna(5,3,Medium), Antenna(12,8,Medium)]
              Cost = $3,000, Coverage = 45/45, Objective = $3,000 ✓ MEILLEUR!
              
  Neighbor 2: [Antenna(5,3,Small), Antenna(12,8,Medium)]
              Cost = $2,500, Coverage = 42/45, Objective = $32,500 ✗ PIRE
              
  Neighbor 3: [Antenna(5,3,Large), Antenna(12,8,Small)]
              Cost = $3,000, Coverage = 43/45, Objective = $23,000 ✗ PIRE
              
  Neighbor 4: [Antenna(5,3,Large), Antenna(12,8,Large)]
              Cost = $4,000, Coverage = 45/45, Objective = $4,000 ✗ PIRE

Accepter Neighbor 1 (objective $3,000 < $3,500)
Continuer la recherche locale depuis cette nouvelle solution...
```

**Pourquoi Best Improvement ?**

- Explore tous les voisins avant de se déplacer (approche exhaustive)
- Garantit de trouver le meilleur mouvement dans le voisinage
- Plus lent que le "first improvement" mais trouve de meilleurs optima locaux
- Crucial pour notre problème où un mauvais mouvement peut faire perdre la couverture

---

### 5. Shaking (perturbation)

**Génération d'un voisin aléatoire** - Échapper aux optima locaux par des changements aléatoires :

```
Donné : Solution courante, Voisinage Nₖ
Action :
  1. Générer UN voisin aléatoire en appliquant la transformation Nₖ
  2. NE PAS évaluer ni comparer - simplement générer
  3. Retourner ce voisin aléatoire
```

**Pourquoi le shaking importe :**

```
Scénario : Bloqué à l'optimum local dans N₁

Solution courante (optimum local dans N₁):
  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(18,14,Small)]
  Coût = $4,000, Toutes les maisons couvertes

Shaking avec N₂ (Move Antenna):
  Déplacer aléatoirement Antenna(12,8) vers (11,7)
  Nouvelle solution: [Antenna(5,3,Medium), Antenna(11,7,Medium), Antenna(18,14,Small)]

Cette perturbation peut :
  - Toujours couvrir toutes les maisons (bon)
  - Ouvrir la possibilité d'un meilleur échange de type (maintenant N₁ peut améliorer)
  - OU empirer temporairement la solution (acceptable — on explore)
```

**Stratégie de shaking pour notre problème de 45 maisons :**
- Commencer par des perturbations petites (N₁) quand la solution est bonne
- Passer à des perturbations plus grandes (N₄, N₅) si bloqué
- Les changements aléatoires empêchent de revenir cycliquement au même optimum local
- Accepter la shake sans condition — on l'améliore ensuite par recherche locale

---

## Algorithme VNS — Structure complète

### Boucle VNS complète

```
Algorithme VNS :
1. Générer solution initiale (algorithme greedy)
2. k = 1 (commencer par le plus petit voisinage)
3. Répéter jusqu'au critère d'arrêt (max itérations ou temps) :
   
   PHASE SHAKING :
     4. Générer un voisin aléatoire x' dans Nₖ(x)
   
   PHASE RECHERCHE LOCALE :
     5. Appliquer la recherche locale Best Improvement : x'' = LocalSearch(x', N₁)
   
   DÉCIDER DU DÉPLACEMENT :
     6. Si objective(x'') < objective(x) :
          → ACCEPTER : x = x'', k = 1 (recommencer par le plus petit voisinage)
       Sinon :
          → REJETER : k = k + 1 (essayer un voisinage plus large)
   
   CHANGEMENT DE VOISINAGE :
     7. Si k > kₘₐₓ : k = 1 (retour au plus petit voisinage)

8. Retourner la meilleure solution trouvée
```

**Principes clés du VNS :**

- **Changement systématique de voisinage** :
  - Succès → redémarrer à N₁ (exploitation)
  - Échec → passer à Nₖ₊₁ (exploration)

- **Recherche locale utilise toujours N₁** :
  - Shaking utilise Nₖ (diversification)
  - Recherche locale utilise N₁ (intensification)

- **Pas de population nécessaire** :
  - Trajectoire à solution unique
  - Mémoire : O(nombre d'antennes) vs O(population × antennes)
  - Plus simple qu'un algorithme génétique

---

## Étape par étape : comment VNS résout notre problème

### Initialisation (Itération 0)

Générer la solution initiale via l'algorithme greedy :

```python
Greedy Initialization :
1. Commencer avec un ensemble d'antennes vide
2. Tant que des maisons restent non couvertes :
     - Trouver les maisons non couvertes
     - Essayer toutes les combinaisons (position, type)
     - Choisir l'antenne qui couvre le plus de maisons non couvertes par dollar
     - Ajouter à la solution
3. Retourner la solution initiale pour VNS
```

**Solution initiale (exemple) :**
- Antennes : 12 antennes placées stratégiquement
- Maisons couvertes : 45/45 (100%)
- Coût : $15,500
- Objectif : $15,500 (pas de pénalité)

**Pourquoi démarrer par greedy ?**
- Un démarrage aléatoire peut avoir une mauvaise couverture (objectif = coût + grosse pénalité)
- Le greedy assure 100% de couverture dès l'itération 1
- VNS améliore à partir de cette base de qualité
- Convergence plus rapide qu'avec un démarrage aléatoire

---

### Itérations 1-10 : Affinage avec N₁ (Swap Type)

**Itération 1 :**

```
Current: 12 antennes, $15,500, complètement couvert
k = 1 (N₁ - Swap Type)

SHAKING:
  Changement aléatoire : Large → Medium à (8,11)
  Shaken: 12 antennes, $15,000, couverture = 44/45
  Objective = $15,000 + $10,000 = $25,000 (PIRE - mais on continue)

LOCAL SEARCH N₁ :
  Tester tous les swaps de type...
  Trouvé : Small → Medium à (5,3) améliore à $3,000 (exemple)
  Retour : 12 antennes, $15,500, 45/45 (même objectif)

MOVE OR NOT :
  Pas d'amélioration → k = 2
```

**Itération 5 :**

```
Current: 12 antennes, $15,500, complètement couvert
k = 2 (N₂ - Move Antenna)

SHAKING:
  Déplacement : Antenna(12,8,Medium) → (11,9)
  Shaken: 12 antennes, $15,500, couverture = 45/45 ✓

LOCAL SEARCH N₁ :
  Trouvé : Large → Medium à (18,14) conserve la couverture et économise $500
  Nouvelle solution: 12 antennes, $15,000, 45/45 ← AMÉLIORATION !

MOVE OR NOT :
  Accepté → k = 1

Meilleur jusqu'ici : $15,000 avec 12 antennes
```

**Schéma général 1-10 :**
- VNS parcourt surtout N₁ et N₂
- Trouve des déclassements (Large→Medium) qui maintiennent la couverture
- Ajustements de position permettent des choix de type moins coûteux
- Coût réduit: $15,500 → $14,000 (exemple)

---

### Itérations 11-25 : Changements structurels avec N₃-N₄

**Itération 18 :**

```
Current: 12 antennes, $14,000, complètement couvert
k = 4 (N₄ - Add/Remove)

SHAKING:
  Identifier : Antenna(7,14,Small) couvre 2 maisons également couvertes par une autre antenne
  Action : SUPPRIMER Antenna(7,14,Small)
  Shaken: 11 antennes, $13,000, couverture = 45/45 ✓

LOCAL SEARCH N₁ :
  Pas d'amélioration supplémentaire

MOVE OR NOT :
  Accepté → k = 1

Meilleur : $13,000 avec 11 antennes ← suppression redondante
```

**Itération 22 :**

```
Current: 11 antennes, $13,000, complètement couvert
k = 3 (N₃ - Replace)

SHAKING:
  Remplacer Antenna(5,3,Medium) par Antenna(6,4,Large)
  Shaken: 11 antennes, $13,500, couverture = 44/45
  Objective = $23,500 (perte temporaire)

LOCAL SEARCH N₁ :
  Suite de swaps permettant de restaurer la couverture et réduire le coût
  Final: 11 antennes, $13,200, 45/45

MOVE OR NOT :
  Pas d'amélioration significative → k = 4
```

**Schéma 11-25 :**
- Voisinages plus grands (N₃, N₄) explorés lorsque N₁,N₂ sont bloqués
- Itération 18 : suppression d'une antenne redondante (12→11)
- N₄ (remove) fructueux quand recouvrements détectés
- Coût réduit de $14,000 → $13,000

---

### Itérations 26-40 : Exploration profonde avec N₅

**Itération 31 :**

```
Current: 11 antennes, $13,000, complètement couvert
k = 5 (N₅ - Swap Two Antennas)

SHAKING:
  Échanger deux antennes (positions + types)
  Shaken: 11 antennes, $13,000, couverture = 43/45
  Objective = $33,000 (perturbation importante)

LOCAL SEARCH N₁ :
  Réparations par échanges de type successifs
  Résultat final: 11 antennes, $13,000, 45/45 (configuration différente)

MOVE OR NOT :
  Accepté pour diversité, k = 1
```

**Itération 35 :**

```
Current: 11 antennes, $13,000, complètement couvert
k = 2

SHAKING:
  Déplacer Antenna(18,10,Medium) → (17,9)

LOCAL SEARCH N₁ :
  Permet déclassement Large→Small ailleurs sans perte de couverture
  Nouvelle solution: 11 antennes, $12,500, 45/45

MOVE OR NOT :
  Accepté → k = 1

Meilleur : $12,500 avec 11 antennes
```

**Schéma 26-40 :**
- N₅ casse les optima locaux et ouvre de nouvelles configurations
- Permet d'obtenir des économies supplémentaires
- Coût réduit : $13,000 → $12,500

---

### Itérations 41-50 : Convergence

**Itération 45 :**

```
Current: 11 antennes, $12,500, complètement couvert
k = 1 → 2 → 3 → 4 → 5 → 1 (parcours des voisinages)

Aucune amélioration trouvée pendant plusieurs itérations
Conclusion : Optimum local fort (peut-être quasi-globale)
```

**Final (Itération 50) :**

```
Solution finale après 50 itérations :
  Antennes : 11
    - 2 Large : $4,000
    - 6 Medium: $9,000
    - 3 Small: $3,000
  Maisons couvertes: 45/45 (100%)
  Coût: $12,500
  Objectif: $12,500
  Temps d'exécution : ~8 secondes
```

**Résumé d'évolution :**
- Itération 0 (greedy) : $15,500, 12 antennes
- Itérations 1-10 : optimisation de type → $14,000, 12 antennes
- Itérations 11-25 : suppression redondante → $13,000, 11 antennes
- Itérations 26-40 : reconfiguration → $12,500, 11 antennes
- Itérations 41-50 : convergence → $12,500, 11 antennes

**Amélioration : $15,500 → $12,500 = $3,000 économisés (19.4% de réduction)**

---

## Choix des paramètres pour notre problème à 45 maisons

### Pourquoi 5 structures de voisinage ?

| Voisinages | Avantages | Inconvénients | Notre choix |
|------------|----------|---------------|-------------|
| 2-3 | Rapide, simple | Exploration limitée | Trop peu |
| **5** | **Bon équilibre** | **Exploration systématique** | **✅ Choisi** |
| 7+ | Très exhaustif | Lent, complexe | Inutile |

**Raisonnement :**
- N₁-N₂ : optimisation locale (utilisés fréquemment)
- N₃-N₄ : diversification moyenne (utilisés occasionnellement)
- N₅ : forte diversification (rare mais cruciale)

---

### Pourquoi 50 itérations maximum ?

**Observations :**
- Itérations 1-10 : amélioration rapide
- Itérations 10-25 : améliorations majeures
- Itérations 25-40 : améliorations mineures
- Itérations 40-50 : convergence

**Temps d'exécution estimé :**
- 30 itérations : ~5 secondes (peut être insuffisant)
- **50 itérations : ~8 secondes (bonne convergence)** ✅
- 100 itérations : ~16 secondes (rendements décroissants)

---

### Pourquoi poids de pénalité = $10,000 par maison ?

- Coût typique d'une antenne : $1,000-$2,000
- Calibration :
  - penalty = $100 → pourrait accepter 90% couverture pour économiser
  - penalty = $1,000 → pourrait encore accepter échange coût/couverture
  - **penalty = $10,000 → la couverture incomplète devient non optimale** ✅
  - penalty = $100,000 → overkill (même comportement)

**Exemple :**
```
Option A: 10 antennes, 44/45 maisons, coût $12,000
         Objective = $12,000 + 1×$10,000 = $22,000

Option B: 11 antennes, 45/45 maisons, coût $13,000  
         Objective = $13,000 + 0 = $13,000 ← bien meilleur
```

---

### Pourquoi Best Improvement plutôt que First Improvement ?

- **First Improvement** : accepte le premier voisin meilleur que la solution courante (plus rapide)
- **Best Improvement** : examine tous les voisins et prend le meilleur (plus lent, plus robuste)

**Notre choix : Best Improvement**
- Plus lent, mais trouve de meilleurs optima locaux
- Important car un mauvais swap peut faire perdre la couverture
- Pour N₁ sur 11 antennes : ~22 voisins à tester → coût raisonnable

---

## Analyse de performance

### Complexité et opérations estimées

Par itération :
- Shaking : O(1) (voisin aléatoire)
- Local search (Best Improvement en N₁) :
  - Pour chaque antenne (~11) × 2 swaps × 45 maisons ≈ 990 vérifications
  - Répété 3-5 rounds → ~3,000-5,000 calculs de distance
- Évaluation objective : 11 × 45 = 495 calculs

Total par itération : ~4,000-6,000 opérations
Pour 50 itérations : ~250,000 opérations

**Comparaison GA vs VNS :**
- GA (50 générations, population 30) : ~700,000 opérations
- VNS (50 itérations) : ~250,000 opérations
- VNS ≈ 3× moins d'opérations

### Temps d'exécution réel (estimation)
- 5-10 secondes sur matériel standard pour le problème 45 maisons
- Plus rapide que GA (30-60s), plus lent que greedy (50-200ms)

---

### Utilisation mémoire
- Solution unique : ~1.2 KB (solution + meilleur backup)
- GA (30 solutions) : ~20 KB
- VNS utilise ~16× moins de mémoire que GA

---

## Comparaison : Greedy vs Genetic vs VNS

| Métrique | Greedy | Genetic Algorithm | VNS |
|----------|--------|-------------------|-----|
| Temps d'exécution | 50-200 ms | 30-60 s | 5-10 s |
| Mémoire | ~0.6 KB | ~20 KB | ~1.2 KB |
| Antennes finales | 12 | 11-12 | 11 |
| Coût final | $14,000-$15,000 | $12,000-$14,000 | $12,500-$13,500 |
| Couverture | Toujours 100% | Habituellement 100% | Toujours 100% |
| Déterminisme | Déterministe | Non-déterministe | Non-déterministe |

**Résumé :** VNS est un compromis pratique : plus rapide que GA, meilleur que greedy.

---

## Avantages et inconvénients pour notre problème

### Avantages ✅
1. **Exploration systématique** avec une seule solution
2. **Échappement efficace** des optima locaux via shaking
3. **Convergence rapide** en partant d'une solution greedy
4. **Flexible et interprétable** (voisinages intuitifs)
5. **Mémoire faible**
6. **Recherche locale déterministe (Best Improvement)**

### Inconvénients ❌
1. Plus lent que greedy (5-10 s vs 50-200 ms)
2. Dépend fortement de la qualité de l'initialisation
3. Surcharge de recherche locale (temps CPU)
4. Pas de garantie d'optimalité globale
5. Conception des voisinages critique
6. Peut continuer sans amélioration (nécessite early-stopping)

---

## Quand utiliser VNS pour le placement d'antennes ?

- Besoin de meilleures solutions que greedy, mais plus rapides que GA
- Espace de solutions avec voisinages clairs
- Disposer d'une bonne solution de départ (greedy)
- Contraintes mémoire strictes
- Budget temps raisonnable (5-15 s)

Quand préférer GA ou Greedy :
- GA : recherche de qualité maximale, parallélisation disponible, pas de bon départ
- Greedy : temps réel, reproduction exacte, simplicité

---

## Résumé et points clés

**Ce que nous avons implémenté**
- Problème : 45 maisons sur 20×15, minimiser coût avec couverture complète
- Solution : VNS avec 5 voisinages, initialisation greedy, recherche locale Best Improvement (N₁), 50 itérations (~8s)

**Résultats typiques**
- Exécution : 5-10 s
- Solution typique : 11 antennes, 100% couverture, coût $12,500-$13,500
- Amélioration sur greedy : $15,000 → $12,500 (~16.7%)

**Conclusions**
- VNS est un compromis pratique : systématique, interprétable, mémoire faible
- Meilleur que greedy, proche de GA en qualité, beaucoup plus rapide que GA

---

### Résumé de l'algorithme

**Caractéristiques clés :**
- Recherche systématique : k=1→5
- Trajectoire à solution unique
- Phases : Shaking (diversification) + Local Search (intensification)
- Objectif : coût + pénalité couverture

**Avantages :**
- Convergence rapide, qualité élevée, faible mémoire

**Limitations :**
- Plus lent que greedy, dépendant de l'initialisation, conception des voisinages

---

*Fichier formaté en markdown — structure conservée et traduit en français comme demandé.*

