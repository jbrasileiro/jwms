package com.github.jbrasileiro.jwms;

import com.github.jbrasileiro.jwms.api.ManuscriptWorkspaceApi;
import java.util.Objects;

/**
 * Composição mínima para FXML: o {@code main} do módulo {@code jwms-app} chama {@link #install}
 * antes de {@code Application.launch}; os controladores obtêm a API aqui.
 */
public final class JwmsServiceProvider {

    private static volatile ManuscriptWorkspaceApi workspaceApi;

    private JwmsServiceProvider() {}

    public static void install(ManuscriptWorkspaceApi api) {
        workspaceApi = Objects.requireNonNull(api, "api");
    }

    public static ManuscriptWorkspaceApi workspace() {
        if (workspaceApi == null) {
            throw new IllegalStateException("JwmsServiceProvider.install() was not called before UI load");
        }
        return workspaceApi;
    }
}
