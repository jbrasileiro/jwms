# jwms

JWMS (Java Writer Manuscript) — JavaFX com módulos `jwms-backend` e `jwms-frontend`.

## Requisitos

- JDK **21** (com `jpackage` para o perfil `dist`).

## Executar (desenvolvimento)

Na **raiz** do repositório. O `-am` (*also make*) inclui o `jwms-backend` no mesmo *build*; **sem `-am`**, o Maven só vê o `jwms-frontend` e tenta ir buscar `jwms-backend` ao repositório local (`.m2`) — onde ainda não está, daí o erro.

```bash
./mvnw -pl jwms-frontend -am javafx:run
```

Windows: `mvnw.cmd -pl jwms-frontend -am javafx:run`

O POM **pai** também declara o `javafx-maven-plugin` com `skip=true` (e `mainClass` preenchido) para o `javafx:run` não falhar no módulo agregador `jwms` nem no `jwms-backend`; só o `jwms-frontend` usa `skip=false`.

Classe principal: `com.github.jbrasileiro.jwms.JavaWriterManuscriptApplication` (propriedade `jwms.main.class` no POM raiz).

**Só a partir da pasta `jwms-frontend`** (com o backend já instalado no `.m2`):

```bash
cd jwms-frontend
../mvnw javafx:run
```

Para isso, instale antes na raiz: `mvnw install` (ou `mvnw -pl jwms-backend install`).

### Erro: `Could not find artifact ... jwms-backend`

Use na raiz: `mvnw -pl jwms-frontend -am ...`, ou `mvnw install` na raiz e depois construa só o frontend com o `jwms-backend` já no `.m2`.

## Empacotar (jpackage, um SO por build)

```bash
./mvnw -Pdist -pl jwms-frontend -am package
```

Saída: `jwms-frontend/target/jpackage-app/JWMS/`

## CI (3 sistemas)

Workflow `.github/workflows/package.yml`: artefactos `jwms-ubuntu-latest`, `jwms-windows-latest`, `jwms-macos-latest`.

## Docker (só build Linux)

```bash
docker build -f Dockerfile.packaging -t jwms-pack .
id=$(docker create jwms-pack)
docker cp "$id":/w/jwms-frontend/target/jpackage-app ./out
docker rm -v "$id"
```
