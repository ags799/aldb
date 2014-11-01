package aldb;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.hamcrest.*;
import org.junit.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class ParserEvaluateTests {
  @Test
  public final void test() throws Err, IOException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}\n"
                  + "run example for exactly 2 A";
    Path modulePath = TestFileCreator.create(moduleString);

    // execute expression
    Module module = Parser.getModuleFromPath(modulePath);
    Command command = module.getAllCommands().get(0);
    A4Solution solution = Solver.getSolution(modulePath, module, command);
    String result = Parser.evaluate(module, solution, "A");

    assertEquals(result, "{A$0, A$1}");
  }
}
