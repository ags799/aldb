package aldb;

import static edu.mit.csail.sdg.alloy4.A4Reporter.NOP;

import edu.mit.csail.sdg.alloy4.*;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.translator.*;

final class Solver {
  /** Returns true if the sigs and command are solvable.  */
  static boolean isSolvable(
      final Iterable<Sig> sigs, final Command command) throws Err {
    A4Options options = new A4Options();
    options.solver = A4Options.SatSolver.SAT4J;
    A4Solution solution = TranslateAlloyToKodkod.execute_command(
        NOP, sigs, command, options);
    return solution.satisfiable();
  }

  private Solver() {}
}
