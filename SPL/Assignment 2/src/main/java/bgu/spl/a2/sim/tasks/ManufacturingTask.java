package bgu.spl.a2.sim.tasks;
/**
 * A class describing a single manufacturing task
 */

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.*;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.*;
import bgu.spl.a2.sim.conf.*;
import bgu.spl.a2.sim.tools.Tool;

public class ManufacturingTask extends Task<Product> {

	private String productName;
	private long startID;
	private Warehouse warehouse;
	private ManufactoringPlan plan;
	private Deferred<Product> deferredResult;
	private Product result;
	private AtomicLong useOnValues;
	private AtomicInteger numOfToolsToUse;

	/**
	 * Constructor
	 * @param productName The name of the product we wish to manufacture.
	 * @param startID The given StartID of the product to manufacture.
	 * @param warehouse The warehouse of the simulator
	 */
	public ManufacturingTask(String productName, long startID, Warehouse warehouse) {
		this.productName = productName;
		this.startID = startID;
		this.warehouse = warehouse;
		this.plan = warehouse.getPlan(productName);
		this.deferredResult = new Deferred<Product>();
		this.result = new Product(startID, productName);
		useOnValues = new AtomicLong(0);
		numOfToolsToUse = new AtomicInteger(plan.getTools().length);
	}

	@Override
	/**
	 * Starts the manufacturing task
	 * Spawns sub-tasks if such are necessary.
	 */
	protected void start() {
		ConcurrentLinkedQueue<ManufacturingTask> partsQueue = new ConcurrentLinkedQueue<>();
		if(plan.getParts().length == 0){
			for (int i=0; i<plan.getTools().length ; i++){
				Deferred<Tool> deferredTool = warehouse.acquireTool(plan.getTools()[i]);
				deferredTool.whenResolved(()->{
					warehouse.releaseTool(deferredTool.get());
					if (numOfToolsToUse.decrementAndGet() == 0){
						result.setFinalID(startID);
						deferredResult.resolve(result);
					}
				});
			}
		}
		else{
			for (String partFromPlan : plan.getParts()){
				ManufacturingTask taskToSpawn = new ManufacturingTask(partFromPlan, startID+1, warehouse);
				partsQueue.add(taskToSpawn);
			}
			for (ManufacturingTask taskToSpawn : partsQueue){
				spawn(taskToSpawn);
			}
			whenResolved(partsQueue, ()->{
				for (ManufacturingTask subProductToAdd : partsQueue)
					result.addPart(subProductToAdd.getResult().get());
				if (plan.getTools().length != 0){
					for (int i=0; i<plan.getTools().length ; i++){
						Deferred<Tool> deferredTool = warehouse.acquireTool(plan.getTools()[i]);
						deferredTool.whenResolved(()->{
							useOnValues.addAndGet(deferredTool.get().useOn(this.result));
							warehouse.releaseTool(deferredTool.get());
							if (numOfToolsToUse.decrementAndGet() == 0){
								result.setFinalID(useOnValues.get()+startID);
								deferredResult.resolve(result);}
						});
					}
				}
				else{
					result.setFinalID(startID);
					deferredResult.resolve(result);
				}
			});


		}
		deferredResult.whenResolved(()->{
			complete(result);
		});
	}



}
