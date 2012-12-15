package org.praisenter.slide.graphics;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a fill using a solid color.
 * @author William Bittle
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlRootElement(name = "ColorFill")
@XmlAccessorType(XmlAccessType.NONE)
public class ColorFill extends AbstractFill implements Fill {
	/** The red component */
	@XmlAttribute(name = "Red")
	protected float red;
	
	/** The green component */
	@XmlAttribute(name = "Green")
	protected float green;
	
	/** The blue component */
	@XmlAttribute(name = "Blue")
	protected float blue;
	
	/** The alpha component */
	@XmlAttribute(name = "Alpha")
	protected float alpha;
	
	/**
	 * Default constructor.
	 */
	public ColorFill() {
		this(Color.BLACK);
	}
	
	/**
	 * Full constructor.
	 * @param red the red component from 0-255
	 * @param green the green component from 0-255
	 * @param blue the blue component from 0-255
	 * @param alpha the alpha component from 0-255
	 */
	public ColorFill(int red, int green, int blue, int alpha) {
		this(new Color(red, green, blue, alpha));
	}
	
	/**
	 * Uses the given color for this {@link ColorFill}.
	 * @param color the color
	 */
	public ColorFill(Color color) {
		float[] components = color.getComponents(null);
		this.red = components[0];
		this.green = components[1];
		this.blue = components[2];
		this.alpha = components[3];
	}

	/**
	 * Full constructor.
	 * @param red the red component from 0-1
	 * @param green the green component from 0-1
	 * @param blue the blue component from 0-1
	 * @param alpha the alpha component from 0-1
	 */
	public ColorFill(float red, float green, float blue, float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	/**
	 * Returns the color for this {@link ColorFill}.
	 * @return Color
	 */
	public Color getColor() {
		return new Color(this.red, this.green, this.blue, this.alpha);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Fill#getPaint(int, int, int, int)
	 */
	@Override
	public Color getPaint(int x, int y, int w, int h) {
		// this fill doesn't need the params
		return new Color(this.red, this.green, this.blue, this.alpha);
	}
	
	/**
	 * Returns the red component.
	 * @return float
	 */
	public float getRed() {
		return this.red;
	}

	/**
	 * Returns the green component.
	 * @return float
	 */
	public float getGreen() {
		return this.green;
	}

	/**
	 * Returns the blue component.
	 * @return float
	 */
	public float getBlue() {
		return this.blue;
	}

	/**
	 * Returns the alpha component.
	 * @return float
	 */
	public float getAlpha() {
		return this.alpha;
	}
}