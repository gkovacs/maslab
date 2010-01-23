/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maslab-9
 */

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class Odometry extends java.lang.Thread {
	public Mouse m = null;
	public Gyroscope g = null;
	public BufferedImage coordmapI = null;
	public WritableRaster coordmapR = null;
	public ImageIcon coordmapC = null;
	public JLabel coordmapL = null;
	public JFrame jf = null;
	public JPanel cp = null;
	public long readtime = 0;
	public long totalx = 0;
	public long totaly = 0;
	public double coordx = 0.0;
	public double coordy = 0.0;
	public boolean running = true;

	public void setupMapping() {
		jf = new JFrame();
		coordmapI = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
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

	public void setup(Mouse nm, Gyroscope ng) {
		m = nm;
		g = ng;
	}

	public void bye() {
		running = false;
	}

	public void run() {
		try {
		setupMapping();
		//coordx = 10*coordmapR.getWidth()/2;
		//coordy = 10*coordmapR.getHeight()/2;
		while (running) {
			if (m.totaly != totaly) {
				long dispy = m.totaly-totaly;
				totaly += dispy;
				//System.out.println(dispy);
				coordx += dispy*Math.sin(g.angle);
				coordy += dispy*Math.cos(g.angle);
				try {
				coordmapR.setSample(coordmapR.getWidth()/2+(int)(coordx/200.0), coordmapR.getHeight()/2+(int)(coordy/200.0), 0, 255);
				}  catch (Exception e) {
				e.printStackTrace();
				}
				coordmapC.setImage(coordmapI);
				coordmapL.setIcon(coordmapC);
				coordmapL.repaint();
			}
			java.lang.Thread.sleep(10);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
