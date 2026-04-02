package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.GeneralMetadataDto;
import com.github.jbrasileiro.jwms.api.LoadGeneralResult;
import com.github.jbrasileiro.jwms.api.ManuscriptWorkspaceApi;
import com.github.jbrasileiro.jwms.api.ProjectSnapshot;
import com.github.jbrasileiro.jwms.api.SaveResult;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/** Ecrã Geral: metadados do livro/autor e persistência via API. */
public final class GeneralScreenController {

    @FXML private TextField generalTitleField;
    @FXML private TextField generalSubtitleField;
    @FXML private TextField generalSeriesField;
    @FXML private TextField generalVolumeField;
    @FXML private TextField generalGenreField;
    @FXML private TextField generalLicenseField;
    @FXML private TextField generalAuthorNameField;
    @FXML private TextField generalAuthorEmailField;

    private final List<TextField> generalFields = new ArrayList<>();

    private ProjectSnapshot openSnapshot;
    private boolean suppressGeneralSave;

    @FXML
    private void initialize() {
        generalFields.clear();
        generalFields.addAll(
                List.of(
                        generalTitleField,
                        generalSubtitleField,
                        generalSeriesField,
                        generalVolumeField,
                        generalGenreField,
                        generalLicenseField,
                        generalAuthorNameField,
                        generalAuthorEmailField));
        for (TextField tf : generalFields) {
            tf.focusedProperty()
                    .addListener(
                            (o, was, now) -> {
                                if (Boolean.TRUE.equals(was) && Boolean.FALSE.equals(now)) {
                                    saveGeneralFromForm();
                                }
                            });
        }
    }

    void clear() {
        openSnapshot = null;
        suppressGeneralSave = true;
        try {
            for (TextField tf : generalFields) {
                tf.setText("");
            }
        } finally {
            suppressGeneralSave = false;
        }
    }

    void applyOpenSnapshot(ProjectSnapshot snapshot) {
        openSnapshot = snapshot;
        loadGeneralFromWorkspace();
    }

    void flushSave() {
        saveGeneralFromForm();
    }

    private void loadGeneralFromWorkspace() {
        if (openSnapshot == null) {
            return;
        }
        suppressGeneralSave = true;
        try {
            LoadGeneralResult r = JwmsServiceProvider.workspace().loadGeneral(openSnapshot);
            GeneralMetadataDto dto =
                    r instanceof LoadGeneralResult.Success s
                            ? s.data()
                            : GeneralMetadataDto.empty();
            applyGeneralDto(dto);
        } finally {
            suppressGeneralSave = false;
        }
    }

    private void applyGeneralDto(GeneralMetadataDto dto) {
        generalTitleField.setText(dto.title());
        generalSubtitleField.setText(dto.subtitle());
        generalSeriesField.setText(dto.series());
        generalVolumeField.setText(dto.volume());
        generalGenreField.setText(dto.genre());
        generalLicenseField.setText(dto.license());
        generalAuthorNameField.setText(dto.authorName());
        generalAuthorEmailField.setText(dto.authorEmail());
    }

    private GeneralMetadataDto readGeneralDtoFromForm() {
        return new GeneralMetadataDto(
                textOrEmpty(generalTitleField),
                textOrEmpty(generalSubtitleField),
                textOrEmpty(generalSeriesField),
                textOrEmpty(generalVolumeField),
                textOrEmpty(generalGenreField),
                textOrEmpty(generalLicenseField),
                textOrEmpty(generalAuthorNameField),
                textOrEmpty(generalAuthorEmailField));
    }

    private static String textOrEmpty(TextField f) {
        return f == null || f.getText() == null ? "" : f.getText();
    }

    private void saveGeneralFromForm() {
        if (suppressGeneralSave || openSnapshot == null) {
            return;
        }
        ManuscriptWorkspaceApi workspace = JwmsServiceProvider.workspace();
        SaveResult result = workspace.saveGeneral(openSnapshot, readGeneralDtoFromForm());
        if (result instanceof SaveResult.Failure) {
            // Falhas de I/O: logging futuro
        }
    }
}
