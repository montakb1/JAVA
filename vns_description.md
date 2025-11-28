Variable Neighborhood Search (VNS) for Antenna Placement Optimization
Problem Statement
Given:

A 20×15 grid representing a geographical area
45 houses randomly distributed (15% density)
Each house contains 100 users requiring cellular coverage
Three antenna types with different coverage radius and costs:

Small: radius = 2 cells, cost = $1,000
Medium: radius = 4 cells, cost = $1,500
Large: radius = 6 cells, cost = $2,000


Coverage is calculated using Euclidean distance
Antennas cannot be placed on houses

Goal:

Achieve 100% coverage (all 45 houses covered)
Minimize total deployment cost
Find optimal or near-optimal antenna placement

Why Variable Neighborhood Search?
Unlike genetic algorithms that maintain populations and use evolutionary operators, VNS systematically explores different neighborhood structures around a current solution. It escapes local optima by changing the search neighborhood rather than maintaining multiple solutions, making it more memory-efficient and often faster while still finding high-quality solutions.
How Variable Neighborhood Search Works
1. Solution Representation
Each solution is a list of antenna placements on our 20×15 grid:
pythonSolution = [
  {x: 5, y: 3, type: "Large", radius: 6, cost: 2000},
  {x: 12, y: 8, type: "Medium", radius: 4, cost: 1500},
  {x: 7, y: 14, type: "Small", radius: 2, cost: 1000}
]
Constraints:

Positions (x, y) must be within grid bounds: 0 ≤ x < 20, 0 ≤ y < 15
Cannot place antennas on house locations
Solution starts with greedy initialization for quality starting point

2. Neighborhood Structures (k-neighborhoods)
VNS Core Concept: Define multiple ways to modify a solution, ordered from small to large perturbations:
N₁ - Swap Antenna Type (Small perturbation):
pythonBefore: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Change one antenna's type
After:  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(7,14,Small)]
        ↑ Large → Medium (saves $500, may reduce coverage)
N₂ - Move Antenna (Medium perturbation):
pythonBefore: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Shift one antenna's position by 1-2 cells
After:  [Antenna(5,3,Large), Antenna(13,9,Medium), Antenna(7,14,Small)]
                              ↑ moved from (12,8) to (13,9)
N₃ - Replace Antenna (Medium-large perturbation):
pythonBefore: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Remove one antenna, add new one at random valid location
After:  [Antenna(5,3,Large), Antenna(18,6,Small), Antenna(7,14,Small)]
                              ↑ replaced (12,8,Medium) with (18,6,Small)
N₄ - Add/Remove Antenna (Large perturbation):
pythonBefore: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Remove antenna covering fewest houses OR add antenna for uncovered area
After:  [Antenna(5,3,Large), Antenna(12,8,Medium)]  ← removed least useful
N₅ - Swap Two Antennas (Large perturbation):
pythonBefore: [Antenna(5,3,Large), Antenna(12,8,Medium), Antenna(7,14,Small)]
Action: Exchange positions and types of two antennas
After:  [Antenna(12,8,Large), Antenna(5,3,Medium), Antenna(7,14,Small)]
        ↑ swapped first two antennas
Why Multiple Neighborhoods?

Small neighborhoods (N₁-N₂): Fine-tune good solutions (exploitation)
Large neighborhoods (N₄-N₅): Escape local optima (exploration)
Progressive search: Start small, go larger if stuck

3. Objective Function (Solution Quality)
How we evaluate each solution:
pythoncost = total_antenna_cost
penalty = uncovered_houses × penalty_weight
objective = cost + penalty  # MINIMIZE this value
```

**Breaking it down for our problem:**

1. **Total Cost** = sum of all antenna costs
   - Each Small antenna: $1,000
   - Each Medium antenna: $1,500
   - Each Large antenna: $2,000

2. **Coverage Penalty** = (45 - houses_covered) × 10,000
   - Uses Euclidean distance: √[(x₁-x₂)² + (y₁-y₂)²]
   - Penalty weight = $10,000 per uncovered house
   - Makes incomplete coverage extremely expensive

3. **Complete Objective**:
   - If all 45 houses covered: objective = cost only
   - If 43/45 houses covered: objective = cost + 2×$10,000 = cost + $20,000

**Example Calculation:**
- Solution with 10 antennas covering 43/45 houses, cost $13,000:
  - coverage_penalty = (45-43) × 10,000 = $20,000
  - objective = $13,000 + $20,000 = **$33,000** (BAD - incomplete coverage)

- Better solution with 11 antennas covering 45/45 houses, cost $14,000:
  - coverage_penalty = (45-45) × 10,000 = $0
  - objective = $14,000 + $0 = **$14,000** (GOOD - complete coverage)

**Why This Objective?**
- Strong pressure to achieve 100% coverage first
- Once coverage achieved, focuses purely on cost minimization
- Simple to compute and interpret

### 4. Local Search (Solution Improvement)

**Best Improvement Local Search** - Systematically improve within one neighborhood:
```
Given: Current solution, Current neighborhood Nₖ
Repeat until no improvement:
  1. Generate ALL possible neighbors in Nₖ
  2. Evaluate objective for each neighbor
  3. If best neighbor < current solution:
       → Accept best neighbor as new current
       → Continue searching
  4. Else:
       → Local optimum reached in Nₖ
       → Stop and return to VNS main loop
