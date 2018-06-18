package at.jku.ssw.java.bytecode.reducer.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Helper that recursively iterates a directory and deletes
     * its file contents before clearing the directory itself.
     *
     * @param path The path to the directory
     * @return the deleted directory path
     * @throws IOException if the path does not denote a directory
     *                     or IO errors occur
     */
    public static Path delete(Path path) throws IOException {
        if (path == null)
            throw new IllegalArgumentException("Path must not be null");

        return Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Helper method that checks whether the given path - assumed to be a
     * directory - is empty.
     * Uses a stream check instead of a content list that may perform better
     * in large non-empty directories.
     *
     * @param path The path (denoting a director)
     * @return true, if the directory is empty; false if it is not
     * @throws IOException if the path is not a directory or IO access fails
     */
    public static boolean isEmpty(Path path) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * Copies the given files to the given target directory.
     *
     * @param src The source files that have to be copied
     * @param out The destination path
     * @return a stream containing the resulting file copies
     */
    public static Stream<Path> copy(Stream<Path> src, Path out) {
        return src
                .map(p -> {
                    try {
                        return Files.copy(p, out.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }
}
