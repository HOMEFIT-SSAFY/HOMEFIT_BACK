package com.happyhome.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DaoPersistenceStyleTest {

    @Test
    void productionDaosDoNotUseJdbcTemplate() throws IOException {
        Path sourceDirectory = Path.of("src/main/java/com/happyhome");

        try (var files = Files.walk(sourceDirectory)) {
            assertThat(files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isPersistenceClass)
                    .filter(path -> fileContains(path, "JdbcTemplate"))
                    .map(Path::toString)
                    .toList())
                    .isEmpty();
        }
    }

    private boolean isPersistenceClass(Path path) {
        String normalizedPath = path.toString().replace('\\', '/');
        String fileName = path.getFileName().toString();
        return normalizedPath.contains("/dao/")
                || normalizedPath.contains("/mapper/")
                || fileName.endsWith("Dao.java")
                || fileName.endsWith("Mapper.java");
    }

    private boolean fileContains(Path path, String text) {
        try {
            return Files.readString(path).contains(text);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }
}
