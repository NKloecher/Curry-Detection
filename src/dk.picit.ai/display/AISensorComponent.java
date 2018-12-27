package dk.picit.ai.display;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Result;
import dk.picit.ai.Sample;
import dk.picit.ai.Sensor;
import dk.picit.ai.Target;
import dk.picit.ai.test.PointFlavor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;


public class AISensorComponent extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	
	private Net net;
	private Sensor[] sensors;
	private Flavor[] flavors;
	private Sample sample;
	
	private double[] sensorInputSample;

	private Graphics2D offScreenGraphics2D;
	private Image offScreenImage;
	private int off_w;
	private int off_h;
	
	private final int size = 4;
	private int tilesize = 24;
	
	public AISensorComponent(Net net, int width, int height) {
		this.net = net;
		this.sensors = net.sensors;
		this.flavors = net.flavors;
		
		Dimension dim = new Dimension(width, height);
		setMaximumSize(dim);
		setMinimumSize(dim);
		setPreferredSize(dim);
		
    	for(Sensor sensor : sensors) {
    		AISourceComponent sourceComp = new AISourceComponent(net, 580, 1);
    		addTab(sensor.id, sourceComp);
    	}
	}

	
	public void updateComponent(Sample sample) {
		if(sample == null || sample.inputs == null) return;
		this.sample = sample;
		
//		for(Sensor sensor : sensors) {
//    		AISourceComponent sourceComp = new AISourceComponent(net, 580, 100);
//    		sourceComp.updateComponent(sample);
//    	}
		
		sensorInputSample = new double[sample.origInputs.length];
		
		// TODO: perhaps nicify
		for(int i = 0; i < sample.origInputs.length; i++) {
			sensorInputSample[i] = sample.inputs[i];
		}
		
		repaint();
	}

	
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();
		if (offScreenImage == null 
				|| bounds.width != off_w
				|| bounds.height != off_h) {
			
			offScreenImage = createImage(bounds.width, bounds.height);
			offScreenGraphics2D = (Graphics2D) offScreenImage.getGraphics();
			offScreenGraphics2D.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			
			off_w = bounds.width;
			off_h = bounds.height;
		}

		paintGrid(offScreenGraphics2D);
		g.drawImage(offScreenImage, 0, 0, null);
	}

	
	private void paintGrid(Graphics2D g2D) {
		g2D.setColor(Color.black);
		g2D.clearRect(0, 0, off_w, off_h);
		
		if(sensorInputSample != null) {
			for(int n = 0; n < 1; n++) {
				for(int m = 0; m < 1; m++) {
					int tileoffset = (m + 3*n)*tilesize*tilesize;
					for(int j = 0; j < tilesize; j++) {
						for(int i = 0; i < tilesize; i++) {
							int br = (int)((255*(1 + sensorInputSample[tileoffset + i + j * tilesize])/2));
		//					int bg = (int)((255*(1 + sensorInputSample[i + j * sensor.width + sensor.width*sensor.width])/2));
		//					int bb = (int)((255*(1 + sensorInputSample[i + j * sensor.width + 2*sensor.width*sensor.width])/2));
							
							g2D.setColor(new Color(br, br, br)); //bg, bb));
							g2D.fillRect(20 + i * (size) + m*tilesize*size, 20 + j * (size) + n*tilesize*size, size, size);
						}
					}
				}
			}
		}
		
		if(sample != null) {
			for(Flavor flavor : flavors) {
				Result result = sample.getResult(flavor, 4, -1, false);
				if(result != null) {
					g2D.setColor(flavor.color);
					
					int ii = 0;
					for(Target target : result.targets) {
						if(flavor instanceof PointFlavor) {
							Point2D.Float value = (Point2D.Float)target.value;
							if(value != null) {
								int s = 2;
								if(ii++ > 0) s = 1;
								g2D.fillOval(20 + 0*tilesize*size + (int)(size*tilesize*(1 + value.x)/2 - s), 
												20 + 0*tilesize*size + (int)(size*tilesize*(1 + value.y)/2 - s), 
												2*s + 1, 2*s + 1);
							}
						}
					}
				}
			}			
		}
	}
}