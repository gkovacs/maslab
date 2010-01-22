/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */
public class MouseController extends java.lang.Thread {
	public Mouse m = null;
	public boolean running = true;
	public float[] leftMotorAction = null;
	public float[] leftMotorWeight = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;
	public float[] rollerAction = null;
	public float[] rollerWeight = null;
	public int idx = 0;
	public int xvel = 0;
	public int yvel = 0;
	public long[] xdisp = null;
	public long[] ydisp = null;
	public long totaldispx = 0;
	public long totaldispy = 0;
	public int state = 0;
	public int unstuckmotion = 0;
	public int unstuckstate = 0;
	public boolean direction = false;

	public static void shiftleft(double[] a, double v) {
		int i = 0;
		for (; i < a.length-1; ++i) {
			a[i] = a[i+1];
		}
		a[i] = v;
	}

	public static void shiftleft(long[] a, long v) {
		int i = 0;
		for (; i < a.length-1; ++i) {
			a[i] = a[i+1];
		}
		a[i] = v;
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

	public long sumArray(long[] a) {
		long tot = 0;
		for (int x = 0; x < a.length; ++x) {
			tot += Math.abs(a[x]);
		}
		return tot;
	}

	public long dispArray(long[] a) {
		long tot = 0;
		for (int x = 0; x < a.length-1; ++x) {
			tot += Math.abs(a[x]-a[x+1]);
		}
		return tot;
	}

	public static boolean reverseb(boolean b) {
		if (b) return false;
		else return true;
	}

	public void run() {
		try {
		rollerWeight[idx] = 0.0f;
		leftMotorWeight[idx] = 0.0f;
		rightMotorWeight[idx] = 0.0f;
		xdisp = new long[20];
		ydisp = new long[20];
		xdisp[0] = 9999;
		xdisp[xdisp.length-1] = 9999;
		ydisp[0] = 9999;
		ydisp[ydisp.length-1] = 9999;
		m = new Mouse();
		m.start();
		while (running) {
			//totaldispx -= xdisp[0];
			//totaldispy -= ydisp[0];
			totaldispx = dispArray(xdisp);
			totaldispy = dispArray(ydisp);
			//if (System.currentTimeMillis() - m.readtime < 50) {
				long ndisp = m.totalx;//-xdisp[xdisp.length-1];
				//totaldispx += Math.abs(ndisp);
				shiftleft(xdisp, ndisp);
				ndisp = m.totaly;//-ydisp[ydisp.length-1];
				//totaldispy += Math.abs(ndisp);
				shiftleft(ydisp, ndisp);
				//xvel = m.output[1];
				//yvel = m.output[2];
				//System.out.println(m.output[1]+","+m.output[2]);
			//} else {
			//	shiftleft(xdisp, 0);
			//	shiftleft(ydisp, 0);
				//xvel = 0;
				//yvel = 0;
				//System.out.println("0,0");
			//}
			//System.out.println("dispx is "+totaldispx);
			System.out.println("dispy is "+totaldispy);
			if (unstuckmotion == 0) {
				leftMotorWeight[idx] = 0.0f;
				rightMotorWeight[idx] = 0.0f;
				float leftact = maxVal(leftMotorAction, leftMotorWeight);
				float rightact = maxVal(rightMotorAction, rightMotorWeight);
				if (totaldispy+totaldispx < 200) {
						unstuckmotion = 60;
						direction = reverseb(direction);
						System.out.println("stuck turning left");
					}
				/*
				if (leftact >= 0.5f && rightact >= 0.5f) { // going forward
					if (totaldispy+totaldispx < 100) {
						unstuckmotion = 20;
						System.out.println("stuck going forward");
					}
				}
				else if (leftact-rightact > 0.7f) { // turning right
					if (totaldispx+totaldispy < 100) {
						unstuckmotion = 20;
						System.out.println("stuck turning right");
					}
				}
				else if (rightact-leftact > 0.7f) { // turning left
					if (totaldispx+totaldispy < 100) {
						unstuckmotion = 20;
						System.out.println("stuck turning left");
					}
				}
				*/
			}
			/*
			if (unstuckmotion == 0 && totaldispy < 500 && state == 2) { // failing to go forward, we're stuck
				unstuckmotion = 300;
			}
			if (unstuckmotion == 0 && (totaldispx > -500 || totaldispx < 500) && state == 0) { // failing to rotate, we're stuck
				unstuckmotion = 300;
			}
			*/
			else {
				leftMotorWeight[idx] = 1.0f;
				rightMotorWeight[idx] = 1.0f;
				if (direction) {
				if (unstuckmotion < 20) { // go right
					leftMotorAction[idx] = 0.7f;
					rightMotorAction[idx] = -0.7f;
				} else if (unstuckmotion < 30) { // go back
					leftMotorAction[idx] = -0.7f;
					rightMotorAction[idx] = -0.7f;
				} else if (unstuckmotion < 50) { // go right
					leftMotorAction[idx] = 0.7f;
					rightMotorAction[idx] = -0.7f;
				} else if (unstuckmotion < 60) { // go back
					leftMotorAction[idx] = -0.7f;
					rightMotorAction[idx] = -0.7f;
				}
				} else {
					if (unstuckmotion < 30) { // go left
					leftMotorAction[idx] = -0.7f;
					rightMotorAction[idx] = 0.7f;
				} else if (unstuckmotion < 30) { // go forward
					leftMotorAction[idx] = 0.7f;
					rightMotorAction[idx] = 0.7f;
				} else if (unstuckmotion < 50) { // go left
					leftMotorAction[idx] = -0.7f;
					rightMotorAction[idx] = 0.7f;
				} else if (unstuckmotion < 60) { // go forward
					leftMotorAction[idx] = 0.7f;
					rightMotorAction[idx] = 0.7f;
				}
				}
				--unstuckmotion;
			}
			
			//long totaldisp = 0;
			//for (int x = 0; x < xdisp.length; ++x) {
				//totaldisp += Math.abs(xdisp[x]);
			//	totaldisp += Math.abs(ydisp[x]);
			//}
			/*
			System.out.println("total displacement x is "+totaldispx);
			System.out.println("total displacement y is "+totaldispy);
			if (totaldispx < 500) {
				System.out.println("stuck x");
			} else {
				//System.out.println("not stuck");
			}
			if (totaldispy < 500) {
				System.out.println("stuck y");
			} else {
				//System.out.println("not stuck");
			}
			*/
			java.lang.Thread.sleep(50);
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
		rollerAction = a.rollerAction;
		rollerWeight = a.rollerWeight;
	}

	public void bye() {
		running = false;
	}
}
