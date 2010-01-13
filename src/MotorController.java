/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maslab-9
 */
public class MotorController {

	public float targetvelocity = 0.0f;
	final float kp;
	final float kd;
	final float ki;
	final int numsamples = 6;
	final int sampintv = 50; // sampling interval, in ms
	float[] samples = new float[numsamples];
	float intg = 0.0f;

	public MotorController(float nkp, float nkd, float nki) {
		kp = nkp;
		kd = nkd;
		ki = nki;
	}

	public static float bound(float v, float max, float min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}
	public static void shiftleft6(float[] a, float v) {
		a[0] = a[1];
		a[1] = a[2];
		a[2] = a[3];
		a[3] = a[4];
		a[4] = a[5];
		a[5] = v;
	}

	public float getPWM(float curvel) {
	try {
		// radius of wheel is 57 mm = 5.7 cm
		// circumference of wheel is 179 mm = 17.9 cm
		// 12 ticks per wheel
		//double targetpos = curpos + position;
		shiftleft6(samples, curvel);
		float cursamp = (samples[3]+samples[4]+samples[5])/3;
		intg += cursamp;
		float prevsamp = (samples[0]+samples[1]+samples[2])/3;
		float derv = (cursamp-prevsamp)/(float)sampintv;
		float outpwm = bound(kp*(cursamp-targetvelocity)-kd*derv+ki*intg, 1.0f, -1.0f);
		//System.out.println("position is "+e0.getPosition()+" ticks are "+e0.getVelocity());
		return outpwm;
	} catch (Exception e) {
		e.printStackTrace();
		return 0.0f;
	}
	}

}