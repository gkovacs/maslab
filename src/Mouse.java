/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maslab-9
 */

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class Mouse extends java.lang.Thread {
	java.io.FileInputStream mouse = null;
	BufferedImage coordmap = null;
	BufferedImage worldmap = null;
	byte[] output = null;
	long totalx = 0;
	long totaly = 0;
	public boolean running = true;
	public byte[] xreadbuf = null;
	public byte[] yreadbuf = null;
	public long[] readtimes = null;
	int bufend = 0;
	int bufendt = 0;
	int bufstart = 0;
	int buflen = 1024;
	long readtime = 0;

	public void run() {
		try {
		mouse = new java.io.FileInputStream("/dev/input/mice");
		/*
		xreadbuf = new byte[buflen];
		yreadbuf = new byte[buflen];
		readtimes = new long[buflen];
		*/
		output = new byte[3];
		while (running) {
			//System.out.println(mouse.available());
			//System.out.println(imouse.ready());
			mouse.read(output);
			totalx += output[1];
			totaly += output[2];
			readtime = System.currentTimeMillis();
			//while (mouse.available() > 0) {
			/*
				bufendt = (bufendt + 1) % buflen;
				mouse.read(output);
				//System.out.println("dx: "+output[1]+" dy: "+output[2]);
				totalx += (xreadbuf[bufendt] = output[1]);
				totaly += (yreadbuf[bufendt] = output[2]);
				readtimes[bufendt] = System.nanoTime();
				bufend = bufendt;
				//System.out.println("time: "+readtimes[bufend]+" totalx: "+(totalx/333.0)+" cm totaly: "+(totaly/333.0)+" cm");
			 */
			//}
		}
		} catch (Exception e) {
			
		}
	}

	public void bye() {
		running = false;
	}

	public void setupImagePanels() {
		
	}
}
