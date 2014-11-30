package aldb;

import java.nio.file.Path;

import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;

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
	private ArrayList<Integer> breakLines;

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

  /** Load an Alloy module at the specified path. */
  @asg.cliche.Command
  public final String file(final String pathString) throws Err {
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
	
	/** Set a breakpoint at the specified line number. */
		@asg.cliche.Command
		public final String breakpoint(final int lineNumber) {
			Integer ln = new Integer(lineNumber);
			Expr match, newFormula; 
			Command newCommand, currentCommand = module.getAllCommands().get(commandIndex);
		
			if(this.breakLines.contains(ln)){
				return "There is already a breakpoint at that line.";
			}
			if(!this.breakLines.add(ln)){
				return "Error adding breakpoint.";
			}
			if(ln == 1){
				return "Error: breakpoints not allowed at line 1.";
			}
		
			SafeList<Sig> allSigs = module.getAllSigs();
			SafeList<Func> allFuncs = module.getAllFunc();
			ConstList<Pair<String,Expr>> allAsserts = module.getAllAssertions();
			SafeList<Pair<String,Expr>> allFacts = module.getAllFacts();
			ConstList<Command> allCommands = module.getAllCommands();
		
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
		
			System.out.println("Sigs:");
			for(Sig sig: allSigs){
				if(ln >= sig.pos.y && ln <= sig.pos.y2){
					System.out.println("Found the spot- it's in sig " + sig.label);
					match = sig;
				}
				System.out.println(sig.label);
			}
		
			System.out.println("Funcs:");
			for(Func func: allFuncs){
				if(ln >= func.pos.y && ln <= func.pos.y2){
					System.out.println("Found the spot- it's in func " + func.label);
					match = func.getBody();
				}
				System.out.println(func.label);
			}
		
			System.out.println("Asserts:");
			for(Pair<String,Expr> pair: allAsserts){
				if(ln >= pair.b.pos.y && ln <= pair.b.pos.y2){
					System.out.println("Found the spot- it's in Assert " + pair.a);
					match = pair.b;
				}
				System.out.println(pair.a);
			}
		
			System.out.println("Facts:");
			for(Pair<String,Expr> pair: allFacts){
				if(ln >= pair.b.pos.y && ln <= pair.b.pos.y2){
					System.out.println("Found the spot- it's in Fact " + pair.a);
					match = pair.b;
				}
				System.out.println(pair.a);
			}
		
			System.out.println("Commands:");
			for(Command c: allCommands){
				if(ln >= c.pos.y && ln <= c.pos.y2){
					System.out.println("Found the spot- it's in Command " + c.label);
					match = c.formula;
				}
				System.out.println(c.label + " has formula");
				System.out.println(c.formula.toString());
			}
		
			/*Okay, we have the Expr that represents what we want to remove.
			Now we make a new formula, without that Expr and change the command to
			that.
			We'll do that at runtime rather than here. All we need is a way to make a
			new formula.
			*/
		
		
			return null;
		}
	
		/** Remove all breakpoints.*/
		@asg.cliche.Command
		public final String removeAllBreakpoints(){
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
}
