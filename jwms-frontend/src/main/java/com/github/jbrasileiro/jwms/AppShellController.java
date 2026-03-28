package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.OpenProjectResult;
import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Raiz da aplicação: MainView desde o arranque; Abrir/Novo usam o menu do MainView. */
public final class AppShellController {

    @FXML private StackPane shellStack;

    private Stage stage;
    private ResourceBundle bundle;
    private Parent welcomeRoot;
    private Parent mainRoot;
    private MainViewController mainController;

    void setStage(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    void setBundle(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
    }

    @FXML
    private void initialize() {}

    void bootstrap() throws IOException {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(bundle, "bundle");
        loadMainAsInitialRoot();
    }

    private void loadMainAsInitialRoot() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppShellController.class.getResource("MainView.fxml"), bundle);
        mainRoot = loader.load();
        mainController = loader.getController();
        mainController.setResourceBundle(bundle);
        mainController.setStage(stage);
        mainController.setAppShell(this);
        mainController.showIdleNoProject();
        shellStack.getChildren().setAll(mainRoot);
        welcomeRoot = null;
        mainRoot.setVisible(true);
        mainRoot.setManaged(true);
    }

    private void loadWelcomeScreen() throws IOException {
        FXMLLoader loader =
                new FXMLLoader(AppShellController.class.getResource("WelcomeView.fxml"), bundle);
        welcomeRoot = loader.load();
        WelcomeController wc = loader.getController();
        wc.bindShell(this, stage, bundle);
        shellStack.getChildren().setAll(welcomeRoot);
        mainRoot = null;
        mainController = null;
    }

    void enterWorkspace(OpenProjectResult result, Stage ownerStage) {
        if (!(result instanceof OpenProjectResult.Success)) {
            throw new IllegalArgumentException("expected success");
        }
        ensureMainLoaded(ownerStage);
        try {
            mainController.applyOpenResult(result, ownerStage);
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(JwmsI18n.bundle().getString("settings.reload.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        showMainWorkspace();
    }

    private void ensureMainLoaded(Stage ownerStage) {
        if (mainRoot != null) {
            return;
        }
        try {
            FXMLLoader loader =
                    new FXMLLoader(AppShellController.class.getResource("MainView.fxml"), bundle);
            mainRoot = loader.load();
            mainController = loader.getController();
            mainController.setResourceBundle(bundle);
            mainController.setStage(ownerStage);
            mainController.setAppShell(this);
            shellStack.getChildren().add(mainRoot);
            mainRoot.setVisible(false);
            mainRoot.setManaged(false);
        } catch (IOException e) {
            throw new IllegalStateException("MainView load failed", e);
        }
    }

    private void showMainWorkspace() {
        if (welcomeRoot != null) {
            welcomeRoot.setVisible(false);
            welcomeRoot.setManaged(false);
        }
        mainRoot.setVisible(true);
        mainRoot.setManaged(true);
        mainRoot.toFront();
    }

    void showWelcomeScreen() throws IOException {
        ResourceBundle fresh = JwmsI18n.bundle();
        this.bundle = fresh;
        loadWelcomeScreen();
        welcomeRoot.setVisible(true);
        welcomeRoot.setManaged(true);
        stage.setTitle(fresh.getString("app.title"));
    }

    void reloadWelcomeAfterLocale(Stage ownerStage) {
        try {
            ResourceBundle fresh = JwmsI18n.bundle();
            this.bundle = fresh;
            FXMLLoader loader =
                    new FXMLLoader(AppShellController.class.getResource("WelcomeView.fxml"), fresh);
            Parent newWelcome = loader.load();
            WelcomeController wc = loader.getController();
            wc.bindShell(this, ownerStage, fresh);
            shellStack.getChildren().clear();
            shellStack.getChildren().add(newWelcome);
            welcomeRoot = newWelcome;
            stage.setTitle(fresh.getString("app.title"));
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(JwmsI18n.bundle().getString("settings.reload.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    void reloadWorkspaceAfterLocale(Stage ownerStage) {
        if (mainRoot == null) {
            return;
        }
        try {
            ResourceBundle fresh = JwmsI18n.bundle();
            this.bundle = fresh;
            FXMLLoader loader =
                    new FXMLLoader(AppShellController.class.getResource("MainView.fxml"), fresh);
            Parent newMain = loader.load();
            MainViewController newCtrl = loader.getController();
            newCtrl.setResourceBundle(fresh);
            newCtrl.setStage(ownerStage);
            newCtrl.setAppShell(this);
            newCtrl.showIdleNoProject();
            shellStack.getChildren().remove(mainRoot);
            mainRoot = newMain;
            mainController = newCtrl;
            shellStack.getChildren().add(mainRoot);
            showMainWorkspace();
            JwmsPreferences.getLastProjectPath()
                    .map(Path::of)
                    .ifPresent(
                            p -> {
                                try {
                                    mainController.restoreProjectFromPath(
                                            p.toAbsolutePath().normalize(), ownerStage);
                                } catch (IOException ex) {
                                    var alert = new Alert(Alert.AlertType.ERROR);
                                    alert.initOwner(ownerStage);
                                    alert.setHeaderText(
                                            JwmsI18n.bundle().getString("settings.reload.error.header"));
                                    alert.setContentText(ex.getMessage());
                                    alert.showAndWait();
                                }
                            });
            stage.setTitle(fresh.getString("app.title"));
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(JwmsI18n.bundle().getString("settings.reload.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
