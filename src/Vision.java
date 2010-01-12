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
	public byte color = 0; // 0 is red, 1 is yellow, 2 is blue

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
		final float k = 0.003f;
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
			//seekStart2(r,r3);
			findExtrema(r, r3);
			ic3.setImage(im3);
			jl3.setIcon(ic3);
			jl3.repaint();
			blankimg(r3);
			if (found > 0) { // moving towards goal
				leftMotorWeight[idx] = 0.5f;
				rightMotorWeight[idx] = 0.5f;
				float rspeed = k*pxoffset;
				float lspeed = -rspeed;
				if (lspeed > rspeed) {
					rspeed += 0.5f-Math.abs(lspeed);
					lspeed = 0.5f;
				} else {
					lspeed += 0.5f-Math.abs(rspeed);
					rspeed = 0.5f;
				}
				lspeed = bound(lspeed, 1.0f, -1.0f);
				rspeed = bound(rspeed, 1.0f, -1.0f);
				leftMotorAction[idx] = lspeed;
				rightMotorAction[idx] = rspeed;
				/*
				if (pxoffset > 0) { // to the right
					leftMotorAction[idx] = 0.5f;
					rightMotorAction[idx] = 0.0f;
				} else {
					leftMotorAction[idx] = 0.0f;
					rightMotorAction[idx] = 0.5f;
				}
				 */
				//leftMotorAction[idx] = bound((float)distance*(1.0f+0.01f*(float)pxoffset)/100.0f, 0.5f, 0.1f);
				//rightMotorAction[idx] = bound((float)distance*(1.0f-0.01f*(float)pxoffset)/100.0f, 0.5f, 0.1f);
			} else { // idly searching, nothing interesting in sight, turn left
				leftMotorWeight[idx] = 0.3f;
				rightMotorWeight[idx] = 0.3f;
				leftMotorAction[idx] = 0.5f;
				rightMotorAction[idx] = 0.5f;

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

	public void setExtrema(final WritableRaster r1, final WritableRaster r2, final int x, final int y, final Extrema m) {
		m.update(x, y);
		r2.setSample(x, y, 0, 255);
		if (isRed(r1,x+1,y) && r2.getSample(x+1, y, 0) != 255) {
			setExtrema(r1,r2,x+1,y,m);
		} if (isRed(r1,x-1,y) && r2.getSample(x-1, y, 0) != 255) {
			setExtrema(r1,r2,x-1,y,m);
		} if (isRed(r1,x,y+1) && r2.getSample(x, y+1, 0) != 255) {
			setExtrema(r1,r2,x,y+1,m);
		} if (isRed(r1,x,y-1) && r2.getSample(x, y-1, 0) != 255) {
			setExtrema(r1,r2,x,y-1,m);
		}
	}

	public static boolean isBlank(WritableRaster r, int x, int y) {
		return r.getSample(x, y, 0) != 255;
	}

	private void setExtrema2(final WritableRaster raster, final WritableRaster r2, final int x, final int y, final Extrema m) {
			Rectangle bounds = raster.getBounds();
			int fillL = x;
			do {
					m.update(fillL, y);
					r2.setSample(fillL, y, 0, 255);
					fillL--;
			} while (fillL >= 0 && isRed(raster, fillL, y) && isBlank(r2, fillL, y));
			fillL++;

			// find the right right side, filling along the way
			int fillR = x;
			do {
					m.update(fillR, y);
					r2.setSample(fillR, y, 0, 255);
					fillR++;
			} while (fillR < bounds.width - 1 && isRed(raster, fillR, y) && isBlank(r2, fillR, y));
			fillR--;

			// checks if applicable up or down
			for (int i = fillL; i <= fillR; i++) {
					if (y > 0 && isRed(raster, i, y - 1) && isBlank(r2, i, y-1)) setExtrema2(raster, r2, i, y - 1, m);
					if (y < bounds.height - 1 && isRed(raster, i, y + 1) && isBlank(r2, i, y+1)) setExtrema2(raster, r2, i, y + 1, m);
			}
	}

	// Returns true if RGBA arrays are equivalent, false otherwise
	// Could use Arrays.equals(int[], int[]), but this is probably a little faster...
	private static boolean isEqualRgba(int[] pix1, int[] pix2) {
			return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3];
	}
	

	public static void drawline(final WritableRaster r, int x, int y, final int x2, final int y2) {
		int w = x2 - x ;
		int h = y2 - y ;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
		int longest = Math.abs(w) ;
		int shortest = Math.abs(h) ;
		if (!(longest>shortest)) {
			longest = Math.abs(h) ;
			shortest = Math.abs(w) ;
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
			dx2 = 0 ;
		}
		int numerator = longest >> 1 ;
		for (int i=0;i<=longest;i++) {
			r.setSample(x, y, 2, 255);
			numerator += shortest ;
		 if (!(numerator<longest)) {
				numerator -= longest ;
				x += dx1 ;
			 y += dy1 ;
			} else {
				x += dx2 ;
				y += dy2 ;
			}
		}
	}

	public void countLine(final WritableRaster r, int x, int y, final int x2, final int y2, final int[] matchvnon) {
		int w = x2 - x ;
		int h = y2 - y ;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
		int longest = Math.abs(w) ;
		int shortest = Math.abs(h) ;
		if (!(longest>shortest)) {
			longest = Math.abs(h) ;
			shortest = Math.abs(w) ;
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
			dx2 = 0 ;
		}
		int numerator = longest >> 1 ;
		for (int i=0;i<=longest;i++) {
			//r.setSample(x, y, 2, 255);
			if (isRed(r,x,y)) {
				++matchvnon[0];
			} else {
				++matchvnon[1];
			}
			numerator += shortest ;
		 if (!(numerator<longest)) {
				numerator -= longest ;
				x += dx1 ;
			 y += dy1 ;
			} else {
				x += dx2 ;
				y += dy2 ;
			}
		}
	}

	public void findExtrema(final WritableRaster r1, final WritableRaster r2) {
		Extrema m = new Extrema();
		int[] matchvnon = new int[2];
		for (int x = 0; x < r1.getWidth(); ++x) {
			int y = 0;
			/*
			int bld = 0;
			for (; bld < 2 && y < r1.getWidth(); ++y) {
				if (isBlue(r1,x,y)) {
					++bld;
					r2.setSample(x, y, 2, 255);
				} else {
					bld = 0;
				}
			}
			*/
			for (; y < r1.getWidth(); ++y) {
				if (isRed(r1,x,y) && isBlank(r2,x,y) &&
					// cardinal
				
					isRed(r1,x+1,y) && isBlank(r2,x+1,y) &&
					isRed(r1,x-1,y) && isBlank(r2,x-1,y) &&
					isRed(r1,x,y+1) && isBlank(r2,x,y+1) &&
					isRed(r1,x,y-1) && isBlank(r2,x,y-1) &&
				
					// diagonals
					
					isRed(r1,x+1,y+1) && isBlank(r2,x+1,y+1) &&
					isRed(r1,x+1,y-1) && isBlank(r2,x+1,y-1) &&
					isRed(r1,x-1,y+1) && isBlank(r2,x-1,y+1) &&
					isRed(r1,x-1,y-1) && isBlank(r2,x-1,y-1)
					
					) {
					m.initval(x, y);
					r2.setSample(x, y, 2, 255);
					setExtrema2(r1, r2, x, y, m);
					r2.setSample(m.lbx, m.lby, 1, 255);
					r2.setSample(m.rbx, m.rby, 1, 255);
					r2.setSample(m.ltx, m.lty, 1, 255);
					r2.setSample(m.rtx, m.rty, 1, 255);
					matchvnon[0] = matchvnon[1] = 0;
					countLine(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4, matchvnon);
					 drawline(r2, m.lbx+(m.rbx-m.lbx)/4, m.lby+(m.rby-m.lby)/4, m.rbx-(m.rbx-m.lbx)/4, m.rby-(m.rby-m.lby)/4);
					//  drawline(r2, m.lbx, m.lby, m.rbx, m.rby);
					countLine(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3, matchvnon);
					 drawline(r2, m.ltx+(m.rbx-m.ltx)/3, m.lty+(m.rby-m.lty)/3, m.rbx-(m.rbx-m.ltx)/3, m.rby-(m.rby-m.lty)/3);
					//  drawline(r2, m.ltx, m.lty, m.rbx, m.rby);
					countLine(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3, matchvnon);
					 drawline(r2, m.rtx+(m.lbx-m.rtx)/3, m.rty+(m.lby-m.rty)/3, m.lbx-(m.lbx-m.rtx)/3, m.lby-(m.lby-m.rty)/3);
					//  drawline(r2, m.rtx, m.rty, m.lbx, m.lby);
					if (matchvnon[0] > matchvnon[1]) {
						System.out.println("ball"+matchvnon[0]+" vs "+matchvnon[1]);
						int lbrb = (m.lbx-m.rbx)*(m.lbx-m.rbx)+(m.lby-m.rby)*(m.lby-m.rby); // bot-left to bot-right distance squared
						int lbrt = (m.lbx-m.rtx)*(m.lbx-m.rtx)+(m.lby-m.rty)*(m.lby-m.rty); // bot-left to top-right distance squared
						int ltrb = (m.ltx-m.rbx)*(m.ltx-m.rbx)+(m.lty-m.rby)*(m.lty-m.rby); // top-left to bot-right distance squared
						if (3*lbrb < lbrt || 3*lbrb < ltrb) { // likely actually a gate // doesn't seem to exactly work
							System.out.println("gate misdetected as ball");
						}
						// TODO radius (intersection) of ball
						double radius = 0.0;
						radius += Math.sqrt(((m.tx-m.bx)*(m.tx-m.bx))/4+((m.ty-m.by)*(m.ty-m.by))/4);
						radius += Math.sqrt(((m.rx-m.lx)*(m.rx-m.lx))/4+((m.ry-m.ly)*(m.ry-m.ly))/4);
						radius += Math.sqrt(((m.ltx-m.rbx)*(m.ltx-m.rbx))/4+((m.lty-m.rby)*(m.lty-m.rby))/4);
						radius += Math.sqrt(((m.rtx-m.lbx)*(m.rtx-m.lbx))/4+((m.rty-m.lby)*(m.rty-m.lby))/4);
						radius /= 4.0;
						circleFound(r2, (m.rx+m.lx)/2, (m.ty+m.by)/2, (int)radius);
						System.out.println("circle found at "+ (m.rx+m.lx)/2+" "+(m.ty+m.by)/2);
						// TODO confirm detection via standard deviation of 8-cardinals
					} else {
						System.out.println("gate"+matchvnon[0]+" vs "+matchvnon[1]);
					}
				}
			}
		}
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

	public void shadeRed(WritableRaster r1, WritableRaster r2) {
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

	public boolean isRed(WritableRaster r1, int x, int y) {
		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
			int r = r1.getSample(x, y, 0);
			int g = r1.getSample(x, y, 1);
			int b = r1.getSample(x, y, 2);
			//if (b < 150 && r > 2*b && g > 2*b) return true; // yellow
			//if (r > 90 && 2*(g+b) < 3*r) return true;
			//if (r > 110 && 3*(g+b) < 4*r) return true; // 26-100
			if (color == 0) { // red
				if (r > 110 && 5*(g+b) < 6*r) return true;
			} else if (color == 1) { // yellow
				if (b < 150 && r > 2*b && g > 2*b) return true; // yellow
			} else if (color == 2) { // blue
				
			}
		} return false;
	}

	public static boolean isBlue(WritableRaster r1, int x, int y) {
		if (x >= 0 && y >= 0 && x < r1.getWidth() && y < r1.getHeight()) {
			int r = r1.getSample(x, y, 0);
			int g = r1.getSample(x, y, 1);
			int b = r1.getSample(x, y, 2);
			//if (b < 150 && r > 2*b && g > 2*b) return true;
			if (b > 110 && 2*(r+g) < 3*b) return true;
			//if (b > 110 && 3*(r+g) < 4*b) return true;
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
		if (r == 0) r = 1; // ugly hack
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
