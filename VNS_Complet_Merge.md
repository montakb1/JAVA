# Partie 1 — Problème & Objectif

Variable Neighborhood Search (VNS) pour l'optimisation du placement d'antennes

## Énoncé du problème
- Grille 20×15
- 45 maisons (15 %)
- 100 utilisateurs par maison
- Antennes : Petit, Moyen, Grand (rayons 2/4/6 – coûts 1000/1500/2000)

**Objectifs :**
- Couverture 100 %
- Minimisation du coût total
- Trouver un placement optimal ou quasi-optimal

## Pourquoi VNS ?
Méthode efficace, memory-friendly, meilleure exploration systématique que GA.

## Représentation de solution
Liste des antennes avec position (x, y), type, rayon et coût.

Contraintes :
- 0 ≤ x < 20, 0 ≤ y < 15
- Pas d’antenne sur les maisons
- Initialisation gloutonne pour une base de qualité


---

# Partie 2 — Structures de voisinage, Recherche locale, Shaking

## Voisinages N1 à N5
- **N1** : changer type
- **N2** : déplacer antenne
- **N3** : remplacer antenne
- **N4** : ajouter/supprimer antenne
- **N5** : échanger deux antennes

## Pourquoi plusieurs voisinages ?
- Petites variations : optimisation locale
- Grandes variations : échapper aux optima locaux

## Fonction objectif
`objectif = coût_total + pénalité`

- Pénalité = (maisons non couvertes × 10 000)
- Distance euclidienne

## Exemples explicatifs
- Solution complète : objectif = coût
- Solution partielle : objectif = coût + pénalités


---

# Partie 3 — Recherche locale, Shaking & Boucle VNS

## Recherche locale « Best Improvement »
1. Générer tous les voisins de Nₖ  
2. Évaluer chaque voisin  
3. Accepter le meilleur si amélioration  
4. Sinon → optimum local atteint  

Avantages :
- Trouve les meilleurs optima locaux
- Meilleure stabilité que First Improvement

## Shaking
Créer un voisin aléatoire dans Nₖ :
- Perturbe la solution courante
- Permet d’échapper aux optima locaux
- Toujours accepté avant recherche locale



---

# Partie 4 — Itérations, Paramètres & Analyse de performance

## Boucle VNS
1. Solution initiale (gloutonne)
2. k = 1
3. Shaking dans Nₖ
4. Recherche locale en N₁
5. Si amélioration → reset k = 1  
6. Sinon → k = k + 1  
7. Si k > k_max → retour à 1

## Résumé d’évolution
- Initial : 12 antennes, 15 500 $
- Optimisation : 11 antennes, 12 500 $

## Paramètres retenus
- 5 voisinages
- 50 itérations
- Pénalité = 10 000 $ par maison non couverte

## Performance
- ~250 000 opérations
- 5–10 secondes d’exécution


---

# Partie 5 — Analyse, Comparaison & Conclusion

## Analyse de performance
- VNS ≈ 3× plus rapide que GA
- Recherche locale = principale charge de calcul
- Mémoire très faible : ~1 Ko

## Avantages
- Exploration structurée
- Bonne qualité de solution
- Très efficace avec initialisation gloutonne

## Inconvénients
- Plus lent que greedy
- Pas de garantie d’optimalité globale
- Design des voisinages crucial

## Comparaisons
Greedy → rapide mais solutions limitées  
GA → qualité maximale mais lent  
VNS → meilleur compromis vitesse/qualité  

## Conclusion
VNS est une méthode puissante et équilibrée pour optimiser le placement d’antennes :  
✔ Bonne qualité  
✔ Rapidité raisonnable  
✔ Mémoire minimale  
✔ Processus interprétable  


---

