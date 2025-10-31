package org.synergy.atari;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;
import java.util.Timer;
import java.util.TimerTask;
import static org.synergy.atari.SignedFixedPointLogExpTable.log;
import static org.synergy.atari.SignedFixedPointLogExpTable.exp;

public class GUI extends Canvas implements Runnable {
	public static final int fps = 60;
	public static final int WIDTH = 320 * 4;
	public static final int HEIGHT = 200 * 4;
	public static final String TITLE = "SYNERGY";

	private static BufferedImage texture = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private static int[] data = ((DataBufferInt) texture.getRaster().getDataBuffer()).getBankData()[0];

	public static JFrame frame;

	private static Thread thread;
	private static Timer timer;

	private static boolean running = false;

	public static int frames;
	public static int ticks;

	{
		texture.setAccelerationPriority(1f);
	}

	public GUI() {
		Dimension size = new Dimension(WIDTH, HEIGHT);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
	}

	public void render() {
		frames++;

		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}

		Graphics g = bs.getDrawGraphics();


		final int my = HEIGHT >> 1;
		final int mx = WIDTH >> 1;

		// exp(log(a)/2)) = sqrt(a)

		/*int yoff = 0;
		
		 * for (int y = 0; y < HEIGHT; y++) {
		 * int ym = y - my;
		 * int yml = log(ym);
		 * int eyy = exp(yml + yml) >> 4; // y^2
		 * 
		 * for (int x = 0; x < WIDTH; x++) {
		 * int xm = x - mx;
		 * int xml = log(xm);
		 * int exx = exp(xml + xml) >> 4; // x^2
		 * 
		 * int r = exx + eyy;
		 * int sq = exp(log(r) >> 1); // sqrt(x^2 + y^2)
		 * int sq2 = sq<<2;
		 * data[x + yoff] = sq2+ticks
		 * }
		 * yoff += WIDTH;
		 * }
		 */

		
		/*for (int y = 0; y < HEIGHT; y++) {
			int ym = y - my;
			for (int x = 0; x < WIDTH; x++) {
				int xm = x - mx;
				int k =  (int) Math.sqrt(ym * ym + xm * xm);
				int cc = k;
				data[x + y * WIDTH] = cc + ticks;
			}
		}*/		 

		/*
		 * for (int y = 0; y < HEIGHT; y++) {
		 * int ym = y - my;
		 * for (int x = 0; x < WIDTH; x++) {
		 * int xm = x - mx;
		 * double a = Math.atan2(ym, xm);
		 * int k = (int)(xm / Math.cos(a));
		 * data[x+ y*WIDTH] = k+ticks;
		 * }
		 * }
		 */
		

		final int pattern = (128 + 16 + 8 + 4);

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int dx = x - mx;
				int z = y + 320;
				int v = ((dx << 14) / z) + (ticks << 8);
				int u = 65536 / z - ticks;
				int cv = v >> 8;
				int cc = (u ^ cv) & pattern;
				data[x + y * WIDTH] = (cc << cc);
			}
		}

		g.drawImage(texture, 0, 0, null);

		g.dispose();
		bs.show();
	}

	class MTask extends TimerTask {
		public void run() {
			if (ticks % fps == 0) {
				System.out.println("ticks: " + ticks + " FRAMES: " + frames);
				frames = 0;
			}
			ticks++;
		}
	}

	public synchronized void start() {
		if (running)
			return;
		running = true;

		timer = new Timer("Tick", true);
		timer.scheduleAtFixedRate(new MTask(), 0, 1000 / fps);

		thread = new Thread(this, "Render Thread");
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		try {
			System.exit(1);
			frame.dispose();
			timer.cancel();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (running) {
			render();
		}
	}

	public static void main(String[] args) {
		GUI gui = new GUI();
		frame = new JFrame("Synergy");
		frame.add(gui);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		gui.start();
	}
}
