package bgu.spl.a2.sim.tasks;
/**
 * A class describing a complete wave task
 * This class holds a collection of sub-tasks which are the products to produce in this wave.
 */
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;

public class WaveTask extends Task<ConcurrentLinkedQueue<Product>> {

	ConcurrentLinkedQueue<ManufacturingTask> waveTasks;
	ConcurrentLinkedQueue<Product> waveProduced;

	/**
	 * Constructor
	 * @param waveTasks The collection of manufacturing tasks. WaveTasks holds the product to be produced in this wave.
	 */
	public WaveTask(ConcurrentLinkedQueue<ManufacturingTask> waveTasks){
		this.waveTasks = waveTasks;
		this.waveProduced = new ConcurrentLinkedQueue<Product>();
	}

	@Override
	/**
	 * Start this wave.
	 * Spawn the sub-tasks.
	 */
	protected void start() {
		if (waveTasks.size() != 0){
			whenResolved(waveTasks, () ->{ 
				addProductsToQueue();
			});
			for (ManufacturingTask task : waveTasks)
				spawn(task);
		}
		else
			complete(waveProduced);
	}
/**
 * After the wave finishes it adds the products it produced to a queue.
 */
	private void addProductsToQueue() {
		for (ManufacturingTask task : waveTasks)
			waveProduced.add(task.getResult().get()); 

		complete(waveProduced);
	}


}
