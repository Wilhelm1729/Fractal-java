package main;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Game extends Canvas implements Runnable {
	
	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	private boolean running = false;
	private Thread thread;
	
	private BufferedImage img;
	
	double zoom = 1;
	double c_re = -0.8721807625423635;
	double c_im = 0.2652135370551579;
	int max_ite = 100;
	double zoomStep = 1.007;
	long maxzoom = (long) Math.pow(10, 16);
	int fr = 0;
	
	double log2 = Math.log(2);
	
	public Game() {
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	}
	
	int[][] mapping = {{0, 0, 0},
	        {25, 7, 26},
	        {9, 1, 47},
	        {4, 4, 73},
	        {0, 7, 100},
	        {12, 44, 138},
	        {24, 82, 177},
	        {57, 125, 209},
	        {134, 181, 229},
	        {211, 236, 248},
	        {241, 233, 191},
	        {248, 201, 95},
	        {255, 170, 0},
	        {204, 128, 0},
	        {153, 87, 0},
	        {106, 52, 3}};
	
	public synchronized void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void run()
	{	
		long timer = 0;
		int updates = 0;
		while(running){
			long startTime = System.nanoTime();
			//update();
			render();
			updates++;
			long endTime = System.nanoTime();
			long delta = endTime - startTime;
			timer += delta / 1000000;
			if (timer > 1000) {
				System.out.println("fps: " + updates + " #fr:" + fr + " zoom: " + zoom + " max_ite: " + max_ite);
				updates = 0;
				timer = 0;
			}
			if (zoom > maxzoom)
				System.exit(0);
		}
		
	}
	
	public int linearInterpolation(int a, int b, double p) {
		return a + (int) (p * (b - a));
	}
	
	private void draw() {
		zoom *= zoomStep;
		fr++;
		int n = (int) (100 + zoom);
		max_ite = n < 10000 ? n : 10000;
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < HEIGHT; y++) {
				double re = (x - WIDTH / 2) / (double) HEIGHT;
				double im = (y - HEIGHT / 2) / (double) HEIGHT;
				re /= zoom;
				im /= zoom;
				
				// Rotate
				double rot = fr / (double) 100;
				double sre = re;
				double cos = Math.cos(rot);
				double sin = Math.sin(rot);
				re = re * cos - im * sin;
				im = sre * sin + im * cos;
				
				re += c_re;
				im += c_im;
				double cre = re;
				double cim = im;
				Color col = new Color(0);
				//int cl = 0;
				
				// iterate
				double mag = 0;
				int ite = 0;
				
				while (mag < 10 && ite < max_ite) {
					double re_temp = re;
					re = re_temp * re_temp - im * im + cre;
					im = 2 * re_temp * im + cim;
					
					mag = re * re + im * im;
					ite += 1;
				}
				
				
				if (ite < max_ite) {
					
					double log_zn = Math.log(mag) / 2d;
					double nu = Math.log(log_zn / log2) / log2;
					
					double iteration = ite + 1 - nu;
					
					int r = linearInterpolation(mapping[(int) iteration % 15][0], mapping[((int) iteration + 1) % 15][0], iteration % 1);
					int g = linearInterpolation(mapping[(int) iteration % 15][1], mapping[((int) iteration + 1) % 15][1], iteration % 1);
					int b = linearInterpolation(mapping[(int) iteration % 15][2], mapping[((int) iteration + 1) % 15][2], iteration % 1);
					
					col = new Color(r, g, b);
					
					
				}
				
				//https://en.wikipedia.org/wiki/Plotting_algorithms_for_the_Mandelbrot_set#Continuous_(smooth)_coloring
				
				/*
				for (int k = 0; k < max_ite; k++) {
					double pre = re;
					re = pre * pre - im * im + cre;
					im = 2 * pre * im + cim;
					
					
					double mag = re * re + im * im;
					if (mag > 10) {
						int ite = k % 15; //(k + fr) % 64;
						col = new Color(mapping[ite][0], mapping[ite][1], mapping[ite][2]);
						//cl = (0xFF0011 & ((k + fr) * 0xFF11FF)) >> 5 & 0xFFFF00;
						break;
					}
				}
				
				*/
				
				
				
				img.setRGB(x, y, col.getRGB()); //img.setRGB(x, y, cl);
				
				
				
				
				
				
			}
		}
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null)  {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		draw();
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
		/*
		try {
			File outputfile = new File("res/frame" + fr + ".png");
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			System.out.println(e);
		}*/
		g.dispose();
		bs.show();
	}

	private void update() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String args[]) {
		
		Game game = new Game();
		game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		game.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		game.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		
		JFrame frame = new JFrame("fractal");
		frame.add(game);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		game.start();
	}

}
