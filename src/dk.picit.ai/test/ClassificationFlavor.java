package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Target;

import java.awt.*;

public class ClassificationFlavor extends Flavor {

	private static final long serialVersionUID = 1959487101694017392L;


	public ClassificationFlavor(Net net, String id, Color color) {
		super(net, id, color);
		
		targets = new Target[8];
		
		int i = 0;
		targets[i++] = new Target(this, 'c');
		targets[i++] = new Target(this, 'C');
		targets[i++] = new Target(this, 'X');
		targets[i++] = new Target(this, 'T');
		targets[i++] = new Target(this, 'Q');
		targets[i++] = new Target(this, 'B');
		targets[i++] = new Target(this, 'E');
		targets[i++] = new Target(this, 'L');
	}
}
