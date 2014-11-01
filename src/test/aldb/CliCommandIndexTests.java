package aldb;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

import edu.mit.csail.sdg.alloy4.Err;

import org.hamcrest.*;
import org.junit.*;

/** Tests for managing Cli.commandIndex. */
public class CliCommandIndexTests {
  @Test
  public final void testShouldInitializeCommandIndexToNullIfNoCommands()
      throws Err, FileNotFoundException, UnsupportedEncodingException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}";
    Path modulePath = TestFileCreator.create(moduleString);
    Cli cli = new Cli(modulePath);
    assertNull(cli.commandIndex);
  }

  @Test
  public final void testShouldInitializeCommandIndexToZeroIfCommands()
      throws Err, FileNotFoundException, UnsupportedEncodingException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}\n"
                  + "run example";
    Path modulePath = TestFileCreator.create(moduleString);
    Cli cli = new Cli(modulePath);
    assertTrue(0 == cli.commandIndex);
  }

  @Test
  public final void testShouldHoldCurrentCommandIndexIfNewIndexNonexistent()
      throws Err, FileNotFoundException, UnsupportedEncodingException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}\n"
                  + "run example";
    Path modulePath = TestFileCreator.create(moduleString);
    Cli cli = new Cli(modulePath);
    cli.setCommand(1);
    assertTrue(0 == cli.commandIndex);
  }

  @Test
  public final void testShouldHoldCurrentCommandIndexIfNewIndexNegative()
      throws Err, FileNotFoundException, UnsupportedEncodingException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}\n"
                  + "run example";
    Path modulePath = TestFileCreator.create(moduleString);
    Cli cli = new Cli(modulePath);
    cli.setCommand(-1);
    assertTrue(0 == cli.commandIndex);
  }

  @Test
  public final void testShouldSetNewCommandIndexIfNewValid()
      throws Err, FileNotFoundException, UnsupportedEncodingException {
    String moduleString = "module test\n"
                  + "sig A {}\n"
                  + "pred example () {}\n"
                  + "run example for 1 A\n"
                  + "run example for exactly 1 A\n";
    Path modulePath = TestFileCreator.create(moduleString);
    Cli cli = new Cli(modulePath);
    cli.setCommand(1);
    assertTrue(1 == cli.commandIndex);
  }
}
