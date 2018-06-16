package bgu.spl.a2;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {
	private int numOfThreads;
	private VersionMonitor poolVersion;
	private Thread[] threadsArray;
	private Processor[] processorsArray;
	private ConcurrentLinkedDeque<Task<?>>[] processorDequesArray;
	/**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * Implementors note: you may not add other constructors to this class nor
     * you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param nthreads the number of threads that should be started by this
     * thread pool
     */
	public WorkStealingThreadPool(int nthreads) {
    	poolVersion = new VersionMonitor();
        numOfThreads = nthreads;
        threadsArray = new Thread[numOfThreads];
        processorsArray = new Processor[numOfThreads];
        processorDequesArray = new ConcurrentLinkedDeque[numOfThreads];
        for (int i = 0 ; i < numOfThreads ; i++){
        	processorsArray[i] = new Processor(i, this);
        	threadsArray[i] = new Thread(processorsArray[i]);
        	processorDequesArray[i] = new ConcurrentLinkedDeque<Task<?>>();
        }
    }
    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
    	Random randomProcessorID = new Random();
    	processorDequesArray[randomProcessorID.nextInt(numOfThreads)].add(task);
    	poolVersion.inc();
     }

    
    /**
     * 
     * @param processorID
     * @return the next task for this processorID, if empty try to steal
     */
    Task<?> getNextTask(int processorID){
    	Task<?> potentialNextTask = null;
    	while (potentialNextTask == null && !(Thread.currentThread().isInterrupted())){
    		potentialNextTask = processorDequesArray[processorID].pollFirst();
    		if (potentialNextTask == null)
    			stealOrWait(processorID);
    	}
    	return potentialNextTask;
    }
    
    /**
     * Go over the processor array in a cyclic order and steal tasks.
     * If there are no tasks to steal, the thread should wait for a version change.
     * @param processorID
     */
    void stealOrWait(int processorID){
    	boolean stole = false;
    	for (int victimID = (processorID+1)%numOfThreads ; victimID != processorID && !stole ; victimID = (victimID+1)%numOfThreads){
    		int sizeOfVictim = processorDequesArray[victimID].size();
    		for (int j = 0 ; j < sizeOfVictim/2 || (sizeOfVictim == 1 && j == 0) ; j++){
    			Task<?> taskToPush = processorDequesArray[victimID].pollLast();
    			if (taskToPush != null)
    				processorDequesArray[processorID].push(taskToPush);
    		}
			if (processorDequesArray[processorID].size() != 0)
				stole = true;
    	}
    	if (!stole){
    	try { 
    			poolVersion.await(poolVersion.getVersion()); } 
    	catch (InterruptedException e) {Thread.currentThread().interrupt();}
    	}
    }
    
    void spawnToProcessorDeque(int processorID, Task<?>... task){
    	for (int i=0 ; i<task.length ; i++){
    		processorDequesArray[processorID].add(task[i]);
    		poolVersion.inc();
    	}
    }
    
    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     *
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException if the thread that shut down the threads is
     * interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     * shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException {
    	for (int i = 0 ; i < threadsArray.length ; i++){
    		threadsArray[i].interrupt();
    		threadsArray[i].join();
    		}
    }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
       for (int i = 0 ; i<numOfThreads ; i++){
    	   threadsArray[i].start();
       }
    }

}
