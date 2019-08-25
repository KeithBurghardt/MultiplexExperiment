package agents;

import java.util.Collections;

import sim.engine.Steppable;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.IntBag;

public class GamePlayerAgent implements Steppable {
	public Stoppable event;
	public boolean preShockStability; //boolean switches to only record time to stability once. 
	public boolean postShockStability;
	
	public GamePlayerAgent(){
		preShockStability = false;
		postShockStability = false;
	}
	
	
	/*********************** STEP METHOD ***************************************************************/
	public synchronized void step(SimState state){
		AgentsSimulation as = (AgentsSimulation)state;
		int steps = (int)as.schedule.getSteps();
		/*
		 * 0. If time for a shock, shock the system (increase tie costs)
		 * 1. Check for pairwise stability (Stopping condition)
		 * 2. Shuffle order of agents
		 * 3. Step through each agent. 
		 * 		i) If noise, agent selects nodes to add/drop at random. 
		 * 		ELSE	Each agent can add one edge and drop one edge
		 * 		iia)  	If adding an edge adds utility, agent proposes to best partner. If agree, edge forms. 
		 * 		iib)	If dropping a (different) edge adds utility, agent drops edge. 
		 * 		iii) 	Else, agent considers all add-drop pairs. If one increases utility, agent
		 * 				proposes to (best) partner. If add is made, drop associated edge. 
		 *  	iv) 	Else, agent drops an existing edge if it will increase utility (chooses biggest increase)
		 */
		
		//Print average degrees
		if(as.doGraphics){
			if(as.schedule.getSteps() < 1)
				System.out.println("time" + "\t" + "degree1" + "\t" + "degree2" + "\t" + "degreetot");
			double[] deg = as.averageDegree();
			System.out.println(as.schedule.getSteps() + "\t" + deg[0] + "\t" + deg[1] + "\t" + deg[2]);
		}
		
		if(as.shockAtEquilibrium && as.noise == 0 && as.pairwiseStability()){ //Check if the network hasn't changed. 
			
			if(as.preshock){
				as.shockNetwork();
				as.preshock = false; 
				as.equilTime1 = steps;
			}
			else if (!as.preshock && steps > as.equilTime1 + 2){
				as.timeToEnd = true; 
				as.equilTime2 = steps;
			}
			
		
			int timeStable = steps - 2; //
			if(steps <= as.timeOfShock && !preShockStability){
				as.equilTime1 = timeStable;
				preShockStability = true;
			}
			else if(steps > (as.timeOfShock) && !postShockStability){
				as.equilTime2 = timeStable;
				postShockStability = true;
			}	
		}
		// "if noise, shock network?" Does not seem to be reasonable, therefore we removed this
		if(/*(!as.shockAtEquilibrium || as.noise > 0) &&*/ (int)as.schedule.getSteps() == as.timeOfShock)//SHOCK!!!
			as.shockNetwork();	
		
		//ALL AGENTS HAVE OPPORTUNITY TO ADD/DELETE TIES
		Agent[] agentListShuf = as.shuffle(as.agentList);//Shuffle the order of agents
		for(int i = 0; i < agentListShuf.length; i++){//Step through each agent: 
			Agent a = agentListShuf[i];			
			a.util_before_turn=as.currentUtility(a);
			
			//Record this before each turn:
			//float turn_util = a.util_before_turn;
			//float others_util = 
			//if(as.random.nextBoolean(as.noise)){ //If noise, add/drop random ties
			//	randomTie(a, as);				
			//}
			//else{ //otherwise, add/drop strategically
				//int MAX_ITER = as.NUM_PLAYERS;
				int num_layers = 2;
				if(as.oneLayerOnly)
					num_layers = 1;
				int counter = 0;
				boolean add = false;//add a new tie? 
				double[]  bestAdd= a.bestAdd(as, as.searchSize);//AddSmartSearch//return [layer, index, gain] of best possible add. index = -1 if fully connected		
				double[] bestDrop = a.bestDrop(as);//, as.searchSize);//return [layer, index, gain] of best possible drop. index = -1 if no current ties
				// Best add-drop combo with a smart search
				//double[][] bestADCombo = a.bestAddDropCombo(as, as.searchSize);//;
				double[] OldBestDrop = bestDrop;
				//for(int k=0; k<2*as.NUM_PLAYERS; k++)//do this set of calculations over and over
				boolean no_links_changed_twice = true;
				// no link will ever change twice, so we add a separate constraint
				//if (as.noise == 0.0)
				///	no_links_changed_twice = false;
				int [][] link_changed;
				link_changed = new int[num_layers][as.NUM_PLAYERS];
				int num_turns = 0;
				//System.out.println("Num Turns: " +num_turns);
				
				//while(no_links_changed_twice)
				
				if(a.IsConfederate)
				{
					// if shock, drop all ties
					long time = as.schedule.getSteps();
					if(time > as.timeOfShock){
						//dropping ties...
						for(int layer=0;layer<2; layer++){
							for(int node=0;node<as.NUM_PLAYERS;node++){
								if(node!=a.index)
								{
									//delete tie; this does nothing if no tie exists
									boolean already_tie = as.coplayerMatrix[layer][a.index][node];
									// delete this tie
									if(already_tie){
										bestDrop[0] = layer;
										bestDrop[1] = node;
										a.dropTie(as,bestDrop);	
									}
								}
							}
						}
					}else{
						// Assume only 2 attempts are made to create a tie; we can change this
						for(int j=0; j<2; ++j)
						{
							// pick a random node
							int rand_node = as.random.nextInt(as.NUM_PLAYERS);
							// pick a random layer (0 or 1)
							int rand_layer = as.random.nextInt(2);
							// if only one layer, pick only that layer
							if (as.oneLayerOnly)
								rand_layer = 0;
						
							//if degree < 12 (if 2 layers) or 6 (if 1 layer)
							if (((a.numTies(as,rand_layer) < as.NUM_PLAYERS-2) && as.oneLayerOnly)
								|| (((a.numTies(as,0) +a.numTies(as,1) )< 2*(as.NUM_PLAYERS-2)) && !as.oneLayerOnly))
							{
								//connect to random node Agent b = agentListShuf[rand_node];
								Agent randAgent = agentListShuf[rand_node];
								//make sure no self-loops (node is not i), and ties will be valid
								boolean already_tie = as.coplayerMatrix[rand_layer][a.index][randAgent.index];
								while(rand_node == i || rand_node == a.IgnoreNode || already_tie){
									rand_node = as.random.nextInt(as.NUM_PLAYERS);
									rand_layer = as.random.nextInt(2);
									randAgent = agentListShuf[rand_node];
									already_tie = as.coplayerMatrix[rand_layer][a.index][randAgent.index];
								}

								// if we create a self-loop, flag as an error
								if(randAgent.index == a.index)
									System.out.println("ERROR in line 130 in GamePlayerAgent.java:\nAttempting to create/remove self-loop");

								num_turns ++;
								bestAdd[0] = rand_layer;
								bestAdd[1] = randAgent.index;
								add = a.addTie(as, bestAdd); 
								// either way, this link has been changed
								link_changed[rand_layer][randAgent.index] = 1;
							}
						}
					}
				}else{
					for(int j=0; j<as.NUM_PLAYERS; ++j)
					{
						
						//System.out.println("utilit = " + bestAdd[2] + " OR " + bestDrop[2]);
						/*
						 * Pseudocode:
						 *  - p: click on a random node
						 *  	- if node already has link, remove link
						 *  	- if node does not have link, add link
						 *  	- Two additional ideas:
						 *  		- IGNORE if we would be undoing something we just did
						 *  		- Do N times: fix this (allows for no moves to be made, and p is interpretable)
						 * 	- Check if we should add a tie AND check if adding is the best option (and utility > 0)
						 * 		- Previous code did not check if this was the best option, but defaults to this
						 *  - Else: drop if utility > 0
						 *  - continue until we remove a link already added or vice versa (in both noise/util max cases)
						 *  
						 *  Overall, this should approximate the set of highest utility choices
						 * 
						 * */
					
						if(as.random.nextBoolean(as.noise)){
						
							// pick a random node
							int rand_node = as.random.nextInt(as.NUM_PLAYERS);
							//make sure no self-loops (node is not i)
							while(rand_node == i){
								rand_node = as.random.nextInt(as.NUM_PLAYERS);
							}
							// pick a random layer (0 or 1)
							int rand_layer = as.random.nextInt(2);
							// if only one layer, pick only that layer
							if (as.oneLayerOnly)
								rand_layer = 0;
							//connect to random node Agent b = agentListShuf[rand_node];
							Agent randAgent = agentListShuf[rand_node];
							// if we create a self-loop, flag as an error
							if(randAgent.index == a.index)
								System.out.println("ERROR in line 197 in GamePlayerAgent.java:\nAttempting to create/remove self-loop");
							// if link from a to random agent has not been changed before
							if(link_changed[rand_layer][randAgent.index] == 0)
							{
								num_turns ++;
								// if there is already a tie...
								boolean already_tie = as.coplayerMatrix[rand_layer][a.index][randAgent.index];
								// delete this tie
								if(already_tie){
									bestDrop[0] = rand_layer;
									bestDrop[1] = randAgent.index;
									a.dropTie(as,bestDrop);	
								}
								// else add a new tie
								else{
									bestAdd[0] = rand_layer;
									bestAdd[1] = randAgent.index;
									add = a.addTie(as, bestAdd); 
								}
								// either way, this link has been changed
								link_changed[rand_layer][randAgent.index] = 1;
							}
							else{
								//else there was an attempt to change a link twice, STOP
								no_links_changed_twice = false;
							}
						}
						else{
						
							add = false;//add a new tie? 
							bestAdd= a.bestAdd(as, as.searchSize);//AddSmartSearch//return [layer, index, gain] of best possible add. index = -1 if fully connected		
							bestDrop = a.bestDrop(as);//, as.searchSize);//return [layer, index, gain] of best possible drop. index = -1 if no current ties
							OldBestDrop = bestDrop;
							// add a new tie if this is the highest utility option
							if(bestAdd[1] >=0 && bestAdd[2] > 0 && bestAdd[2] > bestDrop[2]){// && bestAdd[2]>bestADCombo[0][2]){ //if adding would be a good move
							
								int addLayer = (int)bestAdd[0];
								Agent addAgent = as.agentList[(int)bestAdd[1]];
								//if no link has been changed...
								if(link_changed[addLayer][addAgent.index] == 0)
								{
									num_turns ++;
									// add this tie
									add = a.addTie(as, bestAdd); //returns true if add happens (if mutually beneficial)
									// record that this link has been changed
									link_changed[addLayer][addAgent.index] = 1;
								}else{
									//else there was an attempt to change a link twice, STOP
									no_links_changed_twice = false;
								}
							}
							// else bestAdd has poor utility. 
							// Check if dropping tie is best
							else{
								// if dropping a tie improves your utility
								if(bestDrop[1] >= 0 && bestDrop[2] > 0){
									
									int dropLayer = (int)bestDrop[0];
									// if link has not been changed
									if(link_changed[dropLayer][(int)bestDrop[1]] == 0)
									{
										num_turns ++;
										//drop tie (if it improves utility)
										a.dropTie(as, bestDrop);
										//record that this link has been changed
										link_changed[dropLayer][(int)bestDrop[1]] = 1;
								
									}else{
										// this link was changed twice, STOP
										no_links_changed_twice = false;
									}
								}
								else{// if changes in utility are all 0 or negative, and if there is no noise, break
									if(as.noise == 0)
										break;
								}

							}
						}
					}
					as.num_move_dist[num_turns]++;
				

				// record turn order
				as.turn_order[i] = a.index;
				int count = 0;
				for(int j = 0; j < as.NUM_PLAYERS; j++){//Step through each agent: 
					Agent b = agentListShuf[j];
					as.agent_util_after_turn[i][j] = as.currentUtility(b);
					if(i!=j){
						as.others_util_after_turn[i][count] = as.currentUtility(b);
						count++;
					}
				}
			}//end loop through agents 	
		}
		if(as.doGraphics)
			for(int i = 0; i < as.NUM_PLAYERS; i++){ //this is for visualization
				Agent a =as.agentList[i];
				a.util = as.currentUtility(a);
			}
		if(!as.doGraphics){// && as.collectFullNetwork){
			for(int i = 0; i < as.NUM_PLAYERS; i++){ 
				Agent a =as.agentList[i];
				a.util = as.currentUtility(a);
				a.cumulativeUtil += a.util;
				as.agentUtils[0][i] = a.util;
				as.agentUtils[1][i] = a.cumulativeUtil;
			}
			
			
		}
		
		
	}//end Step Method

