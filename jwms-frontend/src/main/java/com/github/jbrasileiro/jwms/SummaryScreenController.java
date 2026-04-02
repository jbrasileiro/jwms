package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.LoadSummaryResult;
import com.github.jbrasileiro.jwms.api.ManuscriptWorkspaceApi;
import com.github.jbrasileiro.jwms.api.ProjectSnapshot;
import com.github.jbrasileiro.jwms.api.SaveResult;
import com.github.jbrasileiro.jwms.api.SummaryMetadataDto;
import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Ecrã Resumo: quatro textos (um por modo do combo), situação e persistência. */
public final class SummaryScreenController {

    enum SummaryLengthMode {
        SENTENCE,
        PARAGRAPH,
        PAGE,
        FULL
    }

    @FXML private ComboBox<String> situationCombo;
    @FXML private ComboBox<SummaryLengthMode> summaryModeCombo;

    @FXML private VBox sentenceOnlyPane;
    @FXML private Label sentencePanelTitle;
    @FXML private TextArea sentenceTextArea;
    @FXML private Label sentenceWordCountLabel;

    @FXML private SplitPane summarySplit;
    @FXML private Label leftPanelTitle;
    @FXML private Label rightPanelTitle;
    @FXML private TextArea leftTextArea;
    @FXML private TextArea rightTextArea;
    @FXML private Label wordCountLabel;

    private ResourceBundle bundle;

    private ProjectSnapshot openSnapshot;
    private boolean suppressSummarySave;
    /** Evita pull/save ao carregar snapshot ou reaplicar i18n sem mudança real de modo. */
    private boolean loadingSnapshot;

    private String bodySentence = "";
    private String bodyParagraph = "";
    private String bodyPage = "";
    private String bodyFull = "";

    @FXML
    private void initialize() {
        bundle = com.github.jbrasileiro.jwms.i18n.JwmsI18n.bundle();
        situationCombo.setEditable(true);
        situationCombo.setPromptText(bundle.getString("summary.situation.prompt"));

        summaryModeCombo.getItems().setAll(SummaryLengthMode.values());
        summaryModeCombo.setConverter(modeConverter());
        installSummaryModeComboCells();
        summaryModeCombo
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (o, oldMode, newMode) -> {
                            if (newMode == null) {
                                return;
                            }
                            if (Boolean.TRUE.equals(loadingSnapshot)) {
                                applyMode(newMode);
                                return;
                            }
                            pullVisibleToModel(oldMode);
                            applyMode(newMode);
                            saveSummaryFromForm();
                        });
        summaryModeCombo.getSelectionModel().select(SummaryLengthMode.SENTENCE);

        sentenceTextArea
                .textProperty()
                .addListener((o, a, t) -> refreshWordCount());
        rightTextArea.textProperty().addListener((o, a, t) -> refreshWordCount());
        leftTextArea.textProperty().addListener((o, a, t) -> refreshWordCount());

        installSummarySaveOnBlur();

