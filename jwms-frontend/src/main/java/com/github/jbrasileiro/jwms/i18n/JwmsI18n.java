package com.github.jbrasileiro.jwms.i18n;

import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public final class JwmsI18n {

	public static final String BUNDLE_BASE = "com.github.jbrasileiro.jwms.i18n.messages";

	private JwmsI18n() {
	}

	public static Locale resolveLocale() {
		String sys = System.getProperty("jwms.locale");
		if (sys != null && !sys.isBlank()) {
			return Locale.forLanguageTag(sys.trim().replace('_', '-'));
		}
		Optional<String> stored = JwmsPreferences.getLocaleTag();
		if (stored.isPresent()) {
			return Locale.forLanguageTag(stored.get());
		}
		return Locale.getDefault();
	}

	public static ResourceBundle bundle() {
		return ResourceBundle.getBundle(BUNDLE_BASE, resolveLocale(), UTF8Control.INSTANCE);
	}
}
