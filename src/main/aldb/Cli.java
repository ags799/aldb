package aldb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;

import asg.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;

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
    ModuleAndIntegerAndA4Solution moduleAndCommandIndexAndSolution =
        getInitialCliState(modulePath);
    this.module = moduleAndCommandIndexAndSolution.module;
    this.commandIndex = moduleAndCommandIndexAndSolution.integer;
    this.solution = moduleAndCommandIndexAndSolution.solution;
  }

  /** Evaluate an expression. */
  @asg.cliche.Command
  public final String print(final String expression) throws Err {
    return Parser.evaluate(module, solution, expression);
  }

  /** Load an Alloy module at the specified path. */
  @asg.cliche.Command
  public final String file(final String pathString) throws Err {
    Path tempModulePath;
    try {
      tempModulePath = Paths.get(pathString);
    } catch (InvalidPathException e) {
      return "Invalid path.";
    }
    ModuleAndIntegerAndA4Solution moduleAndCommandIndexAndSolution =
        getInitialCliState(tempModulePath);
    this.path = tempModulePath;
    this.module = moduleAndCommandIndexAndSolution.module;
    this.commandIndex = moduleAndCommandIndexAndSolution.integer;
    this.solution = moduleAndCommandIndexAndSolution.solution;
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

  /** Write currently selected Solution to XML file. */
  @asg.cliche.Command
  public final String writeSolution(final String pathString) throws Err {
    if (solution == null) {
      return "No solution to write.";
    } else {
      Path path;
      try {
        path = Paths.get(pathString);
      } catch (InvalidPathException e) {
        return "Invalid path.";
      }
      solution.writeXML(path.toString());
      return "";
    }
  }

  /** Read Solution from XML file. */
  @asg.cliche.Command
  public final String readSolution(final String pathString)
      throws IOException {
    Path path;
    try {
      path = Paths.get(pathString);
    } catch (InvalidPathException e) {
      return "Invalid path.";
    }
    XMLNode xmlNode = new XMLNode(path.toFile());
    A4Solution tempSolution;
    try {
      tempSolution = A4SolutionReader.read(
          module.getAllReachableSigs(), xmlNode);
    } catch (Err e) {
      return e.toString();
    }
    this.solution = tempSolution;
    return "";
  }

  /** Get the next Solution, if there is one. */
  @asg.cliche.Command
  public final String nextSolution() throws Err {
    if (solution == null) {
      return "There are no solutions.";
    } else if (!solution.isIncremental()) {
      return "This is not an incremental solution. "
           + "Try reloading your module with 'file'.";
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

  private ModuleAndIntegerAndA4Solution getInitialCliState(
      final Path modulePath) throws Err {
    Module module = Parser.getModuleFromPath(modulePath);
    if (module.getAllCommands().size() < 1) {
      return new ModuleAndIntegerAndA4Solution(module, null, null);
    } else {
      Integer commandIndex = 0;
      A4Solution solution = Solver.getSolution(
          modulePath, module, module.getAllCommands().get(commandIndex));
      return new ModuleAndIntegerAndA4Solution(module, commandIndex, solution);
    }
  }

  private class ModuleAndIntegerAndA4Solution {
    Module module;
    Integer integer;
    A4Solution solution;

    ModuleAndIntegerAndA4Solution(
        final Module module,
        final Integer integer,
        final A4Solution solution) {
      this.module = module;
      this.integer = integer;
      this.solution = solution;
    }
  }
}
