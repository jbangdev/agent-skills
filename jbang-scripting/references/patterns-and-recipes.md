# JBang Patterns and Recipes

Common patterns for building real-world tools with JBang.

## CLI Tools with Picocli

The most popular pattern — a full-featured command-line tool with argument parsing, help text, and subcommands:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "mytool", mixinStandardHelpOptions = true, version = "1.0",
         description = "My awesome CLI tool")
class mytool implements Runnable {

    @Parameters(index = "0", description = "Input file")
    private String inputFile;

    @Option(names = {"-o", "--output"}, description = "Output file")
    private String outputFile = "output.txt";

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new mytool()).execute(args);
        System.exit(exitCode);
    }

    public void run() {
        if (verbose) System.err.println("Processing " + inputFile);
        // Your logic here
    }
}
```

Create from template: `jbang init --template=cli mytool.java`

## Web Server with Javalin

A lightweight HTTP server in a single file:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:7.1.0
//DEPS org.slf4j:slf4j-simple:2.0.17
//JAVA 21+

import io.javalin.Javalin;

class server {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        var app = Javalin.create().start(port);
        app.get("/", ctx -> ctx.result("Hello from JBang!"));
        app.get("/api/greet/{name}", ctx ->
            ctx.json(java.util.Map.of("greeting", "Hello " + ctx.pathParam("name"))));
    }
}
```

## REST Client with Jackson

Fetch and parse JSON from an API:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.fasterxml.jackson.core:jackson-databind:2.15.2

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.*;

class apiclient {
    public static void main(String[] args) throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/jbangdev/jbang"))
            .header("Accept", "application/json")
            .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var mapper = new ObjectMapper();
        var tree = mapper.readTree(response.body());
        System.out.println("Stars: " + tree.get("stargazers_count"));
        System.out.println("Description: " + tree.get("description").asText());
    }
}
```

## File Processing

Process files line-by-line with Java streams:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

import java.nio.file.*;
import java.util.stream.*;

class linecount {
    public static void main(String[] args) throws Exception {
        for (String path : args) {
            long lines = Files.lines(Path.of(path)).count();
            long words = Files.lines(Path.of(path))
                .flatMap(l -> java.util.Arrays.stream(l.split("\\s+")))
                .filter(w -> !w.isBlank()).count();
            System.out.printf("%s: %d lines, %d words%n", path, lines, words);
        }
    }
}
```

## Database Access

Quick database queries with JDBC:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.h2database:h2:2.2.224

import java.sql.*;

class dbquery {
    public static void main(String[] args) throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:h2:mem:test");
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE users(id INT PRIMARY KEY, name VARCHAR)");
            stmt.execute("INSERT INTO users VALUES(1, 'Alice'), (2, 'Bob')");

            var rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                System.out.printf("User %d: %s%n", rs.getInt("id"), rs.getString("name"));
            }
        }
    }
}
```

## JSON Processing with Gson

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.google.code.gson:gson:2.11.0

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

class jsonformat {
    public static void main(String[] args) throws Exception {
        String input = args.length > 0
            ? Files.readString(Path.of(args[0]))
            : new String(System.in.readAllBytes());

        var gson = new GsonBuilder().setPrettyPrinting().create();
        var element = JsonParser.parseString(input);
        System.out.println(gson.toJson(element));
    }
}
```

## Quarkus CLI Application

Full Quarkus app in a single file with managed dependencies:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.2.0@pom
//DEPS io.quarkus:quarkus-picocli
//JAVA 17+

import picocli.CommandLine.Command;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Command(name = "qapp", mixinStandardHelpOptions = true)
public class qapp implements Runnable {

    @ConfigProperty(name = "greeting", defaultValue = "Hello")
    String greeting;

