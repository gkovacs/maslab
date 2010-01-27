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
	public float[] leftMotorLog = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;
	public float[] rightMotorLog = null;
	public double rollerAction = 0.0;
	public Orc o = null;
	public Motor rightMotor = null;
	public Motor leftMotor = null;
	public Motor rollers = null;
	public byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};

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

	public static void shiftright(float[] a, float v) {
		int i = a.length-1;
		for (; i >= 1; --i) {
			a[i] = a[i-1];
		}
		a[0] = v;
	}

	public void run() {
		try {
		rightMotor = new Motor(o, 1, false);
		leftMotor = new Motor(o, 0, true);
		rollers = new Motor(o, 2, true);
		rightMotorLog = new float[100];
		leftMotorLog = new float[100];
		while (running) {
			int maxidx = 0;
			float maxval = Float.MIN_VALUE;
			for (int i = 0; i < leftMotorWeight.length; ++i) {
				if (leftMotorWeight[i] > maxval) {
					maxidx = i;
					maxval = leftMotorWeight[maxidx];
				}
			}
			float lma = leftMotorWeight[maxidx];
			float rma = rightMotorAction[maxidx];
			leftMotor.setPWM(bound(lma, 1.0f, -1.0f));
			rightMotor.setPWM(bound(rma, 1.0f, -1.0f)*0.9f);
			rollers.setPWM(rollerAction);
			java.lang.Thread.sleep(10);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static float bound(float v, float max, float min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}

	public static double bound(double v, double max, double min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}

	public void setup(int numComrades) {
		try {
		o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		leftMotorAction = new float[numComrades];
		leftMotorWeight = new float[numComrades];
		rightMotorAction = new float[numComrades];
		rightMotorWeight = new float[numComrades];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bye() {
		running = false;
		leftMotor.setPWM(0.0);
		rightMotor.setPWM(0.0);
		rollers.setPWM(0.0);
	}
}
