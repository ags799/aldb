package aldb;

import org.junit.runner.*;
import org.junit.runner.notification.*;

/**
 * Runs all tests and print output to console.
 */
public final class TestRunner {
  public static void main(final String[] args) {
    Result result = JUnitCore.runClasses(MainTest.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    result = JUnitCore.runClasses(IsSolvableTests.class);
    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }
    // TODO add more tests
  }

  private TestRunner() {}
}
