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
	public java.io.FileInputStream mouse = null;
	public BufferedImage coordmapI = null;
	public BufferedImage worldmap = null;
	public WritableRaster coordmapR = null;
	public ImageIcon coordmapC = null;
	public JLabel coordmapL = null;
	public JFrame jf = null;
	public JPanel cp = null;
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
	boolean mapping = false;

	public void setupMapping() {
		jf = new JFrame();
		coordmapI = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
		coordmapR = coordmapI.getRaster();
		coordmapC = new ImageIcon();
		coordmapL = new JLabel();
		coordmapC.setImage(coordmapI);
		coordmapL.setIcon(coordmapC);
		//coordmapL.repaint();
		cp = new JPanel(new GridLayout(1,1));
		cp.add(coordmapL);
		jf.setContentPane(cp);
		jf.setSize(coordmapI.getWidth(), coordmapI.getHeight());
		jf.setVisible(true);
	}

	public void run() {
		try {
		if (mapping) setupMapping();
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
			//System.out.println(totalx+","+totaly);
			if (mapping) {
			coordmapR.setSample((int)totalx+coordmapR.getWidth()/2, (int)-totaly+coordmapR.getHeight()/2, 0, 255);
			coordmapC.setImage(coordmapI);
			coordmapL.setIcon(coordmapC);
			coordmapL.repaint();
			}
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
			e.printStackTrace();
		}
	}

	public void bye() {
		running = false;
	}

	public void setupImagePanels() {
		
	}
}
