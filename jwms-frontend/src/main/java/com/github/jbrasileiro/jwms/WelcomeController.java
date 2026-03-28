package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.CreateProjectResult;
import com.github.jbrasileiro.jwms.api.OpenProjectResult;
import com.github.jbrasileiro.jwms.api.ProjectFileHints;
import com.github.jbrasileiro.jwms.prefs.JwmsPreferences;
import com.github.jbrasileiro.jwms.ui.ProjectFileChooserHelper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class WelcomeController {

    private AppShellController shell;
    private Stage stage;
    private ResourceBundle bundle;

    void bindShell(AppShellController shell, Stage stage, ResourceBundle bundle) {
        this.shell = shell;
        this.stage = stage;
        this.bundle = bundle;
    }

    private ResourceBundle bundle() {
        return bundle;
    }

    @FXML
    private void onOpenProject(ActionEvent event) {
        var chooser = new FileChooser();
        ProjectFileChooserHelper.configureOpenProjectDialog(chooser, bundle());
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        OpenProjectResult result = JwmsServiceProvider.workspace().openProject(path);
        if (result instanceof OpenProjectResult.Failure f) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setHeaderText(bundle().getString("alert.open.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        JwmsPreferences.setLastProjectPath(path.toAbsolutePath().normalize().toString());
        shell.enterWorkspace(result, stage);
    }

    @FXML
    private void onNewProject(ActionEvent event) {
        var chooser = new FileChooser();
        ProjectFileChooserHelper.configureNewProjectSaveDialog(chooser, bundle());
        initialDirectoryFromLastProject().ifPresent(chooser::setInitialDirectory);
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        Path path = ProjectFileHints.ensureProjectExtension(file.toPath());
        boolean overwrite = false;
        if (Files.exists(path)) {
            var confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
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
            alert.initOwner(stage);
            alert.setHeaderText(bundle().getString("alert.new.create.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        Path saved = ((CreateProjectResult.Success) created).path();
        JwmsPreferences.setLastProjectPath(saved.toAbsolutePath().normalize().toString());
        OpenProjectResult opened = JwmsServiceProvider.workspace().openProject(saved);
        if (opened instanceof OpenProjectResult.Failure f) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setHeaderText(bundle().getString("alert.open.error.header"));
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        shell.enterWorkspace(opened, stage);
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
}
