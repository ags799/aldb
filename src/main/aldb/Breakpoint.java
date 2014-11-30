package aldb;

import java.nio.file.Path;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.translator.*;

import java.nio.file.Path;

public class Breakpoint {
	
	Integer lineNumber;
	String type; //sig, func, command, assert, or fact
	Expr clause;
	boolean pointless = false; //If the breakpoint is between brackets... 
	
	
	public Breakpoint(int line, Module module){	
		this.lineNumber = new Integer(line);
		this.type = null;
		this.clause = null;
		setTypeAndClause(line, module); 
		if(type == null || clause == null){
			this.pointless = true;
		}
	}
	
	public Integer getLine() { return lineNumber; }
	
	public String getType() { return type; }
	
	public Expr getClause() { return clause; }
	
	private void setTypeAndClause(int line, Module module){
		
		SafeList<Sig> allSigs = module.getAllSigs();
		SafeList<Func> allFuncs = module.getAllFunc();
		ConstList<Pair<String,Expr>> allAsserts = module.getAllAssertions();
		SafeList<Pair<String,Expr>> allFacts = module.getAllFacts();
		ConstList<Command> allCommands = module.getAllCommands();
		
		for(Sig sig: allSigs){
			if(line >= sig.pos.y && line <= sig.pos.y2){
				System.out.println("Found the spot- it's in sig " + sig.label);
				this.type = "sig";
				this.clause = sig;
				return;
			}
		}
		
		for(Func func: allFuncs){
			if(line >= func.pos.y && line <= func.pos.y2){
				System.out.println("Found the spot- it's in func " + func.label);
				this.type = "func";
				this.clause = func.getBody();
				return;
			}
		}
		
		for(Pair<String,Expr> pair: allAsserts){
			if(line >= pair.b.pos.y && line <= pair.b.pos.y2){
				System.out.println("Found the spot- it's in Assert " + pair.a);
				this.type = "assert";
				this.clause = pair.b;
				return;
			}
		}
		
		for(Pair<String,Expr> pair: allFacts){
			if(line >= pair.b.pos.y && line <= pair.b.pos.y2){
				System.out.println("Found the spot- it's in Fact " + pair.a);
				this.type = "fact";
				this.clause = pair.b;
				return;
			}
		}
		
		for(Command c: allCommands){
			if(line >= c.pos.y && line <= c.pos.y2){
				System.out.println("Found the spot- it's in Command " + c.label);
				this.type = "command";
				this.clause = c.formula;
				return;
			}
		}
		return;
	}
	
}