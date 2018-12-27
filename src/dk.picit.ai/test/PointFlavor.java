package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Target;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;


public class PointFlavor extends Flavor {

	private static final long serialVersionUID = 1959487101694017392L;
	private static final transient Random RND = new Random();
	
	private final int s = 64;
	
	public PointFlavor(Net net, String id, Color color) {
		super(net, id, color);
		
		targets = new Target[s*s + 1];
		
		int i = 0;
		targets[i++] = new Target(this, null);
		
		long tmpx = RND.nextLong() & 0B0000000000000000000000000000000011111111111111111111111111111111L;
		long[] valxs = new long[s];
		for(int j = 0; j < valxs.length; j++) {
			for(int k = 0; k < 11; k++) {
				int idx = RND.nextInt(32);
				tmpx ^= (1L << idx);
			}
			valxs[j] = tmpx;
		}

		long tmpy = RND.nextLong() & 0B1111111111111111111111111111111100000000000000000000000000000000L;
		long[] valys = new long[s];
		for(int j = 0; j < valys.length; j++) {
			for(int k = 0; k < 11; k++) {
				int idx = 32 + RND.nextInt(32);
				tmpy ^= (1L << idx);
			}
			valys[j] = tmpy;
		}
		
		for(int m = 0; m < s; m++) {
			for(int n = 0; n < s; n++) {
				Point2D.Float p = new Point2D.Float((float)((2*m - s)/(s*1.0)), (float)((2*n - s)/(s*1.0)));
				targets[i] = new Target(this, p);
				
				targets[i].targetVals = (valys[n] | valxs[m]);
				
//				double px = p.x;
//				for(int k = 0; k < 10; k++) {
//					if(px < 0) {
//						px = -px;
//						targets[i].targetVals = targets[i].targetVals & (~0L & ~(1L << k));
//					}
//					else targets[i].targetVals = targets[i].targetVals | (1L << k);
//					
//					px = (2*px - 1);
//				}
//				
//				double py = p.y;
//				for(int k = 32; k < 42; k++) {
//					if(py < 0) {
//						py = -py;
//						targets[i].targetVals = targets[i].targetVals & (~0L & ~(1L << k));
//					}
//					else targets[i].targetVals = targets[i].targetVals | (1L << k);
//					
//					py = (2*py - 1);
//				}

//				System.err.println(n + " " + m + " " + Long.toBinaryString(targets[i].targetVals));
				
				i++;
			}
		}
	}
	
	
	@Override
	public Target getTarget(Object value) {
		if(value == null) return targets[0];
		
		Target tmin = null;
		double dmin = Double.MAX_VALUE;
		
		for(Target target : targets) {
			if(target.value != null) {
				double dx = ((Point2D.Float)value).x - ((Point2D.Float)target.value).x;
				double dy = ((Point2D.Float)value).y - ((Point2D.Float)target.value).y;
				double d = Math.sqrt(dx*dx + dy*dy);
				if(d < dmin) {
					dmin = d;
					tmin = target;
				}
			}
		}
		return tmin;
	}
	
	
	@Override
	protected Target[] getAcceptableTargets(Target sampleTarget) {
		if(sampleTarget.value == null) return new Target[] {targets[0]};
		
		//TODO: use value and x,y to find index instead
		
		int targetIdx = 0;
		for(int i = 1; i < targets.length; i++) {
			if(sampleTarget == targets[i]) {
				targetIdx = i;
				break;
			}
		}
		
		if(targetIdx > 2*s && targetIdx < targets.length - 2*s) {
			return new Target[] {sampleTarget, targets[targetIdx - 2*s],
					targets[targetIdx - s - 1], targets[targetIdx - s], targets[targetIdx - s + 1], 
					targets[targetIdx - 2], targets[targetIdx - 1], targets[targetIdx + 1], targets[targetIdx + 2],
					targets[targetIdx + s - 1], targets[targetIdx + s], targets[targetIdx + s + 1],
							targets[targetIdx + 2*s]};
		}
		
		return new Target[] {sampleTarget};
	}
}