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
	public int xvel = 0;
	public int yvel = 0;
	public long[] xdisp = null;
	public long[] ydisp = null;

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

	public void run() {
		try {
		xdisp = new long[10];
		ydisp = new long[10];
		m = new Mouse();
		m.start();
		while (running) {
			if (System.currentTimeMillis() - m.readtime < 100) {
				long nxdisp = m.totalx-xdisp[xdisp.length-1];
				long nydisp = m.totaly-ydisp[ydisp.length-1];
				shiftleft(xdisp, nxdisp);
				shiftleft(ydisp, nydisp);
				//xvel = m.output[1];
				//yvel = m.output[2];
				//System.out.println(m.output[1]+","+m.output[2]);
			} else {
				shiftleft(xdisp, 0);
				shiftleft(ydisp, 0);
				//xvel = 0;
				//yvel = 0;
				//System.out.println("0,0");
			}
			long totaldisp = 0;
			for (int x = 0; x < xdisp.length; ++x) {
				//totaldisp += Math.abs(xdisp[x]);
				totaldisp += Math.abs(ydisp[x]);
			}
			//System.out.println("total displacement is "+totaldisp);
			if (totaldisp < 500) {
				System.out.println("stuck");
			} else {
				System.out.println("not stuck");
			}
			java.lang.Thread.sleep(50);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bye() {
		running = false;
	}
}
