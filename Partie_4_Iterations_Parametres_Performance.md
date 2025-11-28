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
