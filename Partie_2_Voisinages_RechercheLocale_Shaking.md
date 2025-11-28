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
