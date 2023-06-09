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
package org.praisenter.data.slide.text;

import java.util.UUID;

import org.praisenter.Watchable;
import org.praisenter.data.Copyable;
import org.praisenter.data.Identifiable;
import org.praisenter.data.TextType;
import org.praisenter.data.TextVariant;
import org.praisenter.data.slide.ReadOnlySlideComponent;
import org.praisenter.data.slide.ReadOnlySlideRegion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Represents a text component whose text will be supplied from an external source.
 * <p>
 * The external source can place text into the component based on the given types applied
 * to the placeholder.
 * @author William Bittle
 * @version 3.0.0
 */
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName(value = "textPlaceholderComponent")
public final class TextPlaceholderComponent extends TextComponent implements ReadOnlyTextPlaceholderComponent, ReadOnlyTextComponent, ReadOnlySlideComponent, ReadOnlySlideRegion, Copyable, Identifiable {
	private final ObjectProperty<TextType> placeholderType;
	private final ObjectProperty<TextVariant> placeholderVariant;
	private final BooleanProperty textLocked;

	public TextPlaceholderComponent() {
		this.placeholderType = new SimpleObjectProperty<>(TextType.TEXT);
		this.placeholderVariant = new SimpleObjectProperty<>(TextVariant.PRIMARY);
		this.textLocked = new SimpleBooleanProperty(false);
	}
	
	@Override
	public TextPlaceholderComponent copy() {
		TextPlaceholderComponent tc = new TextPlaceholderComponent();
		super.copyTo(tc);
		tc.placeholderType.set(this.placeholderType.get());
		tc.placeholderVariant.set(this.placeholderVariant.get());
		// NOTE: text locking is not copied
		return tc;
	}
	
	@Override
	public TextComponent toTextComponent() {
		TextComponent tc = new TextComponent();
		this.copyTo(tc);
		tc.setId(UUID.randomUUID());
		tc.setText(this.getText());
		return tc;
	}
	
	@Override
	@JsonProperty
	public TextType getPlaceholderType() {
		return this.placeholderType.get();
	}
	
	@JsonProperty
	public void setPlaceholderType(TextType type) {
		this.placeholderType.set(type);
	}
	
	@Override
	@Watchable(name = "placeholderType")
	public ObjectProperty<TextType> placeholderTypeProperty() {
		return this.placeholderType;
	}
	
	@Override
	@JsonProperty
	public TextVariant getPlaceholderVariant() {
		return this.placeholderVariant.get();
	}
	
	@JsonProperty
	public void setPlaceholderVariant(TextVariant variant) {
		this.placeholderVariant.set(variant);
	}
	
	@Override
	@Watchable(name = "placeholderVariant")
	public ObjectProperty<TextVariant> placeholderVariantProperty() {
		return this.placeholderVariant;
	}
	
	// for operation/display only - no need to save
	
	@Override
	public boolean isTextLocked() {
		return this.textLocked.get();
	}
	
	public void setTextLocked(boolean textLocked) {
		this.textLocked.set(textLocked);
	}
	
	@Override
	public BooleanProperty textLockedProperty() {
		return this.textLocked;
	}
}
