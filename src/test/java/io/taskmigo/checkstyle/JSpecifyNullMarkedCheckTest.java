package io.taskmigo.checkstyle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JSpecifyNullMarkedCheckTest {

    @Test
    @DisplayName("Ignores ordinary Java source files")
    void ignoresOrdinaryJavaSourceFiles(@TempDir Path tempDir) throws Exception {
        Path source = Files.writeString(
            tempDir.resolve("Example.java"),
            """
            package io.taskmigo.example;

            final class Example {}
            """,
            UTF_8
        );

        assertEquals(0, violations(source));
    }

    @Test
    @DisplayName("Accepts package-info.java with imported JSpecify NullMarked")
    void acceptsImportedJSpecifyNullMarked(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            """
            @NullMarked
            package io.taskmigo.example;

            import org.jspecify.annotations.NullMarked;
            """,
            UTF_8
        );

        assertEquals(0, violations(packageInfo));
    }

    @Test
    @DisplayName("Accepts package-info.java with fully qualified JSpecify NullMarked")
    void acceptsFullyQualifiedJSpecifyNullMarked(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            """
            @org.jspecify.annotations.NullMarked
            package io.taskmigo.example;
            """,
            UTF_8
        );

        assertEquals(0, violations(packageInfo));
    }

    @Test
    @DisplayName("Accepts package-info.java with a JSpecify annotation wildcard import")
    void acceptsJSpecifyWildcardImport(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            """
            @NullMarked
            package io.taskmigo.example;

            import org.jspecify.annotations.*;
            """,
            UTF_8
        );

        assertEquals(0, violations(packageInfo));
    }

    @Test
    @DisplayName("Rejects package-info.java without NullMarked")
    void rejectsMissingNullMarked(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            "package io.taskmigo.example;\n",
            UTF_8
        );

        assertEquals(1, violations(packageInfo));
    }

    @Test
    @DisplayName("Rejects NullMarked imported from a non-JSpecify package")
    void rejectsNonJSpecifyNullMarked(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            """
            @NullMarked
            package io.taskmigo.example;

            import example.annotations.NullMarked;
            """,
            UTF_8
        );

        assertEquals(1, violations(packageInfo));
    }

    @Test
    @DisplayName("Rejects a non-JSpecify explicit NullMarked import even with a JSpecify wildcard")
    void rejectsNonJSpecifyExplicitNullMarkedWithJSpecifyWildcard(@TempDir Path tempDir) throws Exception {
        Path packageInfo = Files.writeString(
            tempDir.resolve("package-info.java"),
            """
            @NullMarked
            package io.taskmigo.example;

            import example.annotations.NullMarked;
            import org.jspecify.annotations.*;
            """,
            UTF_8
        );

        assertEquals(1, violations(packageInfo));
    }

    private static int violations(Path source) throws Exception {
        // Use the short module name to verify checkstyle_packages.xml is packaged correctly.
        DefaultConfiguration check = new DefaultConfiguration("JSpecifyNullMarked");
        DefaultConfiguration treeWalker = new DefaultConfiguration(TreeWalker.class.getName());
        treeWalker.addChild(check);

        DefaultConfiguration root = new DefaultConfiguration("configuration");
        root.addProperty("charset", UTF_8.name());
        root.addChild(treeWalker);

        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            checker.configure(root);
            return checker.process(List.of(source.toFile()));
        } finally {
            checker.destroy();
        }
    }
}
