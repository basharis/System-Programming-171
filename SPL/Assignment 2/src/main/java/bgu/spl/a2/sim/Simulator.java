/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.*;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tasks.ManufacturingTask;
import bgu.spl.a2.sim.tasks.WaveTask;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

	private static WorkStealingThreadPool wsThreadPool;
	private static JsonObject simulatorJson;
	private static ConcurrentLinkedQueue<WaveTask> wavesQueue = new ConcurrentLinkedQueue<WaveTask>();
	private static WaveTask wave=null, nextWave=null, lastWave=null;

	public static void main(String[] args){
		Simulator.simulatorJson = fileToJsonObject(args[0]);
		int nthreads = getIntMemberValue(Simulator.simulatorJson, "threads");
		WorkStealingThreadPool wstp = new WorkStealingThreadPool(nthreads);
		Simulator.attachWorkStealingThreadPool(wstp);
		ConcurrentLinkedQueue<Product> producedQueue = Simulator.start();
		serializeProducedQueue(producedQueue);
	}
	/**
	 * Begin the simulation
	 * Should not be called before attachWorkStealingThreadPool()
	 */
	public static ConcurrentLinkedQueue<Product> start(){
		Warehouse warehouse = new Warehouse();
		ConcurrentLinkedQueue<Product> producedQueue = new ConcurrentLinkedQueue<>();
		JsonArray tools = Simulator.simulatorJson.getAsJsonArray("tools");
		parseTools(tools, warehouse); // parse and add to warehouse
		JsonArray plans = Simulator.simulatorJson.getAsJsonArray("plans");
		parsePlans(plans, warehouse); // parse and add to warehouse
		JsonArray waves = Simulator.simulatorJson.getAsJsonArray("waves");
		parseWaves(waves, warehouse);
		boolean moreThanOneWave = false;
		CopyOnWriteArrayList<WaveTask> wavesQueueCopy = new CopyOnWriteArrayList<>();
		wavesQueueCopy.addAll(wavesQueue);
		AtomicInteger atomicWaveNumber = new AtomicInteger(0);
		wave = wavesQueue.poll();
		if (wave != null) Simulator.wsThreadPool.submit(wave);
		if (wavesQueue.isEmpty()){
			wave.getResult().whenResolved(() -> {producedQueue.addAll(wavesQueueCopy.get(atomicWaveNumber.getAndIncrement()).getResult().get());});
			wsThreadPool.start();
			CountDownLatch l = new CountDownLatch(1);
			wave.getResult().whenResolved(()->{
				l.countDown();
			});
			try {l.await(); wsThreadPool.shutdown();} catch (InterruptedException e1) {e1.printStackTrace();}
		}
		else{
			while (!(wavesQueue.isEmpty())){
				moreThanOneWave = true;
				nextWave = wavesQueue.poll();
				wave.getResult().whenResolved(() -> {
					producedQueue.addAll(wavesQueueCopy.get(atomicWaveNumber.getAndIncrement()).getResult().get());
					Simulator.wsThreadPool.submit(wavesQueueCopy.get(atomicWaveNumber.get()));
				});
				wave = nextWave;
				lastWave = nextWave;
			}
		}
		if (moreThanOneWave){
			CountDownLatch lastWaveLatch = new CountDownLatch(1);
			lastWave.getResult().whenResolved(() -> {
				producedQueue.addAll(lastWave.getResult().get());
				lastWaveLatch.countDown();
			});
			wsThreadPool.start();
			try {
				lastWaveLatch.await();
				wsThreadPool.shutdown();
			} catch (InterruptedException e) {e.printStackTrace();}
		}

		return producedQueue;
	}


	/**
	 * Parses the waves as given in the JSON file and stores them in a queue of waves.
	 * @param rawWavesArray
	 * @param warehouse
	 */
	private static void parseWaves(JsonArray rawWavesArray, Warehouse warehouse) {
		for (JsonElement wave : rawWavesArray){
			ConcurrentLinkedQueue<ManufacturingTask> currWave = new ConcurrentLinkedQueue<>();
			JsonArray waveAsArray = wave.getAsJsonArray();
			for (JsonElement productType : waveAsArray){
				String productName = productType.getAsJsonObject().get("product").getAsString();
				int quantity = productType.getAsJsonObject().get("qty").getAsInt();
				int startID = productType.getAsJsonObject().get("startId").getAsInt();
				for (int i=0 ; i<quantity ; i++){
					ManufacturingTask taskToAdd = new ManufacturingTask(productName, startID+i, warehouse);
					currWave.add(taskToAdd);
				}	
			}
			WaveTask currWaveTask = new WaveTask(currWave);
			wavesQueue.add(currWaveTask);
		}
	}

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	 * @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool){
		wsThreadPool = myWorkStealingThreadPool;
	}

	/**
	 * 
	 * @param fileName the name of the file to parse
	 * @return a JSON object.
	 */
	private static JsonObject fileToJsonObject(String fileName) {
		JsonObject returnObject = null;
		try {
			FileReader jsonFileReader;
			jsonFileReader = new FileReader(fileName);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(jsonFileReader);
			if (element.isJsonObject())
				returnObject = element.getAsJsonObject();
			return returnObject;
		} 
		catch (FileNotFoundException e) {e.printStackTrace(); return returnObject;}
	}

	/**
	 * 
	 * @param jsonObject an object to parse
	 * @param memberToSearch the member we are looking for
	 * @return the Int value of the key memberToSearch
	 */
	private static int getIntMemberValue(JsonObject jsonObject, String memberToSearch) {
		int returnValue = jsonObject.get(memberToSearch).getAsInt();
		return returnValue;
	}

	/**
	 * parses the JSON array containing the tools and add them to the warehouse.
	 * @param rawToolsArray JSON array to parse
	 * @param warehouse the warehouse to add the tools into
	 */
	private static void parseTools(JsonArray rawToolsArray, Warehouse warehouse){
		try {
			for (int i=0 ; i<rawToolsArray.size() ; i++){
				JsonObject tool = rawToolsArray.get(i).getAsJsonObject();
				addToolToWarehouse(tool, warehouse);
			}
		} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * Adds a tool to the warehouse
	 * @param toolToParse the Tool we add to the warehouse
	 * @param warehouse the Warehouse
	 * @throws IOException
	 */
	private static void addToolToWarehouse(JsonObject toolToParse, Warehouse warehouse) throws IOException{
		int qty=0; 
		String toolName = toolToParse.get("tool").getAsString();
		qty = toolToParse.get("qty").getAsInt();
		switch (toolName){
		case "gs-driver":  warehouse.addTool(new GcdScrewDriver(), qty); break;
		case "np-hammer":  warehouse.addTool(new NextPrimeHammer(), qty); break;
		case "rs-pliers":  warehouse.addTool(new RandomSumPliers(), qty); break;
		default: break;
		}
	}
	/**
	 * parses the plans given in the JSON file.
	 * @param rawPlansArray JSON array of plans
	 * @param warehouse The warehouse to add the plans into
	 */
	private static void parsePlans(JsonArray rawPlansArray, Warehouse warehouse) {
		for (int i=0 ; i<rawPlansArray.size(); i++){
			JsonObject onePlan = rawPlansArray.get(i).getAsJsonObject();
			addPlanToWarehouse(onePlan, warehouse);
		}
	}
	/**
	 * adds a plan to the warehouse
	 * @param planToParse the plan we wish to add
	 * @param warehouse the warehouse to add the plan into.
	 */
	private static void addPlanToWarehouse(JsonObject planToParse, Warehouse warehouse) {
		String productName;
		String[] requiredTools, requiredParts;
		JsonArray requiredToolsJsonArray, requiredPartsJsonArray;

		productName = planToParse.get("product").getAsString();
		requiredToolsJsonArray = planToParse.get("tools").getAsJsonArray();
		requiredTools = new String[requiredToolsJsonArray.size()];
		requiredPartsJsonArray = planToParse.get("parts").getAsJsonArray();
		requiredParts = new String[requiredPartsJsonArray.size()];
		for (int i=0 ; i<requiredToolsJsonArray.size(); i++)
		{
			requiredTools[i] = requiredToolsJsonArray.get(i).getAsString();
		}
		for (int i=0 ; i<requiredPartsJsonArray.size(); i++)
		{
			requiredParts[i] = requiredPartsJsonArray.get(i).getAsString();
		}

		ManufactoringPlan parsedPlan = new ManufactoringPlan(productName, requiredParts, requiredTools);
		warehouse.addPlan(parsedPlan);
	}
	/**
	 * Serializes the produced queue of products and generates a Java Serialized file "result.ser"
	 * @param producedQueue The queue of produced products we wish to Serialize 
	 */
	private static void serializeProducedQueue(ConcurrentLinkedQueue<Product> producedQueue) {
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream("result.ser");
			ObjectOutputStream objectOutputStream;
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(producedQueue);
			objectOutputStream.close();		
		} 
		catch (IOException e) {e.printStackTrace();}
	}

}
