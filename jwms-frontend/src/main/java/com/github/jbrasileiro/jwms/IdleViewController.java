package com.github.jbrasileiro.jwms;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;

/** Vista inicial: dois painéis vazios. */
public final class IdleViewController {

    @FXML private SplitPane rootSplit;

    @FXML
    private void initialize() {
        if (rootSplit != null) {
            Platform.runLater(() -> rootSplit.setDividerPositions(0.5));
        }
    }
}
