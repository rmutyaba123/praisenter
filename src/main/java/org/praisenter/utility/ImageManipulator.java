package org.praisenter.utility;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import com.twelvemonkeys.image.ResampleOp;

public final class ImageManipulator {
	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private ImageManipulator() {}
	
	/**
	 * Returns the "brightness" of the given image between 0.0 and 1.0.
	 * @param image the image
	 * @return double
	 * @see <a href="http://stackoverflow.com/questions/21205871/java-bufferedimage-get-single-pixel-brightness">Log Average Luminance</a>
	 */
	public static double getLogAverageLuminance(BufferedImage image) {
		long pixels = image.getWidth() * image.getHeight();
		float total = 0;
		//http://stackoverflow.com/questions/21205871/java-bufferedimage-get-single-pixel-brightness
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int color = image.getRGB(j, i);
		
				// extract each color component
				int red   = (color >>> 16) & 0xFF;
				int green = (color >>>  8) & 0xFF;
				int blue  = (color >>>  0) & 0xFF;
		
				// calc luminance in range 0.0 to 1.0; using SRGB luminance constants
				float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
		
				total += luminance;
			}
		}
		
		// average luminance
		return total / pixels;
	}
	
	/**
	 * Changes the orientation of the given image to the orientation specified in the JPEG EXIF data
	 * field if present. 
	 * @param image the image
	 * @param orientation the orientation
	 * @return BufferedImage
	 */
	public static final BufferedImage correctExifOrientation(BufferedImage image, int orientation) {
		// unknown or normal(1) orientation
		if (orientation == -1 || orientation == 1) return image;
		
		AffineTransform at = null;
		
		// 2 = flipped about the y-axis
		if (orientation == 2) {
			at = AffineTransform.getTranslateInstance(image.getWidth(), 0);
			at.scale(-1, 1);
			
		// 3 = flipped about both the y-axis and x-axis
		} else if (orientation == 3) {
			at = AffineTransform.getTranslateInstance(image.getWidth(), image.getHeight());
			at.scale(-1, -1);
		
		// 4 = flipped about the x-axis
		} else if (orientation == 4) {
			at = AffineTransform.getTranslateInstance(0, image.getHeight());
			at.scale(1, -1);
		
		// 5 = flipped about the y-axis and rotated counter clockwise 90 degrees
		} else if (orientation == 5) {
			at = AffineTransform.getTranslateInstance(image.getHeight() / 2.0, image.getWidth() / 2.0);
			at.rotate(Math.toRadians(90));
			at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
			at.translate(0, image.getHeight());
			at.scale(1, -1);
			
		// 6 = rotated counter clockwise 90 degrees
		} else if (orientation == 6) {
			at = AffineTransform.getTranslateInstance(image.getHeight() / 2.0, image.getWidth() / 2.0);
			at.rotate(Math.toRadians(90));
			at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		
		// 7 = flipped about the y-axis and rotated clockwise 90 degrees
		} else if (orientation == 7) {
			at = AffineTransform.getTranslateInstance(image.getHeight() / 2.0, image.getWidth() / 2.0);
			at.rotate(Math.toRadians(-90));
			at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
			at.translate(0, image.getHeight());
			at.scale(1, -1);
		
		// 8 = rotated clockwise 90 degrees
		} else if (orientation == 8) {
			at = AffineTransform.getTranslateInstance(image.getHeight() / 2.0, image.getWidth() / 2.0);
			at.rotate(Math.toRadians(-90));
			at.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		} else {
			return image;
		}
		
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		return op.filter(image, null);
	}

	/**
	 * Returns a non-uniformly scaled image of the given image.
	 * @param image the image to scale
	 * @param tw the target width
	 * @param th the target height
	 * @param quality the quality as defined by AffineTransformOp
	 * @return BufferedImage
	 */
	public static final BufferedImage getNonUniformScaledImage(BufferedImage image, int tw, int th, int quality) {
	    // attempt to resize it
		BufferedImageOp op = new ResampleOp(tw, th, quality);
	    return op.filter(image, null);
	}
	
	/**
	 * Returns a uniformly scaled image of the given image.
	 * @param image the image to scale
	 * @param tw the target width
	 * @param th the target height
	 * @param quality the quality as defined by AffineTransformOp
	 * @param scaleDownOnly true if the image should only be scaled down (and not scaled up)
	 * @return BufferedImage
	 */
	public static final BufferedImage getUniformScaledImage(BufferedImage image, int tw, int th, int quality, boolean scaleDownOnly) {
		// get the width/height
        int iw = image.getWidth();
        int ih = image.getHeight();
        
	    // get the scaling factors
	    double pw = (double)tw / (double)iw;
		double ph = (double)th / (double)ih;
		
		// use uniform scaling
		double s = 1.0;
		if (pw < ph) {
			// the width scaling is more dramatic so use it
			s = pw;
		} else {
			s = ph;
		}
		
		if (!scaleDownOnly || s < 1.0) {
		    // attempt to resize it
			BufferedImageOp op = new ResampleOp((int)Math.floor(s * iw), (int)Math.floor(s * ih), quality);
		    return op.filter(image, null);
		}
		
		return image;
	}

	/**
	 * Clears the target image and renders the source image to the target image.
	 * <p>
	 * This method uses a 100% transparent color to clear the target image.
	 * @param source the source image
	 * @param target the target image
	 */
	public static final BufferedImage convertToJpgCompatibleImage(BufferedImage source, Color backgroundColor) {
		if (source.getType() == BufferedImage.TYPE_INT_RGB) return source;
		BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
		copyImage(source, target, backgroundColor);
		return target;
	}
	
	/**
	 * Clears the target image and renders the source image to the target image.
	 * <p>
	 * This method uses a 100% transparent color to clear the target image.
	 * @param source the source image
	 * @param target the target image
	 */
	public static final void copyImage(BufferedImage source, BufferedImage target) {
		copyImage(source, target, TRANSPARENT);
	}
	
	/**
	 * Clears the target image and renders the source image to the target image.
	 * @param source the source image
	 * @param target the target image
	 * @param clearColor the clear color
	 */
	public static final void copyImage(BufferedImage source, BufferedImage target, Color clearColor) {
		Graphics2D tg2d = target.createGraphics();
		// clear the background
		tg2d.setBackground(clearColor);
		tg2d.clearRect(0, 0, target.getWidth(), target.getHeight());
		// for compatibility for offscreen images we need to do this
		tg2d.setColor(clearColor);
		tg2d.fillRect(0, 0, target.getWidth(), target.getHeight());
		// finally copy the image
		tg2d.drawImage(source, 0, 0, null);
		tg2d.dispose();
	}
}
