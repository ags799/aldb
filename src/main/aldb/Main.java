package aldb;

import java.io.IOException;

import asg.cliche.ShellFactory;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;

import org.apache.commons.cli.*;

final class Main {
  public static void main(final String[] args) throws Err, IOException {
    CommandLine commandLineArgs = getCommandLineArgs(args);
    // TODO use Java Path for the -i argument
    ShellFactory.createConsoleShell(
        ">", "aldb v0.0.0", new Cli(commandLineArgs.getOptionValue("i")))
        .commandLoop();
  }

  /**
   * Returns command line arguments.
   * They are:
   *   -i path Path to Alloy module file.
   */
  private static CommandLine getCommandLineArgs(final String[] args)
      throws Err {
    Options options = new Options();
    // -i <path> Path to Alloy module file.
    Option alloyModulePath = OptionBuilder.withArgName("path")
                                          .hasArg()
                                          .isRequired()
                                          .withDescription(
                                              "Path to Alloy module file.")
                                          .withType(String.class)
                                          .create('i');
    options.addOption(alloyModulePath);
    CommandLineParser parser = new GnuParser();
    CommandLine commandLineArgs = null;
    try {
      commandLineArgs = parser.parse(options, args);
    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("aldb", options);
      System.exit(1);
    }
    return commandLineArgs;
  }

  /**
   * Returns String description of module.
   * Vestigial.
   */
  private static String moduleToString(final Module module) {
    StringBuilder output = new StringBuilder();
    output.append("Module Name\n" + module.getModelName() + "\n\n");
    output.append("Assertions\n" + module.getAllAssertions() + "\n\n");
    output.append("Commands\n" + commandsToString(module.getAllCommands())
        + "\n\n");
    output.append("Facts\n" + module.getAllFacts() + "\n\n");
    output.append("Func\n" + module.getAllFunc() + "\n\n");
    output.append("Sigs\n" + module.getAllSigs() + "\n\n");
    return output.toString();
  }

  /**
   * Returns String description of commands.
   * Vestigial.
   */
  private static String commandsToString(final Iterable<Command> commands) {
    StringBuilder sb = new StringBuilder();
    String prefix = "";
    for (Command command : commands) {
      sb.append(prefix + command.formula.toString());
      prefix = "\n";
    }
    return sb.toString();
  }

  private Main() {}
}
