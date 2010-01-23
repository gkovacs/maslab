/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maslab-9
 */

import orc.*;

public class Bump extends java.lang.Thread {
	public boolean running = true;
	public float[] leftMotorAction = null;
	public float[] leftMotorWeight = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;
	public int idx = 0;
	public int state = 0;
	public String[] names = {"none", "fwdleft", "fwdright", "backleft", "backright"};
	public float[] weights = {0.0f, 0.99f, 0.99f, 0.99f, 0.99f};
	public int[] timeouts = {9999, 5, 5, 5, 5, 5, 5};
	public int[] transitions = {0, 0, 0, 0, 0};
	public int statetimeout = 0;
	public Orc o = null;
	public Arbiter arb = null;

	public void run() {
		try {
		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		DigitalInput fwdright = new DigitalInput(o, 9, false, true);
		DigitalInput fwdleft = new DigitalInput(o, 10, false, true);
		DigitalInput backleft = new DigitalInput(o, 12, false, true);
		DigitalInput backright = new DigitalInput(o, 11, false, true);
		while (running) {
			if (statetimeout >= timeouts[state]) { // state timed out, make transition
				setState(transitions[state]);
			} else {
				++statetimeout;
			}
			if (state == 0) {
				if (fwdleft.getValue()) {
					setState(1);
				} if (fwdright.getValue()) {
					setState(2);
				} if (backleft.getValue()) {
					setState(3);
				} if (backright.getValue()) {
					setState(4);
				}
			} else {
				arb.setState(1);
			}
			/*if (state == 1) {
				System.out.println("fwdleft");
				leftMotorAction[idx] = -1.0f;
				rightMotorAction[idx] = -0.5f;
			} else if (state == 2) {
				System.out.println("fwdright");
				leftMotorAction[idx] = -0.5f;
				rightMotorAction[idx] = -1.0f;
			} else if (state == 3) {
				System.out.println("backleft");
				leftMotorAction[idx] = -0.5f;
				rightMotorAction[idx] = 1.0f;
			} else if (state == 4) {
				System.out.println("backright");
				leftMotorAction[idx] = 1.0f;
				rightMotorAction[idx] = -0.5f;
			}*/
			java.lang.Thread.sleep(50);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setState(int newstate) {
		System.err.println("transition to "+names[newstate]);
		state = newstate;
		statetimeout = 0;
		//leftMotorAction[idx] = 0.0f;
		//rightMotorAction[idx] = 0.0f;
		leftMotorWeight[idx] = weights[newstate];
		rightMotorWeight[idx] = weights[newstate];
	}

	public void bye() {
		running = false;
	}

	public void setup(Arbiter a, int ActionWeightIndex) {
		idx = ActionWeightIndex;
		arb = a;
		leftMotorAction = a.leftMotorAction;
		leftMotorWeight = a.leftMotorWeight;
		rightMotorAction = a.rightMotorAction;
		rightMotorWeight = a.rightMotorWeight;
		o = a.o;
	}

}
