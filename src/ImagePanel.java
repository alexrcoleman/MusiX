import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel implements ComponentListener {
	private static final long serialVersionUID = 6355616917210554481L;
	private BufferedImage image;
	private boolean async = true;
	private boolean pressed = false;
	private boolean isButton = false;

	public void makeButton() {
		isButton = true;
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pressed = true;
				ImagePanel.this.repaint();
				ImagePanel.this.getParent().repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				ImagePanel.this.repaint();
				ImagePanel.this.getParent().repaint();
			}
		});
		this.addComponentListener(this);
		resizeImage();
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isAsync() {
		return async;
	}

	public ImagePanel() {
		this.setBackground(new Color(0, 0, 0, 0));
	}

	public ImagePanel(InputStream in) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(in);
	}

	public ImagePanel(BufferedImage image) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(image);
	}

	public ImagePanel(byte[] bytes) {
		this.setBackground(new Color(0, 0, 0, 0));
		setImage(bytes);
	}

	public void setImage(BufferedImage image) {
		BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = convertedImg.createGraphics();
		g.drawImage(image, 0, 0, null);
		this.image = convertedImg;
		resizeImage();
		this.repaint();
	}

	public void setImage(byte[] bytes) {
		if (bytes != null) {
			InputStream in = new ByteArrayInputStream(bytes);
			setImage(in);
		}
	}

	public void setImage(final InputStream in) {
		Thread th = new Thread() {
			public void run() {
				try {
					setImage(ImageIO.read(in));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (java.lang.OutOfMemoryError e) {
					Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
					Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
					for (Thread t : threadArray) {
						System.out.println(t.toString() + ":");
						for (StackTraceElement ste : t.getStackTrace()) {
							System.out.println("\t" + ste.toString());
						}
					}
				}
			}
		};
		th.start();
		while (!async && th.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	private BufferedImage scaled;
	private BufferedImage scaledPressed;
	private int lastWidth = -1;

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (this.getWidth() != lastWidth) {
			resizeImage();
			lastWidth = this.getWidth();
		}
		int width = this.getWidth();
		int height = width;
		int x = 0, y = (this.getHeight() - this.getWidth())/2;
		if(y < 0)
			y = 0;
		if (scaled != null) {
			g.drawImage(pressed ? scaledPressed : scaled, x, y, width,height, null);
		}
		//g.drawString("(" + this.getWidth() + "," + this.getHeight(), this.getWidth()/2, this.getHeight()/2);
		//g.drawLine(0, 0, this.getWidth(), this.getHeight());
	}

	int[] darken(int[] argb, int percentage) {
		int[] result = new int[argb.length];
		for (int i = 0; i < argb.length; i++) {
			result[i] = darkenArgb(argb[i], percentage);
		}
		return result;
	}

	private int darkenArgb(int argb, int percentage) {
		return darkenByte(argb, 3, 100) // keep alpha as-is
				| darkenByte(argb, 2, percentage) | darkenByte(argb, 1, percentage) | darkenByte(argb, 0, percentage);
	}

	private int darkenByte(int argb, int index, int percentage) {
		if (percentage < 0 || percentage > 100) {
			throw new IllegalArgumentException("unexpected percentage: [" + percentage + "]");
		}
		if (index < 0 || index > 3) {
			throw new IllegalArgumentException("unexpected index: [" + index + "]");
		}
		int result = (argb >> index) & 0xFF;
		result = result * percentage / 100;
		return result << index;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	private void resizeImage() {
		if (image == null)
			return;
		this.scaled = toBufferedImage(image.getScaledInstance(this.getWidth() == 0 ? 1 : this.getWidth(), -1, Image.SCALE_AREA_AVERAGING));
		if (isButton) {
			this.scaledPressed = new BufferedImage(scaled.getWidth(), scaled.getHeight(), BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < scaled.getWidth(); x++) {
				for (int y = 0; y < scaled.getHeight(); y++) {
					Color c = new Color(scaled.getRGB(x, y));
					int alpha = (scaled.getRGB(x, y) >> 24) & 0xFF;
					Color d = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 1));
					d = d.darker().darker();
					scaledPressed.setRGB(x, y, d.getRGB());
				}
			}
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (this.getWidth() != this.getHeight()) {
			this.setSize(this.getWidth(), this.getWidth());
		}
		resizeImage();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

}