package aldb;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import org.apache.commons.cli.*;

public final class Main {
  public static void main(final String[] args) throws Err {
    Options options = new Options();
    Option alloyModulePath = OptionBuilder.withArgName("path")
                                          .hasArg()
                                          .isRequired()
                                          .withDescription(
                                              "Path to Alloy module file.")
                                          .withType(String.class)
                                          .create('i');
    options.addOption(alloyModulePath);
    CommandLineParser parser = new GnuParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("aldb", options);
      System.exit(1);
    }
    Module world = CompUtil.parseEverything_fromFile(
        null, null, cmd.getOptionValue("i"));
    StringBuilder output = new StringBuilder();
    output.append("Module Name\n" + world.getModelName() + "\n\n");
    output.append("Assertions\n" + world.getAllAssertions() + "\n\n");
    output.append("Commands\n" + commandsToString(world.getAllCommands()) +
        "\n\n");
    output.append("Facts\n" + world.getAllFacts() + "\n\n");
    output.append("Func\n" + world.getAllFunc() + "\n\n");
    output.append("Sigs\n" + world.getAllSigs() + "\n\n");
    System.out.println(output);
    System.out.println("Hello world!");
  }

  private static String commandsToString(Iterable<Command> commands) {
    StringBuilder sb = new StringBuilder();
    String prefix = "";
    for (Command command : commands) {
      sb.append(prefix + command.toString());
      prefix = "\n";
    }
    return sb.toString();
  }

  public static int add(final int a, final int b) {
    return a + b;
  }

  private Main() {}
}
