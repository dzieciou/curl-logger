package com.github.dzieciou.testing.curl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryFolder {

  private final Path dir;

  public TemporaryFolder() throws IOException {
    this.dir = Files.createTempDirectory("temp-");
  }

  public Path createFile() throws IOException {
    return Files.createFile(this.dir.resolve("test-" + Long.toString(System.nanoTime())));
  }

  public void deleteAll() {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.dir)) {
      for (Path fpath : directoryStream) {
        fpath.toFile().delete();
      }
    } catch (IOException ex) {
    }
  }
}
