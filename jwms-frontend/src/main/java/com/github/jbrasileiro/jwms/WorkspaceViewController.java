package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.GeneralMetadataDto;
import com.github.jbrasileiro.jwms.api.LoadGeneralResult;
import com.github.jbrasileiro.jwms.api.ManuscriptWorkspaceApi;
import com.github.jbrasileiro.jwms.api.ProjectSnapshot;
import com.github.jbrasileiro.jwms.api.SaveResult;
import com.github.jbrasileiro.jwms.ui.NavigationScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/** Vista do projecto aberto: navegação e ecrãs (General, Editor, …). */
public final class WorkspaceViewController {

    private static final double NAV_WIDTH_EXPANDED = 220;
    private static final double NAV_WIDTH_COLLAPSED = 56;

    @FXML private VBox navigationPane;
    @FXML private Label navTitleLabel;
    @FXML private Button navToggleButton;
    @FXML private VBox navButtonContainer;
    @FXML private VBox screenGeneral;
    @FXML private VBox screenSummary;
    @FXML private VBox screenCharacters;
    @FXML private VBox screenPlots;
    @FXML private VBox screenWorld;
    @FXML private VBox screenOutline;
    @FXML private VBox screenEditor;
    @FXML private SplitPane mainSplit;
    @FXML private ListView<String> entryList;

    @FXML private TextField generalTitleField;
    @FXML private TextField generalSubtitleField;
    @FXML private TextField generalSeriesField;
    @FXML private TextField generalVolumeField;
    @FXML private TextField generalGenreField;
    @FXML private TextField generalLicenseField;
    @FXML private TextField generalAuthorNameField;
    @FXML private TextField generalAuthorEmailField;

    private final List<TextField> generalFields = new ArrayList<>();

    private final List<ToggleButton> navToggleButtons = new ArrayList<>();
    private ToggleGroup navToggleGroup;
    private boolean navCollapsed;

    private ResourceBundle bundle;
    private ProjectSnapshot openSnapshot;
    private boolean suppressGeneralSave;

    void setResourceBundle(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
        setupNavigationBar();
    }

    private ResourceBundle bundle() {
        return Objects.requireNonNull(bundle, "ResourceBundle not set");
    }

    @FXML
    private void initialize() {
        generalFields.clear();
        if (generalTitleField != null) {
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
        if (mainSplit != null) {
            Platform.runLater(() -> mainSplit.setDividerPositions(0.55));
        }
    }

    void clearProjectUi() {
        openSnapshot = null;
        suppressGeneralSave = true;
        try {
            for (TextField tf : generalFields) {
                tf.setText("");
            }
        } finally {
            suppressGeneralSave = false;
        }
        if (entryList != null) {
            entryList.getItems().clear();
        }
    }

    void applyLoadedProject(ProjectSnapshot snapshot) {
        openSnapshot = snapshot;
        if (entryList != null) {
            entryList.getItems().clear();
            entryList.getItems().setAll(snapshot.relativeEntryNames());
        }
        loadGeneralFromWorkspace();
        if (navToggleGroup != null && !navToggleButtons.isEmpty()) {
            var sel = navToggleGroup.getSelectedToggle();
            if (sel != null && sel.getUserData() instanceof NavigationScreen ns) {
                showScreen(ns);
            }
        }
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
        if (generalTitleField != null) {
            generalTitleField.setText(dto.title());
        }
        if (generalSubtitleField != null) {
            generalSubtitleField.setText(dto.subtitle());
        }
        if (generalSeriesField != null) {
            generalSeriesField.setText(dto.series());
        }
        if (generalVolumeField != null) {
            generalVolumeField.setText(dto.volume());
        }
        if (generalGenreField != null) {
            generalGenreField.setText(dto.genre());
        }
        if (generalLicenseField != null) {
            generalLicenseField.setText(dto.license());
        }
        if (generalAuthorNameField != null) {
            generalAuthorNameField.setText(dto.authorName());
        }
        if (generalAuthorEmailField != null) {
            generalAuthorEmailField.setText(dto.authorEmail());
        }
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
            // Evitar spam em cada blur; falhas de I/O podem ser tratadas com logging futuro
        }
    }

    private void setupNavigationBar() {
        if (navButtonContainer == null) {
            return;
        }
        navButtonContainer.getChildren().clear();
        navToggleButtons.clear();
        navToggleGroup = new ToggleGroup();
        navToggleGroup
                .selectedToggleProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (oldVal != null
                                    && oldVal.getUserData() == NavigationScreen.GENERAL) {
                                saveGeneralFromForm();
                            }
                            if (newVal != null) {
                                showScreen((NavigationScreen) newVal.getUserData());
                            }
                        });
        for (NavigationScreen screen : NavigationScreen.values()) {
            ToggleButton tb = new ToggleButton();
            tb.setToggleGroup(navToggleGroup);
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label(screen.getIconGlyph());
            icon.getStyleClass().add("nav-icon-label");
            tb.setGraphic(icon);
            tb.setContentDisplay(ContentDisplay.LEFT);
            String label = bundle().getString(screen.getMessageKey());
            tb.setText(label);
            tb.setTooltip(new Tooltip(label));
            tb.setUserData(screen);
            tb.getStyleClass().add("nav-toggle-button");
            navToggleButtons.add(tb);
            navButtonContainer.getChildren().add(tb);
        }
        if (!navToggleButtons.isEmpty()) {
            navToggleGroup.selectToggle(navToggleButtons.getFirst());
        }
        applyNavigationCollapseLayout();
    }

    private Node screenNode(NavigationScreen screen) {
        return switch (screen) {
            case GENERAL -> screenGeneral;
            case SUMMARY -> screenSummary;
            case CHARACTERS -> screenCharacters;
            case PLOTS -> screenPlots;
            case WORLD -> screenWorld;
            case OUTLINE -> screenOutline;
            case EDITOR -> screenEditor;
        };
    }

    private void showScreen(NavigationScreen screen) {
        for (NavigationScreen s : NavigationScreen.values()) {
            Node n = screenNode(s);
            boolean on = s == screen;
            n.setVisible(on);
            n.setManaged(on);
        }
        if (mainSplit != null && screen == NavigationScreen.EDITOR) {
            Platform.runLater(() -> mainSplit.setDividerPositions(0.55));
        }
    }

    @FXML
    private void onToggleNavigation(ActionEvent event) {
        navCollapsed = !navCollapsed;
        if (navToggleButton != null) {
            navToggleButton.setText(navCollapsed ? "\u00BB" : "\u00AB");
        }
        if (navTitleLabel != null) {
            navTitleLabel.setVisible(!navCollapsed);
            navTitleLabel.setManaged(!navCollapsed);
        }
        applyNavigationCollapseLayout();
    }

    private void applyNavigationCollapseLayout() {
        double w = navCollapsed ? NAV_WIDTH_COLLAPSED : NAV_WIDTH_EXPANDED;
        if (navigationPane != null) {
            navigationPane.setMinWidth(w);
            navigationPane.setPrefWidth(w);
            navigationPane.setMaxWidth(w);
        }
        ContentDisplay display = navCollapsed ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT;
        Pos align = navCollapsed ? Pos.CENTER : Pos.CENTER_LEFT;
        for (ToggleButton tb : navToggleButtons) {
            tb.setContentDisplay(display);
            tb.setAlignment(align);
            Object data = tb.getUserData();
            if (data instanceof NavigationScreen ns) {
                tb.setText(navCollapsed ? "" : bundle().getString(ns.getMessageKey()));
            }
        }
    }
}