    public void run() {
        System.out.println(greeting + " from Quarkus + JBang!");
    }
}
```

Create from template: `jbang init --template=qcli qapp.java`

## Testing with JUnit

Run JUnit tests from a single file:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.junit.jupiter:junit-jupiter:5.10.0
//DEPS org.junit.platform:junit-platform-launcher:1.10.0

import org.junit.jupiter.api.*;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;

import static org.junit.jupiter.api.Assertions.*;

class MyTests {
    @Test void testAddition() { assertEquals(4, 2 + 2); }
    @Test void testString()   { assertTrue("JBang".startsWith("J")); }

    public static void main(String[] args) {
        var launcher = LauncherFactory.create();
        var summary = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summary);
        launcher.execute(LauncherDiscoveryRequestBuilder.request()
            .selectors(org.junit.platform.engine.discovery.DiscoverySelectors
                .selectClass(MyTests.class))
            .build());
        var result = summary.getSummary();
        System.out.printf("Tests: %d, Failures: %d%n",
            result.getTestsFoundCount(), result.getTestsFailedCount());
        if (result.getTestsFailedCount() > 0) System.exit(1);
    }
}
```

## JavaFX GUI

Desktop GUI in a single file (JBang auto-configures module path):

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.openjfx:javafx-controls:21:${os.detected.jfxname}
//DEPS org.openjfx:javafx-graphics:21:${os.detected.jfxname}
//JAVA 17+

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class gui extends Application {
    public void start(Stage stage) {
        var label = new Label("Hello JBang!");
        var button = new Button("Click me");
        button.setOnAction(e -> label.setText("Clicked!"));
        stage.setScene(new Scene(new VBox(10, label, button), 300, 200));
        stage.setTitle("JBang FX");
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
```

## Markdown-Embedded Scripts

Run code blocks from Markdown files:

```bash
jbang mynotebook.md
```

JBang extracts and runs Java code blocks from Markdown.

## Multi-File Scripts

Split larger scripts across files:

**main.java:**
```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES model/Person.java util/Formatter.java
//FILES config.properties

import model.Person;
import util.Formatter;

public class main {
    public static void main(String[] args) {
        var person = new Person(args[0]);
        System.out.println(Formatter.format(person));
    }
}
```

**model/Person.java:**
```java
package model;
public class Person {
    public final String name;
    public Person(String name) { this.name = name; }
}
```

## Native Binary

Compile to a native executable for instant startup:

```bash
jbang --native myapp.java
```

Add native-specific options:

```java
//NATIVE_OPTIONS --no-fallback -O2
//NATIVE_OPTIONS -H:ReflectionConfigurationFiles=reflect.json
```

Requires GraalVM with `native-image` installed.

## Self-Bootstrapping Scripts

Make scripts that install JBang automatically if not present:

```bash
jbang bootstrap@jbangdev myapp.java
```

This rewrites the shebang to auto-install JBang before running.

## Piping and Stdin

Read from stdin for Unix-style piping:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.*;

class upper {
    public static void main(String[] args) throws Exception {
        new BufferedReader(new InputStreamReader(System.in))
            .lines()
            .map(String::toUpperCase)
            .forEach(System.out::println);
    }
}
```

Usage: `cat file.txt | jbang upper.java`

## Running Remote Scripts

Run scripts directly from URLs:

```bash
# From GitHub
jbang https://github.com/jbangdev/jbang-examples/blob/main/examples/helloworld.java

# From a Gist
jbang https://gist.github.com/user/abc123

# From catalog alias
jbang hello@jbangdev
jbang gavsearch@jbangdev hibernate
```

## Running JARs and WARs

JBang can also run pre-built artifacts:

```bash
jbang com.h2database:h2:2.2.224                  # Run by Maven GAV
jbang myapp.jar                                    # Run local JAR
jbang https://example.com/app.war                  # Run WAR file
jbang --deps org.postgresql:postgresql:42.6.0 app.jar  # Add deps to JARs
```

## Flight Recorder for Profiling

```bash
jbang --jfr myapp.java                            # Record to myapp.jfr
jbang --jfr=maxage=24h,filename=out.jfr myapp.java  # Custom options
```

Then open with `jmc` (Java Mission Control) or VisualVM.

## Exporting

When a script outgrows single-file scripting:

```bash
jbang export maven myapp.java    # Full Maven project
jbang export gradle myapp.java   # Full Gradle project
jbang export portable myapp.java # Portable JAR + lib/
```

The exported project preserves all `//DEPS`, `//SOURCES`, `//FILES`, and `//JAVA` directives.
