package com.github.jbrasileiro.jwms.i18n;

import lombok.Getter;

@Getter
public enum Enumi18n {
	APP_TILE("app.title");

	private String key;

	Enumi18n(String key) {
		this.key = key;
	}
	

}
