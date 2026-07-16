package com.codecrafters.gitjava;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar ... <init|hash-object|cat-file>");
            return;
        }

        Path repoRoot = Path.of(".").toAbsolutePath().normalize();
        GitRepository repository = new GitRepository(repoRoot);

        switch (args[0]) {
            case "init" -> {
                repository.init();
                System.out.println("Initialized git directory");
            }
            case "hash-object" -> {
                if (args.length < 2) {
                    throw new IllegalArgumentException("Expected a file path");
                }
                String sha = repository.hashObject(Path.of(args[1]));
                System.out.println(sha);
            }
            case "cat-file" -> {
                if (args.length < 2) {
                    throw new IllegalArgumentException("Expected an object id");
                }
                System.out.println(repository.catFile(args[1]));
            }
            case "list-objects" -> {
                List<String> objects = repository.listObjects();
                objects.forEach(System.out::println);
            }
            default -> throw new IllegalArgumentException("Unsupported command: " + args[0]);
        }
    }
}
