package aldb;

import static org.junit.Assert.*;
import static aldb.Main.add;

import org.hamcrest.*;
import org.junit.*;

/**
 * Tests solutions held in Main
 */
public class MainTest {
  @Test
  public final void testAdd() {
    assertEquals(3, add(1, 2));
  }
}
