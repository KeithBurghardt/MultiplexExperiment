package agents;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;

public class Agent {
 public int index; //the agent's index, so that other agents can identify it. 
 double payoff; //the total payoff for the current round. 
 
 double tie_cost_1;
 double tie_cost_2;
 
 double util_before_turn=0; 
 double util=0; 
 double cumulativeUtil = 0; 
 
 //number of links added/dropped each round
 int num_links_added = 0;
 int num_links_dropped = 0;
 
 // are they an altruist
 boolean altruist = false;
 
 int[] neighbor_ids;
 
 /*
  * GET methods (for viz)
  */
 public double getutility (){    return util;        }
 
 /**
  * Constructor
  */
 public Agent(int i, double tc1, double tc2){
  index = i; 
  tie_cost_1 = tc1;
  tie_cost_2 = tc2;
  util = 0; 
  cumulativeUtil = 0; 
  num_links_added = 0;
  num_links_dropped = 0;
 }
 
 
 /**
  * Clustering coefficient for this agent/node in layer x
  * C = (2 * # triangles)/(k*(k-1)), where k is number of edges. 
  */
 public double clustering(AgentsSimulation as, int layer){
  double k = (double)numTies(as, layer);
  if(k < 2)
   return 0; 
  double tri = (double)numTriangles(as, layer);
  double c = (2 * tri)/(k*(k-1));
  return c;
 }
 
 
 /*
  * Return true if agent is fully connected (to all nodes in both layers), false otherwise. 
  */
 public boolean fullyConnected(AgentsSimulation as){
  //else if connected to everyone but shocked altruist
  boolean shocked_altruist = as.altruist && ((int)as.schedule.getSteps() >= as.timeOfShock);
  if(as.oneLayerOnly && numTies(as, 0) == as.NUM_PLAYERS - 1)
   return true;
  else if(as.oneLayerOnly && numTies(as, 0) == as.NUM_PLAYERS - 2 && shocked_altruist)
   return true;
  else if(!as.oneLayerOnly && numTies(as, 0)+numTies(as, 1) >= 2*(as.NUM_PLAYERS - 2) && shocked_altruist)
   return true;
  else if(numTies(as, 0) < as.NUM_PLAYERS - 1 || numTies(as, 1) < as.NUM_PLAYERS - 1)
   return false; 
  else return true; 
 }
 
