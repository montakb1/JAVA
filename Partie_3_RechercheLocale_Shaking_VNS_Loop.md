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

