package bgu.spl.a2.sim.conf;

/**
 * a class that represents a manufacturing plan.
 *
 **/
public class ManufactoringPlan {
	
	private String productName;
	private String[] requiredParts;
	private String[] requiredTools;
	
	/** ManufactoringPlan constructor
	* @param product - product name
	* @param parts - array of strings describing the plans part names
	* @param tools - array of strings describing the plans tools names
	*/
    public ManufactoringPlan(String product, String[] parts, String[] tools){
    	productName = product;
    	requiredParts = parts;
    	requiredTools = tools;
    }

	/**
	* @return array of strings describing the plans part names
	*/
    public String[] getParts(){
    	return requiredParts;
    }

	/**
	* @return string containing product name
	*/
    public String getProductName(){
    	return productName;
    }
	/**
	* @return array of strings describing the plans tools names
	*/
    public String[] getTools(){
    	return requiredTools;
    }

}
