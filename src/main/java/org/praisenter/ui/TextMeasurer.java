package org.praisenter.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

// JAVABUG (M) 07/01/16 If the text wraps a (whole) word by breaking it up, then the font sizing code won't know what size to look for.  There doesn't seem to be a way to control the word wrapping beyond setting a target width.

public final class TextMeasurer {
	private static final Logger LOGGER = LogManager.getLogger();
	
    private static final Text JAVAFX_TEXT_NODE = new Text();
    private static final int MAXIMUM_ITERATIONS = 100;

    // defaults 

    private static final double DEFAULT_WRAPPING_WIDTH = JAVAFX_TEXT_NODE.getWrappingWidth();
    private static final double DEFAULT_LINE_SPACING = JAVAFX_TEXT_NODE.getLineSpacing();
    private static final String DEFAULT_TEXT = JAVAFX_TEXT_NODE.getText();
    private static final TextBoundsType DEFAULT_BOUNDS_TYPE = JAVAFX_TEXT_NODE.getBoundsType();
	
	private TextMeasurer() {}

    private static final void reset() {
    	JAVAFX_TEXT_NODE.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
        JAVAFX_TEXT_NODE.setLineSpacing(DEFAULT_LINE_SPACING);
        JAVAFX_TEXT_NODE.setText(DEFAULT_TEXT);
        JAVAFX_TEXT_NODE.setBoundsType(DEFAULT_BOUNDS_TYPE);
    }
    
    /**
     * Returns the bounds of a paragraph for the given text, font, target width, line spacing
     * and bounds type. 
     * @param text the text to measure
     * @param font the font
     * @param targetWidth the target wrapping width
     * @param lineSpacing the line spacing
     * @param boundsType the bounds type
     * @return Bounds
     */
    public static final Bounds getParagraphBounds(String text, Font font, double targetWidth, double lineSpacing, TextBoundsType boundsType) {
        // setup the node
    	JAVAFX_TEXT_NODE.setText(text);
        JAVAFX_TEXT_NODE.setFont(font);
        JAVAFX_TEXT_NODE.setWrappingWidth(targetWidth);
        JAVAFX_TEXT_NODE.setLineSpacing(lineSpacing);
        JAVAFX_TEXT_NODE.setBoundsType(boundsType);
        // perform the measurement
        final Bounds bounds = JAVAFX_TEXT_NODE.getLayoutBounds();
        // reset the node
        reset();
        return bounds;
    }
    
    /**
     * Returns a new font that allows the text to fit within the given bounds, increasing up
     * to the given maxFontSize and decreasing to fit if needed.
     * @param text the text to measure
     * @param font the font
     * @param maxFontSize the maximum font size
     * @param targetWidth the target wrapping width
     * @param targetHeight the target height
     * @param lineSpacing the line spacing
     * @param boundsType the bounds type
     * @return Bounds
     */
    public static final Font getFittingFontForParagraph(String text, Font font, double maxFontSize, double targetWidth, double targetHeight, double lineSpacing, TextBoundsType boundsType) {
    	Bounds bounds = TextMeasurer.getParagraphBounds(text, font, targetWidth, lineSpacing, boundsType);
		double max = maxFontSize;
		double min = (bounds.getHeight() <= targetHeight && max != Double.MAX_VALUE) ? max : 1.0;
		double cur = font.getSize();
		
		if (cur < 1.0) {
			cur = 1.0;
		}
		
		Font nf = font;
		int i = 0;
		while (bounds.getHeight() > targetHeight || Math.abs(max - min) > 0.1) {
			// check the paragraph height against the maximum height
			if (bounds.getHeight() < targetHeight) {
				// we need to binary search up
				min = cur;
				// compute an estimated next size if the maximum begins with Float.MAX_VALUE
				// this is to help convergence to a safe maximum
				double rmax = (max == Double.MAX_VALUE ? targetHeight * (cur / bounds.getHeight()) : max);
				cur = (cur + rmax) * 0.5;
				nf = new Font(font.getName(), cur);
			} else {
				// we need to binary search down
				max = cur;
				// get the next test font size
				double temp = (min + cur) * 0.5;
				// do a check for minimum font size
				if (temp <= 1.0f) break;
				// its not the minimum so continue
				cur = temp;
				nf = new Font(font.getName(), cur);
			}
			// get the new paragraph height for the new font size
			bounds = TextMeasurer.getParagraphBounds(text, nf, targetWidth, lineSpacing, boundsType);
			// don't run forever
			if (i >= MAXIMUM_ITERATIONS) {
				LOGGER.warn("Hit maximum number of iterations before determining optimal font size. Current: {} Minimum: {} Maximum: {} Target Width: {} Target Height: {} Maximum Size: {} Text: {}",
						cur, min, max, targetWidth, targetHeight, maxFontSize, text);
				break;
			}
			i++;
		}
		if (i > 0) {
			LOGGER.trace("Font fitting iterations: " + i);
		}
		// the Math.min(min, cur) ensures we choose the lower bound
		// the - 1.0 is further insurance that its small enough
		// the Math.min(1.0, x) ensures the minimum font we return is 1
		double size = Math.max(1.0, Math.min(min, cur) - 1.0);
		return new Font(font.getName(), size);
    }
    
    /**
     * Returns the bounds of a line for the given text and font.
     * @param text the text to measure
     * @param font the font
     * @param boundsType the bounds type
     * @return Bounds
     */
    public static final Bounds getLineBounds(String text, Font font, TextBoundsType boundsType) {
    	// setup the node
    	JAVAFX_TEXT_NODE.setText(text);
        JAVAFX_TEXT_NODE.setFont(font);
        JAVAFX_TEXT_NODE.setWrappingWidth(0);
        JAVAFX_TEXT_NODE.setLineSpacing(0);
        JAVAFX_TEXT_NODE.setBoundsType(boundsType);
        // perform the measurement
        final Bounds bounds = JAVAFX_TEXT_NODE.getLayoutBounds();
        // reset the node
        reset();
        return bounds;
    }
    