 /*
  * Return true if agent is has no current ties, false otherwise. 
  */
 public boolean noTies(AgentsSimulation as){
  if(numTies(as, 0) > 0 || numTies(as, 1)  > 0)
   return false; 
  else return true; 
 }
 
 
 /**
  * Return the number of ties held in layer
  */
 public int numTies(AgentsSimulation as, int layer){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && as.coplayerMatrix[layer][this.index][i])
    count++;
  }
  return count;
 }
 /**
  * Return the number of ties held in layer for agent with index i
  */
 public int numTies(AgentsSimulation as, int j, int layer){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != j && as.coplayerMatrix[layer][j][i])
    count++;
  }
  return count;
 }
 /**
  * Return the number of ties held in layer, for a hypothetical adjacency matrix
  */
 public int numTies(AgentsSimulation as, int layer, boolean[][][] adjMatrix){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && adjMatrix[layer][this.index][i])
    count++;
  }
  return count;
 }
 
 /**
  * Return the number of closed triangles in layer
  */
 public int numTriangles(AgentsSimulation as, int layer){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && as.coplayerMatrix[layer][this.index][i]){
    for(int j = 0; j < as.NUM_PLAYERS; j++){
     if(j != i && j != this.index && 
       as.coplayerMatrix[layer][i][j] && as.coplayerMatrix[layer][this.index][j])
      count++;
    }
   }
  }
  count = (int)((double)count * 0.5); //correct for double-counting.
  return count;
 }
 
 /**
  * Return the number of closed triangles in layer, for a hypothetical adjacency matrix
  */
 public int numTriangles(AgentsSimulation as, int layer, boolean[][][] adjMatrix){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && adjMatrix[layer][this.index][i]){
    for(int j = 0; j < as.NUM_PLAYERS; j++){
     if(j != i && j != this.index && 
       adjMatrix[layer][i][j] && adjMatrix[layer][this.index][j])
      count++;
    }
   }
  }
  count = (int)((double)count * 0.5); //correct for double-counting.
  return count;
 }
 
 
 /**
  * Return the number of spillover ties (ties in both layers)
  */
 public int numSpilloverTies(AgentsSimulation as){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && as.coplayerMatrix[0][this.index][i] && as.coplayerMatrix[1][this.index][i])
    count++;
  }
  return count;
 }
 
 /**
  * Return the number of spillover ties (ties in both layers), for hypothetical adjacency matrix
  */
 public int numSpilloverTies(AgentsSimulation as, boolean[][][] adjMatrix){
  int count = 0; 
  for(int i = 0; i < as.NUM_PLAYERS; i++){
   if(i != this.index && adjMatrix[0][this.index][i] && adjMatrix[1][this.index][i])
    count++;
  }
  return count;
 }
 
 
 /**
  * Return the {layer, index, utility gain} of the agent of an edge not currently connected, 
  * whose addition would be result in the largest marginal utility gain. 
  * index = -1 if fully connected. 
  */
 public double[] bestAdd(AgentsSimulation as){ 
  if(fullyConnected(as)){//don't bother if agent is fully connected. 
   double[] temp =  {0.0, -1, 0};
   return temp;
  }
  //otherwise, find the highest utility add. 
  double currentUtil = as.currentUtility(this); //agent's current utility
  double[][] addUtil = as.addUtilities(this); //a matrix of all the new utilities after adds 
  double maxGain = -998;  
  int layer = as.random.nextInt(2); //start search at random layer
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force search to start at layer 0
  int i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  int bestIndex = i; 
  int bestLayer = layer;  
  int countLAYER = 0; 
  int countPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  while(countLAYER < max){
   while(countPLAYER < as.NUM_PLAYERS){
	boolean shocked_altruist = ((as.agentList[i].altruist||this.altruist) && (int)as.schedule.getSteps() >= as.timeOfShock);
    if(!shocked_altruist && i != this.index && !isTie(as, i, layer) && (addUtil[layer][i] - currentUtil) > maxGain ){ 
     maxGain = addUtil[layer][i] - currentUtil;
     bestIndex = i; 
     bestLayer = layer;
    }
    i++;
    if(i >= as.NUM_PLAYERS) i = 0;
    countPLAYER++;
   }
  layer = layer + 1 - 2*layer;
  countLAYER++;
  i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  countPLAYER = 0; 
  }
  double[] bestAdd = {bestLayer, bestIndex, maxGain};
  return bestAdd; 
 }
 
 /**
  * Return the {layer, index, utility gain} of the agent of an edge not currently connected, 
  * whose addition would be result in the largest marginal utility gain. 
  * index = -1 if fully connected. 
  * This one only searches among a list of searchSize randomly selected agents, rather than the entire population.
  */
 public double[] bestAdd(AgentsSimulation as, int searchSize){ 
  if(fullyConnected(as)){//don't bother if agent is fully connected. 
   double[] temp =  {0.0, -1, 0};
   return temp;
  }
  //otherwise, find the highest utility add. 
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  double currentUtil = as.currentUtility(this); //agent's current utility
  double[][] addUtil = as.addUtilities(this); //a matrix of all the new utilities after adds 
  double maxGain = -998;  
  int layer = as.random.nextInt(2); //start search at random layer
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force search to start at layer 0
  int i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  int init = i;
  int bestIndex = i; 
  int bestLayer = layer;  
  int countLAYER = 0; 
  int countPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  while(countLAYER < max){
   while(countPLAYER < searchSize){ //as.NUM_PLAYERS){
	boolean shocked_altruist = ((as.agentList[i].altruist||this.altruist) && (int)as.schedule.getSteps() >= as.timeOfShock);

    if(!shocked_altruist && i != this.index && !isTie(as, i, layer) && (addUtil[layer][i] - currentUtil) > maxGain){ 
     maxGain = addUtil[layer][i] - currentUtil;
     bestIndex = i; 
     bestLayer = layer;
    }
    i = as.random.nextInt(as.NUM_PLAYERS);
    if(i >= as.NUM_PLAYERS) i = 0;
    countPLAYER++;
   }
  layer = layer + 1 - 2*layer;
  countLAYER++;
  countPLAYER = 0; 
  }
  double[] bestAdd = {bestLayer, bestIndex, maxGain};
  return bestAdd; 
 }
 
 public double[] bestSmartSearchAdd(AgentsSimulation as, int searchSize){ 
  if(fullyConnected(as)){//don't bother if agent is fully connected. 
   double[] temp =  {0.0, -1, 0};
   return temp;
  }
  //otherwise, find the highest utility add. 
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  double currentUtil = as.currentUtility(this); //agent's current utility
  double[][] addUtil = as.addUtilities(this); //a matrix of all the new utilities after adds 
  double maxGain = -998;  
  int layer = as.random.nextInt(2); //start search at random layer
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force search to start at layer 0
  int i,j;
  int init;
  int bestIndex = as.random.nextInt(as.NUM_PLAYERS); 
  int bestLayer = layer;  
  int countLAYER = 0; 
  int countPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  // search in a "smart" way until we have looked at all neighbor's neighbors
  //We use the following variables to randomly select ties, and randomly select ties of ties
  Bag tie_i = new Bag(as.NUM_PLAYERS);
  Bag neighbors_tie_j = new Bag(as.NUM_PLAYERS);
  for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
  {
   tie_i.add(ii);
   neighbors_tie_j.add(ii);
  }
  //search through layers at random
  while(countLAYER < max)
  { //if the degree in this layer > 0 and we can still search
   if(numTies(as,layer) > 0 && countPLAYER < searchSize)
   {
    //shuffle ties
    tie_i.shuffle(as.random);
    for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
    {
     //get random tie
     i = (int) tie_i.get(ii);
     // if i is not you, i is a neighbor and we can still look
     if(i != this.index && isTie(as,i,layer)&& countPLAYER < searchSize)
     { 
      // check spillover or triangle benefits with equal probability
      int spillover_or_tri = as.random.nextInt(2);
      
      // check if spillover is a good idea
      if(spillover_or_tri==1)
      {
       //opposite layer
       int Opplayer = layer+1-2*layer;
       // if there is not a tie on the opposite layer 
       // and adding a tie on the opposite later is the best idea yet...
       if(!isTie(as, i,Opplayer ) && (addUtil[Opplayer][i] - currentUtil) > maxGain)
       {
        // find node name j associated with tie i<->j
        maxGain = addUtil[Opplayer][i] - currentUtil;
        bestIndex = i; 
        bestLayer = Opplayer;
       }
       // search size reduced by 1
       countPLAYER++;
      }
      //check if we can make triangle benefits
      else
      {
       
       if (numTies(as,layer) > 1)
       {
        // look at neighbor's neighbors, if they exist, 
        neighbors_tie_j.shuffle(as.random);
        for(int jj=0; jj < as.NUM_PLAYERS; jj++)
        { // select random neighbor's tie
         j = (int) neighbors_tie_j.get(jj);
         // if j is not the tie i
         // and j is not you, and j is one of i's neighbors: (You)---(i)---(j!=you)
         // and you can still search
         if(  j != i 
           && j != this.index 
           && as.coplayerMatrix[layer][i][j] 
           && countPLAYER < searchSize)
         {
         
          //if there is not a tie to j already, and adding creates a greater utility
          if(!isTie(as, j, layer) && (addUtil[layer][j] - currentUtil) > maxGain)
          {  
           maxGain = addUtil[layer][j] - currentUtil;
           bestIndex = j; 
           bestLayer = layer;
          }
          
          countPLAYER++;
         
         }
        }
       }
      }
     }
    }
   }
  // layer = layer + 1 - 2*layer;
  // countLAYER++;
  //} 
  // if we still have neighbor's neighbors to look through, look at random
      if (countPLAYER < searchSize)
      {
       
    //search at random
    tie_i.shuffle(as.random);
    for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
    {
     //if we can still search
     if (countPLAYER < searchSize)
     {
      //get random tie
      i = (int) tie_i.get(ii);
      //if i is not you and we do not have a tie to i
      // and we have a new highest utility
      if(i != this.index && !isTie(as, i, layer) && (addUtil[layer][i] - currentUtil) > maxGain){ 
       maxGain = addUtil[layer][i] - currentUtil;
       bestIndex = i; 
       bestLayer = layer;
      }
      countPLAYER++;
     }
     
    }
      }
    // we exhausted our search on this layer
    // move to the next layer, and reset the player search number
   layer = layer + 1 - 2*layer;
   countLAYER++;
   countPLAYER = 0; 
   
   
  }
  double[] bestAdd = {bestLayer, bestIndex, maxGain};
  return bestAdd; 
 }
 
 
 /**
  * Return the {layer, index, utility gain} of the agent of an edge currently connected, 
  * whose deletion would be result in the largest marginal utility gain. 
  * Index = -1 if no current edges
  */
 public double[] bestDrop(AgentsSimulation as){  
  if(noTies(as)){//don't bother if agent no current edges. 
   double[] temp =  {0.0, -1, 0};
   return temp;
  }
  //otherwise, find the highest utility drop. 
  double currentUtil = as.currentUtility(this); //agent's current utility
  double[][] lossUtil = as.lossUtilities(this); //a matrix of all the new utilities after losses
  
  double maxGain = -998; 
  
  int layer = as.random.nextInt(2); //start search at random layer
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force search to start at layer 0
  int i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  int bestIndex = i; 
  int bestLayer = layer;  
  int countLAYER = 0; 
  int countPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  while(countLAYER < max){
   while(countPLAYER < as.NUM_PLAYERS){
    if(i != this.index && isTie(as, i, layer) && (lossUtil[layer][i] - currentUtil) > maxGain){
     maxGain = lossUtil[layer][i] - currentUtil;
     bestIndex = i; 
     bestLayer = layer;
    }
    i++;
    if(i >= as.NUM_PLAYERS) i = 0;
    countPLAYER++;
   }
  layer = layer + 1 - 2*layer;
  countLAYER++;
  i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  countPLAYER = 0; 
  }
  double[] bestDrop = {bestLayer, bestIndex, maxGain};
  return bestDrop; 
 }
 
 public double[] bestDrop(AgentsSimulation as, int searchSize){  
  if(noTies(as)){//don't bother if agent no current edges. 
   double[] temp =  {0.0, -1, 0};
   return temp;
  }
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  //otherwise, find the highest utility drop. 
  double currentUtil = as.currentUtility(this); //agent's current utility
  double[][] lossUtil = as.lossUtilities(this); //a matrix of all the new utilities after losses
  
  double maxGain = -998; 
  
  int layer = as.random.nextInt(2); //start search at random layer
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force search to start at layer 0
  int i = as.random.nextInt(as.NUM_PLAYERS); //start search at random player
  int bestIndex = i; 
  int bestLayer = layer;  
  int countLAYER = 0; 
  int countPLAYER = 0; 
  int max = 2;
  Bag tie_i = new Bag(as.NUM_PLAYERS);
  for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
  {
   tie_i.add(ii);
  }
  if(as.oneLayerOnly) max = 1;
  while(countLAYER < max)
  {
   //while(countPLAYER < searchSize){
    tie_i.shuffle(as.random);
    for(int ii = 0; ii<as.NUM_PLAYERS;ii++)
    {
     i = (int) tie_i.get(ii);
     if(i != this.index && isTie(as, i, layer) && countPLAYER < searchSize)
     {
      if((lossUtil[layer][i] - currentUtil) > maxGain){ 
       maxGain = lossUtil[layer][i] - currentUtil;
       bestIndex = i; 
       bestLayer = layer;
      }
      countPLAYER++;
     }
    }
   //}
   //move to next layer
   layer = layer + 1 - 2*layer;
   countLAYER++;
   // reset the number of people to look over
   countPLAYER = 0; 
  }
  double[] bestDrop = {bestLayer, bestIndex, maxGain};
  return bestDrop; 
 }
  
 /**
  * Considers each combination of adding and dropping ties, and returns the one with highest marginal utility. 
  * Returns double[2][3]: [0=add, 1=drop][0=layer, 1=index, 2=utility gain]
  */
 public double[][] bestAddDropCombo(AgentsSimulation as){  
  if(this.noTies(as) || this.fullyConnected(as)){//don't bother if agent no current edges or fully connected
   double[][] temp =  {{0.0, -1, 0}, {0.0, -1, 0}};
   return temp;
  }
  double currentUtil = as.currentUtility(this); //agent's current utility
  double maxGain = -998;  
  int addLayer = as.random.nextInt(2); //to get next, i = i + 1 - 2*i (starts search at random layer)
  int dropLayer = as.random.nextInt(2);
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly){
   addLayer = 0; 
   dropLayer = 0; 
  }   
  int i = as.random.nextInt(as.NUM_PLAYERS); //start add search at random player
  int j = as.random.nextInt(as.NUM_PLAYERS); //start drop search at random player
  int bestAddIndex = i; 
  int bestAddLayer = addLayer; 
  int bestDropIndex = j; 
  int bestDropLayer = dropLayer; 
  int addCount = 0; 
  int dropCount = 0;  
  int countADDPLAYER = 0; 
  int countDROPPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  while(addCount < max){//start at random layer   
   while(countADDPLAYER < as.NUM_PLAYERS){//loop over potential adds
	boolean shocked_altruist = ((as.agentList[i].altruist||this.altruist) && (int)as.schedule.getSteps() >= as.timeOfShock);
	    
    if(i != this.index && !isTie(as, as.agentList[i], addLayer) && !shocked_altruist){ //if no tie exists      
     Agent addAgent = as.agentList[i];     
     while(dropCount < max){ //loop over potential deletes
      while(countDROPPLAYER < as.NUM_PLAYERS){
       if(j != this.index && j != i && isTie(as, as.agentList[j], dropLayer)){ //if tie exists
        Agent dropAgent = as.agentList[j];
        double newUtil = as.utilityIfAddDrop(this, addAgent, dropAgent, addLayer, dropLayer);
        double gain = newUtil - currentUtil;
        if(gain > maxGain){//if best move
         maxGain = gain;
         bestAddIndex = i; 
         bestAddLayer = addLayer; 
         bestDropIndex = j; 
         bestDropLayer = dropLayer; 
        } 
       }
       j++;
       if(j >= as.NUM_PLAYERS) j = 0;
       countDROPPLAYER++; 
      }
      dropLayer = dropLayer + 1 - 2*dropLayer;
      dropCount++; 
      countDROPPLAYER = 0;
      j = as.random.nextInt(as.NUM_PLAYERS);
     }
    }
    i = as.random.nextInt(as.NUM_PLAYERS);
    if(i >= as.NUM_PLAYERS) i = 0;
    countADDPLAYER++;    
   }
   addLayer = addLayer + 1 - 2*addLayer;
   addCount++;
   countADDPLAYER = 0;
   i = as.random.nextInt(as.NUM_PLAYERS); 
  }
  double[][] bestAddDrop = {{bestAddLayer, bestAddIndex, maxGain}, {bestDropLayer, bestDropIndex, maxGain}};
  return bestAddDrop; 
 }
 
 
 /**
  * Considers each combination of adding and dropping ties, and returns the one with highest marginal utility. 
  * Returns double[2][3]: [0=add, 1=drop][0=layer, 1=index, 2=utility gain]
  */
 public double[][] bestAddDropCombo(AgentsSimulation as, int searchSize){  
  if(this.noTies(as) || this.fullyConnected(as)){//don't bother if agent no current edges or fully connected
   double[][] temp =  {{0.0, -1, 0}, {0.0, -1, 0}};
   return temp;
  }
  
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  double currentUtil = as.currentUtility(this); //agent's current utility
  double maxGain = -998;  
  int addLayer = as.random.nextInt(2); //to get next, i = i + 1 - 2*i (starts search at random layer)
  int dropLayer = as.random.nextInt(2);
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly){
   addLayer = 0; 
   dropLayer = 0; 
  }   
  int i = as.random.nextInt(as.NUM_PLAYERS); //start add search at random player
  int j = as.random.nextInt(as.NUM_PLAYERS); //start drop search at random player
  int bestAddIndex = i; 
  int init_i = i;
  int init_j = j;
  int bestAddLayer = addLayer; 
  int bestDropIndex = j; 
  int bestDropLayer = dropLayer; 
  int addCount = 0; 
  int dropCount = 0;  
  int countADDPLAYER = 0; 
  int countDROPPLAYER = 0; 
  int max = 2;
  if(as.oneLayerOnly) max = 1;
  while(addCount < max){//start at random layer   
   while(countADDPLAYER < searchSize){ //as.NUM_PLAYERS){//loop over potential adds
    boolean shocked_altruist = ((as.agentList[i].altruist||this.altruist) && (int)as.schedule.getSteps() >= as.timeOfShock);

    if(i != this.index && !isTie(as, as.agentList[i], addLayer) && !shocked_altruist){ //if no tie exists      
     Agent addAgent = as.agentList[i];     
     while(dropCount < max){ //loop over potential deletes
      while(countDROPPLAYER < searchSize){ //as.NUM_PLAYERS){
       if(j != this.index && j != i && isTie(as, as.agentList[j], dropLayer)){ //if tie exists
        Agent dropAgent = as.agentList[j];
        double newUtil = as.utilityIfAddDrop(this, addAgent, dropAgent, addLayer, dropLayer);
        double gain = newUtil - currentUtil;
        if(gain > maxGain){//if best move
         maxGain = gain;
         bestAddIndex = i; 
         bestAddLayer = addLayer; 
         bestDropIndex = j; 
         bestDropLayer = dropLayer; 
        } 
       }
       j = as.random.nextInt(as.NUM_PLAYERS);
       //if(j >= as.NUM_PLAYERS) j = 0;
       countDROPPLAYER++; 
      }
      dropLayer = dropLayer + 1 - 2*dropLayer;
      dropCount++; 
      countDROPPLAYER = 0;
      //j = as.random.nextInt(as.NUM_PLAYERS);
      j = init_j; //search same set of nodes each time. 
     }
    }
    i = as.random.nextInt(as.NUM_PLAYERS);
    //if(i >= as.NUM_PLAYERS) i = 0;
    countADDPLAYER++;    
   }
   addLayer = addLayer + 1 - 2*addLayer;
   addCount++;
   countADDPLAYER = 0;
   //i = as.random.nextInt(as.NUM_PLAYERS); 
   i = init_i; //search the same set of agents each time
  }
  double[][] bestAddDrop = {{bestAddLayer, bestAddIndex, maxGain}, {bestDropLayer, bestDropIndex, maxGain}};
  return bestAddDrop; 
 }
 
 /**
  * Considers each combination of adding and dropping ties, and returns the one with highest marginal utility. 
  * Returns double[2][3]: [0=add, 1=drop][0=layer, 1=index, 2=utility gain]
  */
 public double[][] bestSmartSearchAddDropCombo(AgentsSimulation as, int searchSize){  
  if(this.noTies(as) || this.fullyConnected(as)){//don't bother if agent no current edges or fully connected
   double[][] temp =  {{0.0, -1, 0}, {0.0, -1, 0}};
   return temp;
  }
  // if our search size is too large, set to the number of players
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  double currentUtil = as.currentUtility(this); //agent's current utility
  double maxGain = -998;  
  int addLayer = as.random.nextInt(2); //to get next, i = i + 1 - 2*i (starts search at random layer)
  int dropLayer = as.random.nextInt(2);
  if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly){
   addLayer = 0; 
   dropLayer = 0; 
  }   
  int i = as.random.nextInt(as.NUM_PLAYERS); //start add search at random player
  int j = as.random.nextInt(as.NUM_PLAYERS); //start drop search at random player
  int k = as.random.nextInt(as.NUM_PLAYERS); //start drop search at random player
  int Opplayer = 0;
  int bestAddIndex = i; 
  int init_i = i;
  int init_j = j;
  int bestAddLayer = addLayer; 
  int bestDropIndex = j; 
  int bestDropLayer = dropLayer; 
  int addCount = 0; 
  int dropCount = 0;  
  int countADDPLAYER = 0; 
  int countDROPPLAYER = 0; 
  int max = 2;
  Bag tie_i = new Bag(as.NUM_PLAYERS);
  Bag neighbors_tie_j = new Bag(as.NUM_PLAYERS);
  Bag delete_tie_k = new Bag(as.NUM_PLAYERS);
  for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
  {
   tie_i.add(ii);
   neighbors_tie_j.add(ii);
   delete_tie_k.add(ii);
  }
  // first try to add ties among next-to-nearest neighbors
  if(as.oneLayerOnly) max = 1;
  while(addCount < max){//start at random layer 
   // if the degree > 0 (allowing us to actually delete ties)
   if(numTies(as,addLayer) > 0 && countADDPLAYER < searchSize)
   {
    // look through random ties
    tie_i.shuffle(as.random);
    for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
    {
     // pick a random potential tie
     i = (int) tie_i.get(ii);
     // if not you and is a neighbor and we can still potentially add ties
     if(i != this.index && isTie(as,i,addLayer)&& countADDPLAYER < searchSize)
     {
      // check spillover or triangle benefits with equal probability
      int spillover_or_tri = as.random.nextInt(2);
      // check if spillover is a good idea
      if(spillover_or_tri==1)
      {
        //Spillover: look at opposite layer
        Opplayer = addLayer+1-2*addLayer;
        //if there is not a tie to i in the opposite later
        if(!isTie(as, i, Opplayer)){ 
         
         Agent addAgent = as.agentList[i];   
         //Drop agent code:
         //look through random layers
         while(dropCount < max){ //loop over potential deletes
          delete_tie_k.shuffle(as.random);
          for(int kk=0; kk<as.NUM_PLAYERS;kk++)
          { // pick a random node
           k = (int) delete_tie_k.get(kk);
           // if we can still look through people to drop
           if(countDROPPLAYER < searchSize){ //as.NUM_PLAYERS){
            // if k is not you and is a tie
            if(k != this.index && isTie(as, k, dropLayer)){ //if tie exists
             Agent dropAgent = as.agentList[k];
             //determine the utility of adding and dropping
             double newUtil = as.utilityIfAddDrop(this, addAgent, dropAgent, addLayer, dropLayer);
             double gain = newUtil - currentUtil;
             if(gain > maxGain){//if best move
              maxGain = gain;
              bestAddIndex = i; 
              bestAddLayer = addLayer; 
              bestDropIndex = k; 
              bestDropLayer = dropLayer; 
             } 
             countDROPPLAYER++;
            } 
           }
          }
          dropLayer = dropLayer + 1 - 2*dropLayer;
          dropCount++; 
          countDROPPLAYER = 0;
         }
        }
        countADDPLAYER++;
       
      }
      //check if we can make triangle benefits
      else
      {
       // if our degree is > 1
       if (numTies(as,addLayer) > 1)
       {
        // look at neighbor's neighbors, if they exist, 
        for(int jj=0; jj < as.NUM_PLAYERS; jj++)
        {
         //pick a random candidate for a neighbor's tie
         j = (int) neighbors_tie_j.get(jj);
         // make sure the candidate is not the neighbor
         // and j is not you
         if(j != i && j != this.index)
         {
          // if  j is not tied to you
          // and j is one of i's neighbors: (You)---(i)---(j!=you)
          // and we can still search
          if(  !isTie(as, j, addLayer) 
            && as.coplayerMatrix[addLayer][j][i]
            && countADDPLAYER < searchSize)
          {
           Agent addAgent = as.agentList[j];
           // look through random layers to drop
           while(dropCount < max){ //loop over potential deletes
            delete_tie_k.shuffle(as.random);
            for(int kk=0; kk<as.NUM_PLAYERS;kk++)
            { //if we can still look for ties to drop
             if(countDROPPLAYER < searchSize){ //as.NUM_PLAYERS){
              // if k is not you and there is a tie from you to k
              if(k != this.index && isTie(as, k, dropLayer)){
               
               Agent dropAgent = as.agentList[k];
               double newUtil = as.utilityIfAddDrop(this, addAgent, dropAgent, addLayer, dropLayer);
               double gain = newUtil - currentUtil;
               if(gain > maxGain){//if best move
                maxGain = gain;
                bestAddIndex = i; 
                bestAddLayer = addLayer; 
                bestDropIndex = k; 
                bestDropLayer = dropLayer; 
               } 
               countDROPPLAYER++;
              } 
             }
            }
            dropLayer = dropLayer + 1 - 2*dropLayer;
            dropCount++; 
            countDROPPLAYER = 0;
            //j = as.random.nextInt(as.NUM_PLAYERS);
            j = init_j; //search same set of nodes each time. 
           }
          
           countADDPLAYER++;
          }
         }
        }
       }
      }
     }
    }
   }
   
  // addLayer = addLayer + 1 - 2*addLayer;
  // addCount++;
  // countADDPLAYER = 0;
   //i = as.random.nextInt(as.NUM_PLAYERS); 
  // i = init_i; //search the same set of agents each time
  //}
  // if we have not yet looked at all possible ties...
  //while(addCount < max){//start at random layer 
   tie_i.shuffle(as.random);
   for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
   {
    if(countADDPLAYER < searchSize)
    {
     i = (int) tie_i.get(ii);
     // if the new tie's neighbor (i) is not you
     // and if there is not a tie from you to i yet
     if(i != this.index && !isTie(as, i, addLayer)){ //if no tie exists      
      Agent addAgent = as.agentList[i];     
      while(dropCount < max)
      { //loop over potential deletes 
       delete_tie_k.shuffle(as.random);
       for(int jj = 0; jj < as.NUM_PLAYERS; jj++)
       {
        if(countDROPPLAYER < searchSize)
        {
         // pick a random potential tie
         j = (int) delete_tie_k.get(jj);
         // if j is not you or i (the tie to be added)
         // and if there is a tie to j (to be dropped)
         if(j != this.index && j != i && isTie(as, j, dropLayer)){ //if tie exists
          Agent dropAgent = as.agentList[j];
          double newUtil = as.utilityIfAddDrop(this, addAgent, dropAgent, addLayer, dropLayer);
          double gain = newUtil - currentUtil;
          if(gain > maxGain){//if best move
           maxGain = gain;
           bestAddIndex = i; 
           bestAddLayer = addLayer; 
           bestDropIndex = j; 
           bestDropLayer = dropLayer; 
          } 
          countDROPPLAYER++; 
         }
        }
       }
       dropLayer = dropLayer + 1 - 2*dropLayer;
       dropCount++; 
       countDROPPLAYER = 0;
      }
     }
    countADDPLAYER++; 
    }
   }

   // go to the next layer. we exhausted looking through this later
   addLayer = addLayer + 1 - 2*addLayer;
   addCount++;
   // set "number searched on this layer" to 0
   countADDPLAYER = 0;
  }
  // return best values
  double[][] bestAddDrop = {{bestAddLayer, bestAddIndex, maxGain}, {bestDropLayer, bestDropIndex, maxGain}};
  return bestAddDrop; 
 }
  
 /**
  * Checks to see if adding a tie is beneficial to the other agent. 
  * If so, adds the tie and returns true. 
  * May also randomly accept tie with probability noise. 
  * Otherwise, returns false. 
  */
 public boolean addTie(AgentsSimulation as, double[] bestAdd){
  Agent b = as.agentList[(int)bestAdd[1]];
  int layer = (int)bestAdd[0];
  double otherCurrentUtil = as.currentUtility(b); // partner's current utility
  double otherNewUtil = as.utilityIfAdded(b, this, layer);
  // add if altruist (but not shocked altruist) or improves utility, or noisy add
  //if increases utility or by chance or b is an altruist (always accept ties), assuming no shock
  if((otherNewUtil > otherCurrentUtil || as.random.nextBoolean(as.noise)||b.altruist)){ 
   as.tie_form(this, b, layer);
   this.num_links_added ++;
   return true;
  }
  else return false;
 }
 
 /**
  * Checks to see if adding a tie is beneficial to the other agent. 
  * If so, adds the tie and returns true. 
  * May also randomly accept tie with probability noise.
  * Otherwise, returns false. 
  */
 public boolean addTie(AgentsSimulation as, Agent other, int layer){
  double otherCurrentUtil = as.currentUtility(other); // partner's current utility
  double otherNewUtil = as.utilityIfAdded(other, this, layer);
  //add if altruist (but not shocked altruist) or improves utility, or noisy add
  if((otherNewUtil > otherCurrentUtil || as.random.nextBoolean(as.noise)||other.altruist)){ //if increases utility or by chance
   as.tie_form(this, other, layer);
   this.num_links_added ++;
   return true;
  }
  else return false;
 }
 /**
  * Checks to see if rewiring after adding a tie is beneficial to the other agent. 
  * If so, adds the tie and returns true. 
  * May also randomly accept tie with probability noise.
  * Otherwise, returns false. 
  */
 public boolean is_altruist_neighbor(Agent a, Agent other){
	 if(!a.altruist)
		 return true;
	 
	 for(int i =0; i<a.neighbor_ids.length;++i){
		 if (other.index==a.neighbor_ids[i]){
			 return true;
		 }
	 }
	 return false;
 }
 public boolean addRewire(AgentsSimulation as, Agent other, int addLayer,int searchSize){
  // if our search size is too large, set to the number of players
  if(searchSize > as.NUM_PLAYERS)
   searchSize = as.NUM_PLAYERS;
  double otherCurrentUtil = as.currentUtility(other); // partner's current utility
  double otherAddUtil = as.utilityIfAdded(other, this, addLayer);
  //other always wants to form connection! (if not shocked)
  boolean not_shocked = (int)as.schedule.getSteps() < as.timeOfShock;
  // conditions if neighbor is an altruist
  if(other.altruist){
	  if(other.is_altruist_neighbor(other,this) && not_shocked){
		  otherAddUtil=99999;
	  }else{// if not altruist neighbor OR shocked, don't connect
		  otherAddUtil=-99999;
	  }
  }
  double otherRewireUtil = -999;
  int i;
  int max = 2;
  int dropLayer = as.random.nextInt(2);
  int dropCount = 0;
  int countDROPPLAYER = 0;
  Agent dropAgent = as.agentList[as.random.nextInt(as.NUM_PLAYERS)];
  
  //used to pick agents at random
  Bag delete_tie_i = new Bag(as.NUM_PLAYERS);
  for(int ii = 0; ii < as.NUM_PLAYERS; ii++)
  {
   delete_tie_i.add(ii);
  }
  // across all layers (picked at random)
  if(as.oneLayerOnly) max = 1;
  while(dropCount < max)
  {
   //shuffle agent order
   delete_tie_i.shuffle(as.random);
   for(int ii = 0; ii < as.NUM_PLAYERS;++ii)
   { //random agent
    i = (int) delete_tie_i.get(ii);
    // if we can still look 
    // agent is not neighbor
    //and agent is a neighbor on this layer
    if(countDROPPLAYER < searchSize && other.index!=i && as.coplayerMatrix[dropLayer][other.index][i])
    {
     
     //if dropping this agent increases utility the most
     if(as.utilityIfAddDrop(other, this, as.agentList[i], addLayer, dropLayer)>otherRewireUtil)
     { 
      // record this agent
      dropAgent = as.agentList[i];
      //record top utility
      otherRewireUtil = as.utilityIfAddDrop(other, this, dropAgent, addLayer, dropLayer);
     }
     countDROPPLAYER++;
    }
   }
   // go to the next layer. we exhausted looking through this later
   dropLayer = dropLayer + 1 - 2*dropLayer;
   dropCount++;
   // set "number searched on this layer" to 0
   countDROPPLAYER = 0;
  }
  boolean NoisyAdd = as.random.nextBoolean(as.noise);
  if(other.is_altruist_neighbor(other,this) && not_shocked){
  if(otherAddUtil > otherCurrentUtil||otherRewireUtil > otherCurrentUtil || NoisyAdd){ //if increases utility or by chance
   if(NoisyAdd || otherAddUtil > otherRewireUtil)
   {
    as.tie_form(this, other, addLayer);
    this.num_links_added++;
    return true;
   }
   else if(otherRewireUtil > otherAddUtil)
   {
    as.tie_form(this, other, addLayer);
    this.num_links_added++;
    as.tie_delete(other, dropAgent, dropLayer);
    this.num_links_dropped++;
    return true;
   }
  }
  }
  //else return false;
  return false;
 }
 
 /**
  * Drops the tie 
  */
 public void dropTie(AgentsSimulation as, double[] bestDrop){
  Agent b = as.agentList[(int)bestDrop[1]];
  int layer = (int)bestDrop[0];
  as.tie_delete(this, b, layer);
  this.num_links_dropped ++;
 }
 
 /**
  * Returns true if an edge exists, otherwise return false. 
  */
 public boolean isTie(AgentsSimulation as, Agent other, int layer){
  return as.coplayerMatrix[layer][this.index][other.index]; 
 }
 
 /**
  * Returns true if an edge exists, otherwise return false. 
  */
 public boolean isTie(AgentsSimulation as, int index, int layer){
  return as.coplayerMatrix[layer][this.index][index]; 
 }

 
}//end class