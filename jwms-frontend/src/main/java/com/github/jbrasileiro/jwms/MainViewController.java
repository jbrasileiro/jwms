package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.application.CreateManuscriptProjectUseCase;
import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProjectPaths;
import com.github.jbrasileiro.jwms.application.CreateManuscriptProjectUseCase.CreateManuscriptProjectResult;
import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase;
import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase.OpenManuscriptProjectResult;
import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class MainViewController {

    private final OpenManuscriptProjectUseCase openProject = new OpenManuscriptProjectUseCase();
    private final CreateManuscriptProjectUseCase createProject = new CreateManuscriptProjectUseCase();

    @FXML private SplitPane mainSplit;
    @FXML private ListView<String> entryList;
    @FXML private Label statusLabel;
    @FXML private Label xmlRootLabel;

    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeProjectMenuItem;
    @FXML private MenuItem exportMenuItem;

    private Stage stage;
    private ResourceBundle bundle;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setResourceBundle(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle, "bundle");
    }

    private ResourceBundle bundle() {
        return Objects.requireNonNull(bundle, "ResourceBundle not set");
    }

    @FXML
    private void initialize() {
        if (mainSplit != null) {
            javafx.application.Platform.runLater(() -> mainSplit.setDividerPositions(0.55));
        }
    }

    @FXML
    private void onOpenProject(ActionEvent event) {
        Window owner = stage;
        if (owner == null && entryList != null && entryList.getScene() != null) {
            owner = entryList.getScene().getWindow();
        }
        if (!(owner instanceof Stage ownerStage)) {
            return;
        }
        var chooser = new FileChooser();
        chooser.setTitle(bundle().getString("filechooser.manuskript.title"));
        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                bundle().getString("filechooser.open.filter"),
                                "*" + ManuscriptProjectPaths.EXTENSION));
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        var file = chooser.showOpenDialog(ownerStage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        OpenManuscriptProjectResult result = openProject.open(path);
        applyOpenResult(result, ownerStage);
        if (result instanceof OpenManuscriptProjectResult.Success) {
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
        chooser.setTitle(bundle().getString("filechooser.manuskript.save.title"));
        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                bundle().getString("filechooser.save.filter"),
                                "*" + ManuscriptProjectPaths.EXTENSION));
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        File file = chooser.showSaveDialog(ownerStage);
        if (file == null) {
            return;
        }
        Path path = ensureJwmsSuffix(file.toPath());
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
        CreateManuscriptProjectResult created = createProject.create(path, overwrite);
        if (created instanceof CreateManuscriptProjectResult.Failure f) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("alert.new.create.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        Path saved = ((CreateManuscriptProjectResult.Success) created).path();
        JwmsPreferences.setLastProjectPath(saved.toAbsolutePath().normalize().toString());
        applyOpenResult(openProject.open(saved), ownerStage);
    }

    private void applyOpenResult(OpenManuscriptProjectResult result, Stage ownerStage) {
        if (result instanceof OpenManuscriptProjectResult.Failure f) {
            entryList.getItems().clear();
            xmlRootLabel.setText("");
            statusLabel.setText(bundle().getString("status.open.failed"));
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("alert.open.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        if (result instanceof OpenManuscriptProjectResult.Success s) {
            var p = s.project();
            entryList.getItems().setAll(p.getRelativeEntryNames());
            String formatSuffix =
                    p.isZipContainer()
                            ? bundle().getString("status.format.zip")
                            : bundle().getString("status.format.folder");
            statusLabel.setText(
                    MessageFormat.format(
                            bundle().getString("status.project.loaded"),
                            p.getProjectFile(),
                            String.valueOf(p.getFormatVersion()),
                            formatSuffix));
            xmlRootLabel.setText(
                    p.getSampleXmlRootLocalName()
                            .map(
                                    n ->
                                            MessageFormat.format(
                                                    bundle().getString("xml.root.pattern"), n))
                            .orElseGet(() -> bundle().getString("xml.root.none")));
        }
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

    private static Path ensureJwmsSuffix(Path path) {
        String name = path.getFileName().toString();
        if (!ManuscriptProjectPaths.endsWithProjectExtension(name)) {
            return path.resolveSibling(name + ManuscriptProjectPaths.EXTENSION);
        }
        return path;
    }

    @FXML
    private void onSave(ActionEvent event) {
        // Reservado: Guardar (desactivado até existir modelo editável + Save no backend).
    }

    @FXML
    private void onSaveAs(ActionEvent event) {
        // Reservado: Guardar como…
    }

    @FXML
    private void onCloseProject(ActionEvent event) {
        // Reservado: Fechar projeto
    }

    @FXML
    private void onImport(ActionEvent event) {
        // Reservado: Importar (UI activa; lógica em fases posteriores).
    }

    @FXML
    private void onExport(ActionEvent event) {
        // Reservado: Exportar (desactivado até implementação).
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
            reloadMainView(ownerStage);
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText(bundle().getString("settings.dialog.error.header"));
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private Stage resolveOwnerStage() {
        if (stage != null) {
            return stage;
        }
        if (entryList != null
                && entryList.getScene() != null
                && entryList.getScene().getWindow() instanceof Stage s) {
            return s;
        }
        return null;
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
}
