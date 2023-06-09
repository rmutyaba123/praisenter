package org.praisenter.ui.translations;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.Constants;

public final class Translations {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final String BUNDLE_BASE_NAME = "messages";
	private static final Pattern TRANSLATION_PATTERN = Pattern.compile("^messages_(.+)\\.properties$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final ClassLoader RESOURCES_LOADER;
	
	static {
		ClassLoader cl = null;
		try {
			// create a class loader that loads the resource files from the locales directory
			Path path = Paths.get(Constants.LOCALES_ABSOLUTE_PATH);
			cl = new URLClassLoader(new URL[] { path.toUri().toURL() });
		} catch (MalformedURLException e) {
			LOGGER.error("Failed to convert the locales folder '" + Constants.LOCALES_ABSOLUTE_PATH + "' to a URL: " + e.getMessage(), e);
		}
		
		RESOURCES_LOADER = cl;
	}

	/**
	 * Loads the supported locales and any locales in the locales directory.
	 * @return List&lt;Locale&gt;
	 */
	public static final List<Locale> getAvailableLocales() {
		Set<Locale> locales = new HashSet<Locale>();
		
		// default support
		locales.add(Locale.ENGLISH);
		
		// look in the locales directory for any additional translations
		Path localeDir = Paths.get(Constants.LOCALES_ABSOLUTE_PATH);
		if (Files.exists(localeDir) && Files.isDirectory(localeDir)) {
			try (DirectoryStream<Path> paths = Files.newDirectoryStream(localeDir)) {
				Iterator<Path> it = paths.iterator();
				while (it.hasNext()) {
					Path path = it.next();
					try {
						if (Files.isRegularFile(path)) {
							// attempt to read the name
							String fileName = path.getFileName().toString().toLowerCase();
							Matcher m = TRANSLATION_PATTERN.matcher(fileName);
							if (m.matches()) {
								String lang = m.group(1);
								Locale locale = Locale.forLanguageTag(lang);
								locales.add(locale);
							}
						}
					} catch (Exception ex) {
						LOGGER.warn("Failed to check type of path for path '" + path + "'.", ex);
					}
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to iterate translations in '" + localeDir + "'.", e);
			}
		}
		
		List<Locale> list = new ArrayList<Locale>(locales);
		Collections.sort(list, new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Returns the Locale that matches as close to the given locale or returns the default.
	 * @param locale the locale to find
	 * @param locales the set of available locales
	 * @return Locale
	 */
	public static Locale getClosestMatch(Locale locale, List<Locale> locales) {
		// first see if there's an exact match
		for (Locale l : locales) {
			if (l.equals(locale)) {
				return l;
			}
		}
		
		// next see if there's language/country match
		for (Locale l : locales) {
			if (l.getCountry().equals(locale.getCountry()) &&
				l.getLanguage().equals(locale.getLanguage())) {
				return l;
			}
		}
		
		// finally try to find one that just matches the language
		for (Locale l : locales) {
			if (l.getLanguage().equals(locale.getLanguage())) {
				return l;
			}
		}
		
		// if we still haven't found a match then return the default
		return Locale.ENGLISH;
	}
	
	private Translations() {}
	
	/**
	 * Returns the translation for the given key.
	 * <p>
	 * This method will always return a string, either the current locale's translation,
	 * the default translation, or empty string.
	 * @param key the key
	 * @return String
	 */
	public static final String get(String key) {
		// find the key in the current locale
		Locale locale = Locale.getDefault();
		try {
			// this bundle will fallback to the default bundle when the key is not found
			return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, RESOURCES_LOADER).getString(key);
		} catch (MissingResourceException ex) {
			LOGGER.warn("Failed to find key '" + key + "' for locale '" + locale.toString() + "' and in the default translation.");
		}
		
		// last resort just return the key itself
		return key;
	}
	
	public static final String get(String key, Object... params) {
		String message = get(key);
		return MessageFormat.format(message, params);
	}
}
