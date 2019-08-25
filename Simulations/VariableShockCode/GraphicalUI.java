package agents;

import java.awt.Color;

import javax.swing.JFrame;
import sim.portrayal.network.*;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.Portrayal;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.util.Double2D;
import sim.util.Int2D;

public class GraphicalUI extends GUIState{
	public Display2D display; 
    public JFrame displayFrame;
    public Display2D display2; 
    public JFrame displayFrame2;
    NetworkPortrayal2D edgePortrayal_layer1 = new NetworkPortrayal2D();
    NetworkPortrayal2D edgePortrayal_layer2 = new NetworkPortrayal2D();
    NetworkPortrayal2D edgePortrayal_layer1_SPILL = new NetworkPortrayal2D();
    NetworkPortrayal2D edgePortrayal_layer2_SPILL = new NetworkPortrayal2D();
    //SparseGridPortrayal2D agentsPortrayal_layer1 = new SparseGridPortrayal2D();
    //SparseGridPortrayal2D agentsPortrayal_layer2 = new SparseGridPortrayal2D();
    ContinuousPortrayal2D agentsPortrayal_layer1 = new ContinuousPortrayal2D();
	ContinuousPortrayal2D agentsPortrayal_layer2 = new ContinuousPortrayal2D();
    Portrayal nodePortrayal1 = new sim.portrayal.simple.OvalPortrayal2D(Color.blue, 2.2);
    Portrayal nodePortrayal2 = new sim.portrayal.simple.OvalPortrayal2D(Color.red, 2.2);
    		//OvalPortrayal2D(Color.blue);
    public AgentsSimulation as;
	Color bgcolor = Color.white;
    
    
    public GraphicalUI() {
		super(new AgentsSimulation(System.currentTimeMillis()));
		as = (AgentsSimulation)state;
	}

	// standard construtor
    public GraphicalUI(SimState state) { 
        super(state); // Pass the already created simulation to it
    }
    
    
    
    public void setupPortrayals() {
    	AgentsSimulation as = (AgentsSimulation)state;
    	Continuous2D agentsSpace1 = new Continuous2D(as.gridWidth, as.gridWidth, as.gridHeight); //as.agentsSpace1; //don't forget space2
    	Continuous2D agentsSpace2 = new Continuous2D(as.gridWidth, as.gridWidth, as.gridHeight); //as.agentsSpace2; //don't forget space
    	
    	//place the agents in the spaces: 
    	for(int i = 0; i < as.NUM_PLAYERS; i++){
    		Agent a = as.agentList[i];
    		Double2D aLoc1 = as.agentsSpace1.getObjectLocation(a);
    		Double2D aLoc2 = as.agentsSpace2.getObjectLocation(a);
    		agentsSpace1.setObjectLocation(a, aLoc1);
    		agentsSpace2.setObjectLocation(a, aLoc2);
    	}
    	
    	
    	
    	edgePortrayal_layer1.setField( new SpatialNetwork2D( agentsSpace1, as.links[0] ) );
    	edgePortrayal_layer1.setPortrayalForAll(new SimpleEdgePortrayal2D());
    	edgePortrayal_layer2.setField( new SpatialNetwork2D( agentsSpace2, as.links[1] ) );
    	edgePortrayal_layer2.setPortrayalForAll(new SimpleEdgePortrayal2D());
    	edgePortrayal_layer1_SPILL.setField( new SpatialNetwork2D( agentsSpace1, as.links[2] ) );
    	edgePortrayal_layer2_SPILL.setField( new SpatialNetwork2D( agentsSpace2, as.links[2] ) );    	
    	
    	SimpleEdgePortrayal2D spillEdge = new SimpleEdgePortrayal2D(Color.darkGray, Color.orange);        
    	spillEdge.setShape(SimpleEdgePortrayal2D.SHAPE_LINE_ROUND_ENDS);
    	spillEdge.setBaseWidth(0.8);
    	spillEdge.labelPaint = null;
    	edgePortrayal_layer1_SPILL.setPortrayalForAll(spillEdge);
    	edgePortrayal_layer2_SPILL.setPortrayalForAll(spillEdge);
    	
    	
    	agentsPortrayal_layer1.setField(agentsSpace1);
    	agentsPortrayal_layer2.setField(agentsSpace2);
    	agentsPortrayal_layer1.setPortrayalForAll(nodePortrayal1);
    	agentsPortrayal_layer2.setPortrayalForAll(nodePortrayal2);
    	
    	as.agentsPortrayal_layer1 = agentsPortrayal_layer1;
    	as.agentsPortrayal_layer2 = agentsPortrayal_layer2;
    	
    	//bgcolor = as.bgcolor;
    	
    	
    	display.reset(); 
		display.repaint(); // call the repaint method
		display2.reset(); 
		display2.repaint(); // call the repaint method

		
    }
	
    
    // suppress warning that Display2D has depreciated
    @SuppressWarnings("deprecation")
    
	public void init(Controller c){
    	AgentsSimulation as = (AgentsSimulation)state;
        super.init(c);  
        display = new Display2D(200,200,this,1); 
        displayFrame = display.createFrame(); 
        c.registerFrame(displayFrame);   
        displayFrame.setVisible(true);  
        display.setBackdrop(bgcolor);
        
        display2 = new Display2D(200,200,this,1); 
        displayFrame2 = display2.createFrame(); 
        c.registerFrame(displayFrame2);   
        displayFrame2.setVisible(true);  
        displayFrame2.setLocation(218, 0);
        display2.setBackdrop(bgcolor);
        if(((AgentsSimulation)state).doGraphics){
        	displayFrame.setTitle("Layer 1");
        	display.attach(edgePortrayal_layer1, "Edges1");      	
        	display.attach(edgePortrayal_layer1_SPILL,"SpillEdges1");
        	display.attach(agentsPortrayal_layer1,"Nodes1");
        	displayFrame2.setTitle("Layer 2");
        	display2.attach(edgePortrayal_layer2, "Edges2");
        	display2.attach(edgePortrayal_layer2_SPILL,"SpillEdges2");
        	display2.attach(agentsPortrayal_layer2, "Nodes2");

        }
        //setDisplay(display); //pass to display
    }
    
    
//    protected GraphicalUI() { // Constructor for this class
//        super(new AgentsSimulation(System.currentTimeMillis()));
//        ssc = (SimStateWithSimController)state;
//                  // Create a simulation in it
// }
    
    

    public static String getName() { 
        return "Agents Simulation"; 
               // return a name for what this simulation is about
         }

    public void quit() {
         super.quit(); // Use the already defined quit method
            
         if (displayFrame!=null) displayFrame.dispose(); 
            displayFrame = null;  // when quiting get rid of the display
            display = null;       
    }
    
    


	public void start() {
          super.start(); // use the predefined start method
           setupPortrayals(); // add setupPortrayals method below

    }

        
    public void load(SimState state) {
        super.load(state); // load the simulation into the interface
        setupPortrayals(); // call setuuPortrayals
    }
    
    public Object getSimulationInspectedObject() {
        return state; // This returns the simulation
   }
    
    public static void main(String[] args) {
    	GraphicalUI ex = new GraphicalUI(); 
        Console c = new Console(ex);
        //ssc.setGUIState(ex); //pass to SimState
		c.setVisible(true); // make the console visible
		//ssc.setConsole(c); //Pass to Simstate
        System.out.println("Start Simulation"); 
    }
	
}//end class
