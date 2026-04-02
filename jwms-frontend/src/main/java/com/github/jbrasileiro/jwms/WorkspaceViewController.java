package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.ProjectSnapshot;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/** Vista do projecto aberto: barra lateral e composição dos ecrãs (FXML incluídos). */
public final class WorkspaceViewController {

    private static final double NAV_WIDTH_EXPANDED = 220;
    private static final double NAV_WIDTH_COLLAPSED = 56;

    @FXML private VBox navigationPane;
    @FXML private Label navTitleLabel;
    @FXML private Button navToggleButton;
    @FXML private VBox navButtonContainer;

    @FXML private VBox generalScreen;
    @FXML private GeneralScreenController generalScreenController;

    @FXML private VBox summaryScreen;
    @FXML private SummaryScreenController summaryScreenController;

    @FXML private VBox charactersScreen;
    @FXML private VBox plotsScreen;
    @FXML private VBox worldScreen;
    @FXML private VBox outlineScreen;

    @FXML private VBox editorScreen;
    @FXML private EditorScreenController editorScreenController;

    private final List<ToggleButton> navToggleButtons = new ArrayList<>();
    private ToggleGroup navToggleGroup;
    private boolean navCollapsed;

    private ResourceBundle bundle;

    void setResourceBundle(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
        setupNavigationBar();
        if (summaryScreenController != null) {
            summaryScreenController.applyResourceBundle(bundle);
        }
    }

    private ResourceBundle bundle() {
        return Objects.requireNonNull(bundle, "ResourceBundle not set");
    }

    void clearProjectUi() {
        generalScreenController.clear();
        if (summaryScreenController != null) {
            summaryScreenController.clear();
        }
        editorScreenController.clear();
    }

    void applyLoadedProject(ProjectSnapshot snapshot) {
        generalScreenController.applyOpenSnapshot(snapshot);
        if (summaryScreenController != null) {
            summaryScreenController.applyOpenSnapshot(snapshot);
        }
        editorScreenController.setEntryNames(snapshot.relativeEntryNames());
        if (navToggleGroup != null && !navToggleButtons.isEmpty()) {
            var sel = navToggleGroup.getSelectedToggle();
            if (sel != null && sel.getUserData() instanceof NavigationScreen ns) {
                showScreen(ns);
            }
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
                                generalScreenController.flushSave();
                            }
                            if (oldVal != null
                                    && oldVal.getUserData() == NavigationScreen.SUMMARY
                                    && summaryScreenController != null) {
                                summaryScreenController.flushSave();
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
            case GENERAL -> generalScreen;
            case SUMMARY -> summaryScreen;
            case CHARACTERS -> charactersScreen;
            case PLOTS -> plotsScreen;
            case WORLD -> worldScreen;
            case OUTLINE -> outlineScreen;
            case EDITOR -> editorScreen;
        };
    }

    private void showScreen(NavigationScreen screen) {
        for (NavigationScreen s : NavigationScreen.values()) {
            Node n = screenNode(s);
            boolean on = s == screen;
            n.setVisible(on);
            n.setManaged(on);
        }
        if (screen == NavigationScreen.EDITOR) {
            editorScreenController.restoreDivider();
        }
        if (screen == NavigationScreen.SUMMARY && summaryScreenController != null) {
            Platform.runLater(summaryScreenController::onShown);
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
