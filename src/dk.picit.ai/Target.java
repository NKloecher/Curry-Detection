package dk.picit.ai;

import java.io.Serializable;
import java.util.Random;


public class Target implements Serializable {
	
	private static final long serialVersionUID = 6191333108708956974L;

	private static final transient Random RND = new Random();

	public Flavor flavor;
	public Object value;
	
	public long targetVals;
	public double quality;
	
	
	//TODO: try to remove from Result
	Target() {
		
	}
	
	
	public Target(Flavor flavor, Object value) {
		this.flavor = flavor;
		this.value = value;
		
		targetVals = RND.nextLong();
	}

	
	@Override
	public String toString() {
		return flavor.id + ": " + value;
	}
}