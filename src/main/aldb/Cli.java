package aldb;

import java.nio.file.Path;

import asg.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

/**
 * Command Line Interface using cliche.
 * Must be public to work with cliche.
 */
public class Cli {
  private Path modulePath;
  private Module module;

  Cli(final Path modulePath) throws Err {
    this.modulePath = modulePath;
    // We expect the module to be compilable. aldb is not meant to help
    // debug compilation issues.
    this.module = Parser.getModuleFromPath(modulePath);
  }

  @asg.cliche.Command
  public final String print(final String expression) throws Err {
    // TODO allow user to select commands
    Command command = module.getAllCommands().get(0);
    A4Solution solution = Solver.getSolution(this.modulePath, module, command);
    return Parser.evaluate(module, solution, expression);
  }
}