        applyMode(SummaryLengthMode.SENTENCE);
        refreshWordCount();
    }

    private void installSummarySaveOnBlur() {
        for (TextArea ta : List.of(sentenceTextArea, leftTextArea, rightTextArea)) {
            ta.focusedProperty()
                    .addListener(
                            (o, was, now) -> {
                                if (Boolean.TRUE.equals(was) && Boolean.FALSE.equals(now)) {
                                    saveSummaryFromForm();
                                }
                            });
        }
        if (situationCombo.getEditor() != null) {
            situationCombo
                    .getEditor()
                    .focusedProperty()
                    .addListener(
                            (o, was, now) -> {
                                if (Boolean.TRUE.equals(was) && Boolean.FALSE.equals(now)) {
                                    saveSummaryFromForm();
                                }
                            });
        }
    }

    /**
     * Texto do combo: com duas colunas, mostra «passo anterior — passo actual», alinhado com o
     * painel esquerdo (anterior) e direito (actual).
     */
    private StringConverter<SummaryLengthMode> modeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SummaryLengthMode m) {
                return m == null ? "" : bundle.getString("summary.mode.combo." + keySuffix(m));
            }

            @Override
            public SummaryLengthMode fromString(String s) {
                return null;
            }
        };
    }

    private void installSummaryModeComboCells() {
        summaryModeCombo.setCellFactory(lv -> createSummaryModeListCell());
        summaryModeCombo.setButtonCell(createSummaryModeListCell());
    }

    private ListCell<SummaryLengthMode> createSummaryModeListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(SummaryLengthMode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                StringConverter<SummaryLengthMode> c = summaryModeCombo.getConverter();
                setText(c != null ? c.toString(item) : item.name());
            }
        };
    }

    void onShown() {
        if (summarySplit == null || !summarySplit.isVisible()) {
            return;
        }
        SummaryLengthMode m = summaryModeCombo.getSelectionModel().getSelectedItem();
        Platform.runLater(() -> applyDividerForMode(m));
    }

    private static String keySuffix(SummaryLengthMode m) {
        return switch (m) {
            case SENTENCE -> "sentence";
            case PARAGRAPH -> "paragraph";
            case PAGE -> "page";
            case FULL -> "full";
        };
    }

    void applyOpenSnapshot(ProjectSnapshot snapshot) {
        openSnapshot = snapshot;
        loadSummaryFromWorkspace();
    }

    void flushSave() {
        saveSummaryFromForm();
    }

    private void loadSummaryFromWorkspace() {
        if (openSnapshot == null) {
            return;
        }
        suppressSummarySave = true;
        loadingSnapshot = true;
        try {
            LoadSummaryResult r = JwmsServiceProvider.workspace().loadSummary(openSnapshot);
            SummaryMetadataDto dto =
                    r instanceof LoadSummaryResult.Success s
                            ? s.data()
                            : SummaryMetadataDto.empty();
            applySummaryDto(dto);
        } finally {
            loadingSnapshot = false;
            suppressSummarySave = false;
        }
    }

    private void applySummaryDto(SummaryMetadataDto dto) {
        bodySentence = dto.sentenceText();
        bodyParagraph = dto.paragraphText();
        bodyPage = dto.pageText();
        bodyFull = dto.fullText();
        if (situationCombo.getEditor() != null) {
            situationCombo.getEditor().setText(dto.situation());
        }
        SummaryLengthMode mode = parseLengthMode(dto.lengthMode());
        summaryModeCombo.getSelectionModel().clearSelection();
        summaryModeCombo.getSelectionModel().select(mode);
        refreshWordCount();
    }

    private static SummaryLengthMode parseLengthMode(String stored) {
        if (stored == null || stored.isBlank()) {
            return SummaryLengthMode.SENTENCE;
        }
        try {
            return SummaryLengthMode.valueOf(stored.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SummaryLengthMode.SENTENCE;
        }
    }

    /** Escreve o que está visível nos quatro corpos consoante o modo que estava activo. */
    private void pullVisibleToModel(SummaryLengthMode mode) {
        if (mode == null) {
            return;
        }
        switch (mode) {
            case SENTENCE -> bodySentence = textOrEmpty(sentenceTextArea);
            case PARAGRAPH -> {
                bodySentence = textOrEmpty(leftTextArea);
                bodyParagraph = textOrEmpty(rightTextArea);
            }
            case PAGE -> {
                bodyParagraph = textOrEmpty(leftTextArea);
                bodyPage = textOrEmpty(rightTextArea);
            }
            case FULL -> {
                bodyPage = textOrEmpty(leftTextArea);
                bodyFull = textOrEmpty(rightTextArea);
            }
        }
    }

    /** Mostra nos painéis os textos do estado anterior (esq.) e actual (dir.) para este modo. */
    private void pushTextsForMode(SummaryLengthMode mode) {
        if (mode == null) {
            return;
        }
        if (mode == SummaryLengthMode.SENTENCE) {
            if (sentenceTextArea != null) {
                sentenceTextArea.setText(bodySentence);
            }
            return;
        }
        if (leftTextArea != null && rightTextArea != null) {
            switch (mode) {
                case PARAGRAPH -> {
                    leftTextArea.setText(bodySentence);
                    rightTextArea.setText(bodyParagraph);
                }
                case PAGE -> {
                    leftTextArea.setText(bodyParagraph);
                    rightTextArea.setText(bodyPage);
                }
                case FULL -> {
                    leftTextArea.setText(bodyPage);
                    rightTextArea.setText(bodyFull);
                }
                default -> {}
            }
        }
    }

    private SummaryMetadataDto readSummaryDtoFromForm() {
        SummaryLengthMode mode = summaryModeCombo.getSelectionModel().getSelectedItem();
        if (mode == null) {
            mode = SummaryLengthMode.SENTENCE;
        }
        pullVisibleToModel(mode);
        String situation =
                situationCombo.getEditor() != null && situationCombo.getEditor().getText() != null
                        ? situationCombo.getEditor().getText()
                        : "";
        return new SummaryMetadataDto(
                situation,
                mode.name(),
                bodySentence,
                bodyParagraph,
                bodyPage,
                bodyFull);
    }

    private static String textOrEmpty(TextArea a) {
        return a == null || a.getText() == null ? "" : a.getText();
    }

    private void saveSummaryFromForm() {
        if (suppressSummarySave || openSnapshot == null) {
            return;
        }
        ManuscriptWorkspaceApi workspace = JwmsServiceProvider.workspace();
        SaveResult result = workspace.saveSummary(openSnapshot, readSummaryDtoFromForm());
        if (result instanceof SaveResult.Failure) {
            // Falhas de I/O: logging futuro
        }
    }

    void applyResourceBundle(ResourceBundle newBundle) {
        this.bundle = newBundle;
        situationCombo.setPromptText(bundle.getString("summary.situation.prompt"));
        summaryModeCombo.setConverter(modeConverter());
        installSummaryModeComboCells();
        SummaryLengthMode cur = summaryModeCombo.getSelectionModel().getSelectedItem();
        if (cur == null) {
            cur = SummaryLengthMode.SENTENCE;
        }
        loadingSnapshot = true;
        try {
            summaryModeCombo.getSelectionModel().clearSelection();
            summaryModeCombo.getSelectionModel().select(cur);
            applyMode(cur);
            refreshWordCount();
        } finally {
            loadingSnapshot = false;
        }
    }

    void clear() {
        openSnapshot = null;
        suppressSummarySave = true;
        loadingSnapshot = true;
        try {
            bodySentence = "";
            bodyParagraph = "";
            bodyPage = "";
            bodyFull = "";
            situationCombo.getSelectionModel().clearSelection();
            situationCombo.setValue(null);
            if (situationCombo.getEditor() != null) {
                situationCombo.getEditor().setText("");
            }
            sentenceTextArea.clear();
            leftTextArea.clear();
            rightTextArea.clear();
            summaryModeCombo.getSelectionModel().select(SummaryLengthMode.SENTENCE);
            applyMode(SummaryLengthMode.SENTENCE);
            refreshWordCount();
        } finally {
            loadingSnapshot = false;
            suppressSummarySave = false;
        }
    }

    private void applyMode(SummaryLengthMode mode) {
        if (mode == null || bundle == null) {
            return;
        }
        boolean sentenceOnly = mode == SummaryLengthMode.SENTENCE;
        if (sentenceOnlyPane != null) {
            sentenceOnlyPane.setVisible(sentenceOnly);
            sentenceOnlyPane.setManaged(sentenceOnly);
        }
        if (summarySplit != null) {
            summarySplit.setVisible(!sentenceOnly);
            summarySplit.setManaged(!sentenceOnly);
        }

        if (sentenceOnly) {
            if (sentencePanelTitle != null) {
                sentencePanelTitle.setText(bundle.getString("summary.panel.sentence.only.title"));
            }
            pushTextsForMode(SummaryLengthMode.SENTENCE);
            Platform.runLater(this::refreshWordCount);
            return;
        }

        if (leftPanelTitle == null || rightPanelTitle == null) {
            return;
        }
        switch (mode) {
            case PARAGRAPH -> {
                leftPanelTitle.setText(bundle.getString("summary.panel.left.short"));
                rightPanelTitle.setText(bundle.getString("summary.panel.right.short"));
            }
            case PAGE -> {
                leftPanelTitle.setText(bundle.getString("summary.panel.left.page"));
                rightPanelTitle.setText(bundle.getString("summary.panel.right.page"));
            }
            case FULL -> {
                leftPanelTitle.setText(bundle.getString("summary.panel.left.full"));
                rightPanelTitle.setText(bundle.getString("summary.panel.right.full"));
            }
            default -> {}
        }
        pushTextsForMode(mode);
        Platform.runLater(
                () -> {
                    applyDividerForMode(mode);
                    refreshWordCount();
                });
    }

    private void applyDividerForMode(SummaryLengthMode mode) {
        if (summarySplit == null) {
            return;
        }
        double pos = mode == SummaryLengthMode.PARAGRAPH ? 0.42 : 0.5;
        summarySplit.setDividerPositions(pos);
    }

    private void refreshWordCount() {
        if (bundle == null) {
            return;
        }
        SummaryLengthMode m =
                summaryModeCombo != null
                        ? summaryModeCombo.getSelectionModel().getSelectedItem()
                        : null;
        if (m == SummaryLengthMode.SENTENCE) {
            applyWordCount(
                    sentenceTextArea == null ? "" : sentenceTextArea.getText(),
                    sentenceWordCountLabel,
                    m);
            if (wordCountLabel != null) {
                wordCountLabel.setText("");
            }
        } else {
            applyWordCount(
                    rightTextArea == null ? "" : rightTextArea.getText(),
                    wordCountLabel,
                    m);
            if (sentenceWordCountLabel != null) {
                sentenceWordCountLabel.setText("");
            }
        }
    }

    private static final double WORDS_PER_PAGE_ESTIMATE = 250.0;

    private void applyWordCount(String text, Label target, SummaryLengthMode mode) {
        if (target == null) {
            return;
        }
        int n = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        if (mode == SummaryLengthMode.PAGE || mode == SummaryLengthMode.FULL) {
            double pages = n / WORDS_PER_PAGE_ESTIMATE;
            Locale loc = JwmsI18n.resolveLocale();
            DecimalFormat df =
                    new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(loc));
            target.setText(
                    MessageFormat.format(
                            bundle.getString("summary.wordsWithPages"), n, df.format(pages)));
        } else {
            target.setText(MessageFormat.format(bundle.getString("summary.words"), n));
        }
    }
}
