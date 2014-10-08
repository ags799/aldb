package aldb;

import static aldb.Solver.isSolvable;
import static org.junit.Assert.*;

import java.util.*;

import org.hamcrest.*;
import org.junit.*;

import edu.mit.csail.sdg.alloy4.*;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.*;

public class IsSolvableTests {
  @Test
  public final void testShouldReturnTrueForValidModel() throws Err {
    PrimSig a = new PrimSig("A");
    Expr expr = a.some();
    Command command = new Command(false, -1, -1, -1, expr);
    assertTrue(isSolvable(Arrays.asList(new Sig[]{a}), command));
  }

  @Test
  public final void testShouldReturnFalseForInvalidModel() throws Err {
    PrimSig a = new PrimSig("A");
    Expr expr = a.some().and(a.no());
    Command command = new Command(false, -1, -1, -1, expr);
    assertFalse(isSolvable(Arrays.asList(new Sig[]{a}), command));
  }
}
