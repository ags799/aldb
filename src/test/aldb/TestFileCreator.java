package aldb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

final class TestFileCreator {
  /** Creates a file from given String and returns Path to that file. */
  static Path create(final String contents)
      throws FileNotFoundException, UnsupportedEncodingException {
    Path path = Paths.get("/tmp/test.txt");
    File file = path.toFile();
    file.deleteOnExit();
    PrintWriter writer = new PrintWriter(file, "UTF-8");
    writer.println(contents);
    writer.close();
    return path;
  }

  private TestFileCreator() {}
}
