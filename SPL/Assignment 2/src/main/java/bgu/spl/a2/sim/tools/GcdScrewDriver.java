package bgu.spl.a2.sim.tools;

import java.math.BigInteger;

import bgu.spl.a2.sim.Product;

public class GcdScrewDriver implements Tool {

	@Override
	public String getType() {
		return "gs-driver";
	}

	@Override
	
	public long useOn(Product p) {
		long value = 0;
		for(Product part : p.getParts()){
			value+=Math.abs(func(part.getFinalId()));
		}
		return value;
	}
	
	public long func(long id){
    	BigInteger big1 = BigInteger.valueOf(id);
        BigInteger big2 = BigInteger.valueOf(reverse(id));
        long value = (big1.gcd(big2)).longValue();
        return value;
    }
	
	public long reverse(long value) {
	    long resultNumber = 0;
	    for(long i = value; i !=0 ; i /= 10) {
	        resultNumber = resultNumber * 10 + i % 10;
	    }
	    return resultNumber;        
	}

}
