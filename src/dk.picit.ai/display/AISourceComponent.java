package dk.picit.ai.display;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Sample;
import dk.picit.ai.test.AIClassificationSample;
import dk.picit.ai.test.LineFlavor;
import dk.picit.ai.test.PointFlavor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class AISourceComponent extends JPanel implements MouseListener, KeyListener {
    
	private static final long serialVersionUID = 3788448444428595505L;

    private Dimension dim;
	
	private Image offScreenImage;
    private Graphics2D offScreenGraphics2D;
    
    private ArrayList<Point2D.Float> clicks = new ArrayList<Point2D.Float>();
    private ArrayList<Point2D.Float> altclicks = new ArrayList<Point2D.Float>();
    private ArrayList<Point2D.Float> ctrlaltclicks = new ArrayList<Point2D.Float>();
    
    private Net net;
    private Sample sample;
    
    private JPanel[] imagesPanes; 

    
    AISourceComponent(Net net, int width, int height) {
    	this.net = net;
    	this.dim = new Dimension(width, height);
    	
    	addMouseListener(this);
    	addKeyListener(this);
    	
    	imagesPanes = new JPanel[6];
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
    
    
	public void updateComponent(Sample sample) {
		
		try {
			if(this.sample != sample) {
				BufferedImage bi = ImageIO.read(new File(sample.source));
				Image offScreenImage = bi.getScaledInstance(dim.width, dim.height, BufferedImage.SCALE_FAST);
				if(offScreenGraphics2D != null) offScreenGraphics2D.drawImage(offScreenImage, 0, 0, this);
			}
			
			Float xx = null;
//			for(Flavor flavor : sample.getFlavors()) {
			for(Flavor flavor : net.flavors) {
				if(flavor instanceof PointFlavor) {
//					System.err.println(flavor.id + " " + target + " " + (target == null?"":target.value));
					
					Object value = sample.getValue(flavor);
					if(value != null) {
						if(value instanceof Point2D.Float) {
							Point2D.Float p = (Point2D.Float)value;
							addPoint((int)(dim.width*((1 + p.x)/2.0)), (int)(dim.height*((1 + p.y)/2.0)), flavor.color);
						}
					}
				}
				else if(flavor instanceof LineFlavor) {
//					System.err.println(flavor.id + " " + target + " " + (target == null?"":target.value));
					Object value = sample.getValue(flavor);
					if(value != null) {
						if(value instanceof Float) {
							if(xx == null) {
								xx = (Float)value;
							}
							else {
								addPoint((int)(dim.width*((1 + xx)/2.0)), (int)(dim.height*((1 + (Float)value)/2.0)), flavor.color);
								xx = null;
							}
						}
					}
				}
			}
			
			if(offScreenGraphics2D != null) {
				offScreenGraphics2D.setColor(Color.red);
				
				Area mask = new Area(new Rectangle(0,0,dim.width,dim.height));
				Area shape = new Area(((AIClassificationSample)sample).shape);
				shape.transform(AffineTransform.getScaleInstance(dim.width/2000.0, dim.height/2000.0));
				shape.transform(AffineTransform.getTranslateInstance(dim.width/2, dim.height/2));
				mask.subtract(shape);
				offScreenGraphics2D.draw(mask);
				
	//			PathIterator pi = ((AIClassificationSample)sample).shape.getPathIterator(null);
	//        	for(Point2D.Float p : pi.) {
	//        		
	//        	}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.sample = sample;
		repaint();
	}

	
    public void update(Graphics g){
        paint(g);
    }

    
    public void paintComponent(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = createImage(dim.width, dim.height);
            offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
            //offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        g.drawImage(offScreenImage, (getWidth() - dim.width) / 2, (getHeight() - dim.height) / 2, null);
    }

    
    public void addPoint(double dx, double dy, Color c) {
    	if (offScreenImage == null) {
            offScreenImage = createImage(dim.width, dim.height);
            if(offScreenImage != null) offScreenGraphics2D = (Graphics2D)offScreenImage.getGraphics();
            //offScreenGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        else {
	        //draw scatter points
	        int y = (int)(dy);
	        int x = (int)(dx);
	        offScreenGraphics2D.setColor(c);
	        offScreenGraphics2D.fillOval(x-2, y-2, 5, 5);
        }
    }


	@Override
	public void mouseClicked(MouseEvent e) {

	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isShiftDown()) {
			clearClicks();
			net.updatenext = true;
		}
		else if(e.isControlDown() && e.isAltDown()) {
			ctrlaltclicks.add(new Point2D.Float((float)(2*(1.0*e.getX()/dim.width - 0.5)), (float)(2*(1.0*e.getY()/dim.height - 0.5))));
			if(ctrlaltclicks.size() == 2) {
				sample.addFlavorTarget("Box.isoida_tl", ctrlaltclicks.get(0));
				sample.addFlavorTarget("Box.isoida_br", ctrlaltclicks.get(1));
				((AIClassificationSample)sample).writeMeta();
				clearClicks();
				updateComponent(sample);
				net.updatenext = true;
			}
		}
		else if(e.isAltDown()) {
			altclicks.add(new Point2D.Float((float)(2*(1.0*e.getX()/dim.width - 0.5)), (float)(2*(1.0*e.getY()/dim.height - 0.5))));
			if(altclicks.size() == 2) {
				sample.addFlavorTarget("Box.iso_tl", altclicks.get(0));
				sample.addFlavorTarget("Box.iso_br", altclicks.get(1));
				altclicks.clear();
				((AIClassificationSample)sample).writeMeta();
				updateComponent(sample);
			}
		}
		else if(e.isControlDown()) {
			sample.addFlavorTarget("Box.iso_tl", null);
			sample.addFlavorTarget("Box.iso_br", null);
			sample.addFlavorTarget("Box.rtl", null);
			sample.addFlavorTarget("Box.rtr", null);
			sample.addFlavorTarget("Box.rbr", null);
			sample.addFlavorTarget("Box.rbl", null);
			sample.addFlavorTarget("Box.ftl", null);
			sample.addFlavorTarget("Box.ftr", null);
			sample.addFlavorTarget("Box.isoida_tl", null);
			sample.addFlavorTarget("Box.isoida_br", null);

			((AIClassificationSample)sample).writeMeta();
			updateComponent(sample);
			clearClicks();
		}
		else {
			clicks.add(new Point2D.Float((float)(2*(1.0*e.getX()/dim.width - 0.5)), (float)(2*(1.0*e.getY()/dim.height - 0.5))));
			if(clicks.size() == 6) {
				sample.addFlavorTarget("Box.rtl", clicks.get(0));
				sample.addFlavorTarget("Box.rtr", clicks.get(1));
				sample.addFlavorTarget("Box.rbr", clicks.get(2));
				sample.addFlavorTarget("Box.rbl", clicks.get(3));
				sample.addFlavorTarget("Box.ftl", clicks.get(4));
				sample.addFlavorTarget("Box.ftr", clicks.get(5));

				clicks.clear();
				
				((AIClassificationSample)sample).writeMeta();
				updateComponent(sample);
			}
		}
	}
	
	
	private void clearClicks() {
		clicks.clear();
		altclicks.clear();
		ctrlaltclicks.clear();
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == ' ') {
			net.updatenext = true;
			clicks.clear();
			altclicks.clear();
		}
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}