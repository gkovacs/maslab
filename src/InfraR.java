/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */

import orc.*;

public class InfraR extends java.lang.Thread {
	public boolean running = true;
	public float[] leftMotorAction = null;
	public float[] leftMotorWeight = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;
	public int idx = 0;

	public void run() {
		try {
		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		leftMotorWeight[idx] = 0.5f;
		rightMotorWeight[idx] = 0.5f;
		AnalogInput a = new AnalogInput(o, 0);
		final float dd = 30.0f; // desired distance
		final float k = 0.05f; // proportionality constant
		while (running) {
			float d = (62.5f/(float)a.getVoltage())-20.0f;
			float lspeed = k*(dd-d);
			float rspeed = -lspeed;
			if (lspeed > rspeed) {
				rspeed += (0.7f - lspeed);
				lspeed = 0.7f;
			} else {
				lspeed += (0.7f - rspeed);
				rspeed = 0.7f;
			}
			leftMotorAction[idx] = lspeed;
			rightMotorAction[idx] = rspeed;
			System.out.println(d);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setup(Arbiter a, int ActionWeightIndex) {
		idx = ActionWeightIndex;
		leftMotorAction = a.leftMotorAction;
		leftMotorWeight = a.leftMotorWeight;
		rightMotorAction = a.rightMotorAction;
		rightMotorWeight = a.rightMotorWeight;
	}

	public void bye() {
		running = false;
	}
}
