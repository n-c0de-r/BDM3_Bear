import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Opens an image window and adds a panel below the image
 */
public class GRDM_U3_s0577683 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = { "Original", "Rot-Kanal", "Graustufen", "Negativbild", "Binärbild" };

	public static void main(String args[]) {

		IJ.open("bear.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3_s0577683 pw = new GRDM_U3_s0577683();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		if (imp == null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}

	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int[]) ip.getPixels()).clone();
	}

	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class

	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			// JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			}
		}

		private void changePixelValues(ImageProcessor ip) {

			// Array zum ZurÃ¼ckschreiben der Pixelwerte
			int[] pixels = (int[]) ip.getPixels();

			if (method.equals("Original")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;

						pixels[pos] = origPixels[pos];
					}
				}
			}

			if (method.equals("Rot-Kanal")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						// int g = (argb >> 8) & 0xff;
						// int b = argb & 0xff;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden

						pixels[pos] = (0xFF << 24) | (r << 16) | (0 << 8) | 0;
					}
				}
			}

			// Methode um Negativ zu machen.
			if (method.equals("Negativbild")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 255 - r; // Farbwerte umkehren
						int gn = 255 - g;
						int bn = 255 - b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}

			// Methode um Graustufen zu machen.
			if (method.equals("Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int gray = (r + g + b) / 3;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden

						pixels[pos] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
					}
				}
			}
			
			// Methode um Binär zu machen.
			if (method.equals("Binärbild")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int gray = (r + g + b) / 3;
						
						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden
						
						if (gray>128) {
							pixels[pos] = (0xFF << 24) | (255 << 16) | (255 << 8) | 255;
						} else {
							pixels[pos] = (0xFF << 24) | (0 << 16) | (0 << 8) | 0;
						}
					}
				}
			}

		}

	} // CustomWindow inner class
}
