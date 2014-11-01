package aldb;

import java.io.IOException;
import java.nio.file.Paths;

import asg.cliche.ShellFactory;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;

import org.apache.commons.cli.*;

final class Main {
  public static void main(final String[] args) throws Err, IOException {
    CommandLine commandLineArgs = getCommandLineArgs(args);
    ShellFactory.createConsoleShell(
        ">",
        "aldb v0.0.0",
        new Cli(Paths.get(commandLineArgs.getOptionValue("i")))
    ).commandLoop();
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

  private Main() {}
}
