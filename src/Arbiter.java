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
	public float[] rollerAction = null;
	public float[] rollerWeight = null;

	public float kp = 0.1f;
	public float kd = 0.0f;
	public float ki = 0.0f;

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
		Motor rightMotor = new Motor(o, 0, true);
		Motor leftMotor = new Motor(o, 1, false);
		Motor rollers = new Motor(o, 2, false);
		/*
		QuadratureEncoder e0 = new QuadratureEncoder(o, 0, false);
		QuadratureEncoder e1 = new QuadratureEncoder(o, 1, false);
		MotorController c0 = new MotorController(kp, kd, ki);
		MotorController c1 = new MotorController(kp, kd, ki);
		*/
		while (running) {
			/*
			float lma = maxVal(leftMotorAction, leftMotorWeight);
			float rma = maxVal(rightMotorAction, rightMotorWeight);
			c0.targetvelocity = lma;
			c1.targetvelocity = rma;
			float rightpwm = c0.getPWM((float)e0.getVelocity()*17.9f);
			rightMotor.setPWM(rightpwm);
			float leftpwm = c1.getPWM((float)e1.getVelocity()*17.9f);
			leftMotor.setPWM(leftpwm);
			System.out.println("rightpwm is "+rightpwm+"leftpwm is "+leftpwm);
			java.lang.Thread.sleep(50);
			*/
			float lma = maxVal(leftMotorAction, leftMotorWeight);
			float rma = maxVal(rightMotorAction, rightMotorWeight);
			float rla = maxVal(rollerAction, rollerWeight);
			System.out.println("left: "+lma+" right: "+rma);
			leftMotor.setPWM((float)bound(lma*0.9f, 1.0f, -1.0f));
			rightMotor.setPWM((float)bound(rma, 1.0f, -1.0f));
			rollers.setPWM((float)bound(rla, 1.0f, -1.0f));
			java.lang.Thread.sleep(100);
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
		leftMotorAction = new float[numComrades];
		leftMotorWeight = new float[numComrades];
		rightMotorAction = new float[numComrades];
		rightMotorWeight = new float[numComrades];
		rollerAction = new float[numComrades];
		rollerWeight = new float[numComrades];
	}

	public void bye() {
		running = false;
	}
}
