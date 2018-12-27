package dk.picit.ai.test;

import dk.picit.ai.Net;
import dk.picit.ai.Result;
import dk.picit.ai.Sample;
import dk.picit.ai.Sensor;
import dk.picit.ai.display.AIMainFrame;

import java.awt.*;
import java.io.File;

public class AIMainOCR extends Net {
	
	
	public AIMainOCR(String dataFileName, int width, int height) {
		
		File dataFile = null;
		if(dataFileName != null) dataFile = load(new File(dataFileName));
		
		if(dataFile == null) {
			new Sensor(this, "Cam-RTC", width, height);
			new AsciiFlavor(this, "Classification", Color.orange);
			
			randomize();
		}
	}
	
	
	public static void main(String[] args) {
		
		Dimension tileLayout = new Dimension(1, 1);
		int width = 24;
		int height = 24;
		
//		String defaultPathName = "C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\AI_Picit_Char_Training";		//Original 7k images
		String defaultPathName = "C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\Billeder\\ACD_Data_Chars_AI_Training";	//ACD 1.2k images

		boolean headless = false;
		boolean doTraining = true;
		
		AIMainOCR net = new AIMainOCR("aidata.dat", width, height);
		
		float iterations = 100000;
		AIMainFrame mainFrame = null;
		if(!headless) mainFrame = new AIMainFrame(net, (int)iterations);

		if (new File("samples.dat").exists()) Sample.loadSamples(net.flavors); //loads in all previous samples?? → new samples disappear in the forest
		
		System.out.println("Testing net for iterations:" + iterations);
		
		for(int i = 0; i < iterations; i++) {
//			Sample sample = AIAsciiSample.loadSample(new File("C:\\Users\\picit_nk\\IdeaProjects\\chili-container\\res\\OCR_Input\\all1_6contours5.jpg"), net.flavors, width, height, tileLayout);
			Sample sample = AIAsciiSample.loadRandomSample(defaultPathName, net.flavors, width, height, tileLayout);
			if(sample != null ) {
				final int ii = i;
				sample.getFlavors().stream().forEach( (flavor) -> {
					Result result = sample.getResult(flavor, 3, ii, doTraining);
					if(headless) System.out.println("Looking for: " + sample.source + "\n" + result.toString());
				});
				if(!headless) mainFrame.update(sample, i / iterations);
			}
			else {
				System.err.println("sample is null! " + i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		Sample.saveSamples();
		if(doTraining) net.save();
    }
}