/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */

//import javax.imageio.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class Vision extends java.lang.Thread {
	public boolean running = true;
	public float[] leftMotorAction = null;
	public float[] leftMotorWeight = null;
	public float[] rightMotorAction = null;
	public float[] rightMotorWeight = null;
	public int idx = 0;
	public int found = 0;
	public int lifetime = 0;
	public int distance = Integer.MAX_VALUE;
	public int pxoffset = 0;

	public void run() {
		try {
		//byte[] inet = {(byte)192, (byte)168, (byte)237, (byte)7};
		//Orc o = new orc.Orc(java.net.Inet4Address.getByAddress(inet));
		System.out.println("1");
		orc.camera.Camera c;
		System.out.println("1a");
		c = new orc.camera.Camera("/dev/video0");
		//c = orc.camera.Camera.makeCamera();
		System.out.println("2");
		//orc.camera.Camera c = new orc.camera.Camera("/dev/video0");
		BufferedImage im = c.createImage();
		c.capture(im);
		WritableRaster r = im.getRaster();
		BufferedImage im2 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r2 = im2.getRaster();
		shadeRed(r,r2);
		BufferedImage im3 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r3 = im3.getRaster();
		seekStart2(r,r3);
		JFrame jf = new JFrame();
		ImageIcon ic = new ImageIcon();
		JLabel jl = new JLabel();
		ic.setImage(im);
		jl.setIcon(ic);
		ImageIcon ic2 = new ImageIcon();
		JLabel jl2 = new JLabel();
		ic2.setImage(im2);
		jl2.setIcon(ic2);
		ImageIcon ic3 = new ImageIcon();
		JLabel jl3 = new JLabel();
		ic3.setImage(im3);
		jl3.setIcon(ic3);
		JPanel cp = new JPanel(new GridLayout(1,3));
		cp.add(jl);
		cp.add(jl2);
		cp.add(jl3);
		jf.setContentPane(cp);
		jf.setSize(im.getWidth()*3, im.getHeight());
		jf.setVisible(true);
		while (running) {
			if (found > 0) --found;
			if (lifetime > 0) --lifetime;
			c.capture(im);
			ic.setImage(im);
			jl.setIcon(ic);
			jl.repaint();
			shadeRed(r,r2);
			ic2.setImage(im2);
			jl2.setIcon(ic2);
			jl2.repaint();
			seekStart2(r,r3);
			ic3.setImage(im3);
			jl3.setIcon(ic3);
			jl3.repaint();
			blankimg(r3);
			if (found > 0) { // moving towards goal
				leftMotorWeight[idx] = 0.5f;
				rightMotorWeight[idx] = 0.5f;
				if (pxoffset > 0) { // to the right
					leftMotorAction[idx] = 0.3f;
					rightMotorAction[idx] = 0.0f;
				} else {
					leftMotorAction[idx] = 0.0f;
					rightMotorAction[idx] = 0.3f;
				}
				//leftMotorAction[idx] = bound((float)distance*(1.0f+0.01f*(float)pxoffset)/100.0f, 0.5f, 0.1f);
				//rightMotorAction[idx] = bound((float)distance*(1.0f-0.01f*(float)pxoffset)/100.0f, 0.5f, 0.1f);
			} else { // idly searching, nothing interesting in sight, turn left
				leftMotorWeight[idx] = 0.3f;
				rightMotorWeight[idx] = 0.3f;
				leftMotorAction[idx] = 0.0f;
				rightMotorAction[idx] = 0.3f;
			}
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
	}

	public void bye() {
		running = false;
	}

	public static int bound(int v, int max, int min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}

	public static float bound(float v, float max, float min) {
		if (v > max) return max;
		else if (v < min) return min;
		else return v;
	}

	public static void blankimg(WritableRaster r) {
		for (int x = 0; x < r.getWidth(); ++x) {
			for (int y = 0; y < r.getHeight(); ++y) {
				r.setSample(x, y, 0, 0);
				r.setSample(x, y, 1, 0);
				r.setSample(x, y, 2, 0);
			}
		}
	}

	public static void shadeRed(WritableRaster r1, WritableRaster r2) {
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				if (isRed(r1,x,y)) {
					r2.setSample(x, y, 0, 255);
					r2.setSample(x, y, 1, 255);
					r2.setSample(x, y, 2, 255);
				} else {
					r2.setSample(x, y, 0, 0);
					r2.setSample(x, y, 1, 0);
					r2.setSample(x, y, 2, 0);
				}
			}
		}
	}

	public void seekStart2(WritableRaster r1, WritableRaster r2) {
		for (int x = 0; x < r1.getWidth(); ++x) {
			int y = 0;
			int ey = 0;
			while (ey < r2.getHeight()) {
				if (isRed(r1,x,ey)) {
					while (isRed(r1,x,ey) && ey < r2.getHeight()) ++ey;
					y += ey;
					y /= 2;
					if (!isRed(r1,x-1,y)) {
						if (!isRed(r2,x,y) && !isRed(r2,x+1,y) && !isRed(r2,x+2,y) && !isRed(r2,x+3,y))
							circleDetectRightFull(r1,r2,x,y);
					}
					if (!isRed(r1,x+1,y)) {
						if (!isRed(r2,x,y) && !isRed(r2,x-1,y) && !isRed(r2,x-2,y) && !isRed(r2,x-3,y))
							circleDetectLeftFull(r1,r2,x,y);
					}
				}
				y = ++ey;
			}
		}
	}

	public static boolean isRed(WritableRaster r1, int x, int y) {
		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
			int r = r1.getSample(x, y, 0);
			int g = r1.getSample(x, y, 1);
			int b = r1.getSample(x, y, 2);
			if (r > 80 && g+b < r) return true;
		} return false;
	}

	public void circleDetectRightFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
		int diam = 0;
		while (isRed(r1,startx+diam,starty)) ++diam;
		if (diam/2 == 0) return;
		float[] rvals = new float[diam];
		int h = 0;
		for (int x = startx; x < startx+diam/2; ++x) {
			for (int y = starty; y < r1.getHeight(); ++y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(y-starty);
					++h;
					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		h = (diam+1)/2+1;
		for (int x = startx+diam/2; x < startx+diam; ++x) {
			for (int y = starty; y < r1.getHeight(); ++y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(y-starty);
					--h;
					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		//printList(rvals);
		Arrays.sort(rvals);
		//printList(rvals);
		float lrt = median(rvals);
		for (int x = 0; x < diam; ++x)
			rvals[x] = Math.abs(lrt-rvals[x]);
		Arrays.sort(rvals);
		float ldevt = median(rvals);
		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
		h = 0;
		for (int x = startx; x < startx+diam/2; ++x) {
			for (int y = starty-1; y >= 0; --y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(starty-y);
					++h;
					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		h = (diam+1)/2+1;
		for (int x = startx+diam/2; x < startx+diam; ++x) {
			for (int y = starty-1; y >= 0; --y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(starty-y);
					--h;
					rvals[x-startx] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		Arrays.sort(rvals);
		float lrb = median(rvals);
		for (int x = 0; x < diam; ++x)
			rvals[x] = Math.abs(lrb-rvals[x]);
		Arrays.sort(rvals);
		float ldevb = median(rvals);
		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
		if (ldevt < ldevb) {
			if (ldevt < 0.15f*(float)diam && lrt > 3.0f) {
				circleFound(r2,(int)(startx+Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
			}
		} else {
			if (ldevb < 0.15f*(float)diam && lrb > 3.0f) {
				circleFound(r2,(int)(startx+Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
			}
		}
	}

	public void circleDetectLeftFull(WritableRaster r1, WritableRaster r2, int startx, int starty) {
		int diam = 0;
		while (isRed(r1,startx-diam,starty)) ++diam;
		if (diam/2 == 0) return;
		float[] rvals = new float[diam];
		int h = 0;
		for (int x = startx; x > startx-diam/2; --x) {
			for (int y = starty; y < r1.getHeight(); ++y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(y-starty);
					++h;
					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		h = (diam+1)/2+1;
		for (int x = startx-diam/2; x > startx-diam; --x) {
			for (int y = starty; y < r1.getHeight(); ++y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(y-starty);
					--h;
					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		//printList(rvals);
		Arrays.sort(rvals);
		//printList(rvals);
		float lrt = median(rvals);
		for (int x = 0; x < diam; ++x)
			rvals[x] = Math.abs(lrt-rvals[x]);
		Arrays.sort(rvals);
		float ldevt = median(rvals);
		ldevt += 32.0f*Math.abs((float)lrt-(float)diam/2.0f)/(float)diam;
		//System.out.println("lrt is "+lrt+" ldevt is "+ldevt);
		h = 0;
		for (int x = startx; x > startx-diam/2; --x) {
			for (int y = starty-1; y >= 0; --y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(starty-y);
					++h;
					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		h = (diam+1)/2+1;
		for (int x = startx-diam/2; x > startx-diam; --x) {
			for (int y = starty-1; y >= 0; --y) {
				if (!isRed(r1,x,y)) {
					int c = 2*(starty-y);
					--h;
					rvals[startx-x] = (c*c+4.0f*h*h)/(8.0f*h);
					break;
				}
			}
		}
		Arrays.sort(rvals);
		float lrb = median(rvals);
		for (int x = 0; x < diam; ++x)
			rvals[x] = Math.abs(lrb-rvals[x]);
		Arrays.sort(rvals);
		float ldevb = median(rvals);
		ldevb += 32.0f*Math.abs((float)lrb-(float)diam/2.0f)/(float)diam;
		//if (stoptop < 4 || stoptop*2 < stopbot) ldevt = Float.MAX_VALUE;
		//if (stopbot < 4 || stopbot*2 < stoptop) ldevb = Float.MAX_VALUE;
		//System.out.println("lrb is "+lrb+" ldevb is "+ldevb);
		if (ldevt < ldevb) {
			if (ldevt < 0.15f*(float)diam && lrt > 3.0f) {
				circleFound(r2,(int)(startx-Math.ceil(lrt)),starty,(int)(Math.ceil(lrt)));
			}
		} else {
			if (ldevb < 0.15f*(float)diam && lrb > 3.0f) {
				circleFound(r2,(int)(startx-Math.ceil(lrb)),starty,(int)(Math.ceil(lrb)));
			}
		}
	}

	public void circleFound(WritableRaster r1, int x, int y, int r) {
		if (x >= 0 && x < r1.getWidth() && y >= 0 && y < r1.getHeight()) {
			filledCircle(r1,x,y,r);
			r1.setSample(x, y, 2, 255);
			//System.out.println(r);
			int ndistance = 600/r;
			int npxoffset = x-r1.getWidth()/2;
			System.out.println("distance is "+ndistance+"cm offcenter is "+npxoffset+"px");
			found = 10;
			if (distance > ndistance || lifetime == 0) {
				lifetime = 3;
				distance = ndistance;
				pxoffset = npxoffset;
			}
		}
	}

	public static void filledCircle(WritableRaster r1, int x0, int y0, int r) {
		int xe = Math.min(r1.getWidth(), x0+r+1);
		int ye = Math.min(r1.getHeight(), y0+r+1);
		for (int x = Math.max(0, x0-r); x < xe; ++x) {
			int xq = (x0-x)*(x0-x);
			for (int y = Math.max(0, y0-r); y < ye; ++y) {
				if (xq+(y0-y)*(y0-y) <= r*r)
					r1.setSample(x, y, 0, 255);
			}
		}
	}

	public static float median(float[] arr) {
		if (arr.length == 0) return 0.0f;
		if (arr.length % 2 == 0) {
			return (arr[arr.length/2-1] + arr[arr.length/2])/2;
		} else {
			return arr[arr.length/2];
		}
	}

	public static float median (float[] arr, int end) {
		if (end == 0) return 0.0f;
		if (end % 2 == 0) {
			return (arr[end/2-1] + arr[end/2])/2;
		} else {
			return arr[end/2];
		}
	}

	public static void printList(int[] c) {
		if (c.length == 0) return;
		System.out.print("[ ");
		for (int x = 0; x < c.length-1; ++x) {
			System.out.print(c[x]+", ");
		}
		System.out.println(c[c.length-1]+" ]");
	}

	public static void printList(float[] c) {
		if (c.length == 0) return;
		System.out.print("[ ");
		for (int x = 0; x < c.length-1; ++x) {
			System.out.print(c[x]+", ");
		}
		System.out.println(c[c.length-1]+" ]");
	}
}
