# Étape par étape : Comment le VNS résout notre problème

## Initialisation (Itération 0)

**Générer la solution initiale à l'aide de l'algorithme glouton :**

```python
Greedy Initialization :
1. Commencer avec un ensemble d'antennes vide
2. Tant que des maisons restent non couvertes :
   - Trouver une maison non couverte
   - Essayer toutes les combinaisons (position, type)
   - Choisir l'antenne qui couvre le plus de maisons non couvertes par dollar
   - Ajouter à la solution
3. Retourner comme solution initiale pour le VNS
```

**Solution initiale :**

- Antennes : 12 antennes placées stratégiquement
- Maisons couvertes : 45/45 (100%)
- Coût : $15,500
- Objectif : $15,500 (pas de pénalité car entièrement couvert)

**Pourquoi commencer par greedy ?**

- Un démarrage aléatoire peut donner une mauvaise couverture (objectif = coût + grosse pénalité)
- Le greedy garantit 100% de couverture dès l'itération 1
- Le VNS améliore à partir de cette base de qualité
- Convergence plus rapide que l'initialisation aléatoire


### Itération 1-10 : Affinage avec N₁ (Échange de type)

**Itération 1 :**

- Courant : 12 antennes, $15,500, entièrement couvert
- k = 1 (utilisation de N₁ - Échange de type d'antenne)

**SHAKING :**
- Changement aléatoire : Échanger Antenna(8,11,Large) → Antenna(8,11,Medium)
- Solution secouée : 12 antennes, $15,000, couverture = 44/45
- Objectif = $15,000 + 1×$10,000 = $25,000 (Pire — mais on continue)

**RECHERCHE LOCALE en N₁ :**
- Essayer d'échanger le type de chaque antenne...
- Amélioration trouvée : Échanger Antenna(5,3,Small) → Antenna(5,3,Medium)
- Nouvelle solution : 12 antennes, $15,500, couverture = 45/45
- Objectif = $15,500 (couverture restaurée)
- Tester plus d'échanges... Aucune amélioration supplémentaire
- Retour : 12 antennes, $15,500, 45/45 couvertes

**MOVE OR NOT :**
- Nouvel objectif ($15,500) = Objectif courant ($15,500)
- AUCUNE AMÉLIORATION → k = k+1 = 2


**Itération 5 :**

- Courant : 12 antennes, $15,500, entièrement couvert
- k = 2 (utilisation de N₂ - Déplacement d'antenne)

**SHAKING :**
- Déplacement aléatoire : Antenna(12,8,Medium) de (12,8) à (11,9)
- Solution secouée : 12 antennes, $15,500, couverture = 45/45 ✓

**RECHERCHE LOCALE en N₁ :**
- Depuis la solution déplacée, essayer des échanges de type...
- Trouvé : Échanger Antenna(18,14,Large) → Antenna(18,14,Medium)
- Toujours couvre les mêmes maisons ! Économie de $500
- Nouvelle solution : 12 antennes, $15,000, 45/45 couvertes
- Objectif = $15,000 ← AMÉLIORATION !

**MOVE OR NOT :**
- Nouvel objectif ($15,000) < Courant ($15,500)
- ✓ ACCEPTÉ → Mettre à jour la solution courante
- k = 1 (redémarrer depuis N₁)

**Meilleur jusqu'ici : $15,000 avec 12 antennes**


**Pattern (Itérations 1-10) :**

- Le VNS alterne principalement entre N₁ et N₂
- Trouve plusieurs déclassements de type (Large→Medium, Medium→Small) maintenant la couverture
- De petits ajustements de position permettent de meilleurs choix de type
- Coût réduit de $15,500 à $14,000 (exemple)
- Toujours 12 antennes, toutes les maisons couvertes


### Itération 11-25 : Changements structurels avec N₃-N₄

**Itération 18 :**

- Courant : 12 antennes, $14,000, entièrement couvert
- k = 4 (utilisation de N₄ - Ajouter/Supprimer antenne)

**SHAKING :**
- Identifier : Antenna(7,14,Small) couvre seulement 2 maisons
- Ces 2 maisons sont aussi couvertes par Antenna(8,12,Medium)
- Action : SUPPRIMER Antenna(7,14,Small)
- Solution secouée : 11 antennes, $13,000, couverture = 45/45 ✓
- Objectif = $13,000

**RECHERCHE LOCALE en N₁ :**
- Depuis la solution à 11 antennes, essayer des échanges de type...
- Aucune amélioration trouvée (tous les échanges font perdre la couverture ou augmentent le coût)
- Retour : 11 antennes, $13,000, 45/45 couvertes

**MOVE OR NOT :**
- Nouvel objectif ($13,000) < Courant ($14,000)
- ✓ AMÉLIORATION MAJEURE → Accepter
- k = 1 (redémarrer)

**Meilleur jusqu'ici : $13,000 avec 11 antennes ← antenne redondante supprimée !**


**Itération 22 :**

- Courant : 11 antennes, $13,000, entièrement couvert
- k = 3 (utilisation de N₃ - Remplacer antenne)

**SHAKING :**
- Remplacer Antenna(5,3,Medium) par une nouvelle antenne aléatoire
- Nouvelle antenne : Antenna(6,4,Large) à un emplacement différent
- Solution secouée : 11 antennes, $13,500, couverture = 44/45
- Objectif = $13,500 + $10,000 = $23,500 (perte de couverture temporaire)

**RECHERCHE LOCALE en N₁ :**
- Essayer des échanges de type pour restaurer la couverture...
- Séquence trouvée :
  1. Échanger Antenna(10,8,Small) → Antenna(10,8,Medium) : couvre la maison perdue
     → Couverture restaurée : 45/45, coût = $14,000
  2. Échanger Antenna(6,4,Large) → Antenna(6,4,Medium) : pas de perte de couverture
     → Coût réduit : $13,500
  3. Continuer l'optimisation...
- Final : 11 antennes, $13,200, 45/45 couvertes

**MOVE OR NOT :**
- Nouvel objectif ($13,200) ≈ Courant ($13,000)
- Pas d'amélioration significative → k = k+1 = 4


**Pattern (Itérations 11-25) :**

- Voisinages plus grands (N₃, N₄) explorés lorsque N₁ et N₂ sont bloqués
- Découverte clé à l'itération 18 : suppression d'une antenne redondante (12→11)
- Plusieurs tentatives avec N₃ (replace) majoritairement rejetées
- N₄ (remove) efficace lorsqu'il détecte des recouvrements
- Coût réduit de $14,000 à $13,000
- Nombre d'antennes réduit : 12→11


### Itération 26-40 : Exploration profonde avec N₅

**Itération 31 :**

- Courant : 11 antennes, $13,000, entièrement couvert
- k = 5 (utilisation de N₅ - Échanger deux antennes)
- Bloqué dans N₁-N₄ depuis plusieurs itérations

**SHAKING :**
- Échanger positions + types de deux antennes : Antenna(5,3,Medium) ↔ Antenna(18,10,Large)
- Résultat : Antenna(18,10,Medium) et Antenna(5,3,Large)
- Solution secouée : 11 antennes, $13,000, couverture = 43/45
- Objectif = $13,000 + 2×$10,000 = $33,000 (perturbation importante)

**RECHERCHE LOCALE en N₁ :**
- Réparations majeures nécessaires...
- Séquence d'améliorations trouvée :
  1. Échanger Antenna(5,3,Large) → Antenna(5,3,Medium) : restauration de la couverture
  2. Échanger Antenna(12,7,Medium) → Antenna(12,7,Large) : couvre la 2ᵉ maison perdue
  3. Maintenant 45/45 couvertes, coût = $13,500
  4. Essayer un déclassement : Antenna(15,9,Large) → Antenna(15,9,Medium)
     → Succès ! Toujours 45/45, nouveau coût = $13,000
- Final après recherche locale : 11 antennes, $13,000, 45/45 couvertes (configuration différente)

**MOVE OR NOT :**
- Nouvel objectif ($13,000) = Courant ($13,000)
- Mais CONFIGURATION DIFFÉRENTE
- Peut conduire à de nouvelles améliorations...
- Accepter pour diversité, k = 1


**Itération 35 :**

- Courant : 11 antennes, $13,000, entièrement couvert (nouvelle configuration)
- k = 2 (retour à N₂ après acceptation de l'échange)

**SHAKING :**
- Déplacer Antenna(18,10,Medium) de (18,10) à (17,9)

**RECHERCHE LOCALE en N₁ :**
- Trouvé : Ce changement de position permet :
  Antenna(15,9,Medium) → Antenna(15,9,Small) : pas de perte de couverture !
  → Économie de $500
- Nouvelle solution : 11 antennes, $12,500, 45/45 couvertes

**MOVE OR NOT :**
- Nouvel objectif ($12,500) < Courant ($13,000)
- ✓ AMÉLIORATION → Accepter, k = 1

**Meilleur jusqu'ici : $12,500 avec 11 antennes**


**Pattern (Itérations 26-40) :**

- N₅ (perturbation large) permet de sortir d'un optimum local
- L'échange de deux antennes crée une configuration radicalement différente
- Cette nouvelle configuration ouvre des opportunités d'économie
- Coût réduit de $13,000 à $12,500
- Toujours 11 antennes mais meilleur placement


### Itération 41-50 : Convergence

**Itération 45 :**

- Courant : 11 antennes, $12,500, entièrement couvert
- k = 1 → 2 → 3 → 4 → 5 → 1 (parcours de tous les voisinages)

- Aucune amélioration trouvée dans aucun voisinage pendant 5+ itérations consécutives
- VNS a exploré :
  - Tous les échanges de type (N₁) : aucune amélioration
  - Tous les ajustements de position (N₂) : aucune amélioration
  - Remplacements d'antenne (N₃) : tous pires
  - Ajout/Suppression (N₄) : impossible sans perdre la couverture
  - Échanges deux-antenne (N₅) : mènent à des configurations similaires

- Conclusion : Optimum local fort (peut-être quasi-global)


**Itération 50 (Arrêt) :**

- Solution finale après 50 itérations :
  - Antennes : 11 antennes
    - 2 Large aux positions clés : $4,000
    - 6 Medium pour la couverture principale : $9,000
    - 3 Small pour maisons isolées : $3,000
  - Maisons couvertes : 45/45 (100%)
  - Coût : $12,500
  - Objectif : $12,500
  - Temps d'exécution : ~8 secondes


**Résumé de l'évolution :**

- Itération 0 : Initialisation greedy → $15,500, 12 antennes
- Itérations 1-10 : optimisation de type → $14,000, 12 antennes
- Itérations 11-25 : suppression de redondance → $13,000, 11 antennes
- Itérations 26-40 : changement de configuration → $12,500, 11 antennes
- Itérations 41-50 : convergence → $12,500, 11 antennes (final)

**Amélioration : $15,500 → $12,500 = $3,000 économisés (19.4% de réduction)**

---

## Choix des paramètres pour notre problème à 45 maisons

### Pourquoi 5 structures de voisinage ?

| Voisinages | Avantages | Inconvénients | Notre choix |
|------------|-----------|---------------|-------------|
| 2-3 | Rapide, simple | Exploration limitée | Trop peu |
| **5** | **Bon équilibre** | **Exploration systématique** | **✅ Choisi** |
| 7+ | Très exhaustif | Lent, complexe | Inutile |

**Raisonnement :**
- N₁-N₂ : optimisation locale (utilisation fréquente)
- N₃-N₄ : diversification moyenne (utilisation occasionnelle)
- N₅ : forte diversification (rare mais cruciale)


### Pourquoi 50 itérations maximum ?

**Observations :**
- Itérations 1-10 : amélioration rapide (optimisation de type)
- Itérations 10-25 : amélioration majeure (suppression d'antenne)
- Itérations 25-40 : amélioration mineure (configuration)
- Itérations 40-50 : convergence

**Temps d'exécution estimé :**
- 30 itérations : ~5 secondes (peut ne pas suffire)
- **50 itérations : ~8 secondes (bonne convergence)** ✅
- 100 itérations : ~16 secondes (rendements décroissants)


### Pourquoi poids de pénalité = $10,000 par maison ?

- Coût typique d'une antenne : $1,000-$2,000
- Calibration :
  - penalty = $100 → l'algorithme pourrait accepter 90% de couverture pour économiser
  - penalty = $1,000 → pourrait encore échanger coût/couverture
  - **penalty = $10,000 → la couverture incomplète n'est jamais optimale** ✅
  - penalty = $100,000 → surdimensionné, même comportement

**Exemple :**

Option A: 10 antennes, 44/45 maisons, coût $12,000
Objectif = $12,000 + 1×$10,000 = $22,000

Option B: 11 antennes, 45/45 maisons, coût $13,000
Objectif = $13,000 + 0 = $13,000 ← BIEN MEILLEUR

---

### Pourquoi Best Improvement plutôt que First Improvement ?

- **First Improvement** : accepte le premier voisin meilleur que la solution courante (plus rapide)
- **Best Improvement** : examine tous les voisins et prend le meilleur (plus lent, plus robuste)

**Notre choix : Best Improvement**
- Plus lent, mais trouve de meilleurs optima locaux
- Critique car un mauvais échange peut faire perdre la couverture
- Pour N₁ sur 11 antennes : tester 11×2 = 22 voisins (3 types d'antennes, 2 échanges chacun)
- Coût supplémentaire raisonnable (~50 ms) vs First Improvement

---

## Analyse de performance

### Complexité et opérations estimées

Par itération :
- Shaking : O(1) (génération d'un voisin aléatoire)
- Recherche locale (Best Improvement en N₁) :
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
- 5-10 secondes sur du matériel standard pour le problème 45 maisons
- Plus rapide que GA (30-60 s), plus lent que greedy (50-200 ms)

**Répartition :**
- Itération 0 (initialisation greedy) : ~100 ms (1%)
- Itérations 1-20 : ~4 s (50%)
- Itérations 21-40 : ~3 s (30%)
- Itérations 41-50 : ~2 s (20%)


### Utilisation mémoire
- Stockage solution : seule solution ≈ 10-12 antennes = ~50 octets × 11 = ~0.6 KB
- Sauvegarde meilleure solution : ≈ 0.6 KB
- Total : ~1.2 KB (négligeable)

**Comparaison GA :**
- GA : 30 solutions × 12 antennes ≈ ~20 KB
- VNS : 1 solution × 11 antennes ≈ ~1.2 KB
- VNS utilise ≈ 16× moins de mémoire

---

## Comparaison : Greedy vs Génétique vs VNS

| Métrique | Greedy | Algorithme Génétique | VNS |
|----------|--------|----------------------|-----|
| Temps d'exécution | 50-200 ms | 30-60 s | 5-10 s |
| Mémoire | ~0.6 KB | ~20 KB | ~1.2 KB |
| Antennes finales | 12 | 11-12 | 11 |
| Coût final | $14,000-$15,000 | $12,000-$14,000 | $12,500-$13,500 |
| Couverture | Toujours 100% | Généralement 100% | Toujours 100% |
| Consistance | Identique à chaque fois | Variable (aléatoire) | Variable (stochastique) |
| Qualité solution | Bonne (optimum local) | Potentiellement meilleure (global) | Très bonne (quasi-globale) |
| Complexité | Simple | Complexe | Modérée |

**Insight :** VNS est le compromis pratique : plus rapide que GA, meilleure que greedy.

---

## Avantages et inconvénients pour notre problème

### Avantages ✅
1. Exploration systématique avec une seule solution
2. Échappement efficace des optima locaux (shaking)
3. Convergence rapide en partant d'une solution greedy
4. Flexible et interprétable (voisinages intuitifs)
5. Faible utilisation mémoire
6. Recherche locale déterministe (Best Improvement)

### Inconvénients ❌
1. Plus lent que greedy (5-10 s vs 50-200 ms)
2. Dépend de la qualité de l'initialisation
3. Surcharge due à la recherche locale
4. Pas de garantie d'optimalité globale
5. Conception des voisinages critique
6. Peut continuer sans amélioration (nécessite early-stopping)

---

## Quand utiliser VNS pour le placement d'antennes ?

- Besoin de meilleures solutions que greedy mais plus rapides que GA
- Espace de solutions avec voisinages clairs
- Disposer d'une bonne solution de départ (greedy)
- Contraintes mémoire strictes
- Budget temps raisonnable (5-15 s)


### Quand préférer GA ou Greedy :
- **GA** : qualité maximale, parallélisation disponible, pas de bon départ
- **Greedy** : temps réel, reproductibilité, simplicité

---

## Résumé

**Ce qui a été implémenté**
- Problème : placement d'antennes pour couvrir 45 maisons sur une grille 20×15, minimiser coût
- Solution : VNS avec 5 voisinages, initialisation greedy, recherche locale Best Improvement (N₁), 50 itérations (~8 s)

**Résultats typiques**
- Exécution : 5-10 s
- Solution typique : 11 antennes, 100% couverture, coût $12,500-$13,500
- Amélioration sur greedy : $15,500 → $12,500 (≈ 19.4%)

**Conclusions**
- VNS est un compromis pratique : systématique, interprétable, mémoire faible
- Meilleur que greedy, proche de GA en qualité, beaucoup plus rapide que GA

---

*Fini — contenu formaté en markdown en français, structure conservée.*