```

**What this means for our problem:**

**Example - Local Search in N₁ (Swap Type):**
```
Current: [Antenna(5,3,Large,$2000), Antenna(12,8,Medium,$1500)]
         Cost = $3,500, Coverage = 45/45, Objective = $3,500

Try all type swaps:
  Neighbor 1: [Antenna(5,3,Medium), Antenna(12,8,Medium)]
              Cost = $3,000, Coverage = 45/45, Objective = $3,000 ✓ BETTER!

  Neighbor 2: [Antenna(5,3,Small), Antenna(12,8,Medium)]
              Cost = $2,500, Coverage = 42/45, Objective = $32,500 ✗ WORSE

  Neighbor 3: [Antenna(5,3,Large), Antenna(12,8,Small)]
              Cost = $3,000, Coverage = 43/45, Objective = $23,000 ✗ WORSE

  Neighbor 4: [Antenna(5,3,Large), Antenna(12,8,Large)]
              Cost = $4,000, Coverage = 45/45, Objective = $4,000 ✗ WORSE

Accept Neighbor 1 (objective $3,000 < $3,500)
Continue local search from new solution...
```

**Why Best Improvement?**
- Explores all neighbors before moving (thorough)
- Guaranteed to find best move in neighborhood
- Slower than first improvement but finds better local optima
- Important for our problem where a bad move might lose coverage

### 5. Shaking (Perturbation)

**Random Neighbor Generation** - Escape local optima by making random changes:
```
Given: Current solution, Neighborhood structure Nₖ
Action:
  1. Generate ONE random neighbor using Nₖ transformation
  2. Do NOT evaluate or compare - just generate
  3. Return this random neighbor
```

**Why Shaking Matters:**
```
Scenario: Stuck at local optimum in N₁

Current solution (local optimum in N₁):
  [Antenna(5,3,Medium), Antenna(12,8,Medium), Antenna(18,14,Small)]
  Cost = $4,000, All houses covered

Cannot improve by swapping any single antenna type!

Shaking with N₂ (Move Antenna):
  Randomly move Antenna(12,8) to (11,7)
  New solution: [Antenna(5,3,Medium), Antenna(11,7,Medium), Antenna(18,14,Small)]

This perturbation might:
  - Still cover all houses (good)
  - Open opportunity for better type swap (now local search in N₁ can improve)
  - OR temporarily worsen solution (acceptable - we're exploring)
```

**Shaking Strategy for Our 45-House Problem:**
- Start with small perturbations (N₁) when solution is good
- Increase to larger perturbations (N₄, N₅) if stuck
- Random changes prevent cycling back to same local optimum
- Accept the shake unconditionally - we'll improve it with local search

## VNS Algorithm Structure

### The Complete VNS Loop
```
VNS Algorithm:
1. Generate initial solution (use greedy algorithm)
2. Set k = 1 (start with smallest neighborhood)
3. Repeat until stopping criterion (max iterations or time):

   SHAKING phase:
     4. Generate random neighbor x' in Nₖ(x)

   LOCAL SEARCH phase:
     5. Apply best improvement local search: x'' = LocalSearch(x', N₁)

   MOVE OR NOT:
     6. If objective(x'') < objective(x):
          → ACCEPT: x = x'', k = 1 (restart from smallest neighborhood)
       Else:
          → REJECT: k = k + 1 (try larger neighborhood)

   NEIGHBORHOOD CHANGE:
     7. If k > kₘₐₓ: k = 1 (wrap around to smallest neighborhood)

8. Return best solution found
Key VNS Principles:

Systematic Neighborhood Change:

Success → restart from N₁ (exploit good region)
Failure → increase to Nₖ₊₁ (explore further)


Local Search Always Uses N₁:

Shaking uses Nₖ (diversification)
Local search uses N₁ (intensification)
This combination balances exploration/exploitation


No Population Needed:

Single solution trajectory
Memory: O(number of antennas) vs O(population × antennas)
Simpler than genetic algorithm



Step-by-Step: How VNS Solves Our Problem
Initialization (Iteration 0)
Generate Initial Solution using Greedy Algorithm:
pythonGreedy Initialization:
1. Start with empty antenna set
2. While houses remain uncovered:
     - Find uncovered house
     - Try all (position, type) combinations
     - Pick antenna that covers most uncovered houses per dollar
     - Add to solution
3. Return as initial VNS solution

Initial Solution:
  Antennas: 12 antennas strategically placed
  Houses Covered: 45/45 (100%)
  Cost: $15,500
  Objective: $15,500 (no penalty since fully covered)
