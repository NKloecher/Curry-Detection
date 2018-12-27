package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Sample;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;


public class AIAsciiSample extends Sample {
	
	private transient static final Random RND = new Random();
	
	private transient static File path;
	private transient static File[] fileList;
	
	
	@Override
	public void prepare(Flavor flavor) {
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
				speckle = (RND.nextDouble() - .5) * .3;
				
				inputs[i] += speckle;
				
				if (inputs[i] > 1) inputs[i] = 1;
				else if (inputs[i] < -1) inputs[i] = -1;
			}
		}	
	}

	private static BufferedImage getBufferedImage(File file) throws IOException {
		ImageIO.setUseCache(false);
		return ImageIO.read(Files.newInputStream(file.toPath()));
	}

	
	private static float[] getPixels(File[] files, Flavor[] flavors, int width, int height, Dimension tiledim) throws IOException {
    	float[] origInputs = new float[width*height];
    	
    	BufferedImage bi = getBufferedImage(files[0]);
    	Image ii = bi.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
    	bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	
    	Graphics g0 = bi.getGraphics();
    	g0.drawImage(ii, 0, 0, null);
    	g0.dispose();
		
		if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
	          BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
	          Graphics2D g = tmp.createGraphics();
	          g.drawImage(bi, 0, 0, null);
	          g.dispose();
	          bi = tmp;
	    }

		int[] pixArray = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
		double min = Float.MAX_VALUE, max = Float.MIN_VALUE;
		for (int i = 0; i < pixArray.length; i++) {
			double val = (((pixArray[i] >> 16) & 0xFF)
					+  ((pixArray[i] >>  8) & 0xFF)
					+  ((pixArray[i]      ) & 0xFF))/3;
			if (val > max) max = val;
			if (val < min) min = val;
			pixArray[i] = (int) val;
		}

//		System.out.println("MIN: " + min);
//		System.out.println("MAX: " + max);

		double a = 255/(max-min);
//		double b = 255-a*max;

		// image range to -1 >> 1
		for (int j = 0; j < pixArray.length; j++) {
			// 0-255 greyscale value
			double val = pixArray[j];
			// normalized scale to actual 0-255 values
			val = a*(val-max)+255;
			// scaling to -1 >> 1 STIGMOID
			origInputs[j] = (float) (2*val/255.f - 1);
		}

		return origInputs;
	}

	public static Sample loadMatSample(Mat mat, Flavor[] flavors, int width, int height, Dimension tiledim) {
		AIClassificationSample sample = new AIClassificationSample();
		try {
			sample.addFlavorTarget(flavors[0], "");
			sample.origInputs = getPixelsFromMat(mat, flavors, width, height, tiledim);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(!samples.containsKey(sample.source)) samples.put(sample.source, sample);
		return sample;
	}
	private static BufferedImage getBufferedImageFromMat(Mat mat) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", mat, mob);
		byte ba[] = mob.toArray();
		return ImageIO.read(new ByteArrayInputStream(ba));
	}


	private static float[] getPixelsFromMat(Mat mat, Flavor[] flavors, int width, int height, Dimension tiledim) throws IOException {
		float[] origInputs = new float[width*height];

		BufferedImage bi = getBufferedImageFromMat(mat);
		if (bi == null) return null;
		Image ii = bi.getScaledInstance(width, height, BufferedImage.SCALE_AREA_AVERAGING);
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics g0 = bi.getGraphics();
		g0.drawImage(ii, 0, 0, null);
		g0.dispose();

		if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
			BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = tmp.createGraphics();
			g.drawImage(bi, 0, 0, null);
			g.dispose();
			bi = tmp;
		}

		int[] pixArray = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
		double min = Float.MAX_VALUE, max = Float.MIN_VALUE;
		for (int i = 0; i < pixArray.length; i++) {
			double val = (((pixArray[i] >> 16) & 0xFF)
					+  ((pixArray[i] >>  8) & 0xFF)
					+  ((pixArray[i]      ) & 0xFF))/3;
			if (val > max) max = val;
			if (val < min) min = val;
			pixArray[i] = (int) val;
		}

		double a = 255/(max-min);

		// image range to -1 >> 1
		for (int j = 0; j < pixArray.length; j++) {
			// 0-255 greyscale value
			double val = pixArray[j];
			// normalized scale to actual 0-255 values
			val = a*(val-max)+255;
			// scaling to -1 >> 1 STIGMOID
			origInputs[j] = (float) (2*val/255.f - 1);
		}

		return origInputs;
	}




	public static Sample loadSample(File file, Flavor[] flavors, int width, int height, Dimension tiledim) {

		File[] files = new File[tiledim.width*tiledim.height];
		files[0] = file;
		
		AIClassificationSample sample = new AIClassificationSample();
		sample.source = file.getAbsolutePath();
		
        try {
        	System.err.println(file.getName().charAt(0) + " " + file.getName());
        	sample.addFlavorTarget(flavors[0], file.getName().charAt(0));
        	
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
			        return true;
//			    	return name.endsWith("002.jpg");
			    }
			});
			
			if(fileList == null) return null;
		}

		int i;
		String key;
		
		if(samples.keySet().size() >= 100 && Net.avgHitRate < 0.85) {
//		if(samples.keySet().size() > 20000) {
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