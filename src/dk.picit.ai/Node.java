package dk.picit.ai;

import java.io.Serializable;


public class Node implements Serializable {
	
	private static final long serialVersionUID = 2351667451996976334L;
	
	boolean isInputNode;
	boolean isOutputNode;
	
	int layerno = 0;
	
	public int idxSense = -1;
	public int idxFlavor = -1;
	
	public Flavor flavor;
}