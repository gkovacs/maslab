/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
 *
 * @author geza
 */
public class Main {

    /**
     * @param args the command line arguments
     */

	public static void convolve(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
		int[] rgbf = new int[r1.getNumBands()];
		int[] rgbft = new int[r1.getNumBands()];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				for (int i = 0; i < rgbf.length; ++i)
					rgbf[i] = 0;
				for (int my = 0; my < m.length; ++my) {
					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
					for (int mx = 0; mx < m[my].length; ++mx) {
						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
						for (int i = 0; i < rgbf.length; ++i)
							rgbf[i] += rgbft[i]*m[my][mx];
					}
				}
				for (int i = 0; i < rgbf.length; ++i)
					rgbf[i] /= w;
				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
				r2.setPixel(x, y, rgbf);
			}
		}
	}

	public static void copyChannels(WritableRaster r1, WritableRaster r2, int ch) {
		int[] buf1 = new int[r1.getNumBands()];
		int[] buf2 = new int[r2.getNumBands()];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				r1.getPixel(x, y, buf1);
				for (int i = 0; i < buf2.length; ++i) {
					buf2[i] = buf1[ch+i];
				}
				r2.setPixel(x, y, buf2);
			}
		}
	}

	public static void mergeChannels(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
		int[] buf1 = new int[r1.getNumBands()];
		int[] buf2 = new int[r2.getNumBands()];
		int[] buf3 = new int[r1.getNumBands()+r2.getNumBands()];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r2.getHeight(); ++y) {
				r1.getPixel(x, y, buf1);
				r2.getPixel(x, y, buf2);
				for (int i = 0; i < buf1.length; ++i) {
					buf3[i] = buf1[i];
				} for (int i = 0; i < buf2.length; ++i) {
					buf3[i+buf1.length] = buf2[i];
				}
				r3.setPixel(x, y, buf3);
			}
		}
	}

	public static void sqrtImage(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
		int[] buf1 = new int[3];
		int[] buf2 = new int[3];
		int[] buf3 = new int[3];
		for (int x = 0; x < r3.getWidth(); ++x) {
			for (int y = 0; y < r3.getHeight(); ++y) {
				r1.getPixel(x, y, buf1);
				r2.getPixel(x, y, buf2);
				for (int i = 0; i < buf3.length; ++i)
					buf3[i] = buf1[i]+buf2[i];
				r3.setPixel(x, y, buf3);
			}
		}
	}

	public static int square(int x) {
		return x*x;
	}

	public static void sqrtImageBW(WritableRaster r1, WritableRaster r2, WritableRaster r3) {
		for (int x = 0; x < r3.getWidth(); ++x) {
			for (int y = 0; y < r3.getHeight(); ++y) {
				r3.setSample(x, y, 0, (int)Math.sqrt(square(r1.getSample(x, y, 0))+square(r2.getSample(x, y, 0))));
			}
		}
	}

	public static void normImage(WritableRaster r1, WritableRaster r2) {
		//int[] buf = new int[3];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				//r1.getPixel(x, y, buf);
				//int tot = buf[0]+buf[1]+buf[2];
				//buf[0] *= (400.0f/tot);
				//buf[1] *= (400.0f/tot);
				//buf[2] *= (400.0f/tot);
				//r2.setPixel(x, y, buf);
				int r = r1.getSample(x, y, 0);
				int g = r1.getSample(x, y, 1);
				int b = r1.getSample(x, y, 2);
				int tot = r+g+b;
				r2.setSample(x, y, 0, r*255/tot);
				r2.setSample(x, y, 1, g*255/tot);
				r2.setSample(x, y, 2, b*255/tot);
				r2.setSample(x, y, 3, 255-tot/3);
			}
		}
	}

	public static void mergeAlpha(WritableRaster r1, WritableRaster r2) {
		//int[] buf = new int[3];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				//r1.getPixel(x, y, buf);
				//int tot = buf[0]+buf[1]+buf[2];
				//buf[0] *= (400.0f/tot);
				//buf[1] *= (400.0f/tot);
				//buf[2] *= (400.0f/tot);
				//r2.setPixel(x, y, buf);
				int r = r1.getSample(x, y, 0);
				int g = r1.getSample(x, y, 1);
				int b = r1.getSample(x, y, 2);
				int tot = 3*(255-r1.getSample(x, y, 3));
				r2.setSample(x, y, 0, r*tot/255);
				r2.setSample(x, y, 1, g*tot/255);
				r2.setSample(x, y, 2, b*tot/255);
			}
		}
	}

	public static void rmlastchannel(WritableRaster r1, WritableRaster r2) {
		int[] buf = new int[r1.getNumBands()];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				r1.getPixel(x, y, buf);
				buf[buf.length-1] = 255;
				r2.setPixel(x, y, buf);
			}
		}
	}

	public static void rgb2hsv(WritableRaster r1, WritableRaster r2) {
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				int r = r1.getSample(x, y, 0);
				int g = r1.getSample(x, y, 1);
				int b = r1.getSample(x, y, 2);
				int max = Math.max(Math.max(r, g), b);
				int min = Math.min(Math.min(r, g), b);
				int h = 0;
				if (max == min) {
					
				} else if (max == r) {
					h = (g-b)*85/(2*(max-min));
					if (h < 0)
						h += 255;
				} else if (max == g) {
					h = 85 + (b-r)*85/(2*(max-min));
				} else { // max == b
					h = 170 + (r-g)*85/(2*(max-min));
				}
				r2.setSample(x, y, 0, h); // h
				r2.setSample(x, y, 1, (max == 0) ? 0 : 255*(max-min)/max); // s
				r2.setSample(x, y, 2, max); // v
			}
		}
	}

    public static void main(String[] args) {
		//for (String x : args)
		//	System.out.println(x);
		if (args.length < 1)
		{
			System.out.println("not enough args");
			return;
		}
		File f = new File(args[0]);
		if (!f.exists())
		{
			System.out.println("file "+args[0]+"does not exist");
			return;
		}
		BufferedImage im = null;//new BufferedImage();
		try {
			//ImageIO.read
			im = ImageIO.read(f);
		} catch (IOException e) {
			System.out.println("could not load image");
			return;
		}
		WritableRaster r = im.getRaster();
		BufferedImage im2 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r2 = im2.getRaster();
		//float[][] m = { {01.f, 0.2f, 0.1f}, {0.2f, 0.4f, 0.2f}, {0.1f, 0.2f, 0.1f} };
		//int[][] m = {{1,2,1},{2,4,2},{1,2,1}};
		int[][] m = {{2,4,5,4,2},{4,9,12,9,4},{5,12,15,12,5},{4,9,12,9,4},{2,4,5,4,2}};
		convolve(r, r2, m, 159);
		BufferedImage im3 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r3 = im3.getRaster();
		int[][] m3 = {{1,2,1},{0,0,0},{-1,-2,-1}};
		convolve(r2, r3, m3, 16);
		BufferedImage im4 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r4 = im4.getRaster();
		int[][] m4 = {{1,0,-1},{2,0,-2},{1,0,-1}};
		convolve(r2, r4, m4, 16);
		BufferedImage im5 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r5 = im5.getRaster();
		sqrtImageBW(r3, r4, r5);
		BufferedImage im6 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
		WritableRaster r6 = im6.getRaster();
		normImage(r, r6);
		//BufferedImage im7 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
		//WritableRaster r7 = im7.getRaster();
		//convolve(r6, r7, m, 159);
		BufferedImage im8 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r8 = im8.getRaster();
		copyChannels(r6, r8, 0);
		BufferedImage im9 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r9 = im9.getRaster();
		copyChannels(r6, r9, 3);
		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r10 = im10.getRaster();
		convolve(r9, r10, m, 159);
		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r11 = im11.getRaster();
		convolve(r10, r11, m, 159);
		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
		WritableRaster r12 = im12.getRaster();
		mergeChannels(r8, r11, r12);
		BufferedImage im12n = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r12n = im12n.getRaster();
		mergeAlpha(r12, r12n);
		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r13 = im13.getRaster();
		convolve(r12n, r13, m3, 16);
		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r14 = im14.getRaster();
		convolve(r12n, r14, m4, 16);
		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r15 = im15.getRaster();
		sqrtImageBW(r13, r14, r15);
		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster r16 = im16.getRaster();
		rgb2hsv(r, r16);
		/*
		BufferedImage im10 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r10 = im10.getRaster();
		convolve(r8, r10, m3, 16);
		BufferedImage im11 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r11 = im11.getRaster();
		convolve(r8, r11, m4, 16);
		BufferedImage im12 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r12 = im12.getRaster();
		sqrtImageBW(r10, r11, r12);
		BufferedImage im9b = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r9b = im9b.getRaster();
		convolve(r9, r9b, m, 159);
		BufferedImage im13 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r13 = im13.getRaster();
		convolve(r9b, r13, m3, 16);
		BufferedImage im14 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r14 = im14.getRaster();
		convolve(r9b, r14, m4, 16);
		BufferedImage im15 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r15 = im15.getRaster();
		sqrtImageBW(r13, r14, r15);
		BufferedImage im16 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r16 = im16.getRaster();
		sqrtImageBW(r12, r15, r16);
		*/
		//convolve(r6, r8, m, 159);
		//for (int x = 0; x < 50; ++x) {
		//	for (int y = 0; y < 50; ++y)
		//		im.getR
		//		im.setRGB(x, y, 255*255*255);
		//}
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
		ic3.setImage(im5);
		jl3.setIcon(ic3);
		ImageIcon ic4 = new ImageIcon();
		JLabel jl4 = new JLabel();
		ic4.setImage(im6);
		jl4.setIcon(ic4);
		ImageIcon ic5 = new ImageIcon();
		//JLabel jl5 = new JLabel();
		//ic5.setImage(im7);
		//jl5.setIcon(ic5);
		ImageIcon ic6 = new ImageIcon();
		JLabel jl6 = new JLabel();
		ic6.setImage(im8);
		jl6.setIcon(ic6);
		ImageIcon ic7 = new ImageIcon();
		JLabel jl7 = new JLabel();
		ic7.setImage(im9);
		jl7.setIcon(ic7);
		ImageIcon ic8 = new ImageIcon();
		JLabel jl8 = new JLabel();
		ic8.setImage(im12);
		jl8.setIcon(ic8);
		ImageIcon ic9 = new ImageIcon();
		JLabel jl9 = new JLabel();
		ic9.setImage(im12n);
		jl9.setIcon(ic9);
		ImageIcon ic10 = new ImageIcon();
		JLabel jl10 = new JLabel();
		ic10.setImage(im16);
		jl10.setIcon(ic10);
		JPanel cp = new JPanel(new GridLayout(3,4));
		cp.add(jl);
		cp.add(jl2);
		cp.add(jl3);
		cp.add(jl4);
		//cp.add(jl5);
		cp.add(jl6);
		cp.add(jl7);
		cp.add(jl8);
		cp.add(jl9);
		cp.add(jl10);
		//jf.getContentPane().add(jl);
		//jf.getContentPane().add(jl2);
		//jf.setSize(im.getWidth()*2, im.getHeight());
		//jf.setVisible(true);
		jf.setContentPane(cp);
		jf.setSize(im.getWidth()*4, im.getHeight()*3);
		jf.setVisible(true);
		//jf2.setSize(im2.getWidth(), im2.getHeight());
		//jf2.setVisible(true);

	//System.out.println(args);
        // TODO code application logic here
    }

}
