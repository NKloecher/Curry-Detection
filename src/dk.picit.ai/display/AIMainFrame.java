package dk.picit.ai.display;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Random;


public class AIMainFrame extends JFrame implements ActionListener /*ChangeListener*/ {
	
	private static final long serialVersionUID = 1L;
	private static final Random RND = new Random();
	
	private HashMap<Flavor, AIComponent> fcs = new HashMap<Flavor, AIComponent>();
	
	private Box sensorBox;
	private Box flavorBox;
	private Box resultBox;
	
	private AIComponent flavorComp;
	private AIResultComponent resultComp;
	private AIOutputComponent outputComp;
	private AIOutputComponent consComp;
	private AISensorComponent sensorComp;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuLoadNet = new JMenuItem("Load net");
	private JMenuItem menuLoadSamples = new JMenuItem("Load samples");
	private JMenuItem menuSaveNet = new JMenuItem("Save net");
	private JMenuItem menuSaveNetAs = new JMenuItem("Save net as...");
	private JMenuItem menuSaveSamples = new JMenuItem("Save samples");
	private JMenuItem menuSaveSamplesAs = new JMenuItem("Save samples as...");
	
	private Net net;
	private long lastUpdate = 0;
	

	public AIMainFrame(Net net, int iterations) {
		this.net = net;
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setTitle("AI Testing");
		setBounds(10, 10, 1000, 900);
		
		menuLoadNet.addActionListener(this);
		menuLoadSamples.addActionListener(this);
		menuSaveNet.addActionListener(this);
		menuSaveNetAs.addActionListener(this);
		menuSaveSamples.addActionListener(this);
		menuSaveSamplesAs.addActionListener(this);
		
		menuFile.add(menuLoadNet);
		menuFile.add(menuLoadSamples);
		menuFile.add(menuSaveNet);
		menuFile.add(menuSaveNetAs);
		menuFile.add(menuSaveSamples);
		menuFile.add(menuSaveSamplesAs);
		menuBar.add(menuFile);
		setJMenuBar(menuBar);
		
		Dimension fillv = new Dimension(620, 10);
		Dimension fillh = new Dimension(10, 620);
		
		flavorComp = new AIComponent(net, 600, 300, iterations);
		for(Flavor flavor : net.flavors) {
			fcs.put(flavor, flavorComp);
		}
		
		sensorComp = new AISensorComponent(net, 600, 820);
		resultComp = new AIResultComponent(net.flavors, 600, 250);
		outputComp = new AIOutputComponent(net, 600, 250, iterations);
		consComp = new AIOutputComponent(net, 600, 250, iterations);
		
		Box hori = new Box(BoxLayout.X_AXIS);
		
		sensorBox = new Box(BoxLayout.Y_AXIS);
		resultBox = new Box(BoxLayout.Y_AXIS);
		flavorBox = new Box(BoxLayout.Y_AXIS);
		
		sensorBox.add(new Box.Filler(fillv, fillv, fillv));
		sensorBox.add(sensorComp);
		sensorBox.add(new Box.Filler(fillv, fillv, fillv));
		
		resultBox.add(new Box.Filler(fillv, fillv, fillv));
		resultBox.add(resultComp.panel);
		resultBox.add(new Box.Filler(fillv, fillv, fillv));
		
		flavorBox.add(new Box.Filler(fillv, fillv, fillv));
		flavorBox.add(flavorComp);
		flavorBox.add(new Box.Filler(fillv, fillv, fillv));
		flavorBox.add(outputComp);
		flavorBox.add(new Box.Filler(fillv, fillv, fillv));
		flavorBox.add(consComp);
		flavorBox.add(new Box.Filler(fillv, fillv, fillv));
		
		hori.add(sensorBox);
		hori.add(resultBox);
		hori.add(flavorBox);
		
		JScrollPane scrllHori = new JScrollPane(hori);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrllHori);
		
		pack();
		setVisible(true);
	}
	
	
	private void addFlavorPoint(Flavor flavor, double dx, double dy, Color color, int type) {
		AIComponent fc = fcs.get(flavor);
		fc.addPoint(dx, dy, color, type);
	}
	
	
	float tmpTotMeanTots = 1.0f;
	
	public void update(Sample sample, double progress) {
		
		float totMean = 0;
		int tots = 0;
		
		for(Flavor flavor : net.flavors) {
			addFlavorPoint(flavor, progress, flavor.hitRateAvgMean, flavor.acolor, 0);
			
//			flavor.learningRate = (float)(0.05); //*(1 - flavor.hitRateAvgMeanLong)*Math.pow(0.1, flavor.hitRateAvgMeanLong)); //(RND.nextDouble()*0.4); //*(1 - flavor.hitRateAvgMeanLong)); //*Math.pow(0.33, flavor.hitRateAvgMeanLong));
					
			flavor.hitRateAvgMeanLongPrv = flavor.hitRateAvgMeanLong;
			flavor.hitsPrv = 0; //flavor.hits;
			flavor.hits = 0;
			flavor.tots = 0;
			
			totMean += flavor.hitRateAvgMeanLong;
			tots++;
		}
		
		net.avgHitRate = tmpTotMeanTots = totMean/tots;
//		System.err.println(net.avgHitRate + " " + totMean + " " + tots);
		
		
		addFlavorPoint(net.flavors[0], progress, tmpTotMeanTots, Color.red, 1);
		addFlavorPoint(net.flavors[0], progress, net.flavors[0].hitRateAvgMean, null, 2);
		
		if (lastUpdate < System.currentTimeMillis() - 100) {
			for(AIComponent aifc : fcs.values()) {
				aifc.repaint();
			}
			outputComp.updateComponent(sample);
			lastUpdate = System.currentTimeMillis();
		}
		
		if (net.updatenext || progress == 0) {
			displaySample(sample, progress);
			net.updatenext = false;
		}
	}
	
	
	public void displaySample(Sample sample, double progress) {
		sensorComp.updateComponent(sample);
		resultComp.updateComponent(sample);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(menuSaveNet)) {
			net.save();
		}
		
		if(e.getSource().equals(menuSaveNetAs)) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(new JFileChooser());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            System.out.println(file);
	        } else {
	            //cancelled
	        }
		}
		
		if(e.getSource().equals(menuSaveSamples)) {
			//TODO: unstatify
			Sample.saveSamples();
		}
		
		if(e.getSource().equals(menuSaveSamplesAs)) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(new JFileChooser());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            System.out.println(file);
	        } else {
	            //cancelled
	        }
		}
		
		if(e.getSource().equals(menuLoadNet)) {
			//net.load(file)();
		}
		
		if(e.getSource().equals(menuLoadSamples)) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(new JFileChooser());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            System.out.println(file);
	        } else {
	            //cancelled
	        }
		}
	}
}