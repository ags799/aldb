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
		
			/*We need to define behavior for each of these cases. What happens in the 
			 * case of a breakpoint in each kind of statement? Of course, all of this 
			 * is academic if we can't affect the tree. Equally so if we can't access
			 * the expression itself. 
			 * Sigs, Asserts, Facts are Exprs, Funcs and Commands are Browsables.
			 * As to that issue: the A4Solution has a map from Exprs to Expressions
			 * (the Kodkod expression). We can get it with solution.a2k(). 
			 * For Sigs: solution.a2k(sig) returns an Expression.
			 * That also works for Exprs- but there's actually a todo in the code for
			 * that method, so it may not work the best.
			 * Commands are the sticking point. We may be able to do it with 
			 * solution.s2k(command.label), s2k maps strings to Expressions.
			 * Issue: these mehtods are not public. They're package-scope.
			 * Resolved through decision to change the alloy code. 
			 * Those methods are now public.
			 * Issue: we have access. Now, can we modify in a meaningful way?
			 * May be resolved through changing the code. 
			 * solution.solve(A4Reporter, Command, Simplifier, boolean tryBookExamples) 
			 * seems to be our best bet. We need to alter the formulas, set solution.solved
			 * to false, and then run solve to get a new A4Solution. 
			 * Seem Andrew already figured that much out in Solver. We can run 
			 * TranslateAlloyToKodkod.execute_command(null, sigs, cmd, options) to solve
			 * a module. However, execute_command only takes in the sigs- what happened to
			 * everything else? There's a thing that finds the scope of it all. In this
			 * context, however, "scope" seems to only mean the bitwidth, min/max values,
			 * and how many things it's run for. Kind of seems to be our best bet, though.
			 * Okay, so here's an idea. We maintain a list of breakpoints and just nullify the 
			 * appropriate clauses at run time. ...Which is kind of what I've been meant to be
			 * doing all along. Crap. So we're back to nullifying clauses. Except we're not,
			 * because the solver just takes a list of sigs and a command. We need to kill them
			 * here. Nothing but sigs seem to be touched, though. 
			 * The Command has the formula! In the form of an expression. And we still have
			 * nothing on tracking down individual clauses. We can get the enclosing thing, but
			 * not the clause itself. 
			 * Okay. We're now doing sig-level granularity. I'm thinking a map from things to 
			 * whether or not to use them. So now we track the map from the sig or whatever to
			 * the clause, and find that (somehow) in the formula in the command and then make a new
			 * command for execution. I don't think removing sigs helps at all, it just breaks things
			 * utterly. We should still let the user do it, of course. But in that case we just 
			 * remove it from the list of sigs upon execution. Removing breakpoints now consists of 
			 * re-computing the original formula and re-adding the other breakpoints.
			 * Next thing, then, is to find a way to match a thing in the formula, then remove that thing.
			 * Ooh, a handy thing: command.change(Expr newFormula) returns a new Command with the new
			 * formula. We just have to find a way to modify the formula. Or, better, make a new formula.
			 * We can ExprVar.make(Pos pos, String label, Type type) a thing to match against using
			 * hasVar(ExprVar), maybe.
			 * I'm an idiot. The a2k method gives you an Expression, not an Expr. Actually going to blame 
			 * the code on this. BUT it doesn't actually matter- we don't need it anymore.
			 * We just need a way to make a formula and a way to find a new formula. We'll change the 
			 * command at runtime. 
			 * At runtime: iterate over breakpoints or formula elements, add or remove as necessary to
			 * the selected command's formula. Probably easier to add. We can get a list of subnodes and
			 * iterate through them.  
			 * Alternately, we could just create a new A4Solution altogether by making
			 * the constructor public. 
			 * A4Solution(String originalCommand, int bitwidth, int maxseq, Set<String> stringAtoms, 
			 * 	Collection<String> atoms, final A4Reporter rep, A4Options opt, int expected);
			 * The first three arguments of that are preserved, as is opt, but the others
			 * are more difficult to reconstruct. I think the first approach may be more 
			 * viable. 
			 * Note: preds are both commands and funcs. 
			 */
		
			//module.showAsTree(null);
		
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
		
			/* Okay. We can *show* the module as a tree- I just need access to the tree itself, so
				that I can find the appropriate clause. I'll poke at it a bunch.
				The other thing we need is getting the stuff from the line number. Let's look into that.
				Okay, here's what I've found on that: 
				Relevant Module methods:
					SafeList<Sig> getAllSigs();
					SafeList<Func> getAllFunc();
					ConstList<Pair<String,Expr>> getAllAssertions();
					SafeList<Pair<String,Expr>> getAllFacts();
					ConstList<Command> getAllCommands();
				Sigs, Funcs, Facts, Assertions, and Commands all have a Pos member that contains their
				start and endline in the file. That's the pos.y and pos.y2 member in everything except
				Sigs. Those have a list of Attr objects that have the pos. Expr objects (like facts 
				and assertions) have a "closingBracket" Pos member as well, not sure what's up with it.
				Let's do some experimentation.
				*/
		
		
		
		}
}
