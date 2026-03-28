package com.github.jbrasileiro.jwms.ui;

import java.util.ResourceBundle;
import javafx.stage.FileChooser;

public final class ProjectFileChooserHelper {

    private ProjectFileChooserHelper() {}

    public static void configureOpenProjectDialog(FileChooser chooser, ResourceBundle bundle) {
        chooser.setTitle(bundle.getString("filechooser.manuskript.title"));
        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                bundle.getString("filechooser.open.filter"),
                                "*.jwms", "*.msk"));
    }

    public static void configureNewProjectSaveDialog(FileChooser chooser, ResourceBundle bundle) {
        chooser.setTitle(bundle.getString("filechooser.manuskript.save.title"));
        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                bundle.getString("filechooser.save.filter"), "*.jwms"));
    }
}
