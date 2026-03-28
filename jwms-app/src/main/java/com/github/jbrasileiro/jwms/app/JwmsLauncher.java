package com.github.jbrasileiro.jwms.app;

import com.github.jbrasileiro.jwms.JavaWriterManuscriptApplication;
import com.github.jbrasileiro.jwms.JwmsServiceProvider;
import com.github.jbrasileiro.jwms.adapter.DefaultManuscriptWorkspaceApi;

/** Composição: instala a API do backend antes de arrancar a UI JavaFX. */
public final class JwmsLauncher {

    private JwmsLauncher() {}

    public static void main(String[] args) {
        JwmsServiceProvider.install(new DefaultManuscriptWorkspaceApi());
        JavaWriterManuscriptApplication.main(args);
    }
}
