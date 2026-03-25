package com.github.jbrasileiro.jwms;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaWriterManuscriptApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("JWMS — Java Writer Manuscript");

        FXMLLoader loader =
                new FXMLLoader(JavaWriterManuscriptApplication.class.getResource("MainView.fxml"));
        Parent root = loader.load();
        MainViewController controller = loader.getController();
        controller.setStage(stage);

        stage.setScene(new Scene(root, 960, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
