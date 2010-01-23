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
	public float[] rollerAction = null;
	public float[] rollerWeight = null;
	public Orc o = null;
	public byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};

	public float kp = 0.1f;
	public float kd = 0.0f;
	public float ki = 0.0f;

	public int state = 0;
	public int timeback = 0;
	public int cooldown = 0;

	public void setState(int newstate) {
		if (cooldown > 0) return;
		state = newstate;
	}

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
		Motor rightMotor = new Motor(o, 0, true);
		Motor leftMotor = new Motor(o, 1, false);
		Motor rollers = new Motor(o, 2, false);
		/*
		QuadratureEncoder e0 = new QuadratureEncoder(o, 0, false);
		QuadratureEncoder e1 = new QuadratureEncoder(o, 1, false);
		MotorController c0 = new MotorController(kp, kd, ki);
		MotorController c1 = new MotorController(kp, kd, ki);
		*/

		rightMotorLog = new float[100];
		leftMotorLog = new float[100];
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
			if (state == 0) { // arbitrate
			if (cooldown > 100) --cooldown;
			float lma = maxVal(leftMotorAction, leftMotorWeight);
			float rma = maxVal(rightMotorAction, rightMotorWeight);
			float rla = maxVal(rollerAction, rollerWeight);
			shiftright(leftMotorLog, lma);
			shiftright(rightMotorLog, rma);
			System.out.println("left: "+lma+" right: "+rma);
			leftMotor.setPWM((float)bound(lma, 1.0f, -1.0f)*0.95);
			rightMotor.setPWM((float)bound(rma, 1.0f, -1.0f));
			rollers.setPWM((float)bound(rla, 1.0f, -1.0f));
			} else { // turning back time
				leftMotor.setPWM(-leftMotorLog[timeback]);
				rightMotor.setPWM(-rightMotorLog[timeback]);
				if (++timeback >= leftMotorLog.length) {
					setState(0);
					timeback = 0;
					cooldown = 100;
				}
			}
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
		rollerAction = new float[numComrades];
		rollerWeight = new float[numComrades];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bye() {
		running = false;
	}
}
