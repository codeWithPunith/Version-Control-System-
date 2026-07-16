package com.codecrafters.gitjava;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitRepositoryTest {
    @Test
    void initCreatesGitDirectory(@TempDir Path tempDir) throws Exception {
        GitRepository repository = new GitRepository(tempDir);
        repository.init();

        assertTrue(Files.exists(tempDir.resolve(".git")));
        assertTrue(Files.exists(tempDir.resolve(".git/HEAD")));
    }

    @Test
    void hashObjectWritesAReadableBlob(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("hello.txt");
        Files.writeString(file, "hello from java");

        GitRepository repository = new GitRepository(tempDir);
        repository.init();
        String sha = repository.hashObject(file);

        assertTrue(sha.matches("[0-9a-f]{40}"));
    }

    @Test
    void inflateThrowsIOExceptionForInvalidCompressedData() {
        assertThrows(IOException.class, () -> ZlibHelper.inflate(new byte[] {1, 2, 3}));
    }
}
