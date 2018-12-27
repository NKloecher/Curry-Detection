package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Result;
import dk.picit.ai.Target;

import java.awt.*;


public class LineFlavor extends Flavor {

	private static final long serialVersionUID = 1959487101694017392L;


	public LineFlavor(Net net, String id, Color color) {
		super(net, id, color);
		
		targets = new Target[51 + 1];
		
		int i = 0;
		targets[i++] = new Target(this, null);
		for(int n = -25; n <= 25; n++) {
			targets[i++] = new Target(this, new Float(n/20.0));
		}
	}
	
	
	@Override
	public Target getTarget(Object value) {
		if(value == null) return targets[0];
		
		Target tmin = null;
		double dmin = Double.MAX_VALUE;
		
		for(Target target : targets) {
			if(target.value != null) {
				double d = (Float)value - (Float)target.value;
				d = d*d;
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
		
		int targetIdx = 0;
		for(int i = 1; i < targets.length; i++) {
			if(sampleTarget == targets[i]) {
				targetIdx = i;
				break;
			}
		}
		if(targetIdx == 0) System.err.println("targetIdx == 0 " + id);
		
		int minIdx = Math.max(targetIdx - 1, 1);
		int maxIdx = Math.min(targetIdx + 1, targets.length - 1);
		Target[] t = new Target[maxIdx - minIdx + 1];
		
//		System.err.println(targetIdx + " " + t.length + " " + id);
		
		int nn = 0;
		for(int i = minIdx; i <= maxIdx; i++) {
			t[nn++] = targets[i];
		}
		return t;
	}
	
	
	@Override
	protected float[] getAcceptableTargetVals(Target sampleTarget, Result result) {
		Target resultTarget = result.targets[0];
		Target[] tp = getAcceptableTargets(sampleTarget);
		Target[] tn = getAcceptableTargets(resultTarget);
		
		float[] tVal = new float[64];
		for(int idx = 0; idx < 64; idx++) {
			if(tp.length == 1 && tp[0].value == null) {
				tVal[idx] += (2*((tp[0].targetVals >> idx) & 1) - 1);
//				if(resultTarget != sampleTarget) tVal[idx] -= 0.05*(2*((resultTarget.targetVals >> idx) & 1) - 1);
//				for(Target ttn : tn) {
//					if(ttn != tp[0]) tVal[idx] -= 0.05*(2*((resultTarget.targetVals >> idx) & 1) - 1);
//				}
			}
			else {
				int ii = 0;
				for(Target tt : tp) {
					if(tt == sampleTarget) {
						tVal[idx] += 0.60*(2*((tt.targetVals >> idx) & 1) - 1);
//						for(Target ttn : tn) {
//							if(ttn != tt) tVal[idx] -= 0.2*(2*((resultTarget.targetVals >> idx) & 1) - 1);
//						}
					}
					else if(ii == 1 || ii == 3) tVal[idx] += 0.2*(2*((tt.targetVals >> idx) & 1) - 1);
//					else if(ii == 0 || ii == 5) tVal[idx] -= 0.1*(2*((tt.targetVals >> idx) & 1) - 1);
					ii++;
				}
//				if(resultTarget != sampleTarget) tVal[idx] -= 0.05*(2*((resultTarget.targetVals >> idx) & 1) - 1);
			}
			
//			if(tVal[idx] < -0.5) tVal[idx] = -1.0f;
//			else if(tVal[idx] > 0.5) tVal[idx] = 1.0f;
//			else tVal[idx] = 0;
			
//			System.err.println(idx + " " + tVal[idx]);
		}
		return tVal;
	}
}