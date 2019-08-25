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
		// "if noise, shock network?" Does not seem to be reasonable, therefore we removed this
		if(/*(!as.shockAtEquilibrium || as.noise > 0) &&*/ (int)as.schedule.getSteps() == as.timeOfShock)//SHOCK!!!
			as.shockNetwork();	
		
		//ALL AGENTS HAVE OPPORTUNITY TO ADD/DELETE TIES
		Agent[] agentListShuf = as.shuffle(as.agentList);//Shuffle the order of agents
		for(int i = 0; i < agentListShuf.length; i++){//Step through each agent: 
			Agent a = agentListShuf[i];		
			a.util_before_turn=as.currentUtility(a);
			if (a.altruist){
				// if nor shocked, attempt to add a tie with all neighbors in neighbor_id list (6 random agents)
				if ((int)as.schedule.getSteps() < as.timeOfShock){
					for (int layer = 0; layer<2; layer++){
						for(int k=0; k<a.neighbor_ids.length; k++){
							Agent n = as.agentList[a.neighbor_ids[k]];
							a.addTie(as, n, layer);
						}
					}
				}
				//if shocked, drop all ties
				else{
					double numTies1 = (double)(a.numTies(as, 0));
					double numTies2 = (double)(a.numTies(as, 1));
					for (int layer = 0; layer<2; layer++){
						for(int n_id=0; n_id<as.NUM_PLAYERS; n_id++){
							Agent b = as.agentList[n_id];
  							as.tie_delete(a, b,layer);
						}
					}
				}
			}
				
			else{
				if(as.random.nextBoolean(as.noise)){ //If noise, add/drop random ties
					randomTie(a, as);				
				}
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
						a.num_links_added = 0;
						a.num_links_dropped = 0;
						boolean add = false;//add a new tie? 
						double[] bestAdd = a.bestAdd(as, as.searchSize);//a.bestAdd(as, as.searchSize);////return [layer, index, gain] of best possible add. index = -1 if fully connected		
						double[] bestDrop = a.bestDrop(as, as.searchSize);//return [layer, index, gain] of best possible drop. index = -1 if no current ties
						// Best add-drop combo with a smart search
						double[][] bestADCombo = a.bestAddDropCombo(as, as.searchSize);//a.bestAddDropCombo(as, as.searchSize);//
						double[] OldBestDrop = bestDrop;
						//ADD A TIE? 
						// Try to add a tie if the utility if:
						//		- the best add is greater than the utility of dropping
						//		- the best add is greater than the utility of rewiring (drop + add)
						if(bestAdd[1] >=0 && bestAdd[2] > 0 && bestAdd[2] > bestDrop[2] && bestAdd[2]>bestADCombo[0][2]){ //if adding would be a good move
							//if (add==false){
							
							int addLayer = (int)bestAdd[0];
							Agent addAgent = as.agentList[(int)bestAdd[1]];
							add = a.addRewire(as, addAgent, addLayer,as.searchSize);
							if(add) //if a new tie is added
								bestDrop = a.bestDrop(as);//, as.searchSize);//check for new best drop. 
							    
							if(bestDrop[1] >= 0 && bestDrop[2] > 0 && !(bestDrop[0] == bestAdd[0] && bestDrop[1] == bestAdd[1])){ //if dropping increases utility and it's not the link just added, drop it
								a.dropTie(as, bestDrop); //drop this tie
							}
						}
						// bestAdd has poor utility. 
						// Check if rewiring (drop + add) is best
						// if not, check if dropping improves utility
						else{
							//ADD-DROP? 
							boolean addAD = false;
							// Check if we should add and drop (switch)
							//if there's a good combo that is better than just dropping
							if(bestADCombo[0][1] >= 0 && bestADCombo[0][2] > 0 && bestADCombo[0][2]>bestDrop[2])
							{
								int addLayer = (int)bestADCombo[0][0];
								Agent addAgent = as.agentList[(int)bestADCombo[0][1]];
								addAD = a.addRewire(as, addAgent, addLayer,as.searchSize);
								if(addAD) //if successful, drop linked tie
									a.dropTie(as, bestADCombo[1]);
								else if(bestDrop[2] > 0) //otherwise, drop worst tie if it increases utility
									a.dropTie(as, bestDrop);
							}
							//JUST DROP? 
							if(bestDrop[1] >= 0 && bestDrop[2] > 0){ //drop worst tie if it increases utility
								a.dropTie(as, bestDrop);	
							}
						}
					}
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

	
	/*
	 * Returns a random agent. 
	 */
	public Agent pickRandomAgent(AgentsSimulation as){
		int x = as.random.nextInt(as.NUM_PLAYERS);
		return as.agentList[x];
	}
	
	
	/*
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
			// do NOT add if neighbor is a shocked altruist
			boolean shocked_altruist_addagent = addAgent.altruist && ((int)as.schedule.getSteps() >= as.timeOfShock);
			while(shocked_altruist_addagent ||addAgent.index == ego.index || (as.coplayerMatrix[0][ego.index][addAgent.index] && as.coplayerMatrix[1][ego.index][addAgent.index]))
			{
				addAgent = pickRandomAgent(as);//can't pick self or someone to whom one is connected in both layers			
				shocked_altruist_addagent = addAgent.altruist && ((int)as.schedule.getSteps() >= as.timeOfShock);
			}
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
