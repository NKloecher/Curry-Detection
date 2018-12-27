package dk.picit.ai;

import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Sample implements Serializable {

	private static final long serialVersionUID = -3114095332037332559L;
	private static final Random RND = new Random();

	public static transient HashMap<String, Sample> samples = new HashMap<String, Sample>();

	public String source;
	public float[] origInputs;

	public transient float[] inputs;
	private transient float[] oVal;
	private transient float[] deltasAccum;

	private HashMap<String, Object> keyValues = new HashMap<String, Object>();
	private transient HashMap<Flavor, Object> flavorValues = new HashMap<Flavor, Object>();
	private transient HashMap<Flavor, Target> flavorTargets = new HashMap<Flavor, Target>();

	private transient int curSessionNo = Integer.MAX_VALUE;


	public float[] getOvals() {
		return oVal;
	}


	public static void saveSamples() {
		System.out.println("serializing samples");
		try {
			File file = new File("samples.tmp");
			FileOutputStream fout = new FileOutputStream(file);
			GZIPOutputStream zos = new GZIPOutputStream(fout);
			ObjectOutputStream oos = new ObjectOutputStream(zos);

			oos.writeObject(samples);
			oos.close();

			File fileOld = new File("samples.dat");
			if(fileOld.exists()) {
				boolean checkDelete = fileOld.delete();
				if(checkDelete) {
					file.renameTo(new File("samples.dat"));
				}
			}
			else file.renameTo(new File("samples.dat"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("done serializing samples");
	}


	public static void loadSamples(Flavor[] flavors) {
		try {
			File file = new File("samples.dat");
			FileInputStream fis = new FileInputStream(file);
			GZIPInputStream gis = new GZIPInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream (gis);

			System.out.println("deserializing samples");

			samples = ((HashMap<String, Sample>)ois.readObject());

			for(Sample sample : samples.values()) {
				sample.flavorValues = new HashMap<Flavor, Object>();
				sample.flavorTargets = new HashMap<Flavor, Target>();

				for(String key : sample.keyValues.keySet()) {
					for(Flavor flavor : flavors) {
						if(flavor.id.equals(key)) {
							sample.addFlavorTarget(flavor, sample.keyValues.get(key));
							break;
						}
					}
				}
			}

			ois.close();
			fis.close();

			System.out.println("done deserializing samples");
		}
		catch (FileNotFoundException e) {
			System.out.println("No such file: samples.dat");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Result getResult(Flavor flavor, int resultSize, int sessionNo, boolean adjust) {
//    System.err.println("getResult: " + flavor.id);

		if(oVal == null) {
			oVal = new float[flavor.net.nodes.length];
		}

		if(adjust && deltasAccum == null) {
			deltasAccum = new float[flavor.net.nodes.length];
		}

		if(adjust && curSessionNo == sessionNo) {
			for(int i = 0; i < oVal.length; i++) {
				deltasAccum[i] = 0;
			}
		}

		prepare(flavor);
		setOutput(flavor);

		Result result = calcResult(flavor, adjust?Math.max(2, resultSize):resultSize);
		if(adjust) {
			adjustWeights(flavor, result);
		}
		return result;
	}



	private Result calcResult(Flavor flavor, int resultSize) {
		float[] targettHits = calcTargetHits(flavor);

		float[] maxPopulated = new float[resultSize];
		int[] maxIdx = new int[resultSize];

		Result result = new Result(resultSize);
		for(int k = 0; k < resultSize; k++) {
			for(int j = 0; j < targettHits.length; j++) {
				if(targettHits[j] >= maxPopulated[k]) {
					maxPopulated[k] = targettHits[j];
					maxIdx[k] = j;
					result.targets[k] = flavor.targets[j];
				}
			}

			result.targets[k].quality = targettHits[maxIdx[k]];
			targettHits[maxIdx[k]] = -1;
		}
		return result;
	}


	private float[] calcTargetHits(Flavor flavor) {
		long sampleResult = 0;
		for(int i = 0; i < 64; i++) {
			if(oVal[flavor.firstFlavoridx + i] > 0) sampleResult |= (1L << i);
//			System.err.println(flavor.id + " " + oVal[flavor.firstFlavoridx + i] + " " + i + " " + flavor.firstFlavoridx + " " + (flavor.firstFlavoridx + i));
		}

		final float[] targetHits = new float[flavor.targets.length];
		for(int k = 0; k < targetHits.length; k++) {
			targetHits[k] = Long.bitCount(~(flavor.targets[k].targetVals ^ sampleResult))/64.0f;
//			for(int i = 0; i < 64; i++) {
////				if(oVal[flavor.firstFlavoridx + i] > 0) sampleResult |= (1L << i);	
////				System.err.println(oVal[flavor.firstFlavoridx + i]);
//				
//				float t = (flavor.targets[k].targetVals >> i) & 1L;
//				float o = oVal[flavor.firstFlavoridx + i];
//				
////				if(t > 0) targettHits[k] += 1 - (2.0 - o)*(1 - (2.0 - o))/4.0;
////				else targettHits[k] += 1 - (2.0 + o)*(2.0 + o)/4.0;
//				
//				if(t > 0 && o > t) targetHits[k] += 0;
//				else if(t > 0) targetHits[k] += (t-o)*(t-o);
//				if(t < 0 && o < t) targetHits[k] += 0;
//				else targetHits[k] += (t-o)*(t-o);
//			}
//			targetHits[k] = 1 - (float) Math.sqrt(targetHits[k]/(1*64));
		}
		return targetHits;
	}


	protected void prepare(Flavor flavor) {

	}


//	private void setOutput(Flavor flavor) {
//		for (int j = flavor.net.lastInputNo + 1; j < flavor.net.nodes.length; j++) {
//			float tVal = 0;
//			
//			final int lowerIdx = (j + 0)*flavor.net.connectCount;
//			final int upperIdx = (j + 1)*flavor.net.connectCount;
//			for(int i = lowerIdx; i < upperIdx; i++) {
//				float t = 0;
//				if(flavor.net.nodes[flavor.net.conIdxs[i]].isInputNode) t = inputs[flavor.net.nodes[flavor.net.conIdxs[i]].idxSense] * flavor.net.conWeights[i];
//				else t = oVal[flavor.net.conIdxs[i]] * flavor.net.conWeights[i];
//				
//				tVal += t;
//			}
//			
//			if(tVal > 10.0) tVal = 10.0f;
//			else if(tVal < -10.0) tVal = -10.0f;
//			oVal[j] = tVal;
//		}
//	}


	private void setOutput(Flavor flavor) {

//		System.err.println(flavor.id + " setoutput ------- ");

		for (int j = flavor.net.lastInputNo + 1; j < flavor.net.nodes.length; j++) {
			if(j >= flavor.net.firstOutputNo && flavor.net.nodes[j].flavor != flavor) continue;

			final int lowerIdx = (j + 0)*flavor.net.connectCount;
			final int upperIdx = (j + 1)*flavor.net.connectCount;

			boolean doScale = true;
			boolean doScaleUp = true;

			float tVal = 0;

			while(doScale) {
				doScale = false;
				tVal = 0;

				for(int i = lowerIdx; i < upperIdx; i++) {
					float t = 0;
					if(flavor.net.nodes[flavor.net.conIdxs[i]].isInputNode)
						t = inputs[flavor.net.nodes[flavor.net.conIdxs[i]].idxSense] * flavor.net.conWeights[i];
					else
						t = oVal[flavor.net.conIdxs[i]] * flavor.net.conWeights[i];

					tVal += t;
				}

				if(tVal < -2.0 || tVal > 2.0) {
					//scale input weights if tVal goes too high
					doScale = true;
					for(int i = lowerIdx; i < upperIdx; i++) {
						flavor.net.conWeights[i] /= 2;
					}
				}
				else if(!doScaleUp && tVal > -0.5 && tVal < 0.5) {
					//scale input weights if tVal goes too low

					doScale = true;
					doScaleUp = false;

					for(int i = lowerIdx; i < upperIdx; i++) {
						flavor.net.conWeights[i] *= 2;
					}
				}
			}
			oVal[j] = tVal;

			if(tVal == 0) {
				// new small random value TODO: analyze need of
				//System.err.println("tVal: " + tVal);
				oVal[j] = (RND.nextFloat() - .5f) * 0.01f;
			}
//			else System.err.println("tVal: " + tVal);
		}
	}


	private void adjustWeights(Flavor flavor, Result result) {

//		final Target resultTarget = result.targets[0];
//		final Target result2ndTarget = result.targets[1];

		final Target sampleTarget = getTarget(flavor);
		flavor.calcHitRate(sampleTarget, result);

		flavor.tots++;
//		if(sampleTarget == resultTarget /*&& resultTarget.quality > 0.9*/) return;

//		Target[] acceptableTargets = flavor.getAcceptableTargets(sampleTarget);

		float layerno = 20;
		float momentum = 1.0f;
		float cutOff = 0.1f;

		float learningRate = 0.005f; //0.05f
		boolean isCorrect = sampleTarget.value.equals(result.targets[0].value);

		if (isCorrect) {
			learningRate = (float) (learningRate * (1-result.targets[0].quality/2));
		}
		else {
			learningRate = (float) (learningRate * (1+result.targets[0].quality/2));
		}

		flavor.learningRate = learningRate;

		for (int j = flavor.net.nodes.length - 1; j > flavor.net.lastInputNo ; j--) {
			if(flavor.net.nodes[j].layerno < layerno) {
				layerno = flavor.net.nodes[j].layerno;
//					momentum *= 1.3; //1.33f;
//					cutOff /= 25;
			}

			if(flavor.net.nodes[j].isOutputNode) {
				if(flavor.net.nodes[j].flavor != flavor) continue;

//					if(tVal[flavor.net.nodes[j].idxFlavor] > -0.001f && tVal[flavor.net.nodes[j].idxFlavor] < 0.001f) continue;

//					boolean resultAmongAcceptables = false;
//					boolean result2ndAmongAcceptables = false;

//					boolean consistent = true;
//					float prvVal = 0;

//					for(Target atarget : acceptableTargets) {
//						float factor = 1.0f;
//						if(sampleTarget != atarget) factor = 0.3f;
//						if(atarget == resultTarget) resultAmongAcceptables = true;
//						if(atarget == result2ndTarget) result2ndAmongAcceptables = true;

				//float[] tVal = flavor.getAcceptableTargetVals(atarget, result);
//						float tVal = (2*((atarget.targetVals >> flavor.net.nodes[j].idxFlavor) & 1L) - 1);
//						if(prvVal != 0 && prvVal != tVal) consistent = false;
//						prvVal = tVal;

//							if(resultTarget != acceptableTarget) {
//								tVal[idx] -= (2*((resultTarget.targetVals >> idx) & 1L) - 1);
//								tVal[idx] /= 2;
//							}

//						deltasAccum[j] += factor*(tVal[flavor.net.nodes[j].idxFlavor] - oVal[j]);
//						deltasAccum[j] += factor*(tVal - oVal[j]);

				float tVal = (2*((sampleTarget.targetVals >> flavor.net.nodes[j].idxFlavor) & 1L) - 1);
				deltasAccum[j] = tVal - oVal[j];

//						System.err.println(flavor.id + " " +  tVal + " " + oVal[j]);

//						if(oVal[j] > -0.05 && oVal[j] < 0.05) deltasAccum[j] *= 1.5;
//						else if(oVal[j] > -0.1 && oVal[j] < 0.1) deltasAccum[j] *= 1.25;
//						else if(oVal[j] > -0.2 && oVal[j] < 0.2) deltasAccum[j] *= 1.15;
//						else if(oVal[j] > -0.4 && oVal[j] < 0.4) deltasAccum[j] *= 1.10;
//						else if(oVal[j] > -0.8 && oVal[j] < 0.8) deltasAccum[j] *= 1.05;
//					}
//					if(!resultAmongAcceptables && consistent) {
//						float tVal = (2*((resultTarget.targetVals >> flavor.net.nodes[j].idxFlavor) & 1L) - 1);
//						if(tVal != prvVal) deltasAccum[j] -= 1.0*(tVal - oVal[j]);
//					}
//					if(!result2ndAmongAcceptables && consistent) {
//						float tVal = (2*((result2ndTarget.targetVals >> flavor.net.nodes[j].idxFlavor) & 1L) - 1);
//						if(tVal != prvVal) deltasAccum[j] -= 0.5*(tVal - oVal[j]);
//					}

//					if(deltasAccum[j] > -0.001f && deltasAccum[j] < 0.001f) continue;
			}
			if(deltasAccum[j] == 0) continue;

			final int lowerIdx = (j + 0)*flavor.net.connectCount;
			final int upperIdx = (j + 1)*flavor.net.connectCount;
			for(int k = lowerIdx; k < upperIdx; k++) {
				float d = deltasAccum[j]*flavor.net.conWeights[k]*flavor.learningRate /**flavor.boost*/ * momentum ;
//					if(d > -cutOff && d < cutOff) continue;

				if(d > 10f) d = 10f;
				else if(d < -10f) d = -10f;
				deltasAccum[flavor.net.conIdxs[k]] += d;

//					if(flavor.id.equals("Box.rbl")) System.err.println(d + " " + flavor.net.conWeights[k] + " " + oVal[flavor.net.conIdxs[k]]);

				if(flavor.net.conWeights[k]*oVal[flavor.net.conIdxs[k]] > 0) flavor.net.conWeights[k] += d;
				else flavor.net.conWeights[k] -= d;
			}
		}

//		System.err.println("///////////" + flavor.id + " " + wasAdjusted);	
	}


	public void addFlavorTarget(Flavor flavor, Object value) {
		keyValues.put(flavor.id, value);

		Target target = flavor.getTarget(value);
		flavorTargets.put(flavor, target);
		flavorValues.put(flavor, value);
	}


	public void addFlavorTarget(String flavorId, Object value) {
		Flavor flavor = getFlavorForId(flavorId);
		addFlavorTarget(flavor, value);
	}


	public Set<Flavor> getFlavors() {
		return flavorTargets.keySet();
	}


	public Flavor getFlavorForId(String flavorId) {
		for(Flavor flavor : flavorTargets.keySet()) {
			if(flavor.id.equals(flavorId)) return flavor;
		}
		return null;
	}


	public Object getValue(Flavor flavor) {
		return flavorValues.get(flavor);
	}


	public Target getTarget(Flavor flavor) {
		return flavorTargets.get(flavor);
	}
}