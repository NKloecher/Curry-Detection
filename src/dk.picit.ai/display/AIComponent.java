/*
 * Created on 2005-01-03
 *
 * This file AIComponent.java is part of the FreeCode projekt
 */
package dk.picit.ai.display;

import dk.picit.ai.Net;
import dk.picit.ai.Sample;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class AIComponent extends JComponent {
    
	private static final long serialVersionUID = 1685443253803077186L;
	
	private static final Random RND = new Random();
	
	private Dimension dim;
    private Image offScreenImage;
    private Graphics2D offScreenGraphics2D;
    
    private int ileft,iright,itop,ibottom,ih,iw;
    
    double xmin = -0.10;
    double xmax = 1.0;
    double ymin = -0.10;
    double ymax = 1.0;
    
    private int iterations;
    private long startTime;
	private long lastUpdate = System.currentTimeMillis();
	private double lastDistance = 0;
	private double hitRateAvg;
	private double lastHitRateAvg;
	private double hitRateSpeed;
    
	private Net net;


    AIComponent(Net net, int width, int height, int iterations) {
    	this.net = net;
        dim = new Dimension(width, height);
        this.iterations = iterations;
    }
    
    
    AIComponent(Net net, int width, int height, double xmin, double xmax, double ymin, double ymax, int iterations) {
        this(net, width, height, iterations);
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }
    
    
    @Override
    public Dimension getPreferredSize() {
    	return dim;
    }
    
    @Override
    public Dimension getMinimumSize() {
    	return dim;
    }
    
    @Override
    public Dimension getMaximumSize() {
    	return dim;
    }
    
    @Override
    public void update(Graphics g){
        paint(g);
    }

    
    public void paintComponent(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = createImage(dim.width, dim.height);
            offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
            //offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            paintGrid(offScreenGraphics2D);
        }
        //offScreenGraphics2D.translate(.1, 0);
        
        g.drawImage(offScreenImage, (getWidth() - dim.width) / 2, (getHeight() - dim.height) / 2, null);
    }

    
    private void paintGrid(Graphics2D g2D) {
        int h = dim.height - 1;
        int w = dim.width - 1;

        ileft = 30;
        iright = 10;
        itop = 10;
        ibottom = 20;
        iw = w - ileft - iright;
        ih = h - itop - ibottom;
        
        g2D.setColor(Color.black);
        g2D.fillRect(0, 0, w, h);

        //draw grid
        drawHorizontalGrid(g2D);
        drawVerticalGrid(g2D);
        drawCoordinateLines(g2D);
    }
    
    private void drawHitRate(Graphics2D g, double rate) {
    	int[] size = net.getSize();
    	g.setColor(Color.black);
		g.fillRect(15, 6, 64, 54);
		if(startTime == 0) startTime = System.currentTimeMillis();
		g.setColor(Color.white);
        g.drawString(String.format("%.1f", 0.001*(System.currentTimeMillis()-startTime)), 15, 16);
		g.drawString(String.format("%.1f", 100*hitRateAvg) + " - " + String.format("%.1f", hitRateSpeed), 15, 30);
		g.drawString("" + size[1], 15, 44);
		g.drawString("" + Sample.samples.size() + " - " + String.format("%.1f", rate), 15, 58);
    }
    
    private void drawCoordinateLines(Graphics2D g) {
        g.setColor(Color.white);
        int x = ileft + (int)(iw*((0.0 - xmin)/(xmax - xmin)));
        int y = itop  + ih - (int)(ih*((0.0 - ymin)/(ymax - ymin)));
        g.drawLine(x, itop, x, itop + ih);
        g.drawLine(ileft, y, ileft + iw, y);
    }
    
    private void drawHorizontalGrid(Graphics2D g) {
        g.setColor(Color.gray);
        drawHorizontalMarker(g, 0.0);
        for(double i = 0.25; i < xmax; i += 0.25) {
            drawHorizontalMarker(g, i);
        }
        for(double i = -0.25; i > xmin; i -= 0.25) {
            drawHorizontalMarker(g, i);
        }
    }
    
    private void drawVerticalGrid(Graphics2D g) {
        g.setColor(Color.gray);
        drawVerticalMarker(g, 0.0);
        for(double i = 0.25; i < ymax; i += 0.25) {
            drawVerticalMarker(g, i);
        }
        for(double i = -0.25; i > ymin; i -= 0.25) {
            drawVerticalMarker(g, i);
        }
    }
    
    
    private void drawHorizontalMarker(Graphics2D g, double pos) {
        int y = itop + ih - (int)(ih*((pos - ymin)/(ymax-ymin)));
        g.drawLine(ileft, y, ileft + iw, y);
        g.drawString("" + pos, 3, y);
    }
    
    
    private void drawVerticalMarker(Graphics2D g, double pos) {
        int x = ileft + (int)(iw*((pos - xmin)/(xmax - xmin)));
        g.drawLine(x, itop, x, itop + ih);
        g.drawString("" + pos, x, itop + ih + ibottom - 3);
    }
    

    public void addPoint(double dx, double dy, Color c, int type) {
    	if (offScreenImage == null) {
            offScreenImage = createImage(dim.width, dim.height);
            offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
            offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            paintGrid(offScreenGraphics2D);
        }

    	if (type == 2 && lastUpdate < System.currentTimeMillis() - 1000) {
    		hitRateSpeed = 1000.0*iterations*(dx - lastDistance)*(hitRateAvg)/(System.currentTimeMillis() - lastUpdate);
    		drawHitRate(offScreenGraphics2D, 1000.0*iterations*(dx - lastDistance)/(System.currentTimeMillis() - lastUpdate));
    		lastUpdate = System.currentTimeMillis();
    		lastHitRateAvg = hitRateAvg;
    		lastDistance = dx;
    	}
    	else if(type == 1) {
        	hitRateAvg = dy;
    	}
    	
    	if(type < 2) {
    		//draw scatter points
	        int y = itop  + ih - (int)(1.0*ih*((dy - ymin)/(ymax - ymin)));
	        int x = ileft + (int)(1.0*iw*((dx /*+ 0.001*RND.nextGaussian()*/ - xmin)/(xmax - xmin)));
	        offScreenGraphics2D.setColor(c);
	        
	        if(type == 1) {
	        	offScreenGraphics2D.fillOval(x-1, y-1, 2, 2);
	        }
	        else {
	        	offScreenGraphics2D.fillOval(x, y, 1, 1);
	        }
        }
    }
    
    
    public void addLine(double dx, double dy, double dx2, double dy2, Color c) {
        //Pixel pix = new Pixel(p, c);
        //points.addElement(pix);
    	
    	if (offScreenImage == null) {
            offScreenImage = createImage(dim.width, dim.height);
            offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
            //offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            paintGrid(offScreenGraphics2D);
        }
        
        //draw scatter points
        int y = itop  + ih - (int)(1.0*ih*((dy - ymin)/(ymax - ymin)));
        int x = ileft + (int)(1.0*iw*((dx - xmin)/(xmax - xmin)));
        int y2 = itop  + ih - (int)(1.0*ih*((dy2 - ymin)/(ymax - ymin)));
        int x2 = ileft + (int)(1.0*iw*((dx2 - xmin)/(xmax - xmin)));
        offScreenGraphics2D.setColor(c);
        offScreenGraphics2D.drawLine(x, y, x2, y2);
    }
}