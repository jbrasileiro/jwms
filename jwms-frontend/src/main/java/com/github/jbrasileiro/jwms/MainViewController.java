package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.CreateProjectResult;
import com.github.jbrasileiro.jwms.api.OpenProjectResult;
import com.github.jbrasileiro.jwms.api.ProjectFileHints;
import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
import com.github.jbrasileiro.jwms.ui.ProjectFileChooserHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class MainViewController {

    @FXML private StackPane contentPane;

    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeProjectMenuItem;
    @FXML private MenuItem exportMenuItem;

    private Parent idleRoot;
    private Parent workspaceRoot;
    private WorkspaceViewController workspaceCtrl;

    private Stage stage;
    private ResourceBundle bundle;
    private AppShellController appShell;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setAppShell(AppShellController appShell) {
        this.appShell = appShell;
    }

    void setResourceBundle(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
        if (workspaceCtrl != null) {
            workspaceCtrl.setResourceBundle(bundle);
        }
    }

    private ResourceBundle bundle() {
        return Objects.requireNonNull(bundle, "ResourceBundle not set");
    }

    void showIdleNoProject() throws IOException {
        if (workspaceCtrl != null) {
            workspaceCtrl.clearProjectUi();
        }
        openIdleView();
        if (closeProjectMenuItem != null) {
            closeProjectMenuItem.setDisable(true);
        }
    }

    private void ensureIdleLoaded() throws IOException {
        if (idleRoot != null) {
            return;
        }
        FXMLLoader loader =
                new FXMLLoader(MainViewController.class.getResource("IdleView.fxml"), bundle());
        idleRoot = loader.load();
    }

    private void ensureWorkspaceLoaded() throws IOException {
        if (workspaceRoot != null) {
            return;
        }
        FXMLLoader loader =
                new FXMLLoader(MainViewController.class.getResource("WorkspaceView.fxml"), bundle());
        workspaceRoot = loader.load();
        workspaceCtrl = loader.getController();
        workspaceCtrl.setResourceBundle(bundle());
    }

    private void openIdleView() throws IOException {
        ensureIdleLoaded();
        contentPane.getChildren().setAll(idleRoot);
    }

    private void openWorkspaceView() throws IOException {
        ensureWorkspaceLoaded();
        contentPane.getChildren().setAll(workspaceRoot);
    }

    void applyOpenResult(OpenProjectResult result, Stage ownerStage) throws IOException {
        if (result instanceof OpenProjectResult.Failure f) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("alert.open.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        if (result instanceof OpenProjectResult.Success s) {
            ensureWorkspaceLoaded();
            workspaceCtrl.applyLoadedProject(s.snapshot());
            openWorkspaceView();
            if (closeProjectMenuItem != null) {
                closeProjectMenuItem.setDisable(false);
            }
        }
    }

    void restoreProjectFromPath(Path path, Stage ownerStage) throws IOException {
        applyOpenResult(
                JwmsServiceProvider.workspace().openProject(path.toAbsolutePath().normalize()),
                ownerStage);
    }

    @FXML
    private void onOpenProject(ActionEvent event) {
        Window owner = resolveOwnerWindow();
        if (!(owner instanceof Stage ownerStage)) {
            return;
        }
        var chooser = new FileChooser();
        ProjectFileChooserHelper.configureOpenProjectDialog(chooser, bundle());
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        var file = chooser.showOpenDialog(ownerStage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        OpenProjectResult result = JwmsServiceProvider.workspace().openProject(path);
        try {
            applyOpenResult(result, ownerStage);
        } catch (IOException e) {
            showLoadViewError(ownerStage, e);
        }
        if (result instanceof OpenProjectResult.Success) {
            JwmsPreferences.setLastProjectPath(path.toAbsolutePath().normalize().toString());
        }
    }

    @FXML
    private void onNewProject(ActionEvent event) {
        Stage ownerStage = resolveOwnerStage();
        if (ownerStage == null) {
            return;
        }
        var chooser = new FileChooser();
        ProjectFileChooserHelper.configureNewProjectSaveDialog(chooser, bundle());
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        File file = chooser.showSaveDialog(ownerStage);
        if (file == null) {
            return;
        }
        Path path = ProjectFileHints.ensureProjectExtension(file.toPath());
        boolean overwrite = false;
        if (Files.exists(path)) {
            var confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(ownerStage);
            confirm.setHeaderText(bundle().getString("alert.new.overwrite.header"));
            confirm.setContentText(
                    MessageFormat.format(bundle().getString("alert.new.overwrite.content"), path));
            Optional<ButtonType> answer = confirm.showAndWait();
            if (answer.isEmpty() || answer.get() != ButtonType.OK) {
                return;
            }
            overwrite = true;
        }
        CreateProjectResult created = JwmsServiceProvider.workspace().createProject(path, overwrite);
        if (created instanceof CreateProjectResult.Failure f) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("alert.new.create.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        Path saved = ((CreateProjectResult.Success) created).path();
        JwmsPreferences.setLastProjectPath(saved.toAbsolutePath().normalize().toString());
        try {
            applyOpenResult(JwmsServiceProvider.workspace().openProject(saved), ownerStage);
        } catch (IOException e) {
            showLoadViewError(ownerStage, e);
        }
    }

    private void showLoadViewError(Stage ownerStage, IOException e) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(ownerStage);
        alert.setHeaderText(bundle().getString("settings.reload.error.header"));
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    @FXML
    private void onSave(ActionEvent event) {
        // Reservado
    }

    @FXML
    private void onSaveAs(ActionEvent event) {
        // Reservado
    }

    @FXML
    private void onCloseProject(ActionEvent event) {
        try {
            showIdleNoProject();
        } catch (IOException e) {
            Stage ownerStage = resolveOwnerStage();
            if (ownerStage != null) {
                showLoadViewError(ownerStage, e);
            }
        }
    }

    @FXML
    private void onImport(ActionEvent event) {
        // Reservado
    }

    @FXML
    private void onExport(ActionEvent event) {
        // Reservado
    }

    @FXML
    private void onSettings(ActionEvent event) {
        Stage ownerStage = resolveOwnerStage();
        if (ownerStage == null) {
            return;
        }
        try {
            FXMLLoader settingsLoader =
                    new FXMLLoader(
                            MainViewController.class.getResource("SettingsDialog.fxml"), bundle());
            VBox content = settingsLoader.load();
            SettingsDialogController settingsCtrl = settingsLoader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(ownerStage);
            dialog.setTitle(bundle().getString("settings.title"));
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            String tag = settingsCtrl.getSelectedLanguageTag();
            if (tag == null || tag.isBlank()) {
                JwmsPreferences.clearLocaleTag();
            } else {
                JwmsPreferences.setLocaleTag(tag);
            }
            ResourceBundle.clearCache();
            if (appShell != null) {
                appShell.reloadWorkspaceAfterLocale(ownerStage);
            } else {
                reloadMainView(ownerStage);
            }
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("settings.dialog.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void reloadMainView(Stage ownerStage) {
        try {
            ResourceBundle newBundle = JwmsI18n.bundle();
            FXMLLoader loader =
                    new FXMLLoader(
                            JavaWriterManuscriptApplication.class.getResource("MainView.fxml"),
                            newBundle);
            Parent root = loader.load();
            MainViewController newCtrl = loader.getController();
            newCtrl.setResourceBundle(newBundle);
            newCtrl.setStage(ownerStage);
            newCtrl.setAppShell(appShell);
            newCtrl.showIdleNoProject();
            ownerStage.getScene().setRoot(root);
            ownerStage.setTitle(newBundle.getString("app.title"));
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("settings.reload.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onQuit(ActionEvent event) {
        Platform.exit();
    }

    private static Optional<File> initialDirectoryFromLastProject() {
        return JwmsPreferences.getLastProjectPath()
                .map(Path::of)
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .map(Path::getParent)
                .filter(Files::isDirectory)
                .map(Path::toFile);
    }

    private Window resolveOwnerWindow() {
        if (stage != null) {
            return stage;
        }
        if (contentPane != null
                && contentPane.getScene() != null
                && contentPane.getScene().getWindow() != null) {
            return contentPane.getScene().getWindow();
        }
        return null;
    }

    private Stage resolveOwnerStage() {
        Window w = resolveOwnerWindow();
        return w instanceof Stage s ? s : null;
    }
}
