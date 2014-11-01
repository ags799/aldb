package aldb;

import org.junit.runner.*;
import org.junit.runner.notification.*;

/** Runs all tests and print output to console. */
public final class TestRunner {
  public static void main(final String[] args) {
    Result result = JUnitCore.runClasses(SolverIsSolvableTests.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    result = JUnitCore.runClasses(SolverIsSolvableTests.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    result = JUnitCore.runClasses(ParserEvaluateTests.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    result = JUnitCore.runClasses(CliCommandIndexTests.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    // TODO add more tests
  }

  private TestRunner() {}
}
