/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */

import maslab.telemetry.*;
import maslab.telemetry.channel.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class BotClientSender extends java.lang.Thread {
	public ImageChannel origim = null;
	public ImageChannel procim = null;
	public BufferedImage origI = null;
	public BufferedImage procI = null;
	public boolean running = true;

	public void run() {
		while (origim == null) {
			try {
				java.lang.Thread.sleep(1000);
				origim = new ImageChannel("origim");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		while (procim == null) {
			try {
				java.lang.Thread.sleep(1000);
				procim = new ImageChannel("procim");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		while (running) {
			try {
				java.lang.Thread.sleep(1000);
				origim.publish(origI);
				procim.publish(procI);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void bye() {
		running = false;
	}
}
