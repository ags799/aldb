package aldb;

import java.io.IOException;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;

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
	private Command currentCommand;
  private A4Solution solution;
	private ArrayList<Integer> breakLines;
	private ArrayList<Breakpoint> breakpoints = new ArrayList<Breakpoint>();

  Cli(final Path modulePath) throws Err {
    this.modulePath = modulePath;
    ModuleAndIntegerAndA4Solution moduleAndCommandIndexAndSolution =
        getInitialCliState(modulePath);
    this.module = moduleAndCommandIndexAndSolution.module;
    this.commandIndex = moduleAndCommandIndexAndSolution.integer;
    this.solution = moduleAndCommandIndexAndSolution.solution;
    this.breakLines = new ArrayList<Integer>();
    this.currentCommand = this.module.getAllCommands().get(commandIndex);
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
    this.modulePath = tempModulePath;
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

  /** Read Solution from XML file.
   *
   * Does not work! Doesn't check for correctness of solution.
   */
  /*@asg.cliche.Command
  public final String readSolution(final String pathString)
      throws Err, IOException {
    Path path;
    try {
      path = Paths.get(pathString);
    } catch (InvalidPathException e) {
      return "Invalid path.";
    }
    XMLNode xmlNode = new XMLNode(path.toFile());
    A4Solution tempSolution;
    tempSolution = A4SolutionReader.read(
        module.getAllReachableSigs(), xmlNode);
    if (Solver.isSolution(tempSolution,
                          this.modulePath,
                          this.module,
                          module.getAllCommands().get(commandIndex))) {
      this.solution = tempSolution;
      return "";
    } else {
      return "Supplied solution does not satsify the model.";
    }
  }*/

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
	
	/** Set a breakpoint at the specified line number. */
		@asg.cliche.Command
		public final String breakpoint(final int lineNumber) throws Err {
			Integer ln = new Integer(lineNumber);
			Command newCommand;
		
			if(this.breakLines.contains(ln)){
				return "There is already a breakpoint at that line.";
			}
			if(!this.breakLines.add(ln)){
				return "Error adding breakpoint.";
			}
			if(ln == 1){
				return "Error: breakpoints not allowed at line 1.";
			}
		
			/* Sigs, Asserts, Facts are Exprs, Funcs and Commands are Browsables.
			 * Okay, so here's an idea. We maintain a list of breakpoints and just nullify the 
			 * appropriate clauses at run time. So we're back to nullifying clauses. Except we're not,
			 * because the solver just takes a list of sigs and a command. We need to kill them
			 * here. 
			 * The Command has the formula in the form of an Expr. 
			 * We're doing sig-level granularity. If there's a breakpoint that has that Expr, we 
			 * don't include it when assembling a new command for execution. 
			 * I don't think removing sigs helps at all, it just breaks things
			 * utterly. We should still let the user do it, of course. 
			 * command.change(Expr newFormula) returns a new Command with the new formula.
			 * We just have to find a way to make a new formula. For testing purposes, we can
			 * ExprVar.make(Pos pos, String label, Type type) an ExprVar to match against using
			 * hasvar(ExprVar). We need to find out what's included- I belive it's funcs, asserts,
			 * and facts. 
			 * At runtime: iterate over those Exprs, iterate over breakpoints. For each Expr that 
			 * isn't in a Breakpoint, we add it to a new formula.
			 * Note: preds are both commands and funcs. However, adding something twice shouldn't be
			 * an issue. 
			 */
		
			breakpoints.add(new Breakpoint(lineNumber, module));
			newCommand = currentCommand.change(CommandBuilder.buildCommand(module, breakpoints, module.getAllCommands().get(commandIndex)));
			this.solution = Solver.getSolution(modulePath, module, newCommand);
		
			
			/*Okay, we have the Expr that represents what we want to remove.
			Now we make a new formula without that Expr and change the command to
			that.
			We'll do that at runtime rather than here. All we need is a way to make a
			new formula.
			*/
		
			return "Added breakpoint at line " + lineNumber;
		}
	
		/** Remove all breakpoints.*/
		@asg.cliche.Command
		public final String removeAllBreakpoints() throws Err {
			this.currentCommand = module.getAllCommands().get(this.commandIndex);
			this.solution = Solver.getSolution(modulePath, module, currentCommand);
			this.breakLines.clear();
			this.breakpoints.clear();
			return "All breakpoints removed.";
		}
	
		/** Remove a breakpoint at the specified line number. */
		@asg.cliche.Command
		public final String removeBreakpoint(final int lineNumber){
			// remove breakpoint. I've added a list of line numbers as a class member to keep track.
			Integer ln = new Integer(lineNumber);
			if(!this.breakLines.contains(ln)){
				return "No breakpoint at that line.";
			}
			if(this.breakLines.remove(ln)){
				for(Breakpoint b: breakpoints){
					if(b.getLine() == lineNumber){
						breakpoints.remove(b);
					}
				}
				return "Breakpoint at " + lineNumber + " removed.";
			}else{
				return "Error removing breakpoint.";
			}
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
