package com.github.jbrasileiro.jwms.ui;

/** Secções da barra lateral (General, Summary, …). */
public enum NavigationScreen {
    GENERAL("nav.general", "\u2699"),
    SUMMARY("nav.summary", "\uD83D\uDCCB"),
    CHARACTERS("nav.characters", "\uD83D\uDC64"),
    PLOTS("nav.plots", "\uD83D\uDCC8"),
    WORLD("nav.world", "\uD83C\uDF10"),
    OUTLINE("nav.outline", "\u2630"),
    EDITOR("nav.editor", "\u270E");

    private final String messageKey;
    private final String iconGlyph;

    NavigationScreen(String messageKey, String iconGlyph) {
        this.messageKey = messageKey;
        this.iconGlyph = iconGlyph;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getIconGlyph() {
        return iconGlyph;
    }
}
