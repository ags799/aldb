package aldb;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;

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
  Integer commandIndex;
  private A4Solution solution;

  Cli(final Path modulePath) throws Err {
    this.modulePath = modulePath;
    // TODO handle bad path, bad module
    this.module = Parser.getModuleFromPath(modulePath);

    if (this.module.getAllCommands().size() < 1) {
      this.commandIndex = null;
      this.solution = null;
    } else {
      this.commandIndex = 0;
      this.solution = Solver.getSolution(
          this.modulePath, this.module,
          this.module.getAllCommands().get(this.commandIndex));
    }
  }

  /** Evaluate an expression. */
  @asg.cliche.Command
  public final String print(final String expression) throws Err {
    return Parser.evaluate(module, solution, expression);
  }

  /** Reload the existing Alloy module. */
  @asg.cliche.Command
  public final String reload() throws Err {
    this.module = Parser.getModuleFromPath(modulePath);
    return "";
  }

  /** Load an Alloy module at the specified path. */
  @asg.cliche.Command
  public final String reload(final String pathString) throws Err {
    Path tempModulePath;
    Module tempModule;
    try {
      tempModulePath = Paths.get(pathString);
      tempModule = Parser.getModuleFromPath(tempModulePath);
    } catch (InvalidPathException e) {
      return "Invalid path.";
    } catch (Err e) {
      return "Alloy module does not compile. Here's the error message:\n"
           + e.toString();
    }
    this.modulePath = tempModulePath;
    this.module = tempModule;
    return "";
  }

  /** Print currently selected Solution. */
  @asg.cliche.Command
  public final String printSolution() throws Err {
    if (solution == null) {
      return "";
    } else {
      return solution.toString();
    }
  }

  /** Get the next Solution, if there is one. */
  @asg.cliche.Command
  public final String nextSolution() throws Err {
    if (solution == null) {
      return "There are no solutions.";
    } else if (solution.next() == solution) {
      solution = Solver.getSolution(
          modulePath, module,
          module.getAllCommands().get(commandIndex));
      return "There are no more solutions. Using first solution.";
    } else {
      solution = solution.next();
      return "";
    }
  }

  /** Show selected command. */
  @asg.cliche.Command
  public final String printCommand() throws Err {
    if (this.commandIndex == null) {
      return "No command index set.";
    } else {
      return this.commandIndex
           + ": "
           + this.module.getAllCommands().get(this.commandIndex);
    }
  }

  /** Set command by index. */
  @asg.cliche.Command
  public final String setCommand(final int commandIndex) throws Err {
    if (commandIndex >= this.module.getAllCommands().size()
        || commandIndex < 0) {
      return "Index out of range.";
    } else {
      this.commandIndex = commandIndex;
      return "New command is " + printCommand();
    }
  }

  /** List commands available for this.module. */
  @asg.cliche.Command
  public final String listCommands() throws Err {
    // TODO what if there are no commands
    StringBuilder sb = new StringBuilder();
    int i = 0;
    String prefix = "";
    for (Command command : module.getAllCommands()) {
      sb.append(prefix + i + ": " + command);
      prefix = "\n";
      i += 1;
    }
    return sb.toString();
  }
}
