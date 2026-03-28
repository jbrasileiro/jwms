package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.i18n.JwmsI18n;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaWriterManuscriptApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle bundle = JwmsI18n.bundle();
        stage.setTitle(bundle.getString("app.title"));

        URL shellUrl = JavaWriterManuscriptApplication.class.getResource("AppShell.fxml");
        FXMLLoader loader = new FXMLLoader(shellUrl, bundle);
        Parent root = loader.load();
        AppShellController shell = loader.getController();
        shell.setStage(stage);
        shell.setBundle(bundle);
        shell.bootstrap();

        Scene scene = new Scene(root, 960, 600);
        URL cssUrl = JavaWriterManuscriptApplication.class.getResource("jwms.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