	// What this does:
	// We find how long it takes before an agent randomly 
	/*public int find_rand_t(AgentsSimulation as, int n)
	{
		int trand = 0;
		double p = 1.0;
		double rand = as.random.nextDouble();
		int t_max = 2*(n-1);
		double num_links_possible = (double) 2*(n-1);
		for(int t = 0; t < t_max; t++){
			if(p < rand){
				trand = t;
				break;
			}
			p = p*(1 - t/num_links_possible);
		}
		return trand;
	}*/
	//OLD method (it works!)
/*	public synchronized void step(SimState state){
		AgentsSimulation as = (AgentsSimulation)state;
		/*
		 * 0. If time for a shock, shock the system (increase tie costs)
		 * 1. Check for pairwise stability (Stopping condition)
		 * 2. Shuffle order of agents
		 * 3. Step through each agent. 
		 * 		i) If noise, agent selects nodes to add/drop at random. 
		 * 		ELSE	Each agent can add one edge and drop one edge
		 * 		iia)  	If adding an edge adds utility, agent proposes to best partner. If agree, edge forms. 
		 * 		iib)	If dropping a (different) edge adds utility, agent drops edge. 
		 * 		iii) 	Else, agent considers all add-drop pairs. If one increases utility, agent
		 * 				proposes to (best) partner. If add is made, drop associated edge. 
		 *  	iv) 	Else, agent drops an existing edge if it will increase utility (chooses biggest increase)
		 */
		/*
		//Print average degrees
		if(as.doGraphics){
			if(as.schedule.getSteps() < 1)
				System.out.println("time" + "\t" + "degree1" + "\t" + "degree2" + "\t" + "degreetot");
			double[] deg = as.averageDegree();
			System.out.println(as.schedule.getSteps() + "\t" + deg[0] + "\t" + deg[1] + "\t" + deg[2]);
		}
		
		if(as.shockAtEquilibrium && as.noise == 0 && as.pairwiseStability()){ //Check if the network hasn't changed. 
			int steps = (int)as.schedule.getSteps();
			if(as.preshock){
				as.shockNetwork();
				as.preshock = false; 
				as.equilTime1 = steps;
			}
			else if (!as.preshock && steps > as.equilTime1 + 2){
				as.timeToEnd = true; 
				as.equilTime2 = steps;
			}
			
			int timeStable = steps - 2; //
			if(steps <= as.timeOfShock && !preShockStability){
				as.equilTime1 = timeStable;
				preShockStability = true;
			}
			else if(steps > (as.timeOfShock) && !postShockStability){
				as.equilTime2 = timeStable;
				postShockStability = true;
			}	
		}
		//(!as.shockAtEquilibrium || as.noise > 0) &&
		if((int)as.schedule.getSteps() == as.timeOfShock)//SHOCK!!!
			as.shockNetwork();	
		
		//ALL AGENTS HAVE OPPORTUNITY TO ADD/DELETE TIES
		Agent[] agentListShuf = as.shuffle(as.agentList);//Shuffle the order of agents
		for(int i = 0; i < agentListShuf.length; i++){//Step through each agent: 
			Agent a = agentListShuf[i];					
			if(as.random.nextBoolean(as.noise)) //If noise, add/drop random ties
				\(a, as);							
			else{ //otherwise, add/drop strategically
				
				for(int k=0; k<as.NUM_PLAYERS; k++)//do this set of calculations over and over
				{
					/*
					 * Pseudocode:
					 * 	- Check if we should add a tie AND check if adding is the best option (and utility > 0)
					 * 		- Previous code did not check if this was the best option, but defaults to this
					 *  - Else: check if we should both add AND drop a tie if utility > 0 and if this is better than dropping
					 *  	- Previous code did not check if this was the best option
					 *  - Else: drop if utility > 0
					 *  
					 *  Overall, this produces the set of highest utility choices
					 * 
					 * */
	/*
					boolean add = false;//add a new tie? 
					double[] bestAdd = a.bestAdd(as, as.searchSize);//return [layer, index, gain] of best possible add. index = -1 if fully connected		
					double[] bestDrop = a.bestDrop(as);//return [layer, index, gain] of best possible drop. index = -1 if no current ties
					double[][] bestADCombo = a.bestAddDropCombo(as, as.searchSize);
					double[] OldBestDrop = bestDrop;
					//ADD A TIE? 
					// Try to add a tie if the utility if:
					//		- the best add is greater than the utility of dropping
					//		- the best add is greater than the utility of rewiring (drop + add)
					if(bestAdd[1] >=0 && bestAdd[2] > 0 && bestAdd[2] > bestDrop[2] && bestAdd[2]>bestADCombo[0][2]){ //if adding would be a good move
						//if (add==false){
						add = a.addTie(as, bestAdd); //returns true if add happens (if mutually beneficial)
						if(add) //if a new tie is added
							bestDrop = a.bestDrop(as);//check for new best drop. 
						if(bestDrop[1] >= 0 && bestDrop[2] > 0 && !(bestDrop[0] == bestAdd[0] && bestDrop[1] == bestAdd[1])){ //if dropping increases utility and it's not the link just added, drop it
							a.dropTie(as, bestDrop); //drop this tie
						}
						//}
					}
					// bestAdd has poor utility. 
					// Check if rewiring (drop + add) is best
					// if not, check if dropping improves utility
					else{
						//ADD-DROP? 
						boolean addAD = false;
						//do{// Check if we should add and drop (switch)
							//if there's a good combo that is better than just dropping
							if(bestADCombo[0][1] >= 0 && bestADCombo[0][2] > 0 && bestADCombo[0][2]>bestDrop[2]){ 
								addAD = a.addTie(as, bestADCombo[0]); //attempt to add. 
								if(addAD) //if successful, drop linked tie
									a.dropTie(as, bestADCombo[1]);
								else if(bestDrop[2] > 0) //otherwise, drop worst tie if it increases utility
									a.dropTie(as, bestDrop);
							}
							//JUST DROP? 
							if(bestDrop[1] >= 0 && bestDrop[2] > 0){ //drop worst tie if it increases utility
								a.dropTie(as, bestDrop);	
								//bestDrop = a.bestDrop(as);
							}
							
						//}while(!addAD);
					}
					//OldBestDrop = bestDrop;
					//bestAdd = a.bestAdd(as, as.searchSize);//return [layer, index, gain] of best possible add. index = -1 if fully connected		
					//bestDrop = a.bestDrop(as);//return [layer, index, gain] of best possible drop. index = -1 if no current ties
					
				}
			}
		}//end loop through agents 	
		
		if(as.doGraphics)
			for(int i = 0; i < as.NUM_PLAYERS; i++){ //this is for visualization
				Agent a =as.agentList[i];
				a.util = as.currentUtility(a);
			}
		if(!as.doGraphics){// && as.collectFullNetwork){
			for(int i = 0; i < as.NUM_PLAYERS; i++){ 
				Agent a =as.agentList[i];
				a.util = as.currentUtility(a);
				a.cumulativeUtil += a.util;
				as.agentUtils[0][i] = a.util;
				as.agentUtils[1][i] = a.cumulativeUtil;
			}
			
			
		}
		
		
	}//end Step Method
*/
	
