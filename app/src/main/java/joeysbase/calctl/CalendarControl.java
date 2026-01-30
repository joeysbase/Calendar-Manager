package joeysbase.calctl;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * The {@code CalendarControl} class serves as the entry point for the "calctl" command-line tool.
 * This tool provides a simple interface for managing calendar events, including adding, editing,
 * deleting, and searching for events. It supports various subcommands to perform these operations.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Displays version information with the {@code -v} or {@code --version} option.
 *   <li>Supports disabling colored output with the {@code --no-color} option.
 *   <li>Allows forcing operations without confirmation using the {@code --force} option.
 * </ul>
 *
 * <p>Subcommands:
 *
 * <ul>
 *   <li>{@code Add} - Add a new event to the calendar.
 *   <li>{@code Agenda} - Display a list of upcoming events.
 *   <li>{@code Delete} - Remove an event from the calendar.
 *   <li>{@code Edit} - Modify an existing event.
 *   <li>{@code ListCommand} - List all events in the calendar.
 *   <li>{@code Search} - Search for events based on criteria.
 *   <li>{@code Show} - Show details of a specific event.
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 *   java -jar calctl.jar [OPTIONS] [SUBCOMMAND]
 * </pre>
 *
 * <p>The {@code CalendarControl} class initializes the {@code EventEngine} to manage calendar data
 * and delegates execution to the appropriate subcommand.
 *
 * @version 0.1.0
 */
@Command(
    name = "calctl",
    mixinStandardHelpOptions = true,
    version = "calctl 0.1.0",
    description = "A simple calendar command line tool",
    subcommands = {
      Add.class,
      Agenda.class,
      Delete.class,
      Edit.class,
      ListCommand.class,
      Search.class,
      Show.class
    })
@SuppressWarnings("PMD")
public class CalendarControl implements Callable<Integer> {

  @Option(
      names = {"--no-color"},
      description = "Disable colored output")
  boolean noColor;

  @Option(
      names = {"--force"},
      description = "Force operation without confirmation")
  boolean force;

  EventEngine eventEngine;

  /** Init command. */
  public static void main(String[] args) {
    int exitCode = new CommandLine(new CalendarControl()).execute(args);
    System.exit(exitCode);
  }

  /**
   * Executes the run method, which initializes the event engine by loading its state from a
   * persistent source. This method is typically called when the associated thread is started.
   */
  @Override
  public Integer call() {
    System.out.println("Hello from calctl, you can use --help to find out how to use.");
    System.out.println("Or visit Github page for examples.");
    return 0;
  }
}
