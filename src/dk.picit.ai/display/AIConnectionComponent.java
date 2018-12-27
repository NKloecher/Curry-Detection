package dk.picit.ai.display;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Sample;
import dk.picit.ai.Target;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;


public class AIConnectionComponent extends JComponent implements MouseListener {
    
	private static final long serialVersionUID = 1685443253803077186L;
	private static final Random RND = new Random();
	private static final int STACKSIZE = 10;
	private static final int STACKWIDTH = 287;
	
	private Dimension dim;
	private Graphics2D offScreenGraphics2D;

	private Image offScreenImage;
    private Image[] imglayers;
    private boolean[] pselected;
    private boolean[] nselected;
    private boolean[] sselected;
    private boolean pallselected = true;
    private boolean nallselected = true;
    private boolean sallselected = false;
    
    private int ileft,iright,itop,ibottom,ih,iw;
    
    double xmin = -0.10;
    double xmax = 1.0;
    double ymin = -0.10;
    double ymax = 1.0;
    
	private long lastUpdate = System.currentTimeMillis();
	
	private Net net;
	private static transient int[][][] posVals;
	private static transient int[][][] negVals;
	private int curidx;


    AIConnectionComponent(Net net, int width, int height, int iterations) {
    	this.net = net;
    	dim = new Dimension(width, height);
    	imglayers = new Image[net.flavors.length];
    	pselected = new boolean[net.flavors.length];
    	nselected = new boolean[net.flavors.length];
    	sselected = new boolean[net.flavors.length];
    	
    	for(int i = 0; i < net.flavors.length; i++) {
    		pselected[i] = true;
    		nselected[i] = true;
    	}
    	
    	posVals = new int[net.flavors.length][STACKSIZE][STACKWIDTH];
    	negVals = new int[net.flavors.length][STACKSIZE][STACKWIDTH];
    	
    	addMouseListener(this);
    }
    
    
    AIConnectionComponent(Net net, int width, int height, double xmin, double xmax, double ymin, double ymax, int iterations) {
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
            offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            paintGrid(offScreenGraphics2D);
        }
        
        g.drawImage(offScreenImage, (getWidth() - dim.width) / 2, (getHeight() - dim.height) / 2, null);
        
        int i = 0;
        for(Flavor flavor : net.flavors) {
        	imglayers[i] =  new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        	Graphics2D g2d = (Graphics2D)imglayers[i].getGraphics();
        	g2d.setColor(flavor.color);
        	
        	int max = 1;
    		for(int j = 0; j < STACKWIDTH; j++) {
    			int sums = 0;
    			for(int c = 0; c < posVals[i].length; c++) {
    				sums += posVals[i][c][j];
    				sums += negVals[i][c][j];
    			}
    			
    			if(max < sums) max = sums;
    		}

    		for(int j = 0; j < STACKWIDTH; j++) {
    			
    			int sump = 0;
    			int sumn = 0;
    			for(int c = 0; c < posVals[i].length; c++) {
    				sump += posVals[i][c][j];
    				sumn += negVals[i][c][j];
    			}
    			
    			float dyp = 1.0f * sump / max;
    			float dyn = 1.0f * sumn / max;
    			float dys = 1.0f * (sump + sumn) / max;
    			
    			float dx = 1.0f * j / STACKWIDTH;
                int x = ileft + (int)(1.0*iw*((dx - + xmin)/(xmax - xmin)));
                
                int y = itop  + ih - (int)(1.0*ih*((dyp - ymin)/(ymax - ymin)));
                if(pselected[i]) g2d.fillOval(x, y, 3, 3);
               
                y = itop  + ih - (int)(1.0*ih*((dyn - ymin)/(ymax - ymin)));
                if(nselected[i]) g2d.fillOval(x, y, 3, 3);
                
                y = itop  + ih - (int)(1.0*ih*((dys - ymin)/(ymax - ymin)));
                if(sselected[i]) g2d.fillOval(x, y, 3, 3);
    		}
            
    		g.drawImage(imglayers[i], (getWidth() - dim.width) / 2, (getHeight() - dim.height) / 2, null);
        	
        	i++;
        }
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
        
