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
