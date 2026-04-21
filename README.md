# larer

Private **mshrai/larer** publishes a small Java library to **GitHub Packages** (Maven). Pushes to `main` run the workflow in `.github/workflows/publish.yml` and deploy the artifact.

## Published coordinates

| Field        | Value              |
|-------------|--------------------|
| Group ID    | `com.mshrai`       |
| Artifact ID | `larer`            |
| Version     | `0.1.0` (bump in `pom.xml` for new releases) |
| Registry URL | `https://maven.pkg.github.com/mshrai/larer` |

Maven dependency:

```xml
<dependency>
    <groupId>com.mshrai</groupId>
    <artifactId>larer</artifactId>
    <version>0.1.0</version>
</dependency>
```

Example usage:

```java
import com.mshrai.larer.DummyGreeter;

String s = DummyGreeter.greet("team");
```

---

## Using this package in another project (Spring Boot)

### 1. Add the GitHub Packages repository

In the consumer project’s **`pom.xml`**, declare the repository that hosts this package (same URL for any dependency published from **mshrai/larer**):

```xml
<project>
    <!-- ... -->

    <repositories>
        <repository>
            <id>github-larer</id>
            <url>https://maven.pkg.github.com/mshrai/larer</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.mshrai</groupId>
            <artifactId>larer</artifactId>
            <version>0.1.0</version>
        </dependency>
    </dependencies>
</project>
```

Use the **same** `<id>` (`github-larer` here) as the `<server><id>` in your Maven settings (next section).

### 2. Authenticate Maven (required for a private package)

GitHub Packages needs a token with **`read:packages`** (and usually **`read:user`** with classic PATs).

**On your machine:** create or edit `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github-larer</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

Replace `YOUR_GITHUB_USERNAME` and `YOUR_PERSONAL_ACCESS_TOKEN`. The `<id>` must match the repository `<id>` in the consumer `pom.xml`.

**Org SSO:** if **mshrai** uses SAML SSO, authorize the PAT for the organization in GitHub (Settings → Developer settings → PAT → Configure SSO).

**Cross-repo access:** if the consuming repo is also under **mshrai**, ensure GitHub Actions is allowed to read this package: in the **larer** package settings on GitHub, grant the consuming repository (or the org) access to the package, or use a machine-user PAT stored as a repository secret (see below).

---

## Containerized consumer (Docker + Spring Boot)

The running container only needs the **built JAR** and a JRE. It does **not** need GitHub credentials. Credentials are only required when **Maven resolves** `com.mshrai:larer` (during `mvn package`), so plan where that step runs.

### Recommended: build the JAR in CI, then a small runtime image

1. In the **application** repository, use GitHub Actions (or another CI system): check out the app, configure Maven with access to GitHub Packages (see below), run `mvn --batch-mode package`.
2. Use a runtime-only `Dockerfile` that copies the artifact produced in the previous step (no Maven, no token in the image):

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
USER 1000:1000
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Push the image from the same workflow after `mvn package`, or use `docker/build-push-action` with the JAR already in `target/`.

**Maven auth in the consumer workflow** (same idea as this repo’s publish job: server id must match your consumer `pom.xml` repository id, e.g. `github-larer`):

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "17"
    cache: maven
    server-id: github-larer
    server-username: ${{ github.actor }}
    server-password: ${{ secrets.GITHUB_TOKEN }}
```

If `GITHUB_TOKEN` cannot download this org-private package from another repo, create a **fine-grained or classic PAT** with **`read:packages`** (and SSO authorization for **mshrai**), store it as e.g. `MAVEN_GITHUB_TOKEN`, and use that for `server-password`, or grant the consuming workflow access to the package under **mshrai/larer → Package settings → Manage Actions access**.

More options: [setup-java](https://github.com/actions/setup-java).

### Optional: run `mvn` inside Docker

If the image build must run Maven (multi-stage `Dockerfile`), the build stage needs a valid `~/.m2/settings.xml` with `read:packages`. Prefer **[Docker BuildKit secrets](https://docs.docker.com/build/building/secrets/)** so the token is not baked into layers, or inject credentials only in CI and never commit them. Avoid storing PATs in `COPY`’d files in the repository.

---

## Summary

| Scenario | What you need |
|----------|----------------|
| Local `mvn` | `~/.m2/settings.xml` + PAT with `read:packages` |
| Spring Boot `pom.xml` | `<repository>` pointing at `https://maven.pkg.github.com/mshrai/larer` + matching `<server><id>` |
| Docker build | Maven + credentials at **build** time (secret or CI-generated `settings.xml`); runtime image only needs the JAR |
| Publishing **this** repo | Push to `main`; workflow uses `GITHUB_TOKEN` with `packages: write` |

Official reference: [Working with the Apache Maven registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry).
