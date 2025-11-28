# Analyse de Performance et Choix de la Méthode Best Improvement

## Notre Choix : Best Improvement
- Examiner **TOUS** les voisins dans le voisinage courant \(N_k\)
- Choisir **la meilleure amélioration absolue**
- Plus lent, mais trouve de meilleurs optima locaux
- Crucial pour notre problème : un mauvais changement de type peut faire perdre la couverture

### Exemple pour notre problème (45 maisons, 11 antennes)
- Recherche locale dans \(N_1\) : tester 11 × 2 = **22 voisins** (3 types, 2 remplacements par antenne)
- Temps supplémentaire : ~50 ms (par rapport à First Improvement)
- Rentable : souvent **500 à 1000 $ d'économie** supplémentaires

---

# Analyse des Performances

## Complexité Computationnelle

### Par itération

### **1. Shaking : O(1)**
- Génère **un seul voisin aléatoire**
- Temps constant quel que soit le voisinage

### **2. Local Search (Best Improvement dans N₁)**
Pour chaque antenne (~11 en moyenne) :
- Tester changement vers 2 autres types → 11 × 2 = 22 essais
- Pour chaque essai : vérifier couverture des 45 maisons

**Opérations :**
```
11 × 2 × 45 = 990 vérifications de couverture
```
Nombre de rounds (jusqu'à stagnation) : 3–5

**Total :** ~3 000 – 5 000 calculs de distance

### **3. Évaluation de l’objectif**
- Vérifier la couverture de toutes les maisons
```
11 antennes × 45 maisons = 495 calculs
```

### **Total par itération :**
```
≈ 4 000 à 6 000 opérations
```
### **Pour 50 itérations :**
```
≈ 50 × 5 000 = 250 000 opérations
```

---

## Comparaison avec l’Algorithme Génétique
- **GA :** ~700 000 opérations (50 générations)
- **VNS :** ~250 000 opérations (50 itérations)

➡️ **VNS ≈ 3× plus léger** qu’un algorithme génétique

---

# Temps d’Exécution Réel

Pour notre problème (45 maisons, 11 antennes) :
- **5 à 10 secondes** sur un PC standard
- Beaucoup plus rapide que GA : **30–60 secondes**
- Plus lent que greedy : **50–200 ms**

### Détail du temps par phases
- Itération 0 (initialisation gloutonne) : ~100 ms (1%)
- Itérations 1–20 : ~4 secondes (50%)
- Itérations 21–40 : ~3 secondes (30%)
- Itérations 41–50 : ~2 secondes (20%)

---

# Utilisation Mémoire
### Stockage solution
- 11 antennes × ~50 bytes ≈ **0,6 KB**
- Solution optimale stockée : +0,6 KB

**Total : ~1,2 KB**

### Comparé au GA
- GA : 30 solutions × 12 antennes ≈ **20 KB**
- VNS : **1,2 KB**

➡️ **VNS utilise ~16× moins de mémoire que GA**

---

# Comparaison : Greedy vs GA vs VNS

| Critère | Greedy | Genetic Algorithm | VNS |
|--------|--------|-------------------|------|
| Temps d’exécution | 50–200 ms | 30–60 s | 5–10 s |
| Vitesse | 🥇 1er | 🥉 3e | 🥈 2e |
| Mémoire | 0,6 KB | 20 KB | 1,2 KB |
| Utilisation mémoire | 🥇 1er | 🥉 3e | 🥈 2e |
| Antennes finales | 12 | 11–12 | 11 |
| Coût final | 14k–15k | 12k–14k | 12,5k–13,5k |
| Classement coût | 🥉 3e | 🥇 1er | 🥈 2e |
| Couverture | 100% | 95–100% | 100% |
| Déterminisme | Oui | Non | Non |
| Qualité solution | Bonne | Excellente | Très bonne |
| Complexité | Simple | Complexe | Modérée |

---

# Points Clés
- **VNS = compromis idéal** : rapide + solutions de haute qualité
- Solutions proches du GA (±5%) en **5× moins de temps**
- Couverture toujours garantie : 100%
- Mémoire minimaliste : 16× moins que GA

---

# Avantages du VNS pour notre problème

## ✓ 1. Exploration systématique
- Voisinages \(N_1 → N_5\) testés de façon ordonnée
- Pas de population à gérer (contrairement au GA)
- Très prédictible et contrôlable

## ✓ 2. Échappe efficacement aux optima locaux
- Shaking = diversification contrôlée
- N₅ (swap deux antennes) permet des percées majeures
- Exemple : passage de 13 000$ à 12 500$

## ✓ 3. Convergence rapide
- Grâce au greedy initial
- Local Search appliqué à chaque itération
- Amélioration majeure avant l’itération 25

## ✓ 4. Flexible
- On peut ajouter de nouveaux voisinages :
  - N₆ : échanges inter-zones
  - N₇ : regroupement par cluster

## ✓ 5. Faible utilisation mémoire
- Idéal pour systèmes embarqués ou mobiles

## ✓ 6. Recherche locale déterministe
- Best Improvement toujours fiable
- Plus stable que les mutations GA

---

# Inconvénients du VNS

## ❌ 1. Plus lent que Greedy
- 5–10 secondes vs 100 ms
- Inadéquat en temps réel

## ❌ 2. Qualité dépend du départ
- Mauvais greedy initial → VNS moins performant

## ❌ 3. Local Search coûteuse
- 60% du temps total

## ❌ 4. Pas de garantie d’optimum global
- Peut rester piégé dans un optimum fort

## ❌ 5. Dépend de la définition des voisinages
- Mauvais voisinages → mauvaise performance

## ❌ 6. Peut tourner en rond
- Ex : itérations 40–50 sans amélioration

---

# Quand utiliser VNS ?
### Utiliser VNS quand :
- Vous voulez **mieux que greedy**, mais **plus rapide que GA**
- Vous avez un **bon point de départ (greedy)**
- Vous avez besoin d’un **résultat en 5–15 secondes**
- Vous pouvez définir des voisinages cohérents
- Vous travaillez dans un environnement mémoire limité

### Utiliser GA quand :
- Objectif qualité maximale
- Problème très complexe, multimodal
- Temps illimité
- Possibilité de paralléliser

### Utiliser Greedy quand :
- Temps réel (<1 seconde)
- Résultat suffisamment bon
- Simplicité et reproductibilité nécessaires

---

# Résumé
### Ce que nous avons implémenté
- Problème : couverture 45 maisons sur grille 20×15, minimiser le coût
- Méthode : VNS (Variable Neighborhood Search)
- Voisinages : N₁ à N₅
- Initialisation gloutonne
- Local Search (Best Improvement)
- Shaking systématique
- 50 itérations (~8 s)

### Résultats
- Temps : 5–10 s
- Solution : 11 antennes, coût 12 500–13 500 $
- Amélioration : **+16,7% par rapport au greedy**

---

# Pourquoi apprendre VNS ?
- Montre la puissance des recherches locales structurées
- Illustr