	/**
	 * Returns a random agent. 
	 */
	public Agent pickRandomAgent(AgentsSimulation as){
		int x = as.random.nextInt(as.NUM_PLAYERS);
		return as.agentList[x];
	}
	
	
	/**
	 * First, attempts to add a new tie. Chooses a random unconnected edge, and proposes adding tie. 
	 * Next, chooses an existing tie (excepting one just added) and drops it 
	 */
	public void randomTie(Agent ego, AgentsSimulation as){
				
		int addIndex = -1;
		int addLayer = -1; 
		
		int numTies0 = ego.numTies(as, 0) + ego.numTies(as, 1); //initial number of ties
		//ADD NEW TIE
		if(!ego.fullyConnected(as)){//Can't add tie if fully connected
			Agent addAgent = pickRandomAgent(as);//Find a random tie that doesn't exist
			while(addAgent.index == ego.index || (as.coplayerMatrix[0][ego.index][addAgent.index] && as.coplayerMatrix[1][ego.index][addAgent.index]))
				addAgent = pickRandomAgent(as);//can't pick self or someone to whom one is connected in both layers			
			int layer = as.random.nextInt(2); //choose a random layer
			if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) layer = 0; //force agent to start looking in layer 0 first? 
			if(as.coplayerMatrix[layer][ego.index][addAgent.index] && !as.oneLayerOnly)
				layer = layer + 1 - 2*layer; //if connected to agent in that layer, switch to other layer			
			if(ego.addTie(as, addAgent, layer)){//attempt to add tie. 
				addIndex = addAgent.index;
				addLayer = layer;
			}
		}		
		//DELETE TIE
		if(!ego.noTies(as) && numTies0 > 0){//make sure the agent has some ties, other than any just added
			int dropIndex = as.random.nextInt(as.NUM_PLAYERS);//Choose random tie
			int dropLayer = as.random.nextInt(2);//Choose random layer
			if(as.alwaysStartSearchAtLayer0 || as.oneLayerOnly) dropLayer = 0; //force agent to start looking in layer 0 first?
			//make sure other node is not self, is currently tied in layer, and isn't just one just added
			while(dropIndex == ego.index || !ego.isTie(as, dropIndex, dropLayer) || (dropIndex == addIndex && dropLayer == addLayer)){
				dropIndex = as.random.nextInt(as.NUM_PLAYERS);
				dropLayer = as.oneLayerOnly ? 0 : as.random.nextInt(2);
			}			
			as.tie_delete(ego, as.agentList[dropIndex], dropLayer);	//drop tie
		}
	}
	
	
	
}//end class
