package dk.picit.ai;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public abstract class Net {

	protected static final Random RND = new Random();
	
	public Node[] nodes;
	int hiddenlayers = 7;
	private int layersize = 0;
	
	int lastInputNo = 0;
	int firstOutputNo = 0;
	
	public int[] conIdxs;
	public float[] conWeights;
	
	public Sensor[] sensors;
	public Flavor[] flavors;
	
	public int connectCount = 64; // 128
	
	public static transient float avgHitRate; 
	public static boolean updatenext = false;
	
	
	protected Net() {}
	
	
	public void randomize() {
		System.out.println("Randomizing");
		
		setInputOutputMarkers();
		setLayerNoForNodes();
		setNodeConnections();

		//debug - check sanity
		float[] check = new float[hiddenlayers + 2];
		for (int k = 0; k < nodes.length; k++) {
			check[nodes[k].layerno]++;
		}
		
		for (int k = 0; k < check.length; k++) {
			if(check[k] > 0) System.out.println("Layer " + k + ": " + check[k] + " (" + layersize + ") " + (k==0?1.00:check[k-1]/check[k]));
			else System.out.println("Layer " + k + ": " + check[k] + " (" + layersize + ") NA");
		}
		
		for(Flavor flavor : flavors) {
			int lastHiddenLayerConnections = 0;
			int notlastHiddenLayerConnections = 0;
			for (int j = 0; j < nodes.length; j++) {
				final int lowerIdx = (j + 0)*flavor.net.connectCount;
				final int upperIdx = (j + 1)*flavor.net.connectCount;
				
				for(int i = lowerIdx; i < upperIdx; i++) {
					if(nodes[j].flavor == flavor) {
						if(nodes[conIdxs[i]].layerno == hiddenlayers) lastHiddenLayerConnections++;
						else notlastHiddenLayerConnections++;
					}
				}
			}
			
			System.out.println(flavor.id + " connections to output layer: " + lastHiddenLayerConnections + " " + notlastHiddenLayerConnections);
		}
	}
	
	
	private void setInputOutputMarkers() {
		for (int k = 0; k < nodes.length; k++) {
			if(nodes[k] == null) {
				System.err.println("Node was null - should not happen at this point");
				nodes[k] = new Node();
			}
			
			if(nodes[k].isInputNode) lastInputNo = k;
			else if(nodes[k].isOutputNode) nodes[k].layerno = hiddenlayers + 1;
			else firstOutputNo = k + 1;
		}
		
		System.out.println(firstOutputNo + " " + lastInputNo);
	}
	
	
	private void setLayerNoForNodes() {
		layersize = (firstOutputNo - lastInputNo) / hiddenlayers;
		
		int z = 0;
		for (int k = lastInputNo + 1; k < firstOutputNo; k++) {
			for(int m = 1; m < hiddenlayers; m++) {
				if(z <= m*layersize) {
					nodes[k].layerno = m;
					break;
				}
			}
			z++;
			
			if(nodes[k].layerno == 0) nodes[k].layerno = hiddenlayers;
		}
	}
	
	
	private void setNodeConnections() {
		conIdxs = new int[connectCount*nodes.length];
		conWeights = new float[connectCount*nodes.length];
		
		for (int k = lastInputNo + 1; k < nodes.length; k++) {
			//find connections possibilities for every layer node except nodes in input layer
			
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			// look only for nodes in prev layer
			int maxNo = Math.min(k, firstOutputNo);
			for (int kk = 0; kk < maxNo; kk++) {
				if(nodes[kk].layerno == nodes[k].layerno - 1) list.add(kk);
			}
	        
			//setting random connections for nodes
			for(int i = k*connectCount; i < (k + 1)*connectCount; i++) {
				if(list.size() > 0) {
					conIdxs[i] = list.remove(RND.nextInt(list.size()));
					conWeights[i] = (float)(RND.nextGaussian() * .075f);
				}
			}
		}
	}

	
	public int[] getSize() {
		int[] size = new int[2];
		size[0] = nodes.length;
		size[1] = conIdxs.length - lastInputNo*connectCount;
		return size;
	}

	
	public void save() {
		System.out.println("serializing");
		try {
			File file = new File("aidata.tmp");
			FileOutputStream fout = new FileOutputStream(file);
			GZIPOutputStream zos = new GZIPOutputStream(fout);
			ObjectOutputStream oos = new ObjectOutputStream(zos);

			oos.writeObject(nodes);
			oos.writeObject(connectCount);
			oos.writeObject(lastInputNo);
			oos.writeObject(firstOutputNo);
			oos.writeObject(conIdxs);
			oos.writeObject(conWeights);
			oos.writeObject(sensors);
			oos.writeObject(flavors);

			oos.close();
			
			File fileOld = new File("aidata.dat");
			if(fileOld.exists()) {
			  boolean checkDelete = fileOld.delete();
			  if(checkDelete) {
				file.renameTo(new File("aidata.dat"));
			  }
			}
			else file.renameTo(new File("aidata.dat"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("done serializing");
	}
	
	
    public File load(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			GZIPInputStream gis = new GZIPInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream (gis);			
			
			System.out.println("deserializing");
			
			nodes = (Node[])ois.readObject();
			connectCount = (int)ois.readObject();
			lastInputNo = (int)ois.readObject();
			firstOutputNo = (int)ois.readObject();
			conIdxs = (int[])ois.readObject();
			conWeights = (float[])ois.readObject();
			sensors = (Sensor[])ois.readObject();
			flavors = (Flavor[])ois.readObject();
			
			ois.close();
			fis.close();
			
			System.out.println("loading senses");
			for(int i = 0; i < sensors.length; i++) {
				sensors[i].net = this;
			}
			
			System.out.println("loading flavors");
			for(int i = 0; i < flavors.length; i++) {
				flavors[i].net = this;
				System.out.println("loading flavor[" + i + "]: " + flavors[i].id);
			}
			
			System.out.println("done deserializing");			
			return file;
		}
		catch (FileNotFoundException e) {
			System.out.println("No such file: " + file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}