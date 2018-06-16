package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;

import java.util.concurrent.*;

import bgu.spl.a2.Deferred;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {
	
	private ConcurrentLinkedDeque<ManufactoringPlan> plans;
	private ConcurrentLinkedDeque<Tool> gsdrivers;
	private ConcurrentLinkedDeque<Tool> nphammers;
	private ConcurrentLinkedDeque<Tool> rspliers;
	private ConcurrentLinkedQueue<Deferred<Tool>> gsdriversDeferredQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> nphammersDeferredQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> rspliersDeferredQueue;
	

	/**
	* Constructor
	*/
    public Warehouse(){
    	plans = new ConcurrentLinkedDeque<>();
    	gsdrivers = new ConcurrentLinkedDeque<>();
    	nphammers = new ConcurrentLinkedDeque<>();
    	rspliers = new ConcurrentLinkedDeque<>();
    	gsdriversDeferredQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
    	nphammersDeferredQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
    	rspliersDeferredQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
    }

	/**
	* Tool acquisition procedure
	* Note that this procedure is non-blocking and should return immediately
	* @param type - string describing the required tool
	* @return a deferred promise for the  requested tool
	*/
    public Deferred<Tool> acquireTool(String type){
    	Deferred<Tool> deferredTool = new Deferred<Tool>();
    	Tool polledFromQueue;
    	switch(type){
		case "gs-driver":
			polledFromQueue = gsdrivers.poll();
			if (polledFromQueue != null){
				deferredTool.resolve(polledFromQueue);
			}
			else
				gsdriversDeferredQueue.add(deferredTool);
			break;
		case "np-hammer":  
			polledFromQueue = nphammers.poll();
			if (polledFromQueue != null){
				deferredTool.resolve(polledFromQueue);
			}
			else
				nphammersDeferredQueue.add(deferredTool);
			break;
		case "rs-pliers":  
			polledFromQueue = rspliers.poll();
			if (polledFromQueue != null){
				deferredTool.resolve(polledFromQueue);
			}
			else
				rspliersDeferredQueue.add(deferredTool);
			break;
		default: break;
    	}
    	return deferredTool;
	}

	/**
	* Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	* @param tool - The tool to be returned
	*/
    public void releaseTool(Tool tool){
    	ConcurrentLinkedQueue<Deferred<Tool>> deferredQueue = getToolTypeDeferredQueue(tool);
    	Deferred<Tool> deferredToolToResolve = deferredQueue.poll();
    	if (deferredToolToResolve != null)
    		deferredToolToResolve.resolve(tool);
    	else
    		addTool(tool);
    }

	
	private ConcurrentLinkedQueue<Deferred<Tool>> getToolTypeDeferredQueue(Tool tool) {
    	switch(tool.getType()){
		case "gs-driver": return gsdriversDeferredQueue;
		case "np-hammer": return nphammersDeferredQueue;
		case "rs-pliers": return rspliersDeferredQueue;
		default: return null;
    	}
	}


	/**
	* Getter for ManufactoringPlans
	* @param product - a string with the product name for which a ManufactoringPlan is desired
	* @return A ManufactoringPlan for product
	*/
    public ManufactoringPlan getPlan(String product){
    	for (ManufactoringPlan iter : plans){
    		if (iter.getProductName().equals(product))
    			return iter;
    	}
    	return null;
    }

	/**
	* Store a ManufactoringPlan in the warehouse for later retrieval
	* @param plan - a ManufactoringPlan to be stored
	*/
    public void addPlan(ManufactoringPlan plan){
    	plans.add(plan);
    }
    
	/**
	* Store a qty Amount of tools of type tool in the warehouse for later retrieval
	* @param tool - type of tool to be stored
	* @param qty - amount of tools of type tool to be stored
	*/
    public void addTool(Tool tool, int qty){
    	String toolType = tool.getType();
    	tool = null;
    	for (int i = 0 ; i < qty ; i++){
    		addTool(toolType);
    	}
    }
    
    private void addTool(String toolType){
    	switch(toolType){
    		case "gs-driver":  gsdrivers.add(new GcdScrewDriver()); break;
    		case "np-hammer":  nphammers.add(new NextPrimeHammer()); break;
    		case "rs-pliers":  rspliers.add(new RandomSumPliers()); break;
    		default: break;
    	}
    }
    private void addTool(Tool tool){
    	switch(tool.getType()){
    		case "gs-driver":  gsdrivers.add((GcdScrewDriver) tool); break;
    		case "np-hammer":  nphammers.add((NextPrimeHammer) tool); break;
    		case "rs-pliers":  rspliers.add((RandomSumPliers) tool); break;
    		default: break;
    	}
    }
}
