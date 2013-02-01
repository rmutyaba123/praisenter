/*
 * Copyright (c) 2011-2013 William Bittle  http://www.praisenter.org/
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
package org.praisenter.slide.ui.present;

import org.praisenter.slide.Slide;
import org.praisenter.transitions.TransitionAnimator;

/**
 * Represents a send presentation event.
 * @author William Bittle
 * @version 2.0.0
 * @since 2.0.0
 */
public class SendEvent implements PresentationEvent {
	/** The slide */
	protected Slide slide;
	
	/** The animator */
	protected TransitionAnimator animator;
	
	/**
	 * Full constructor.
	 * @param slide the slide; this will be copied
	 * @param animator the animator
	 */
	public SendEvent(Slide slide, TransitionAnimator animator) {
		// always use a copy of the slide since it could be reused by 
		// the rest of the application, this shouldn't be a problem anyway
		// since the copy is really fast (mostly immutable objects)
		this.slide = slide.copy();
		this.animator = animator;
	}
	
	/**
	 * Returns the slide.
	 * @return {@link Slide}
	 */
	public Slide getSlide() {
		return this.slide;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.ui.present.PresentationEvent#getAnimator()
	 */
	public TransitionAnimator getAnimator() {
		return this.animator;
	}
}