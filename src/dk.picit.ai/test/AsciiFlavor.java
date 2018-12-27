package dk.picit.ai.test;

import dk.picit.ai.Flavor;
import dk.picit.ai.Net;
import dk.picit.ai.Target;

import java.awt.*;

public class AsciiFlavor extends Flavor {

	private static final long serialVersionUID = 1959487101694017392L;


	public AsciiFlavor(Net net, String id, Color color) {
		super(net, id, color);
		
		targets = new Target[37];
		
		int i = 0;
		targets[i++] = new Target(this, '0');
		targets[i++] = new Target(this, '1');
		targets[i++] = new Target(this, '2');
		targets[i++] = new Target(this, '3');
		targets[i++] = new Target(this, '4');
		targets[i++] = new Target(this, '5');
		targets[i++] = new Target(this, '6');
		targets[i++] = new Target(this, '7');
		targets[i++] = new Target(this, '8');
		targets[i++] = new Target(this, '9');
		targets[i++] = new Target(this, '_');
		targets[i++] = new Target(this, 'A');
		targets[i++] = new Target(this, 'B');
		targets[i++] = new Target(this, 'C');
		targets[i++] = new Target(this, 'D');
		targets[i++] = new Target(this, 'E');
		targets[i++] = new Target(this, 'F');
		targets[i++] = new Target(this, 'G');
		targets[i++] = new Target(this, 'H');
		targets[i++] = new Target(this, 'I');
		targets[i++] = new Target(this, 'J');
		targets[i++] = new Target(this, 'K');
		targets[i++] = new Target(this, 'L');
		targets[i++] = new Target(this, 'M');
		targets[i++] = new Target(this, 'N');
		targets[i++] = new Target(this, 'O');
		targets[i++] = new Target(this, 'P');
		targets[i++] = new Target(this, 'Q');
		targets[i++] = new Target(this, 'R');
		targets[i++] = new Target(this, 'S');
		targets[i++] = new Target(this, 'T');
		targets[i++] = new Target(this, 'U');
		targets[i++] = new Target(this, 'V');
		targets[i++] = new Target(this, 'W');
		targets[i++] = new Target(this, 'X');
		targets[i++] = new Target(this, 'Y');
		targets[i++] = new Target(this, 'Z');
	}
}
