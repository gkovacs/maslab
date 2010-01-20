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

	public static double bound(double v, double max, double min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}

	public static void shiftleft(double[] a, double v) {
		int i = 0;
		for (; i < a.length-1; ++i) {
			a[i] = a[i+1];
		}
		a[i] = v;
	}

	public static double averageArray(double[] a) {
		double total = 0.0;
		for (int i = 0; i < a.length; ++i) {
			total += a[i];
		}
		return total / (double)a.length;
	}

	public void run() {
		try {
		byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		AnalogInput leftIR = new AnalogInput(o, 7);
		AnalogInput rightIR = new AnalogInput(o, 0);
		AnalogInput crossLeftIR = new AnalogInput(o, 5);
		AnalogInput crossRightIR = new AnalogInput(o, 2);
		final double desv = 150.0;
		final double desvCross = 30.0;
		final double kp = 0.002;
		final double kd = 0.001;
		double prevleft = leftIR.getVoltage();
		double prevright = rightIR.getVoltage();
		double prevCrossLeft = crossLeftIR.getVoltage();
		double prevCrossRight = crossRightIR.getVoltage();
		double[] leftIRreadings = new double[3];
		double[] rightIRreadings = new double[3];
		double[] crossLeftIRreadings = new double[3];
		double[] crossRightIRreadings = new double[3];
		java.util.Arrays.fill(leftIRreadings, prevleft);
		java.util.Arrays.fill(rightIRreadings, prevright);
		java.util.Arrays.fill(crossRightIRreadings, prevCrossLeft);
		java.util.Arrays.fill(crossLeftIRreadings, prevCrossRight);
		int rightcooldown = 0;
		int leftcooldown = 0;
		while (running) {
			shiftleft(leftIRreadings, 62.5/leftIR.getVoltage());
			shiftleft(rightIRreadings, 62.5/rightIR.getVoltage());
			shiftleft(crossLeftIRreadings, 62.5/crossLeftIR.getVoltage());
			shiftleft(crossRightIRreadings, 62.5/crossRightIR.getVoltage());
			double left = averageArray(leftIRreadings);//a.getVoltage()
			//double left = 999999.0;
			double right = averageArray(rightIRreadings);
			//double right = averageArray(rightIRreadings);
			
			double crossLeft = averageArray(crossLeftIRreadings);
			double crossRight = averageArray(crossRightIRreadings);
			
			System.out.println("left is "+left+"right is "+right);
			//System.out.println(right);
			double lspeed = 0.0;
			double rspeed = 0.0;
			double lspeedCross = 0.0;
			double rspeedCross = 0.0;
			boolean sideVote = false;
			boolean crossVote = false;
			if (leftcooldown > 0) --leftcooldown;
			if (rightcooldown > 0) --rightcooldown;
			/*
			if (left > 150.0 && right > 150.0) { // just go straight
				//leftMotorWeight[idx] = 0.5f;
				//rightMotorWeight[idx] = 0.5f;
				//lspeed = 0.6;
				//rspeed = 0.6;
			}
			else*/ if (left > right) {
				//leftMotorWeight[idx] = 0.8f;
				//rightMotorWeight[idx] = 0.8f;
				double error = left-desv;
				if (error > 100.0) error = 100.0;
				if (error < -100.0) error = -100.0;
				double basevel = 0.7;
				//double basevel = bound(1.0-error, 0.7, 0.6);
				lspeed = -(kp*error-kd*(left-prevleft));//+basevel;
				rspeed = (kp*error-kd*(left-prevleft));//+basevel;
				if (lspeed > rspeed) {
					rspeed += basevel-Math.abs(lspeed);
					lspeed = basevel;
				} else {
					lspeed += basevel-Math.abs(rspeed);
					rspeed = basevel;
				}
				sideVote = true;
			} else {
				//leftMotorWeight[idx] = 0.8f;
				//rightMotorWeight[idx] = 0.8f;
				double error = right-desv;
				if (error > 100.0) error = 100.0;
				if (error < -100.0) error = -100.0;
				//double basevel = bound(1.0-error, 0.7, 0.6);
				double basevel = 0.7;
				lspeed = (kp*error-kd*(right-prevright));//+basevel;
				rspeed = -(kp*error-kd*(right-prevright));//+basevel;
				if (lspeed > rspeed) {
					rspeed += basevel-Math.abs(lspeed);
					lspeed = basevel;
				} else {
					lspeed += basevel-Math.abs(rspeed);
					rspeed = basevel;
				}
				sideVote = true;
			}
			prevleft = left;
			prevright = right;
			
			double basevel = 0.7;
				if (crossLeft < 30.0 || crossRight < 30.0) {
					//rspeed = -rspeed ;
					//lspeed = -lspeed;
					if (left > right) {
						if (rightcooldown == 0) {
							rspeed = -basevel;
							lspeed = basevel;
							leftcooldown = 10;
						} else {
							rspeed = -basevel;
							lspeed = -basevel;
						}
					} else {
						if (leftcooldown == 0) {
							lspeed = -basevel;
							rspeed = basevel;
							rightcooldown = 10;
						} else {
							rspeed = -basevel;
							lspeed = -basevel;
						}
					}
				}
				/*
				else if (crossLeft < 30.0) {
					//rspeed = -rspeed ;//2*basevel;
					//lspeed = -lspeed;
					rspeed -= basevel;
					//lspeed += basevel;
				}
				else if (crossRight < 30) {
					//rspeed = -rspeed ;//2*basevel;
					//lspeed = -lspeed;
					lspeed -= basevel;
					//rspeed += basevel;
				}
				*/
				//crossVote = true;

			
			if (!sideVote && !crossVote) {
				leftMotorWeight[idx] = 0.5f;
				rightMotorWeight[idx] = 0.5f;
				leftMotorAction[idx] = 0.6f;
				rightMotorAction[idx] = 0.6f;
			} else {
				leftMotorWeight[idx] = 0.8f;
				rightMotorWeight[idx] = 0.8f;
				if (sideVote && !crossVote) {
					leftMotorAction[idx] = (float)lspeed;
					rightMotorAction[idx] = (float)rspeed;
				} else if (crossVote && !sideVote) {
					leftMotorAction[idx] = (float)lspeedCross;
					rightMotorAction[idx] = (float)rspeedCross;
				} else { // crossVote && sideVote
					leftMotorAction[idx] = (float)((lspeed+lspeedCross)/2.0);
					rightMotorAction[idx] = (float)((rspeed+rspeedCross)/2.0);
				}
			}
			/*
			if (lspeed > rspeed) {
				rspeed += basevel-Math.abs(lspeed);
				lspeed = basevel;
			} else {
				lspeed += basevel-Math.abs(rspeed);
				rspeed = basevel;
			}
			*/
			java.lang.Thread.sleep(20);
		}
		/* log distance ir
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
		*/
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
