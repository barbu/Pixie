package org.pixie;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class EffectsMenu extends JMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	public Pixie pixie;
	JMenuItem blur, value, invert, fade, colorize, histogram, shear, sharpen,
			emboss, mean_removal, smooth;

	public class Blur implements ImageAction {
		public int amount;

		public Blur(int amt) {
			amount = amt;
		}

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			// create the blur kernel
			int numCoords = amount * amount;
			float blurFactor = 1.0f / numCoords;

			float[] blurKernel = new float[numCoords];
			for (int j = 0; j < numCoords; j++)
				blurKernel[j] = blurFactor;

			Kernel k = new Kernel(amount, amount, blurKernel);
			ConvolveOp blur = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);
			g2.drawImage(c.getRenderImage(), blur, 0, 0);
		}
	}

	public class Mean_Removal implements ImageAction {
		public int amount;

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			BufferedImage temp = pixie.canvas.getRenderImage();

			Kernel kernel = new Kernel(3, 3, new float[] { -1, -1, -1, -1, 9,
					-1, -1, -1, -1 });

			BufferedImageOp op = new ConvolveOp(kernel);
			temp = op.filter(temp, null);

			g2.drawImage(c.getRenderImage(), op, 0, 0);
		}
	}

	public class Smooth implements ImageAction {
		public int amount;

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			BufferedImage temp = pixie.canvas.getRenderImage();

			Kernel kernel = new Kernel(3, 3,
					new float[] { (float) 1 / 9, (float) 1 / 9, (float) 1 / 9,
							(float) 1 / 9, (float) 1 / 9, (float) 1 / 9,
							(float) 1 / 9, (float) 1 / 9, (float) 1 / 9 });

			BufferedImageOp op = new ConvolveOp(kernel);
			temp = op.filter(temp, null);

			g2.drawImage(c.getRenderImage(), op, 0, 0);

		}
	}

	public class Emboss implements ImageAction {
		public int amount;

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			BufferedImage temp = pixie.canvas.getRenderImage();

			Kernel kernel = new Kernel(3, 3, new float[] { -2, 0, 0, 0, 1, 0,
					0, 0, 2 });

			BufferedImageOp op = new ConvolveOp(kernel);
			temp = op.filter(temp, null);

			g2.drawImage(c.getRenderImage(), op, 0, 0);

		}
	}

	public class Value implements ImageAction {
		public float amount;

		public Value(float amt) {
			amount = amt;
		}

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;
			float[] scale = { amount, amount, amount, 1.0f }; // keep alpha
			float[] offsets = { 0.0f, 0.0f, 0.0f, 0.0f };
			RescaleOp value = new RescaleOp(scale, offsets, null);
			g2.drawImage(c.getRenderImage(), value, 0, 0);
		}
	}

	public class Invert implements ImageAction {
		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2d = (Graphics2D) g;
			float[] negFactors = { -1.0f, -1.0f, -1.0f, 1.0f }; // keep alpha
			float[] offsets = { 255f, 255f, 255f, 0.0f };
			RescaleOp invert = new RescaleOp(negFactors, offsets, null);
			g2d.drawImage(c.getRenderImage(), invert, 0, 0);
		}
	}

	public class Fade implements ImageAction {
		public Color fadeTo;
		public float amount;

		public Fade(Color to, float amt) {
			fadeTo = to;
			amount = amt;
		}

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2d = (Graphics2D) g;

			Composite oldComp = g2d.getComposite();
			Color oldCol = g2d.getColor();

			g2d.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, amount));
			g2d.setColor(fadeTo);
			Dimension d = c.getImageSize();
			g2d.fillRect(0, 0, d.width, d.height);
			g2d.setColor(oldCol);
			g2d.setComposite(oldComp);
		}
	}

	public class Sharpen implements ImageAction {

		public void paint(Graphics g) {
			Canvas c = pixie.canvas;
			Graphics2D g2 = (Graphics2D) g;

			BufferedImage temp = pixie.canvas.getRenderImage();
			Kernel kernel = new Kernel(3, 3, new float[] { 0, (float) -2 / 3,
					0, (float) -2 / 3, (float) 11 / 3,

					(float) -2 / 3, 0, (float) -2 / 3, 0 });
			BufferedImageOp op = new ConvolveOp(kernel);
			temp = op.filter(temp, null);

			g2.drawImage(c.getRenderImage(), op, 0, 0);
		}
	}

	public void applyAction(ImageAction act) {
		Canvas c = pixie.canvas;
		c.acts.add(act);
		c.redrawCache();
	}

	public EffectsMenu(Pixie pixie) {
		super("Effects");
		this.pixie = pixie;

		blur = new JMenuItem("Blur");
		blur.addActionListener(this);
		add(blur);

		value = new JMenuItem("Value");
		value.addActionListener(this);
		add(value);

		invert = new JMenuItem("Invert");
		invert.addActionListener(this);
		add(invert);

		fade = new JMenuItem("Fade to Black");
		fade.addActionListener(this);
		add(fade);

		addSeparator();

		colorize = new JMenuItem("Colorize");
		colorize.setEnabled(false);
		add(colorize);

		histogram = new JMenuItem("Histogram");
		histogram.addActionListener(this);
		add(histogram);

		sharpen = new JMenuItem("Sharpen");
		sharpen.addActionListener(this);
		add(sharpen);

		shear = new JMenuItem("Shear");
		shear.addActionListener(this);
		add(shear);

		smooth = new JMenuItem("Smooth");
		smooth.addActionListener(this);
		add(smooth);

		emboss = new JMenuItem("Emboss");
		emboss.addActionListener(this);
		add(emboss);

		mean_removal = new JMenuItem("Mean_Removal");
		mean_removal.addActionListener(this);
		add(mean_removal);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == blur) {
			Integer integer = IntegerDialog.getInteger("Blur amount (1-9)", 1,
					9, 3, 3);
			if (integer != null)
				applyAction(new Blur(integer));
			return;
		}

		if (e.getSource() == smooth) {
			applyAction(new Smooth());
			return;
		}
		if (e.getSource() == emboss) {
			applyAction(new Emboss());
			return;
		}
		if (e.getSource() == mean_removal) {
			applyAction(new Mean_Removal());
			return;
		}

		if (e.getSource() == value) {
			Integer integer = IntegerDialog.getInteger("Value", -10, 10, 0, 5);
			if (integer != null)
				applyAction(new Value((integer + 10) / 10.0f));
			return;
		}
		if (e.getSource() == invert) {
			applyAction(new Invert());
			return;
		}
		if (e.getSource() == shear) {
			AffineTransform tx = new AffineTransform();
			Integer ox = IntegerDialog.getInteger("Ox (-5 , 5)", -5, 5, 0, 3);
			Integer oy = IntegerDialog.getInteger("Oy (-5 , 5)", -5, 5, 0, 3);
			tx.shear((float) ox / 10, (float) oy / 10);
			AffineTransformOp op = new AffineTransformOp(tx,
					AffineTransformOp.TYPE_BILINEAR);
			BufferedImage tempBufferedImage = op.filter(
					pixie.canvas.getRenderImage(), null);
			tempBufferedImage = op.filter(tempBufferedImage, null);
			pixie.canvas.setImage(tempBufferedImage);
			System.out.println("test");
			return;
		}

		if (e.getSource() == fade) {
			Integer integer = IntegerDialog.getInteger("Fade amount (0-256)",
					0, 256, 128, 64);
			if (integer != null)
				applyAction(new Fade(Color.BLACK, ((float) integer) / 256.0f));
			return;
		}

		if (e.getSource() == sharpen) {
			applyAction(new Sharpen());
			return;
		}

		if (e.getSource() == histogram) {
			BufferedImage temp = pixie.canvas.getRenderImage();
			// Get RGB
			int[][] bins = new int[3][256];
			int height = temp.getHeight();
			int width = temp.getWidth();

			java.awt.image.Raster raster = temp.getRaster();
			int max = -1;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int c1 = raster.getSample(i, j, 0);
					int c2 = raster.getSample(i, j, 1);
					int c3 = raster.getSample(i, j, 2);

					bins[0][c1]++;
					bins[1][c2]++;
					bins[2][c3]++;
					max = Math.max(
							max,
							Math.max(bins[0][c1],
									Math.max(bins[0][c2], bins[0][c3])));
				}
			}

			BufferedImage output = new BufferedImage(255, 900,
					BufferedImage.TYPE_INT_RGB);
			int rgb = 0;
			for (int k = 0; k < 3; k++) {
				for (int i = 0; i < 255; i++)
					for (int j = 0; j < 300; j++) {
						// System.out.println(bins[k][i]);
						if (j < bins[k][i] * 300 / max) {
							switch (k) {
							case 0:
								rgb = (255 << 16) | (0 << 8) | 0;
								break;
							case 1:
								rgb = (0 << 16) | (255 << 8) | 0;
								break;
							case 2:
								rgb = (0 << 16) | (0 << 8) | 255;
								break;
							}
							output.setRGB(i, (k + 1) * 300 - j - 1, rgb);
						} else {
							rgb = (255 << 16) | (255 << 8) | 255;
							output.setRGB(i, (k + 1) * 300 - j - 1, rgb);
						}
					}
			}

			// java.io.FileWriter fstream;
			try {
				java.io.File f = pixie.getFile(true);
				if (f == null) {
					javax.swing.JOptionPane.showMessageDialog(null,
							"You haven't choose any path. Aborting!");
					return;
				}

				ImageIO.write(output, "PNG", f);
				/*
				 * fstream = new java.io.FileWriter(f.getAbsoluteFile());
				 * java.io.BufferedWriter out = new
				 * java.io.BufferedWriter(fstream); for (int i = 0; i < 3; i++)
				 * { for (int j = 0; j < 256; j++) out.write(bins[i][j] + " ");
				 * out.write("\n\r\n"); } //Close the output stream out.close();
				 */
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			javax.swing.JOptionPane.showMessageDialog(null,
					"Histograma a fost salvata!");
			return;
		}
	}
}
