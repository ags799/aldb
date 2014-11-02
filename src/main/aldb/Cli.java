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

  Cli(final Path modulePath) throws Err {
    this.modulePath = modulePath;
    // We expect the module to be compilable. aldb is not meant to help
    // debug compilation issues.
    this.module = Parser.getModuleFromPath(modulePath);
    if (this.module.getAllCommands().size() < 1) {
      this.commandIndex = null;
    } else {
      this.commandIndex = 0;
    }
  }

  /** Evaluate an expression on the first solution of this.module. */
  @asg.cliche.Command
  public final String print(final String expression) throws Err {
    Command command = module.getAllCommands().get(this.commandIndex);
    // TODO allow user to select a solution
    A4Solution solution = Solver.getSolution(this.modulePath, module, command);
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

  /** Set command. */
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
