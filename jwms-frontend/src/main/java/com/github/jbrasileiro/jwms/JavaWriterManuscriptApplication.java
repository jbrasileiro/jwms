package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.i18n.Enumi18n;
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
		stage.setTitle(bundle.getString(Enumi18n.APP_TILE.getKey()));
		URL resource = JavaWriterManuscriptApplication.class.getResource("MainView.fxml");
		FXMLLoader loader = new FXMLLoader(resource, bundle);
		Parent root = loader.load();
		MainViewController controller = loader.getController();
		controller.setResourceBundle(bundle);
		controller.setStage(stage);

		stage.setScene(new Scene(root, 960, 600));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
