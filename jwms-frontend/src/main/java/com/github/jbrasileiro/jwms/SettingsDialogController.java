package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public final class SettingsDialogController {

    public record LocaleOption(String languageTag, String displayName) {
        @Override
        public String toString() {
            return displayName;
        }
    }

    @FXML private ComboBox<LocaleOption> localeCombo;

    @FXML
    private void initialize() {
        ResourceBundle b = JwmsI18n.bundle();
        var items =
                FXCollections.observableArrayList(
                        new LocaleOption("", b.getString("settings.locale.system")),
                        new LocaleOption("en", b.getString("settings.locale.en")),
                        new LocaleOption("pt-PT", b.getString("settings.locale.pt_PT")),
                        new LocaleOption("pt-BR", b.getString("settings.locale.pt_BR")));
        localeCombo.setItems(items);

        String current = JwmsPreferences.getLocaleTag().orElse("");
        items.stream()
                .filter(o -> o.languageTag().equals(current))
                .findFirst()
                .ifPresent(localeCombo.getSelectionModel()::select);
        if (localeCombo.getSelectionModel().getSelectedItem() == null) {
            localeCombo.getSelectionModel().selectFirst();
        }
    }

    String getSelectedLanguageTag() {
        LocaleOption o = localeCombo.getSelectionModel().getSelectedItem();
        return o == null ? "" : o.languageTag();
    }
}
