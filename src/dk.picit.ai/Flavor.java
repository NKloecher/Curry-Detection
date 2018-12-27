package dk.picit.ai;

import java.awt.*;
import java.io.Serializable;


public class Flavor implements Serializable {
	
	private static final long serialVersionUID = 41557179072976543L;
	
	private float ratio = 5f/3;
	
	protected transient Net net;
	public String id;
	
	public int firstFlavoridx;
	
	public Target[] targets;
//	protected Flavor[] subFlavors;
	
	public Color color;
	public Color acolor;
	
	public transient float rmsVal = 1.0f;
	public transient float boost = 1.0f;
	
	public int hits;
	public int hitsPrv;
	public int tots;
	
	private long hitBits;
	private int hitRateAvgIdx;
	public float hitRateAvg;
	
	public float hitRateAvgMean;
	public float learningRate = 0.01f;
	public float hitRateAvgMeanLongPrv = 0.0f;
	
	private boolean[] hitRateAvgLong = new boolean[256];
	private int hitRateAvgIdxLong = 0;
	public float hitRateAvgMeanLong = 0;
	
	public Flavor(Net net, String id, Color color) {
		this.id = id;
		this.net = net;
		this.color = color;
		if(color == null) color = Color.white;
		this.acolor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 55);
		
		connect(net);
	}
	
	
	public void connect(Net net) {
		this.net = net;
		int addSize = (int)(64*ratio);
		
		if(net.nodes == null) {
			net.nodes = new Node[addSize];
			net.firstOutputNo = 0;
		}
		else if(net.firstOutputNo <= 0) net.firstOutputNo = net.nodes.length;
		
		Node[] newnodes = new Node[net.nodes.length + addSize];
		
		//copy old input nodes to new array
		for(int i = 0; i <= net.lastInputNo; i++) {
			newnodes[i] = net.nodes[i];
		}
		
		int newOffset = 0;
		
		//copy all from hidden layers to new array
		for(int i = net.lastInputNo + 1; i < net.firstOutputNo; i++) {
			if(net.nodes[i] == null) net.nodes[i] = new Node();
			if(net.nodes[i].isOutputNode) break;
			
			newnodes[i] = net.nodes[i];
			newOffset = i;
		}
		newOffset++;
		
		System.out.println("new offset: " + newOffset + " addSize: " + addSize);
		
		//insert new hidden and output nodes
		for(int i = newOffset; i < newOffset + addSize ; i++) {
			newnodes[i] = new Node();
		}
		
		newOffset += addSize;
		
		System.out.println("new offset: " + newOffset + " firstOutputNo: " + net.firstOutputNo + " " + net.nodes.length);
		
		//append old output nodes
		int jj = 0;
		for(int i = net.firstOutputNo; i < net.nodes.length; i++) {
			newnodes[newOffset + jj++] = net.nodes[i];
		}
		
		net.nodes = newnodes;
		
		// reversely loop through net nodes to find this flavors output nodes
		int idx = 64 - 1;
		for(int j = net.nodes.length - 1; j >= 0; j --) {
			Node node = net.nodes[j];
			if(node == null) {
				System.out.println("Node null, should not happen: " + j);
				node = net.nodes[j] = new Node();
			}
			
			if(!node.isOutputNode) {
				System.out.println("New output node: " + net.nodes.length + " " + j + " " + idx + " " + (net.nodes.length - j));
				firstFlavoridx = j;
				node.isOutputNode = true;
				node.idxFlavor = idx--;
				node.flavor = this;
				net.firstOutputNo = j;
			}
			if(idx < 0) break;
		}
		
		// add flavor to flavors array	
		if(net.flavors == null) {
			net.flavors = new Flavor[1];
			net.flavors[0] = this;
		}
		else {
			Flavor[] newflavours = new Flavor[net.flavors.length + 1];
			for(int i = 0; i < net.flavors.length; i++) {
				newflavours[i] = net.flavors[i];
			}
			newflavours[net.flavors.length] = this;
			net.flavors = newflavours;
		}
		
		System.out.println(this.id + " " + 64 + " " + addSize);
		
		//check sanity
		for(Flavor flavor : net.flavors) {
			int nullNodes = 0;
			int flavorNodes = 0;
			int thisFlavourNodes = 0;

			for(int j = net.nodes.length - 1; j >= 0; j --) {
				Node node = net.nodes[j];
				
				if(node == null) nullNodes++;
				else if(node.isOutputNode) flavorNodes++;
				
				if(node.isOutputNode && flavor.equals(node.flavor)) {
					thisFlavourNodes++;
					flavor.firstFlavoridx = j;
				}
			}
			System.out.println("New Flavors size: " + net.flavors.length 
					+ " " + thisFlavourNodes + " " + flavorNodes + " " + nullNodes 
					+ " " + net.nodes.length + " " + flavor.id + " " + flavor.firstFlavoridx);
		}
		System.out.println();
	}

	
	
	void calcHitRate(Target sampleTarget, Result result) {
		
		boolean acceptedResult = false;
		
		long bit = ~(1L << (hitRateAvgIdx++ % 64));
		hitBits &= bit;
		for(Target acceptableTarget : getAcceptableTargets(sampleTarget)) {
			if(result.targets[0] == acceptableTarget) {
				hitBits |= ~bit;
				hits++;
				acceptedResult = true;
				break;
			}
		}
		hitRateAvg = (float)(Long.bitCount(hitBits)/64.0f);
		hitRateAvgMean = (hitRateAvgMean + hitRateAvg)/2;
		
//		if(this instanceof PointFlavor && sampleTarget.value != null && result.targets[0].value != null) {
//			Point2D.Float st = (Point2D.Float)sampleTarget.value;
//			Point2D.Float rt = (Point2D.Float)result.targets[0].value;
//			boost = (float)(1.5*(1 + (Math.sqrt((st.x - rt.x)*(st.x - rt.x) + (st.y - rt.y)*(st.y - rt.y)) / 4.0)));
//		}
//		else if(this instanceof LineFlavor && sampleTarget.value != null && result.targets[0].value != null)
//			boost = 1 + ((Float)sampleTarget.value - (Float)result.targets[0].value)*((Float)sampleTarget.value - (Float)result.targets[0].value)/8;
//		else if(sampleTarget.value == null && result.targets[0].value != null)
//			boost = 1.2f;
//		else 
			boost = 1.0f;
		
//		System.err.println(boost);
			
		hitRateAvgLong[hitRateAvgIdxLong++ % hitRateAvgLong.length] = acceptedResult;
			
		float hitRateAvgSum = 0;
		for(int k = 0; k < hitRateAvgLong.length; k++) hitRateAvgSum += (hitRateAvgLong[k]?1:0);
		if(hitRateAvgIdxLong < hitRateAvgLong.length) hitRateAvgMeanLong = hitRateAvgSum / hitRateAvgIdxLong;
		else hitRateAvgMeanLong = hitRateAvgSum / hitRateAvgLong.length;
	}
	
		
	public Target getTarget(Object value) {
		if(value == null) return targets[0];
		for(int i = 0; i < targets.length; i++) {
			if(value.equals(targets[i].value)) return targets[i];
		}
		return null;
	}
	
	
	protected float[] getAcceptableTargetVals(Target acceptableTarget, Result result) {
		Target resultTarget = result.targets[0];
		
		float[] tVal = new float[64];
		for(int idx = 0; idx < 64; idx++) {
			tVal[idx] += (2*((acceptableTarget.targetVals >> idx) & 1L) - 1);
			
//			if(resultTarget != acceptableTarget) {
//				tVal[idx] -= (2*((resultTarget.targetVals >> idx) & 1L) - 1);
//				tVal[idx] /= 2;
//			}
		}
		return tVal;
	}

	
	
	protected Target[] getAcceptableTargets(Target sampleTarget) {
		return new Target[] {sampleTarget};
	}
}