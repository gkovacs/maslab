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
		int[] rgbf = new int[3];
		int[] rgbft = new int[3];
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

	public static void convolveBW(WritableRaster r1, WritableRaster r2, int[][] m, int w) {
		int[] rgbf = new int[1];
		int[] rgbft = new int[3];
		for (int x = 0; x < r1.getWidth(); ++x) {
			for (int y = 0; y < r1.getHeight(); ++y) {
				rgbf[0] = 0;
				for (int my = 0; my < m.length; ++my) {
					if (y+my-m.length/2 < 0 || y+my-m.length/2 >= r1.getHeight()) continue;
					for (int mx = 0; mx < m[my].length; ++mx) {
						if (x+mx-m[my].length/2 < 0 || x+mx-m[my].length/2 >= r1.getWidth()) continue;
						r1.getPixel(x+mx-m[my].length/2, y+my-m.length/2, rgbft);
						//System.out.println("("+rgbft[0]+","+rgbft[1]+","+rgbft[2]+")");
						for (int i = 0; i < rgbft.length; ++i)
							rgbf[0] += rgbft[i]*m[my][mx];
					}
				}
				rgbf[0] /= w;
				//System.out.println("("+rgbf[0]+","+rgbf[1]+","+rgbf[2]+")");
				r2.setPixel(x, y, rgbf);
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
				r3.setSample(x, y, 0, Math.sqrt(square(r1.getSample(x, y, 0))+square(r2.getSample(x, y, 0))));
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
		convolveBW(r2, r3, m3, 16);
		BufferedImage im4 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r4 = im4.getRaster();
		int[][] m4 = {{1,0,-1},{2,0,-2},{1,0,-1}};
		convolveBW(r2, r4, m4, 16);
		BufferedImage im5 = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster r5 = im5.getRaster();
		sqrtImageBW(r3, r4, r5);
		//for (int x = 0; x < 50; ++x) {
		//	for (int y = 0; y < 50; ++y)
		//		im.getR
		//		im.setRGB(x, y, 255*255*255);
		//}
		JFrame jf = new JFrame();
		ImageIcon ic = new ImageIcon();
		JLabel jl = new JLabel();
		ic.setImage(im2);
		jl.setIcon(ic);
		ImageIcon ic2 = new ImageIcon();
		JLabel jl2 = new JLabel();
		ic2.setImage(im3);
		jl2.setIcon(ic2);
		ImageIcon ic3 = new ImageIcon();
		JLabel jl3 = new JLabel();
		ic3.setImage(im4);
		jl3.setIcon(ic3);
		ImageIcon ic4 = new ImageIcon();
		JLabel jl4 = new JLabel();
		ic4.setImage(im5);
		jl4.setIcon(ic4);
		JPanel cp = new JPanel(new GridLayout(1,4));
		cp.add(jl);
		cp.add(jl2);
		cp.add(jl3);
		cp.add(jl4);
		//jf.getContentPane().add(jl);
		//jf.getContentPane().add(jl2);
		//jf.setSize(im.getWidth()*2, im.getHeight());
		//jf.setVisible(true);
		jf.setContentPane(cp);
		jf.setSize(im.getWidth()*4, im.getHeight());
		jf.setVisible(true);
		//jf2.setSize(im2.getWidth(), im2.getHeight());
		//jf2.setVisible(true);

	//System.out.println(args);
        // TODO code application logic here
    }

}