    /**
     * Returns a new font that allows the text to fit within the given width, increasing up
     * to the given maxFontSize and decreasing to fit if needed.
     * @param text the text to measure
     * @param font the font
     * @param maxFontSize the maximum font size
     * @param targetWidth the target width
     * @param boundsType the bounds type
     * @return Bounds
     */
    public static final Font getFittingFontForLine(String text, Font font, double maxFontSize, double targetWidth, TextBoundsType boundsType) {
    	Bounds bounds = TextMeasurer.getLineBounds(text, font, boundsType);
		double max = maxFontSize;
		double min = (bounds.getWidth() < targetWidth && max != Double.MAX_VALUE) ? max : 1.0;
		double cur = font.getSize();
		
		if (cur < 1.0) {
			cur = 1.0;
		}
		
		Font nf = font;
		int i = 0;
		while (bounds.getWidth() > targetWidth || Math.abs(max - min) > 0.1) {
			// check the paragraph height against the maximum height
			if (bounds.getWidth() < targetWidth) {
				// we need to binary search up
				min = cur;
				// compute an estimated next size if the maximum begins with Float.MAX_VALUE
				// this is to help convergence to a safe maximum
				double rmax = (max == Double.MAX_VALUE ? targetWidth * (cur / bounds.getWidth()) : max);
				cur = (cur + rmax) * 0.5;
				nf = new Font(font.getName(), cur);
			} else {
				// we need to binary search down
				max = cur;
				// get the next test font size
				double temp = (min + cur) * 0.5;
				// do a check for minimum font size
				if (temp <= 1.0f) break;
				// its not the minimum so continue
				cur = temp;
				nf = new Font(font.getName(), cur);
			}
			// get the new paragraph height for the new font size
			bounds = TextMeasurer.getLineBounds(text, nf, boundsType);
			// don't run forever
			if (i >= MAXIMUM_ITERATIONS) {
				LOGGER.warn("Hit maximum number of iterations before determining optimal font size. Current: {} Minimum: {} Maximum: {} Target Width: {} Maximum Size: {} Text: {}",
						cur, min, max, targetWidth, maxFontSize, text);
				break;
			}
			i++;
		}
		if (i > 0) {
			LOGGER.trace("Font fitting iterations: " + i);
		}
		// the Math.min(min, cur) ensures we choose the lower bound
		// the - 1.0 is further insurance that its small enough
		// the Math.max(1.0, x) ensures the minimum font we return is 1
		double size = Math.max(1.0, Math.min(min, cur) - 1.0);
		return new Font(font.getName(), size);
    }
    
    /**
     * Returns a new font that allows the text to fit within the given width, increasing up
     * to the given maxFontSize and decreasing to fit if needed.
     * @param text the text to measure
     * @param font the font
     * @param maxFontSize the maximum font size
     * @param targetWidth the target width
     * @param boundsType the bounds type
     * @return Bounds
     */
    public static final Font getFittingFontForLine(String text, Font font, double maxFontSize, double targetWidth, double targetHeight, TextBoundsType boundsType) {
    	Bounds bounds = TextMeasurer.getLineBounds(text, font, boundsType);
		double max = maxFontSize;
		double min = (bounds.getWidth() < targetWidth && bounds.getHeight() < targetHeight && max != Double.MAX_VALUE) ? max : 1.0;
		double cur = font.getSize();
		
		if (cur < 1.0) {
			cur = 1.0;
		}
		
		Font nf = font;
		int i = 0;
		while (bounds.getWidth() > targetWidth || bounds.getHeight() > targetHeight || Math.abs(max - min) > 0.1) {
			// check the paragraph height against the maximum height
			if (bounds.getWidth() < targetWidth && bounds.getHeight() < targetHeight) {
				// we need to binary search up
				min = cur;
				// compute an estimated next size if the maximum begins with Float.MAX_VALUE
				// this is to help convergence to a safe maximum
				double rmax = (max == Double.MAX_VALUE ? targetWidth * (cur / bounds.getWidth()) : max);
				cur = (cur + rmax) * 0.5;
				nf = new Font(font.getName(), cur);
			} else {
				// we need to binary search down
				max = cur;
				// get the next test font size
				double temp = (min + cur) * 0.5;
				// do a check for minimum font size
				if (temp <= 1.0f) break;
				// its not the minimum so continue
				cur = temp;
				nf = new Font(font.getName(), cur);
			}
			// get the new paragraph height for the new font size
			bounds = TextMeasurer.getLineBounds(text, nf, boundsType);
			// don't run forever
			if (i >= MAXIMUM_ITERATIONS) {
				LOGGER.warn("Hit maximum number of iterations before determining optimal font size. Current: {} Minimum: {} Maximum: {} Target Width: {} Maximum Size: {} Text: {}",
						cur, min, max, targetWidth, maxFontSize, text);
				break;
			}
			i++;
		}
		if (i > 0) {
			LOGGER.trace("Font fitting iterations: " + i);
		}
		// the Math.min(min, cur) ensures we choose the lower bound
		// the - 1.0 is further insurance that its small enough
		// the Math.max(1.0, x) ensures the minimum font we return is 1
		double size = Math.max(1.0, Math.min(min, cur) - 1.0);
		return new Font(font.getName(), size);
    }
}

