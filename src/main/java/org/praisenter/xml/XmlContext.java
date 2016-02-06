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
package org.praisenter.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

/**
 * Represents a XML context, marshaller and unmarshaller for a given class.
 * @author William Bittle
 * @version 3.0.0
 */
final class XmlContext {
	/** The JAXB context */
	final JAXBContext context;
	
	/** The marshaller */
	final Marshaller marshaller;
	
	/** The unmarshaller */
	final Unmarshaller unmarshaller;
	
	/** Hidden constructor */
	private XmlContext(
			JAXBContext context,
			Marshaller marshaller,
			Unmarshaller unmarshaller) {
		this.context = context;
		this.marshaller = marshaller;
		this.unmarshaller = unmarshaller;
	}

	/**
	 * Creates a new {@link XmlContext} object for the given class.
	 * @param clazz the class
	 * @return {@link XmlContext}
	 * @throws PropertyException thrown if an exception occurs while assigning a marshaller property
	 * @throws JAXBException thrown if an exception occurs while trying to build the {@link XmlContext}
	 */
	static final XmlContext create(Class<?> clazz) throws PropertyException, JAXBException {
		// create the JAXB context
		JAXBContext context = JAXBContext.newInstance(clazz);
		
		// create the unmarshaller
		Unmarshaller unmarshaller = context.createUnmarshaller();
		
		// create the marshaller
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		
		return new XmlContext(context, marshaller, unmarshaller);
	}
}
