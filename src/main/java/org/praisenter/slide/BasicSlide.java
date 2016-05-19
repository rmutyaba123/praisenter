/*
 * Copyright (c) 2015-2016 William Bittle  http://www.praisenter.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of Praisenter nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.praisenter.slide;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.praisenter.ReadonlyIterator;
import org.praisenter.Tag;
import org.praisenter.slide.animation.SlideAnimation;
import org.praisenter.slide.text.BasicTextComponent;
import org.praisenter.slide.text.DateTimeComponent;
import org.praisenter.slide.text.TextPlaceholderComponent;

/**
 * Implementation of the {@link Slide} interface.
 * @author William Bittle
 * @version 3.0.0
 */
@XmlRootElement(name = "slide")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({
	SongSlide.class,
	BibleSlide.class
})
public class BasicSlide extends AbstractSlideRegion implements Slide, SlideRegion {
	/** The slide format version */
	@XmlAttribute(name = "version", required = false)
	final String version = Slide.VERSION;
	
	/** The slide components */
	@XmlElementRefs({
		@XmlElementRef(type = MediaComponent.class),
		@XmlElementRef(type = BasicTextComponent.class),
		@XmlElementRef(type = DateTimeComponent.class),
		@XmlElementRef(type = TextPlaceholderComponent.class)
	})
	@XmlElementWrapper(name = "components", required = false)
	final List<SlideComponent> components;

	/** The slide animations */
	@XmlElement(name = "animation", required = false)
	@XmlElementWrapper(name = "animations", required = false)
	final List<SlideAnimation> animations;
	
	// for internal use really
	/** The slide path; can be null */
	Path path;
	
	/** The slide name */
	@XmlElement(name = "name")
	String name;
	
	// other
	
	/** The time the slide will show in milliseconds */
	@XmlElement(name = "time", required = false)
	long time;
	
	/** The tags */
	@XmlElement(name = "tag", required = false)
	@XmlElementWrapper(name = "tags", required = false)
	final Set<Tag> tags;
	
	/**
	 * Default constructor.
	 */
	public BasicSlide() {
		// JAXB should overwrite the id
		this(UUID.randomUUID());
	}
	
	/**
	 * Internal constructor for setting an explicit id.
	 * @param id the id
	 */
	BasicSlide(UUID id) {
		super(id);
		this.components = new ArrayList<SlideComponent>();
		this.animations = new ArrayList<SlideAnimation>();
		this.time = Slide.TIME_FOREVER;
		this.tags = new TreeSet<Tag>();
	}
	
