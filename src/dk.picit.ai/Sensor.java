package dk.picit.ai;

import java.io.Serializable;


public class Sensor implements Serializable {
	
	private static final long serialVersionUID = 2291975964373172542L;

	public String id;
	
	/* The neural net this sense was attached to */
	public transient Net net;
	private float ratio = 200f;
//	private float ratio = 5f/3; // Normal ratio

	public int width;
	public int height;
	
	/* Index numbers for these input nodes in net*/
	public int[] inputIdxs;
	
	
	public Sensor(Net net, String id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
		
		inputIdxs = new int[width*height];
		connect(net);
	}
	
	
	public void connect(Net net) {
		this.net = net;
		if(net.nodes == null) net.nodes = new Node[(int)(width*height*ratio)];
		else if(net.nodes.length < (net.lastInputNo + width*height)*ratio) {
			Node[] newnodes = new Node[(int)((net.lastInputNo  + width*height)*ratio)];
			for(int i = 0; i < net.lastInputNo; i++) {
				newnodes[i] = net.nodes[i];
			}
			
			int newOffset = (int)(width*height*ratio);
			for(int i = net.lastInputNo + 1; i < net.lastInputNo; i++) {
				newnodes[i + newOffset] = net.nodes[i];
			}
			
			net.nodes = newnodes;
		}
		
		int idx = 0;
		for (int j = net.lastInputNo; j < net.nodes.length; j++) {
			Node node = net.nodes[j];
			if(node == null) node = net.nodes[j] = new Node();
				
			if(!node.isInputNode) {
				//set needed input nodes
				node.isInputNode = true;
				inputIdxs[idx++] = j;
				node.idxSense = j;
				
				if(idx >= inputIdxs.length) {
					net.lastInputNo = j;
					break;
				}
			}
			else if(node.isInputNode) node.idxSense = j;
		}
		
		// add sense to senses array
		if(net.sensors == null) {
			net.sensors = new Sensor[1];
			net.sensors[0] = this;
		}
		else {
			Sensor[] newsenses = new Sensor[net.sensors.length + 1];
			for(int i = 0; i < net.sensors.length; i++) {
				newsenses[i] = net.sensors[i];
			}
			
			newsenses[net.sensors.length] = this;
			net.sensors = newsenses;
		}
		
		int nullNodes = 0;
		int sensorNodes = 0;
		for(Node node : net.nodes) {
			if(node == null) nullNodes++;
			else if(node.isInputNode) sensorNodes++;
		}
		System.out.println("New Sensors size: " + net.sensors.length + " " + sensorNodes + " " + nullNodes + " " + net.nodes.length);
	}
}