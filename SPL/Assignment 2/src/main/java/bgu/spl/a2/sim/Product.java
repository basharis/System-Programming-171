package bgu.spl.a2.sim;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements Serializable {
	
	private long myStartID;
	private long myFinalID;
	private String myName;
	private List<Product> listOfSubProducts;
	/**
	* Constructor 
	* @param startId - Product start id
	* @param name - Product name
	*/
    public Product(long startId, String name){
    	myStartID = startId;
    	myName = name;
    	listOfSubProducts = new CopyOnWriteArrayList<Product>();
    }

	/**
	* @return The product name as a string
	*/
    public String getName(){
    	return myName;
    }

	/**
	* @return The product start ID as a long. start ID should never be changed.
	*/
    public long getStartId(){
    	return myStartID;
    }
    
	/**
	* @return The product final ID as a long. 
	* final ID is the ID the product received as the sum of all UseOn(); 
	*/
    public long getFinalId(){
    	return myFinalID;
    }

	/**
	* @return Returns all parts of this product as a List of Products
	*/
    public List<Product> getParts(){
    	return listOfSubProducts;
    }

	/**
	* Add a new part to the product
	* @param p - part to be added as a Product object
	*/
    public void addPart(Product p){
    	listOfSubProducts.add(p);
    }

	public void setFinalID(long useOnResult) {
		myFinalID = useOnResult;
	}
	@Override
	public String toString(){
		String returnString = "";
		returnString = returnString+"ProductName: "+this.myName+"  Product Id = "+myFinalID+"\n"+"PartsList {\n";
		for (Product productToString : listOfSubProducts){
			returnString=returnString+productToString.toString();
		}
		returnString = returnString+"}\n";
		return returnString;
	}
}