	/**
	 * Copies over the values of this slide to the given slide.
	 * @param to the slide to copy to
	 */
	protected void copy(Slide to) {
		// copy over the super class stuff
		this.copy((SlideRegion)to);
		// copy the components
		for (int i = 0; i < this.components.size(); i++) {
			SlideComponent sc = this.components.get(i);
			SlideComponent copy = sc.copy();
			to.addComponent(copy);
			
			// component animations
			List<SlideAnimation> animations = this.getAnimations(sc.getId());
			for(SlideAnimation animation : animations) {
				to.getAnimations().add(animation.copy(copy.getId()));
			}
		}
		
		// slide animations
		List<SlideAnimation> animations = this.getAnimations(this.getId());
		for(SlideAnimation animation : animations) {
			to.getAnimations().add(animation.copy(to.getId()));
		}
		
		// copy other props
		to.setTime(this.time);
		to.setName(this.name);
		to.getTags().addAll(this.tags);
		
		// NOTE: path is NOT copied
		// NOTE: this method should copy everything since it
		// will be used by subclasses
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.SlideRegion#copy()
	 */
	@Override
	public BasicSlide copy() {
		// NOTE: this generates a new id
		BasicSlide slide = new BasicSlide();
		// copy over the stuff
		this.copy(slide);
		return slide;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getPath()
	 */
	@Override
	public Path getPath() {
		return this.path;
	}

	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#setPath(java.nio.file.Path)
	 */
	@Override
	public void setPath(Path path) {
		this.path = path;
	}

	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getVersion()
	 */
	@Override
	public String getVersion() {
		return this.version;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getTime()
	 */
	@Override
	public long getTime() {
		return time;
	}

	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#setTime(long)
	 */
	@Override
	public void setTime(long time) {
		this.time = time;
	}

	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#addComponent(org.praisenter.slide.SlideComponent)
	 */
	@Override
	public void addComponent(SlideComponent component) {
		int order = this.getNextIndex();
		((SlideComponent)component).setOrder(order);
		this.components.add(component);
		this.sortComponentsByOrder(this.components);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#removeComponent(org.praisenter.slide.SlideComponent)
	 */
	@Override
	public boolean removeComponent(SlideComponent component) {
		// no re-sort required here
		if (this.components.remove(component)) {
			this.animations.removeIf(st -> st.getId().equals(component.getId()));
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getComponentIterator()
	 */
	@Override
	public Iterator<SlideComponent> getComponentIterator() {
		return new ReadonlyIterator<SlideComponent>(this.components.iterator());
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getComponents(java.lang.Class)
	 */
	@Override
	public <E extends SlideComponent> List<E> getComponents(Class<E> clazz) {
		List<E> components = new ArrayList<E>();
		for (SlideComponent component : this.components) {
			if (clazz.isInstance(component)) {
				components.add(clazz.cast(component));
			}
		}
		return components;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#moveComponentUp(org.praisenter.slide.SlideComponent)
	 */
	@Override
	public void moveComponentUp(SlideComponent component) {
		// move the given component up in the order
		
		// get all the components
		List<SlideComponent> components = this.components;
		// verify the component exists on this slide
		if (components.contains(component) && components.size() > 0) {
			int size = components.size();
			// see if the component is already in the last position
			if (components.get(size - 1).equals(component)) {
				// if it is, then just return its order
				return;
			} else {
				// if its not in the last position then we need to 
				// move it up and change the subsequent component (move it back by one)
				int order = component.getOrder();
				for (SlideComponent cmp : components) {
					// see if the current component order is greater
					// than this component's order
					if (cmp.getOrder() == order + 1) {
						// we only need to move back the next component
						cmp.setOrder(cmp.getOrder() - 1);
						break;
					}
				}
				// move the given component up
				component.setOrder(order + 1);
				// resort the components
				this.sortComponentsByOrder(this.components);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#moveComponentDown(org.praisenter.slide.SlideComponent)
	 */
	@Override
	public void moveComponentDown(SlideComponent component) {
		// move the given component down in the order
		
		// get all the components
		List<SlideComponent> components = this.components;
		// verify the component exists on this slide
		if (components.contains(component) && components.size() > 0) {
			// see if the component is already in the first position
			if (components.get(0).equals(component)) {
				// if it is, then just return its order
				return;
			} else {
				// if its not in the first position then we need to 
				// move it down and change the previous component (move it up by one)
				int order = component.getOrder();
				for (SlideComponent cmp : components) {
					// find the previous component
					if (cmp.getOrder() == order - 1) {
						// we only need to move up the previous component
						cmp.setOrder(cmp.getOrder() + 1);
						break;
					}
				}
				// move the given component up
				component.setOrder(order - 1);
				// resort the components
				this.sortComponentsByOrder(this.components);
			}
		}
	}
	
	/**
	 * Sorts the given components using their z-ordering.
	 * @param components the list of components to sort
	 */
	private <E extends SlideComponent> void sortComponentsByOrder(List<E> components) {
		Collections.sort(components);
	}
	
	/**
	 * Returns the next order index in the list of components.
	 * @return int
	 */
	private int getNextIndex() {
		List<SlideComponent> components = this.components;
		if (components.size() > 0) {
			int maximum = 1;
			for (SlideComponent component : components) {
				if (maximum < component.getOrder()) {
					maximum = component.getOrder();
				}
			}
			return maximum + 1;
		} else {
			return 1;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#hasPlaceholders()
	 */
	@Override
	public boolean hasPlaceholders() {
		for (SlideComponent component : this.components) {
			if (TextPlaceholderComponent.class.isInstance(component)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#fit(int, int)
	 */
	@Override
	public void fit(int width, int height) {
		// compute the resize percentages
		double pw = (double)width / (double)this.width;
		double ph = (double)height / (double)this.height;
		// set the slide size
		this.width = width;
		this.height = height;
		// set the sizes for the components
		for (SlideComponent component : this.components) {
			component.adjust(pw, ph);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getAnimations()
	 */
	@Override
	public List<SlideAnimation> getAnimations() {
		return this.animations;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getAnimations(java.util.UUID)
	 */
	@Override
	public List<SlideAnimation> getAnimations(UUID id) {
		List<SlideAnimation> animations = new ArrayList<SlideAnimation>();
		for (int i = 0; i < this.animations.size(); i++) {
			SlideAnimation st = this.animations.get(i);
			if (st.getId().equals(id)) {
				animations.add(st);
			}
		}
		return animations;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getTags()
	 */
	@Override
	public Set<Tag> getTags() {
		return this.tags;
	}
}
