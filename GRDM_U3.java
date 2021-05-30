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
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = { "Original", "Rot-Kanal", "Graustufen", "Negativ", "Binär", "5 Graustufen", "10 Graustufen",
			"Fehlerdiffusion", "Sepia", "6 Farben" };

	public static void main(String args[]) {

		IJ.open("bear.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
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

						pixels[pos] = (0xFF << 24) | (r << 16) | (0 << 8) | 0;
					}
				}
			}

			// Methode um Negativ zu machen.
			if (method.equals("Negativ")) {

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
						// Durchschnitt berechnen
						int gray = (r + g + b) / 3;

						pixels[pos] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
					}
				}
			}

			// Methode um 5 Graustufen zu machen.
			if (method.equals("5 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						// Durchschnitt berechnen
						int gray = (r + g + b) / 3;

						// Schwellenbereich erzeugen, ähnlich der Streifen der USA Flagge
						double schwelle = 255 / 4.0;

						/*
						 * Grauwert durch Schwellenwert teilen und Ganzzahl berechnen. Je nachdem
						 * entstehen werte zwischen 0 und 4 (5 Farben), die wiederum multipliziert, mit
						 * dem Schwellenwert selbst, jeweils ein vielfaches von 255/4 ergeben: 0, 63,25
						 * ... 255.
						 */
						gray = (int) ((int) (gray / schwelle) * schwelle);

						pixels[pos] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
					}
				}
			}

			// Methode um 10 Graustufen zu machen.
			if (method.equals("10 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						// Durchschnitt berechnen
						int gray = (r + g + b) / 3;

						// Schwellenbereich erzeugen, ähnlich der Streifen der USA Flagge
						double schwelle = 255 / 9.0;

						/*
						 * Grauwert durch Schwellenwert teilen und Ganzzahl berechnen. Je nachdem
						 * entstehen werte zwischen 0 und 9 (10 Farben), die wiederum multipliziert, mit
						 * dem Schwellenwert selbst, jeweils ein vielfaches von 255/4 ergeben: 0, 28, 56
						 * ... 255.
						 */
						gray = (int) ((int) (gray / schwelle) * schwelle);

						pixels[pos] = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
					}
				}
			}

			// Methode um Binär zu machen.
			if (method.equals("Binär")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						// Durchschnitt berechnen
						int gray = (r + g + b) / 3;

						// Wird der Schwellenwert überschritten, ist der Pixel weiß
						if (gray > 128) {
							pixels[pos] = (0xFF << 24) | (255 << 16) | (255 << 8) | 255;
						} else { // sonst schwarz
							pixels[pos] = (0xFF << 24) | (0 << 16) | (0 << 8) | 0;
						}
					}
				}
			}

			// Methode um Fehlerdiffusion zu machen.
			// Formel nach https://www.youtube.com/watch?v=0t8BHaLsXTM&t=255s
			if (method.equals("Fehlerdiffusion")) {

				for (int y = 0; y < height; y++) {
					int error = 0;

					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Durchschnitt berechnen
						int gray = (r + g + b) / 3;

						/*
						 * Alter Code, nicht mehr korrekt if (gray + error < 128) { error = gray +
						 * error; } else { error = 255 - (gray - error); } gray += error;
						 * ----------------------------------
						 */

						gray = gray + error;

						// Wird der Schwellenwert überschritten, ist der Pixel weiß
						// Fehlerzahl wird fortgeführt.
						if (gray < 128) {
							error = gray;
							pixels[pos] = (0xFF << 24) | (0 << 16) | (0 << 8) | 0;
						} else {
							error = gray - 255;
							pixels[pos] = (0xFF << 24) | (255 << 16) | (255 << 8) | 255;
						}
					}
				}
			}

			// Methode um Sepia zu machen.
			if (method.equals("Sepia")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Durchschnitt berechnen und Filterstärke setzen
						int gray = (r + g + b) / 3;
						int depth = 20;
						int intensity = 30;

						// Sepiaberechnung gemäß Tipp von Milana Tran:
						// https://www.tutorialspoint.com/how-to-convert-a-colored-image-to-sepia-image-using-java-opencv-library
						int rn = gray + (depth * 2);
						int gn = gray + depth;
						int bn = gray - intensity;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden
						rn = Math.min(255, Math.max(0, rn));
						gn = Math.min(255, Math.max(0, gn));
						bn = Math.min(255, Math.max(0, bn));

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}

			// Methode um 6 Farben zu machen.
			if (method.equals("6 Farben")) {
				//6 Farben aus dem Beispielbild in Hexnotation.
				int[] six_colors = {0x665A51,0x222524, 0x9E9086, 0x2E6489, 0xD1CFD0, 0x6996A2};
				
				double maxDist;
				double minDist;

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						//Distanz auf Maximum setzen
						maxDist = Math.sqrt(Math.pow(255,2)*3);

						for (int c : six_colors) {
							int red = (c >> 16) & 0xff;
							int grn = (c >> 8) & 0xff;
							int blu = c  & 0xff;
							
							//Distanz zwischen Pixel und aktueller Farbe berechnen
							minDist = Math.sqrt(Math.pow(red-r, 2) + Math.pow(grn-g, 2) + Math.pow(blu-b, 2));
							/*
							 * Wenn die neue Distanz kleiner ist (unser Ziel!), dann diese als neues Maximum übernehmen
							 * (evtl ist die nächste Farbe kleiner) und schon mal auf den Pixel anwenden, bis eine 
							 * bessere Farbe gefunden wird, die dann die neue Obergrence bildet.
							 */
							if (maxDist > minDist) {
								maxDist = minDist;
								//Sobald die kleinste Distanz gefunden ist, die Farbwerte schreiben.
								pixels[pos] = (0xFF << 24) | (red << 16) | (grn << 8) | blu;
							}
						}
						
						int rn;
						int gn;
						int bn;

						//pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}

		}

	} // CustomWindow inner class
}
