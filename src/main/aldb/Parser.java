package aldb;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

final class Parser {
  /** WIP */
  static Expr parseInstance(Module module, String instanceString) throws Err {
    return CompUtil.parseOneExpression_fromString(module, instanceString);
  }

  private Parser() {}
}
