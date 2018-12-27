package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Sample;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AIClassificationSample extends Sample {
	
	private static final long serialVersionUID = -5741662238918749184L;

	private transient static final Random RND = new Random();
	
	private transient static File path;
	private transient static File[] fileList;
	
	private transient static ExecutorService service = Executors.newFixedThreadPool(3);
	
	private transient short[] mask;
	
	public static Area shape = new Area();
	
	public void writeMeta() {
		BufferedWriter metawriter = null;
    	try  {
    		String metafilename = source.replace(".jpg", ".meta");
    		metawriter = new BufferedWriter(new FileWriter(metafilename));
    		for(Flavor flavor : getFlavors()) {
    			Object value = getValue(flavor);
				if(flavor instanceof PointFlavor) {
					if(value != null) {
						Point2D.Float p = (Point2D.Float)value;
						metawriter.write(flavor.id + ":" + p.x + "," + p.y + "\r\n");
					} else {
						metawriter.write(flavor.id + ":NA" + "\r\n");
					}
				}
				else metawriter.write(flavor.id + ":" + value + "\r\n");
				
				System.out.println(flavor.id + ":" + value);
    		}
    		metawriter.flush();
    		metawriter.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	
	
	@Override
	protected void prepare(Flavor flavor) {
		super.prepare(flavor);
		
		inputs = new float[origInputs.length];
		for(int i = 0; i < origInputs.length; i++) {
			inputs[i] = origInputs[i];
		}
		
		boolean useSpeckles = true;
		if(useSpeckles) {
    		//add speckles
			double speckle = 0;
			for(int i = 0; i < inputs.length; i++) {
				speckle = (RND.nextDouble() - .5) * .1;
				
				inputs[i] += speckle;
				
				if (inputs[i] > 1) inputs[i] = 1;
				else if (inputs[i] < -1) inputs[i] = -1;
			}
		}
		
//		if(mask == null) mask = new short[origInputs.length];
//		for(int i = 0; i < inputs.length; i++) {
//			if(mask[i] == 0) inputs[i] = -1;
//		}
		
//		char c = (char)getValue(getFlavorForId("Classification"));
//		int ic = (int)c;
//		for(int i = 0; i < 32; i++) {
//			if ((ic & 1 << i) != 0) inputs[i] = 1;
//			else inputs[i] = -1;
//		}
	}
	
	
//	@Override
//	public void addFlavorTarget(String flavorId, Object value) {
//		Flavor flavor = getFlavorForId(flavorId);
//		if(flavor == null) {
//			for(Flavor fl : flavors) {
//        		if(flavorId.equals(fl.id)) {
//        			flavor = fl;
//        			break;
//        		}
//        	}
//		}
//		addFlavorTarget(flavor, value);
//	}
	
	
	private static BufferedImage getBufferedImage(File file) throws IOException {
		ImageIO.setUseCache(false);
		return ImageIO.read(Files.newInputStream(file.toPath()));
//		return ImageIO.read(file);
	}
	
	
	private static float[] getPixels(File[] files, Flavor[] flavors, int width, int height, Dimension tiledim) throws IOException {
    	float[] origInputs = new float[width*height];
    	
    	int tileheight = height/tiledim.height;
    	int tilewidth = width/tiledim.width;
//    	System.out.println(width + " " +  height + " " + tilewidth + " " + tileheight);

		
    	for(int n = 0; n < tiledim.height; n++) {
    		for(int m = 0; m < tiledim.width; m++) {
    			
	        	BufferedImage bi = getBufferedImage(files[m + tiledim.width*n]);
				final int imgwidth = bi.getWidth();
				final int imgheight = bi.getHeight();
				
//				if(n == 0 && m == 1) {
//					Area mask = new Area(new Rectangle(0,0,imgwidth,imgheight));
//					Area shape = new Area(AIClassificationSample.shape);
//					shape.transform(AffineTransform.getScaleInstance(imgwidth/2000.0, imgheight/2000.0));
//					shape.transform(AffineTransform.getTranslateInstance(imgwidth/2, imgheight/2));
//					mask.subtract(shape);
//					
//					System.err.println(shape.getBounds2D());
//					
//					Graphics2D g2d = bi.createGraphics();
//					g2d.setColor(Color.black);
//					g2d.fill(mask);
//					g2d.dispose();
//				}
				
				
//				final int[] pixArray = new int[imgwidth*imgheight];
//				bi.getRGB(0, 0, imgwidth, imgheight, pixArray, 0, imgwidth);
				if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
			          BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
			          Graphics2D g = tmp.createGraphics();
			          g.drawImage(bi, 0, 0, null);
			          g.dispose();
			          bi = tmp;
			    }
			    final int[] pixArray = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();

				final int scalewidth = imgwidth/tilewidth;
				final int scaleheight = imgheight/tileheight;
				
				for (int k = 0; k < tileheight; k++) {
					int idx = scalewidth/2 + k*scaleheight*imgwidth;
					int offset = k*tilewidth + (m + tiledim.width*n)*tilewidth*tileheight;
					
						int bidx = idx + imgwidth;
						for (int j = 0; j < tilewidth; j++) {
							origInputs[j + offset] = (2*(((pixArray[bidx] >> 16) & 0xFF)/255.f - .5f)
												   +  2*(((pixArray[bidx] >>  8) & 0xFF)/255.f - .5f)
												   +  2*(((pixArray[bidx]      ) & 0xFF)/255.f - .5f))/3;
							
							bidx += scalewidth;
						}
				}
    		}
    	}
    	
    	return origInputs;
	}
	
	
	private static Sample loadSample(File file, Flavor[] flavors, int width, int height, Dimension tiledim) {
//		if(AIClassificationSample.flavors == null) AIClassificationSample.flavors = flavors;

		File[] files = new File[tiledim.width*tiledim.height];
		files[0] = file; //new File(file.getAbsolutePath().replace("002.jpg", "009.jpg"));
//		files[1] = file;
//		files[2] = new File(file.getAbsolutePath().replace("002.jpg", "007.jpg"));
//		files[3] = new File(file.getAbsolutePath().replace("002.jpg", "006.jpg"));
//		files[4] = new File(file.getAbsolutePath().replace("002.jpg", "014.jpg"));
//		files[5] = new File(file.getAbsolutePath().replace("002.jpg", "001.jpg"));
		
		AIClassificationSample sample = new AIClassificationSample();
		sample.source = file.getAbsolutePath();
		
//		for(Flavor flavor : flavors) {
//    		sample.addFlavorTarget(flavor, null);
//    		
//    		//TODO: UNHACK
//    		if(flavor instanceof ClassificationFlavor) {
//    			sample.addFlavorTarget(flavor, file.getName().charAt(8));
//    		}
//    	}
    	
        try {
        	String metafilename = sample.source.replace(".jpg", ".meta");
        	BufferedReader metareader = null;
        	try  {
	        	metareader = new BufferedReader(new FileReader(metafilename));
	        	
	        	String metaline;
	        	while((metaline = metareader.readLine()) != null) {
	        		String[] metaInfo = metaline.split(":");
	        		if(metaInfo[0].equals("ClassId")) metaInfo[0] = "Classification"; 
	        		else if(metaInfo[0].equals("Classification")) ;
//	        		else if(!metaInfo[0].startsWith("Box.rt")) 
//	        			continue;
	        			
	        		Flavor flavor = null;
	        		for(Flavor fl : flavors) {
		        		if(fl.id.equals(metaInfo[0])) {
		        			flavor = fl;
		        			break;
		        		}
		        	}
	        		
	        		if(flavor instanceof LineFlavor) {
	        			Flavor flavorx = null;
		        		for(Flavor fl : flavors) {
			        		if(fl.id.equals(metaInfo[0] + ".x")) {
			        			flavorx = fl;
			        			break;
			        		}
			        	}
	        			
	        			Flavor flavory = null;
		        		for(Flavor fl : flavors) {
			        		if(fl.id.equals(metaInfo[0] + ".y")) {
			        			flavory = fl;
			        			break;
			        		}
			        	}
		        		
	        			if(metaInfo[1].equals("NA")) {
	        				sample.addFlavorTarget(flavorx, null);
	        				sample.addFlavorTarget(flavory, null);
	        			}
	        			else {
	        				String[] f = metaInfo[1].split(",");
	        				sample.addFlavorTarget(flavorx, Float.parseFloat(f[0]));
	        				sample.addFlavorTarget(flavory, Float.parseFloat(f[1]));
	        			}
			        }
	        		else if(flavor instanceof PointFlavor) {
	        			if(metaInfo[1].equals("NA")) {
	        				sample.addFlavorTarget(flavor, null);
	        			}
	        			else {
	        				String[] f = metaInfo[1].split(",");
	        				sample.addFlavorTarget(flavor, new Point2D.Float(Float.parseFloat(f[0]), Float.parseFloat(f[1])));
	        			}
			        }
	        		else if(flavor instanceof ClassificationFlavor) {
	        			sample.addFlavorTarget(flavor, metaInfo[1].charAt(0));
	        		}
	        	}
        	} catch (FileNotFoundException e) {
			} finally {
				if(metareader != null) metareader.close();
			}
        	
        	Object o0 = sample.getValue(sample.getFlavorForId("Classification"));
        	if(o0 != null) {
	        	char cl = (char)o0;
	        	if(cl == 'L' || cl == 'X' || cl == 'E') {
	        		for(Flavor fl : flavors) {
		        		if("Box.iso_tl.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_tl.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_br.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_br.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.isoida_tl.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.isoida_tl.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.isoida_br.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.isoida_br.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        	}
	        	}
	        	else if(cl == 'T') {
	        		for(Flavor fl : flavors) {
		        		if("Box.iso_tl.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_tl.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_br.x".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        		else if("Box.iso_br.y".equals(fl.id)) sample.addFlavorTarget(fl, null);
		        	}
	        	}
        	}
        	
        	Polygon newShape = new Polygon();
        	Point2D.Float[] box = new Point2D.Float[6];
        	for(Flavor fl : flavors) {
        		if(sample.getTarget(fl) != null) {
	        		Object o = sample.getTarget(fl).value;
	        		if(o != null) {
		        		if("Box.rtr".equals(fl.id)) box[0] = (Point2D.Float)o;
		        		else if("Box.rbr".equals(fl.id)) box[1] = (Point2D.Float)o;
		        		else if("Box.rbl".equals(fl.id)) box[2] = (Point2D.Float)o;
		        		else if("Box.rtl".equals(fl.id)) box[3] = (Point2D.Float)o;
		        		else if("Box.ftl".equals(fl.id)) box[4] = (Point2D.Float)o;
		        		else if("Box.ftr".equals(fl.id)) box[5] = (Point2D.Float)o;
	        		}
        		}
        	}

        	for(int i = 0; i < box.length; i++) {
        		if(box[i] != null) newShape.addPoint((int)(1000*box[i].x), (int)(1000*box[i].y));
        	}
        	shape.add(new Area(newShape));
        	
        	sample.origInputs = getPixels(files, flavors, width, height, tiledim);
        } catch(Exception e) {
            e.printStackTrace();
        }
		
        if(!samples.containsKey(sample.source)) samples.put(sample.source, sample);
		return sample;
	}
	
	
	public static Sample loadRandomSample(String pathStr, Flavor[] flavors, int width, int height, Dimension tiledim) {

		if(path == null) {
			path = new File(pathStr);
		
			fileList = path.listFiles(new FilenameFilter() {
			    @Override
			    public boolean accept(File dir, String name) {
//			    	if(RND.nextInt(10) > 0) return false;
			        return name.endsWith("002.jpg"); // || name.endsWith("007.jpg") || name.endsWith("009.jpg");
			    }
			});
			
			if(fileList == null) return null;
		}

		int i;
		String key;
		
//		System.err.println(Net.avgHitRate + " " + samples.keySet().size());
		
		if(samples.keySet().size() > 50 && Net.avgHitRate < 0.75) {
			i = RND.nextInt(samples.keySet().size());
			String[] s = new String[samples.keySet().size()];
			key = (String)samples.keySet().toArray(s)[i];
		} else {
			i = RND.nextInt(fileList.length);
			key = fileList[i].getAbsolutePath();
		}
		
		if(samples.containsKey(key)) return samples.get(key);
		else {
			Sample sample = loadSample(fileList[i], flavors, width, height, tiledim);
			samples.put(key, sample);
			return sample;
		}
	}
}