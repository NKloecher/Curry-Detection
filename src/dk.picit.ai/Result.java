package dk.picit.ai;

public class Result {

	public Target[] targets;
	
	
	public Result(int resultSize) {
		targets = new Target[resultSize];
		for(int i = 0; i < targets.length; i++ ) {
			targets[i] = new Target();
		}
	}
	
	
	@Override
	public String toString() {
		String str = "";
		for(Target target : targets) {
			str = "Found target: " + target + " [" + String.format("%4.3f", target.quality) + "]\n";
		}
		return str;
	}
}