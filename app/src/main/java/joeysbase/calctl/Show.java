package joeysbase.calctl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * The {@code Show} class is a command-line utility for displaying details of a specific event. It
 * is part of a calendar management application and provides options for outputting event details in
 * either a human-readable format or JSON format. The class also checks for and displays any
 * conflicts with other events.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>Use the {@code -j} or {@code --json} option to output the event details in JSON format.
 *   <li>If no event is found with the specified ID, an error message is displayed and the program
 *       exits.
 *   <li>If conflicts are detected with other events, a warning message is displayed along with the
 *       conflicting events.
 * </ul>
 *
 * <p>Annotations:
 *
 * <ul>
 *   <li>{@code @Command}: Specifies the name, description, and help options for the command.
 *   <li>{@code @ParentCommand}: Links this command to its parent command, {@code CalendarControl}.
 *   <li>{@code @Option}: Defines the command-line option for JSON output.
 * </ul>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>{@code CalendarControl}: The parent command that provides access to the event engine.
 *   <li>{@code Event}: Represents an event in the calendar.
 *   <li>{@code PrettyEvent}: Utility class for formatting event details.
 * </ul>
 *
 * <p>Methods:
 *
 * <ul>
 *   <li>{@code run()}: Executes the command, displaying event details and checking for conflicts.
 * </ul>
 */
@Command(
    name = "show",
    description = "Show details of a specific event",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Show implements Callable<Integer> {

  @Parameters(index = "0", description = "ID of the event to show.", defaultValue = "")
  String id;

  @Option(
      names = {"-j", "--json"},
      description = "Output in JSON format")
  boolean json;

  /**
   * Executes the logic to display an event based on its ID. If the event is not found, an error
   * message is printed, and the program exits with a status of 1. If the event is found, it is
   * displayed in either JSON format or a detailed pretty-printed format, depending on the `json`
   * flag. Additionally, the method checks for conflicting events and displays a warning with
   * details of the conflicting events if any are found.
   *
   * <p>Preconditions: - The `id` field must be set to the ID of the event to be displayed. - The
   * `calendarControl` field must be initialized and provide access to the event engine.
   *
   * <p>Postconditions: - The event details are printed to the console. - If conflicts are detected,
   * a warning with conflicting event details is printed. - The program exits with a status of 0 on
   * success or 1 if the event is not found.
   */
  @Override
  public Integer call() {
    EventEngine eventEngine;
    try {
      eventEngine = EventEngine.load();
    } catch (Exception e) {
      System.err.println("Error: Unable to create event engine.");
      System.err.println(e.getMessage());
      return 1;
    }
    if (eventEngine == null) {
      System.err.println("Error: Unable to initialize event engine.");
      return 1;
    }
    if(id.isEmpty()){
      System.err.println("Error: Please specify an id of event to show.");
      return 1;
    }
    Event event = eventEngine.findById(id);
    if (event == null) {
      System.err.println("Error: No event found with ID " + "\"" + id + "\".");
      return 1;
    } else {
      if (json) {
        System.out.println(event.toJsonString());
      } else {
        System.out.println(PrettyEvent.showEventsInDetail(new ArrayList<>(List.of(event))));
      }
      List<String> conflictingEvents = eventEngine.checkConflicts(event);
      if (!conflictingEvents.isEmpty()) {
        System.out.println("Warning: The following events conflict with this event:");
        System.out.println(PrettyEvent.showConflicts(eventEngine.findByIds(conflictingEvents)));
      }
      return 0;
    }
  }
}
