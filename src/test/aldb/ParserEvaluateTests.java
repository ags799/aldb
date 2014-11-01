package aldb;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.hamcrest.*;
import org.junit.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class ParserEvaluateTests {
  @Test
  public final void testShouldFail() throws
      Err, IOException {
    // create test file
    Path modulePath = Paths.get("/tmp/test.als");
    PrintWriter writer = new PrintWriter(modulePath.toFile(), "UTF-8");
    String alloyModule = "module test\n"
                       + "sig A {}\n"
                       + "pred example () {}\n"
                       + "run example for exactly 2 A";
    writer.println(alloyModule);
    writer.close();

    // execute expression
    Module module = Parser.getModuleFromPath(modulePath);
    Command command = module.getAllCommands().get(0);
    A4Solution solution = Solver.getSolution(modulePath, module, command);
    String result = Parser.evaluate(module, solution, "A");

    // delete test file
    try {
      Files.delete(modulePath);
    } catch (NoSuchFileException x) { }

    assertEquals(result, "{A$0, A$1}");
  }
}
