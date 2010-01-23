/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maslab-9
 */

import orc.*;

public class Gyroscope extends java.lang.Thread {

	public int anglei = 0;
	public double angle = 0;
	public Orc o = null;
	public long[] angles = null;
	public long[] baseang = null;
	long prevtime = 0;
	boolean running = true;

	public void run() {
		try {
		WiiMotionPlus g = new WiiMotionPlus(o);
		int numsamples = 300;
		angles = new long[3];
		baseang = new long[3];
		while (numsamples > 0) {
			int[] angvel = g.readAxes();
			baseang[0] += angvel[0];
			baseang[1] += angvel[1];
			baseang[2] += angvel[2];
			--numsamples;
		}
		baseang[0] /= 300;
		baseang[1] /= 300;
		baseang[2] /= 300;
		prevtime = System.nanoTime();
		while (running) {
			//double degrees = g.getTheta()/0.005;
			//System.out.println(degrees);
			//System.out.println(g.getTheta());
			//printList(g.readAxes());
			int[] angvel = g.readAxes();
			angvel[0] -= baseang[0];
			angvel[1] -= baseang[1];
			angvel[2] -= baseang[2];
			if (angvel[0] < 100 && angvel[0] > -100) angvel[0] = 0;
			if (angvel[1] < 100 && angvel[1] > -100) angvel[1] = 0;
			if (angvel[2] < 100 && angvel[2] > -100) angvel[2] = 0;
			//printList(angvel);
			long deltatime = System.nanoTime()-prevtime;
			//System.out.println(deltatime);
			angles[0] += (angvel[0]*deltatime)/100000;
			angles[1] += (angvel[1]*deltatime)/100000;
			angles[2] += (angvel[2]*deltatime)/100000;
			//printList(angles);
			//System.out.println(angles[1]);
			anglei = (int)((angles[1]*360/75000000) % 360);
			System.out.println(anglei);
			angle = ((angles[1]*2.0*Math.PI/75000000) % 2.0*Math.PI);
			//System.out.println(angle);
			//System.out.println(angles[1]*360/80000000);
			prevtime += deltatime;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setup(Arbiter a, int ActionWeightIndex) {
		o = a.o;
	}

	public void bye() {
		running = false;
	}
}
