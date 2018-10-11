
## Files
- SPINS Experimental Procedure.pdf states the procedure to run our experiment and output variables seen in the Z-Tree code
- ParseExperimentData.csv is the output file (all experiment data we use in our paper)

## Statistics
For our manuscript we use python code seen in the DataAnalysis section to record the following statistics:
- Number of Ties: node degree in one layer plus the node degree in the other
- Mean Clustering Coefficient: local clustering coefficient average over both layers. If a node has degree 0 or 1 in one layer, then clustering coefficient is defined as 0 in that layer.
- Fraction Spillover: 2*(number of ties that connect to the same nodes in both layers)/(number of ties). If the number of ties is 0, then this statistic is undefined and we therefore ignore this datapoint (this is rarely an issue in empirical data).
- Utility: Amount of utility subjects gain at the end of the period.

All data is recorded by period (a period is defined as a single player's turn), but in the manuscript we plot data by round, where a round is defined as the end of the 6th period, when everyone will have played once.

## Codebook for parsed data:

- ExperimentNumber: An ordinal number unique to each experiment

- NumberOfNodes: Number of nodes in each layer (6)

- TrainingPeriods: Number of training rounds (either 2 or 3)

- PreShockRounds: Either round 5 if there is a shock or 12 if there is not.

- TieBenefit: Tie benefit (1,0)

- TriangleBenefit: Triangle benefit (either 0.3 or 0.9)

- SpilloverBenefit: Spillover benefit (either 0.3 or 0.9)

- PreShockCost: Pre-shock tie cost (either 0.2 or 0.6)

- PostShockCost: Post-shock tie cost (either 0.2 or 0.6)

- NumberNodesShocked: Number of nodes shocked after round 5.

- PointsMultiplier: Multiply this value by profit to get the amount of money being gained by a player. The player receives a minimum of $10.

- Period: The period of time a single player can offer and drop ties.

- ActivePlayer: Whose turn it is this period.

- PlayerName: A unique identifier of each player in a given experiment (values 1-6)

- Shocked: Whether the node will be shocked after round 5

- Profit: Current profit per period

- PlayerConnectedToLayer-[layer]_Node[i]: The layer and neighboring node a PlayerName connects to (1 if there is a connection or 0 otherwise)

- gender: subject's gender (if stated)

- year: what year, e.g., freshman in college (if stated)

- major: what major if they are in college

