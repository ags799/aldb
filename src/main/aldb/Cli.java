package aldb;

import asg.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

/**
 * Command Line Interface using cliche.
 * Must be public to work with cliche.
 */
public class Cli {
  // TODO this should be Java Path
  private String alloyModelPath;
  private Module world;

  Cli(final String alloyModelPath) throws Err {
    this.alloyModelPath = alloyModelPath;
    this.world = CompUtil.parseEverything_fromFile(null, null, alloyModelPath);
  }

  @asg.cliche.Command
  public String hello() {
      return "Hello, World!";
  }

  @asg.cliche.Command
  public String instance() {
      return "instance";
  }
}
