# MultiplexExperiment
This repository contains data and the code used to create and analyze data for the manuscript: 

Daniel J. Simmons, Keith Burghardt, Luba Levin-Banchik, Michelle C. Phillips, and Zeev Maoz. "Exploration and Exploitation in Dynamic Cooperative Multiplex Networks" (in submission) (2018)

Analysis code by Keith Burghardt and z-Tree code is by Daniel J. Simmons, Keith Burghardt, and Michelle C. Phillips

The repository in GitHub is split into 4 parts: experiment code, simulations, data analysis, and data/protocol

- "Z-Tree Files": experiments are performed using custom-written z-Tree code. This code is based on code from the paper: Martijn J Burger and Vincent Buskens. "Social context and network formation: An experimental study" Social Networks 31(1):63–75 (2009)

- "Simulations": Agent-Based Models discussed in the paper are written in Java code and based on code by Paul Smaldino (https://www.comses.net/codebases/5148/releases/1.0.0/). Description of simulation algorithm is in our forthcoming paper.

- "DataAnalysis": for data analysis, we use python code that reads in the raw output of the simulation and experiment data and spits out a user-friendly .csv file

- "Data and Protocol": shows the data and associated protocol we use to run our experiments
