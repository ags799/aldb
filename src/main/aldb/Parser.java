package aldb;

import java.nio.file.Path;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

final class Parser {
  /** Parses a Module from a file at modulePath. */
  static Module getModuleFromPath(final Path modulePath) throws Err {
    return CompUtil.parseEverything_fromFile(
        null, null, modulePath.toString());
  }

  /** Evaluates expression on the given Module and Solution. */
  static String evaluate(
      final Module module, final A4Solution solution, final String expression)
      throws Err {
    Expr result = CompUtil.parseOneExpression_fromString(module, expression);
    return solution.eval(result).toString();
  }

  private Parser() {}
}
