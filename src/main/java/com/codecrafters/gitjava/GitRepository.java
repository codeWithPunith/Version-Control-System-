package com.codecrafters.gitjava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public class GitRepository {
    private final Path root;

    public GitRepository(Path root) {
        this.root = root;
    }

    public void init() throws IOException {
        Path gitDir = root.resolve(".git");
        Files.createDirectories(gitDir.resolve("objects"));
        Files.createDirectories(gitDir.resolve("refs/heads"));
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public String hashObject(Path filePath) throws IOException {
        byte[] content = Files.readAllBytes(filePath);
        String header = "blob " + content.length + "\0";
        byte[] fullContent = combine(header.getBytes(), content);
        String sha = sha1Hex(fullContent);
        writeObject(sha, fullContent);
        return sha;
    }

    public String catFile(String objectId) throws IOException {
        Path objectPath = resolveObjectPath(objectId);
        byte[] compressed = Files.readAllBytes(objectPath);
        byte[] decompressed = ZlibHelper.inflate(compressed);
        int nullIndex = indexOfNull(decompressed);
        if (nullIndex < 0) {
            throw new IOException("Invalid object payload");
        }
        return new String(decompressed, nullIndex + 1, decompressed.length - nullIndex - 1, java.nio.charset.StandardCharsets.UTF_8);
    }

    public void writeObject(String objectId, byte[] payload) throws IOException {
        Path objectDir = root.resolve(".git/objects").resolve(objectId.substring(0, 2));
        Files.createDirectories(objectDir);
        Path objectPath = objectDir.resolve(objectId.substring(2));
        if (Files.exists(objectPath)) {
            return;
        }
        byte[] compressed = ZlibHelper.deflate(payload);
        Files.write(objectPath, compressed);
    }

    public Path resolveObjectPath(String objectId) {
        return root.resolve(".git/objects").resolve(objectId.substring(0, 2)).resolve(objectId.substring(2));
    }

    private static byte[] combine(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static int indexOfNull(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    private static String sha1Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }

    public List<String> listObjects() throws IOException {
        Path objectsDir = root.resolve(".git/objects");
        if (!Files.exists(objectsDir)) {
            return List.of();
        }
        return Files.walk(objectsDir)
                .filter(Files::isRegularFile)
                .map(path -> path.getParent().getFileName().toString() + path.getFileName().toString())
                .collect(Collectors.toList());
    }
}
