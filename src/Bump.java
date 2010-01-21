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
	public float[] weights = {0.0f, 2.0f, 2.0f, 2.0f, 2.0f};
	public int[] timeouts = {9999, 30, 30, 30, 30, 30, 30};
	public int[] transitions = {0, 0, 0, 0, 0};
	public int statetimeout = 0;
	public Orc o = null;

	public void run() {
		try {
		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		DigitalInput fwdleft = new DigitalInput(o, 9, false, true);
		DigitalInput fwdright = new DigitalInput(o, 10, false, true);
		DigitalInput backleft = new DigitalInput(o, 11, false, true);
		DigitalInput backright = new DigitalInput(o, 12, false, true);
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
			} if (state == 1) {
				System.out.println("fwdleft");
			} else if (state == 2) {
				System.out.println("fwdright");
			} else if (state == 3) {
				System.out.println("backleft");
			} else if (state == 4) {
				System.out.println("backright");
			}
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
		leftMotorAction[idx] = 0.0f;
		rightMotorAction[idx] = 0.0f;
		leftMotorWeight[idx] = weights[newstate];
		rightMotorWeight[idx] = weights[newstate];
	}

	public void bye() {
		running = false;
	}

	public void setup(Arbiter a, int ActionWeightIndex) {
		idx = ActionWeightIndex;
		leftMotorAction = a.leftMotorAction;
		leftMotorWeight = a.leftMotorWeight;
		rightMotorAction = a.rightMotorAction;
		rightMotorWeight = a.rightMotorWeight;
		o = a.o;
	}

}
