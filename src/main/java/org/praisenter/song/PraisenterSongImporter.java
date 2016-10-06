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
package org.praisenter.song;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.JAXBException;

import org.praisenter.xml.XmlIO;

// TODO create a formatidentifyingimporter
// FEATURE allow reading of a zip file with multiple songs in it

/**
 * Importer for the Praisenter 3.0.0 song format.
 * @author William Bittle
 * @version 3.0.0
 */
public final class PraisenterSongImporter implements SongImporter {
	/* (non-Javadoc)
	 * @see org.praisenter.song.SongImporter#read(java.nio.file.Path)
	 */
	@Override
	public List<Song> read(Path path) throws IOException, SongImportException {
		List<Song> songs = new ArrayList<Song>();
		// only open files
		if (Files.isRegularFile(path)) {
			// only open xml files
			FileTypeMap map = MimetypesFileTypeMap.getDefaultFileTypeMap();
			String mimeType = map.getContentType(path.toString());
			if (mimeType.equals("application/xml")) {
				try (InputStream is = Files.newInputStream(path)) {
					// read in the xml
					try {
						Song song = XmlIO.read(is, Song.class);
						songs.add(song);
					} catch (JAXBException e) {
						throw new SongImportException(e);
					}
				}
			}
		}
		return songs;
	}
}