        drawFlavorBoxes(g2D);
    }
    
    
    private void drawFlavorBoxes(Graphics2D g) {
    	
    	g.setColor(Color.white);
		g.drawRect(10, 50 + -2*16, 11, 11);
		if(pallselected) {
			g.fillOval(13, 53 + -2*16, 6, 6);
		}
		g.drawRect(30, 50 + -2*16, 11, 11);
		if(nallselected) {
			g.fillOval(33, 53 + -2*16, 6, 6);
		}
		g.drawRect(50, 50 + -2*16, 11, 11);
		if(sallselected) {
			g.fillOval(53, 53 + -2*16, 6, 6);
		}
		
    	
        int i = 0;
    	for(Flavor flavor : net.flavors) {
    		g.setColor(flavor.color);
    		g.drawRect(10, 50 + i*16, 11, 11);
    		if(pselected[i]) {
    			g.fillOval(13, 53 + i*16, 6, 6);
    		}
    		
    		g.drawRect(30, 50 + i*16, 11, 11);
    		if(nselected[i]) {
    			g.fillOval(33, 53 + i*16, 6, 6);
    		}
    		
    		g.drawRect(50, 50 + i*16, 11, 11);
    		if(sselected[i]) {
    			g.fillOval(53, 53 + i*16, 6, 6);
    		}
    		
    		i++;
    	}
    }
    
    
    private void drawCoordinateLines(Graphics2D g) {
        g.setColor(Color.white);
        int x = ileft + (int)(iw*((0.0 - xmin)/(xmax - xmin)));
        int y = itop  + ih - (int)(ih*((0.0 - ymin)/(ymax - ymin)));
        g.drawLine(x, itop, x, itop + ih);
        g.drawLine(ileft + 50, y, ileft + iw, y);
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
        g.drawLine(ileft + 50, y, ileft + iw, y);
//        g.drawString("" + pos, 3, y);
    }
    
    
    private void drawVerticalMarker(Graphics2D g, double pos) {
        int x = ileft + (int)(iw*((pos - xmin)/(xmax - xmin)));
        g.drawLine(x, itop, x, itop + ih);
//        g.drawString("" + pos, x, itop + ih + ibottom - 3);
    }
    

    public void updateComponent(Sample sample) {
    	int i = 0;
		for(Flavor flavor : net.flavors) {
			Target sampleTarget = sample.getTarget(flavor);
			if(sampleTarget != null) {
				float[] oVal = sample.getOvals();
				
				int zz = 0;
				for(int z = flavor.firstFlavoridx; z < flavor.firstFlavoridx + 64; z++) {
					for(int j = 0; j < posVals[i][curidx].length; j++) {
						if(oVal[z] >= (4.0*j/STACKWIDTH - 2.0) && oVal[z] <= (4.0*(j + 1)/STACKWIDTH - 2.0)) {
							if(((sampleTarget.targetVals >> zz) & 1L) > 0) posVals[i][curidx][j]++;
							else negVals[i][curidx][j]++;
							
							break;
						}
					}
					zz++;
				}
			}

			i++;
		}

    	if (lastUpdate < System.currentTimeMillis() - 100) {
    		lastUpdate = System.currentTimeMillis();
    		
    		curidx = (curidx + 1) % STACKSIZE;
    		repaint();
    	}
    }
    
    
    public void addLine(double dx, double dy, double dx2, double dy2, Color c) {
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


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		
		Rectangle prect = new Rectangle(10, 50 + -2*16, 12, 12);
		Rectangle nrect = new Rectangle(30, 50 + -2*16, 12, 12);
		Rectangle srect = new Rectangle(50, 50 + -2*16, 12, 12);
		
		if(prect.contains(e.getPoint())) {
			pallselected = !pallselected;
			for(int i = 0; i < pselected.length; i++) {
				pselected[i] = pallselected;
			}
		}
		else if(nrect.contains(e.getPoint())) {
			nallselected = !nallselected;
			for(int i = 0; i < nselected.length; i++) {
				nselected[i] = nallselected;
			}
		}
		else if(srect.contains(e.getPoint())) {
			sallselected = !sallselected;
			for(int i = 0; i < sselected.length; i++) {
				sselected[i] = sallselected;
			}
		}
		else {
			int i = 0;
	    	for(Flavor flavor : net.flavors) {
	    		Rectangle rect;
	    		rect = new Rectangle(10, 50 + i*16, 12, 12);
	    		if(rect.contains(e.getPoint())) pselected[i] = !pselected[i];
	    		
	    		rect = new Rectangle(30, 50 + i*16, 12, 12);
	    		if(rect.contains(e.getPoint())) nselected[i] = !nselected[i];
	    		
	    		rect = new Rectangle(50, 50 + i*16, 12, 12);
	    		if(rect.contains(e.getPoint())) sselected[i] = !sselected[i];
	    		
	    		i++;
	    	}
		}
		
    	offScreenImage = createImage(dim.width, dim.height);
		offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
		offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		paintGrid(offScreenGraphics2D);
		repaint();
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}