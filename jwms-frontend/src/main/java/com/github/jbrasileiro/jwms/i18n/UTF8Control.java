package com.github.jbrasileiro.jwms.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class UTF8Control extends ResourceBundle.Control {

	public static final UTF8Control INSTANCE = new UTF8Control();

	private UTF8Control() {
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		if (!"java.properties".equals(format)) {
			return null;
		}
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		try (InputStream stream = loader.getResourceAsStream(resourceName)) {
			if (stream == null) {
				return null;
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				return new PropertyResourceBundle(reader);
			}
		}
	}
}
