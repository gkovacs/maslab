/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */

import orc.*;

public class Arbiter extends java.lang.Thread {
	public boolean running = true;
	public float[] leftMotorAction = null;
	public float[] leftMotorWeight = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;

	public float maxVal(float[] vals, float[] weights) {
		float maxweight = 0.0f;
		float maxval = 0.0f;
		for (int i = 0; i < weights.length; ++i) {
			if (weights[i] > maxweight) {
				maxweight = weights[i];
				maxval = vals[i];
			}
		}
		return maxval;
	}

	public void run() {
		try {
		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		Motor rightMotor = new Motor(o, 0, false);
		Motor leftMotor = new Motor(o, 1, false);
		while (running) {
			float lma = maxVal(leftMotorAction, leftMotorWeight);
			float rma = maxVal(rightMotorAction, rightMotorWeight);
			System.out.println("left: "+lma+" right: "+rma);
			leftMotor.setPWM((float)lma);
			rightMotor.setPWM((float)rma);
			java.lang.Thread.sleep(100);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setup(int numComrades) {
		leftMotorAction = new float[numComrades];
		leftMotorWeight = new float[numComrades];
		rightMotorAction = new float[numComrades];
		rightMotorWeight = new float[numComrades];
	}

	public void bye() {
		running = false;
	}
}
