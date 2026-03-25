package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase;
import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase.OpenManuscriptProjectResult;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class MainViewController {

    private final OpenManuscriptProjectUseCase openProject = new OpenManuscriptProjectUseCase();

    @FXML private SplitPane mainSplit;
    @FXML private ListView<String> entryList;
    @FXML private Label statusLabel;
    @FXML private Label xmlRootLabel;

    private Stage stage;

    void setStage(Stage stage) {
        this.stage = stage;
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
        chooser.setTitle("Abrir projeto Manuskript");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Manuskript", "*.msk"));
        var file = chooser.showOpenDialog(ownerStage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        OpenManuscriptProjectResult result = openProject.open(path);
        if (result instanceof OpenManuscriptProjectResult.Failure f) {
            entryList.getItems().clear();
            xmlRootLabel.setText("");
            statusLabel.setText("Falha ao abrir.");
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setHeaderText("Não foi possível abrir o projeto");
            alert.setContentText(String.join("\n", f.errors()));
            alert.showAndWait();
            return;
        }
        if (result instanceof OpenManuscriptProjectResult.Success s) {
            var p = s.project();
            entryList.getItems().setAll(p.getRelativeEntryNames());
            statusLabel.setText(
                    "Ficheiro: "
                            + p.getProjectFile()
                            + "\nFormato: v"
                            + p.getFormatVersion()
                            + (p.isZipContainer() ? " (ZIP)" : " (pasta ao lado do .msk)"));
            xmlRootLabel.setText(
                    p.getSampleXmlRootLocalName()
                            .map(n -> "plots.xml (ou primeiro .xml): elemento raiz = " + n)
                            .orElse("Sem plots.xml / .xml para amostra."));
        }
    }
}
