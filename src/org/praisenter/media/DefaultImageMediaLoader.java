package org.praisenter.media;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.praisenter.resources.Messages;

/**
 * Default implementation for loading image media.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class DefaultImageMediaLoader implements ImageMediaLoader, MediaLoader<ImageMedia> {
	/* (non-Javadoc)
	 * @see org.praisenter.media.MediaLoader#isSupported(java.lang.String)
	 */
	@Override
	public boolean isSupported(String mimeType) {
		String[] supportedTypes = ImageIO.getReaderMIMETypes();
		for (String type : supportedTypes) {
			if (type.equalsIgnoreCase(mimeType)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.media.MediaLoader#getMediaType()
	 */
	@Override
	public Class<ImageMedia> getMediaType() {
		return ImageMedia.class;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.media.MediaLoader#load(java.lang.String)
	 */
	@Override
	public ImageMedia load(String filePath) throws MediaException {
		Path path = FileSystems.getDefault().getPath(filePath);
		if (Files.exists(path) && Files.isRegularFile(path)) {
			ImageInputStream in = null;
			try {
				in = ImageIO.createImageInputStream(path.toFile());
				Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
				// loop through the readers until we find one that works
				while (readers.hasNext()) {
					ImageReader reader = readers.next();
					reader.setInput(in);
					ImageMediaFile file = new ImageMediaFile(filePath, reader.getFormatName(), reader.getWidth(0), reader.getHeight(0));
					try {
						return new ImageMedia(file, reader.read(0));
					} finally {
						reader.dispose();
					}
				}
				// no readers
				throw new MediaException(MessageFormat.format(Messages.getString("media.loader.ex.noImageReader"), filePath));
			} catch (IOException e) {
				throw new MediaException(MessageFormat.format(Messages.getString("media.loader.ex.io"), filePath));
			} finally {
				if (in != null) {
					try {
						in.close();
						// just eat the exception if we get one here
					} catch (IOException e) {}
				}
			}
		} else {
			throw new MediaException(MessageFormat.format(Messages.getString("media.loader.ex.notFound"), filePath));
		}
	}
}
