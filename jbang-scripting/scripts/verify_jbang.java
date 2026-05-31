///usr/bin/env jbang "$0" "$@" ; exit $?
//DESCRIPTION Verifies that JBang is correctly installed and working
//JAVA 11+
//DEPS com.google.code.gson:gson:2.11.0

import com.google.gson.Gson;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * JBang Verification Script
 *
 * Run with: jbang verify_jbang.java
 *
 * This script checks:
 * 1. JBang is installed and on PATH
 * 2. Java version is available
 * 3. Dependency resolution works (this script has a //DEPS directive)
 * 4. Basic Java features work correctly
 */
class verify_jbang {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       JBang Verification Script         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        // Check 1: JBang is running (if we got here, it works!)
        check("JBang execution", true,
              "This script is running via JBang");

        // Check 2: Java version
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        check("Java runtime", javaVersion != null,
              "Java " + javaVersion + " (" + javaVendor + ")");

        // Check 3: Java version >= 11
        int majorVersion = getMajorVersion(javaVersion);
        check("Java version >= 11", majorVersion >= 11,
              "Major version: " + majorVersion);

        // Check 4: Dependency resolution (Gson was loaded via //DEPS)
        try {
            var gson = new Gson();
            var json = gson.toJson(Map.of("status", "ok"));
            check("Dependency resolution (Gson)", json.contains("ok"),
                  "Gson loaded and working — //DEPS directive resolved correctly");
        } catch (Exception e) {
            check("Dependency resolution (Gson)", false,
                  "Failed: " + e.getMessage());
        }

        // Check 5: JSON round-trip with the dependency
        try {
            var gson = new Gson();
            var data = new LinkedHashMap<String, Object>();
            data.put("tool", "jbang");
            data.put("working", true);
            data.put("checks_passed", passed);
            String json = gson.toJson(data);
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = gson.fromJson(json, Map.class);
            check("JSON round-trip", "jbang".equals(parsed.get("tool")),
                  "Serialized and deserialized successfully");
        } catch (Exception e) {
            check("JSON round-trip", false, "Failed: " + e.getMessage());
        }

        // Check 6: System properties
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        check("System properties", osName != null,
              osName + " / " + osArch);

        // Check 7: File I/O
        try {
            var tmpFile = java.nio.file.Files.createTempFile("jbang-verify-", ".txt");
            java.nio.file.Files.writeString(tmpFile, "jbang works!");
            String content = java.nio.file.Files.readString(tmpFile);
            java.nio.file.Files.delete(tmpFile);
            check("File I/O", "jbang works!".equals(content),
                  "Temp file created, written, read, and deleted");
        } catch (Exception e) {
            check("File I/O", false, "Failed: " + e.getMessage());
        }

        // Check 8: HTTP client (Java 11+)
        try {
            var client = java.net.http.HttpClient.newHttpClient();
            check("HTTP client available", client != null,
                  "java.net.http.HttpClient ready for API calls");
        } catch (Exception e) {
            check("HTTP client available", false, "Failed: " + e.getMessage());
        }

        // Summary
        System.out.println();
        System.out.println("════════════════════════════════════════════");
        System.out.printf("  Results: %d passed, %d failed, %d total%n",
                          passed, failed, passed + failed);
        System.out.println("════════════════════════════════════════════");

        if (failed == 0) {
            System.out.println();
            System.out.println("  ✅ JBang is working correctly!");
            System.out.println();
            System.out.println("  Try these next:");
            System.out.println("    jbang init --template=cli myapp.java");
            System.out.println("    jbang myapp.java --help");
            System.out.println("    jbang edit myapp.java");
            System.out.println();
        } else {
            System.out.println();
            System.out.println("  ❌ Some checks failed. See details above.");
            System.out.println();
            System.exit(1);
        }
    }

    static void check(String name, boolean passed_check, String detail) {
        String icon = passed_check ? "✅" : "❌";
        if (passed_check) passed++; else failed++;
        System.out.printf("  %s %s%n", icon, name);
        System.out.printf("     %s%n", detail);
    }

    static int getMajorVersion(String version) {
        if (version == null) return 0;
        // Handle versions like "17.0.1", "11.0.2", "1.8.0_302"
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        int dot = version.indexOf('.');
        if (dot > 0) version = version.substring(0, dot);
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
