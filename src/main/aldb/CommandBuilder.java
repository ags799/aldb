package aldb;

import java.util.ArrayList;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprList;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Type;

public class CommandBuilder {
	
	public static Expr buildCommand(Module module, ArrayList<Breakpoint> breakpoints, Command command){ //Also need to know current command
		ExprList retval = null;
		Expr holding = null;
		
		
		//SafeList<Sig> allSigs = module.getAllSigs();
		SafeList<Func> allFuncs = module.getAllFunc();
		//ConstList<Pair<String,Expr>> allAsserts = module.getAllAssertions();
		SafeList<Pair<String,Expr>> allFacts = module.getAllFacts();
		//ConstList<Command> allCommands = module.getAllCommands();
		
		/*
		for(Sig s: allSigs){
			if(checkAgainstSigs(s, breakpoints)){
				//add clause
				ExprList.makeAND(null, null, retval, s);
			}
		}*/
		
		for(Func f: allFuncs){
			if(checkAgainstFuncs(f, breakpoints)){
				//add clause
				if(retval == null){
					if(holding == null){
						holding = f.getBody();
					}else{
						retval = ExprList.makeAND(null, null, holding, f.getBody());
					}
				}else{
					retval = ExprList.makeAND(null, null, retval, f.getBody());
				}
			}
		}
		/*
		for(Pair<String,Expr> p: allAsserts){
			if(checkAgainstAsserts(p.b, breakpoints)){
				//add clause
			}
		}*/
		for(Pair<String,Expr> p: allFacts){
			if(checkAgainstFacts(p.b, breakpoints)){
				//add clause
				if(retval == null){
					if(holding == null){
						holding = p.b;
					}else{
						retval = ExprList.makeAND(null, null, holding, p.b);
					}
				}else{
					retval = ExprList.makeAND(null, null, retval, p.b);
				}
			}
		}
		
		/*
		for(Command c: allCommands){
			if(checkAgainstCommands(c, breakpoints)){
				//add clause
			}
		}
		*/
		System.out.println("Old formula: " + command.formula.toString());
		System.out.println("New formula: " + retval.toString());
		return retval;
	}
	
	private static boolean checkAgainstSigs(Sig s, ArrayList<Breakpoint> breakpoints){
		boolean include = true;
		for(Breakpoint b: breakpoints){
			if(b.pointless == false && b.getType().equals("sig") && b.getClause().isSame(s)){ //Add command hasVar!
				include = false;
			}
		}
		return include;
	}
	
	private static boolean checkAgainstFuncs(Func f, ArrayList<Breakpoint> breakpoints){
		boolean include = true;
		for(Breakpoint b: breakpoints){
			if(b.pointless == false && b.getType().equals("func") && b.getClause().isSame(f.getBody())){
				include = false;
			}
		}
		return include;
	}
	
	private static boolean checkAgainstAsserts(Expr e, ArrayList<Breakpoint> breakpoints){
		boolean include = true;
		for(Breakpoint b: breakpoints){
			if(b.pointless == false && b.getType().equals("assert") && b.getClause().isSame(e)){
				include = false;
			}
		}
		return include;
	}
	
	private static boolean checkAgainstFacts(Expr e, ArrayList<Breakpoint> breakpoints){
		boolean include = true;
		for(Breakpoint b: breakpoints){
			if(b.pointless == false && b.getType().equals("fact") && b.getClause().isSame(e)){
				include = false;
			}
		}
		return include;
	}
	
	private static boolean checkAgainstCommands(Command c, ArrayList<Breakpoint> breakpoints){
		boolean include = true;
		for(Breakpoint b: breakpoints){
			if(b.pointless == false && b.getType().equals("command") && b.getClause().isSame(c.formula)){
				include = false;
			}
		}
		return include;
	}

}
