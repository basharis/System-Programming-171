package bgu.spl.a2.sim.tools;

import java.util.Random;
import bgu.spl.a2.sim.Product;

public class RandomSumPliers implements Tool {

	@Override
	public String getType() {
		return "rs-pliers";
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
    	Random rand = new Random(id);
        long  sum = 0;
        for (long i = 0; i < id % 10000; i++) {
            sum += rand.nextInt();
        }

        return sum;
    }
}
