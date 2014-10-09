package aldb;

import static aldb.Parser.parseInstance;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.hamcrest.*;
import org.junit.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

public class ParserParseInstanceTests {
  @Test
  public final void testShouldFail() throws Err {
    // create test file
    Path testModulePath = Paths.get("/tmp/test.als");
    // TODO continue using Path
    PrintWriter writer = new PrintWriter(testModulePath, "UTF-8");
    String alloyModule =
        "module test\n" +
        "pred example () {}\n" +
        "run example";
    writer.println(alloyModule);
    writer.close();

    // execute expression
    Expr result = parseInstance(
        CompUtil.parseEverything_fromFile(null, null, testModulePath), "");
    System.out.println(result);

    // delete test file
    try {
      Files.delete(testModulePath);
    } catch (NoSuchFileException x) {}

    assertTrue(false);
  }
}
