package com.github.jbrasileiro.jwms;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;

/** Ecrã Editor: lista de entradas e área de edição (placeholder). */
public final class EditorScreenController {

    @FXML private SplitPane mainSplit;
    @FXML private ListView<String> entryList;

    @FXML
    private void initialize() {
        Platform.runLater(() -> mainSplit.setDividerPositions(0.55));
    }

    void clear() {
        entryList.getItems().clear();
    }

    void setEntryNames(List<String> names) {
        entryList.getItems().setAll(names);
    }

    void restoreDivider() {
        Platform.runLater(() -> mainSplit.setDividerPositions(0.55));
    }
}
