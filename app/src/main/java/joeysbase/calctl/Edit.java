package joeysbase.calctl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import edu.northeastern.timeduration.Duration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * The {@code Edit} class is a command-line utility for editing an existing event in the calendar.
 * It allows users to modify various attributes of an event, such as title, date, time, duration,
 * description, and location. The class validates the input parameters and ensures that the edited
 * event does not conflict with other events unless the `--force` option is specified.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>Specify the ID of the event to edit as a required parameter.
 *   <li>Use optional flags to modify specific attributes of the event:
 *       <ul>
 *         <li>{@code --title}: New title of the event.
 *         <li>{@code --date}: New date in YYYY-MM-DD format.
 *         <li>{@code --time}: New start time in HH:MM format.
 *         <li>{@code --duration}: New duration in Ww Dd Hh Mm format.
 *         <li>{@code --description}: New description of the event.
 *         <li>{@code --location}: New location of the event.
 *       </ul>
 *   <li>If the `--force` option is set, conflict checking is skipped.
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Validates date and time formats.
 *   <li>Checks for conflicts with other events unless `--force` is used.
 *   <li>Updates the event's last modified timestamp.
 *   <li>Persists changes to the event storage file.
 * </ul>
 *
 * <p>Errors:
 *
 * <ul>
 *   <li>Displays an error message and exits if the event ID is not found.
 *   <li>Displays an error message and exits if the input date or time format is invalid.
 *   <li>Displays an error message and exits if the edited event conflicts with other events (unless
 *       `--force` is used).
 * </ul>
 *
 * <p>Example:
 *
 * <pre>
 *   calctl edit 123 --title "New Event Title" --date 2023-10-15 --time 14:00 --duration "1h 30m"
 * </pre>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>{@code CalendarControl}: The parent command that manages the calendar.
 *   <li>{@code EventEngine}: Handles event storage, retrieval, and conflict checking.
 *   <li>{@code PrettyEvent}: Utility for displaying event details and conflicts.
 * </ul>
 */
@Command(
    name = "edit",
    description = "Edit an existing event in the calendar",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Edit implements Callable<Integer> {

  @ParentCommand CalendarControl calendarControl;

  @Parameters(index = "0", description = "ID of the event to edit.", defaultValue = "")
  String id;

  @Option(
      names = {"--title"},
      paramLabel = "TITLE",
      description = "New title of the event",
      defaultValue = "")
  String title;

  @Option(
      names = {"--date"},
      paramLabel = "DATE",
      description = "New date in YYYY-MM-DD format",
      defaultValue = "")
  String date;

  @Option(
      names = {"--time"},
      paramLabel = "START_TIME",
      description = "New start time in HH:MM format",
      defaultValue = "")
  String startTime;

  @Option(
      names = {"--duration"},
      paramLabel = "DURATION",
      description = "New duration in Ww Dd Hh Mm format",
      defaultValue = "")
  String duration;

  @Option(
      names = {"--description"},
      paramLabel = "DESCRIPTION",
      description = "New description",
      defaultValue = "")
  String description;

  @Option(
      names = {"--location"},
      paramLabel = "LOCATION",
      description = "New location of the event",
      defaultValue = "")
  String location;

  /**
   * Updates an existing event in the calendar based on the provided parameters. The method performs
   * the following steps: 1. Finds the event by its ID and removes it from the event engine. 2.
   * Updates the event's title, start date/time, duration, description, and location if provided. 3.
   * Validates the new date and time formats. 4. Checks for conflicts with other events unless the
   * `--force` option is set. 5. Adds the updated event back to the event engine and saves the
   * changes to a file. 6. Prints success or error messages based on the operation's outcome.
   *
   * <p>Error Handling: - If the event with the specified ID is not found, the program exits with an
   * error message. - If the duration cannot be parsed, the program exits with an error message. -
   * If conflicts are detected and the `--force` option is not set, the program exits with an error
   * message.
   *
   * <p>Preconditions: - The `id` must correspond to an existing event in the event engine. - The
   * `date` and `startTime` (if provided) must be in valid ISO_LOCAL_DATE and ISO_LOCAL_TIME
   * formats, respectively. - The `duration` (if provided) must be a valid duration string.
   *
   * <p>Postconditions: - The event is updated with the new details and saved to the event engine. -
   * If the `--force` option is set, conflict checking is skipped.
   *
   * <p>Outputs: - Success message with the updated event details if the operation is successful. -
   * Error messages for invalid inputs, conflicts, or other issues.
   */
  @Override
  public Integer call() {
    EventEngine eventEngine = null;
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
    Event event = eventEngine.findById(id);
    if (event == null) {
      System.err.println("Error: No event found with ID " + "\"" + id + "\".");
      return 1;
    } else {
      eventEngine.removeByEvent(event);
    }
    if (!title.isEmpty()) {
      event.setTitle(title);
    }
    if (!date.isEmpty() || !startTime.isEmpty()) {
      LocalDateTime newStartDateTime = event.getStartDateTime();
      if (!date.isEmpty()) {
        if (!isValidDate(date)) {
          return 1;
        }
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        newStartDateTime = LocalDateTime.of(localDate, newStartDateTime.toLocalTime());
      }
      if (!startTime.isEmpty()) {
        if (!isValidTime(startTime)) {
          return 1;
        }
        LocalTime localTime = LocalTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_TIME);
        newStartDateTime = LocalDateTime.of(newStartDateTime.toLocalDate(), localTime);
      }
      event.setStartDateTime(newStartDateTime);
    }
    if (!duration.isEmpty()) {
      try {
        Duration newDuration = new Duration(duration);
        event.setDuration(newDuration);
      } catch (Exception e) {
        System.err.println("Error: Unable to parse duration.");
        System.err.println(e.getMessage());
        return 1;
      }
    }
    if (!description.isEmpty()) {
      event.setDescription(description);
    }
    if (!location.isEmpty()) {
      event.setLocation(location);
    }
    event.setTimeUpdated(LocalDateTime.now());
    if (this.calendarControl.force) {
      System.out.println("Warning: --force option is set. Skipping event conflicts checking.");
    } else {
      List<String> conflictIds = eventEngine.checkConflicts(event);
      if (!conflictIds.isEmpty()) {
        List<Event> conflictingEvents = eventEngine.findByIds(conflictIds);
        System.err.println("Error: The edited event conflicts with the following events:");
        System.err.println(PrettyEvent.showConflicts(conflictingEvents));
        System.err.println("Consider using calctl --force option to edit anyway.");
        return 1;
      }
    }
    eventEngine.addEvent(event);
    try {
      eventEngine.toFile();
    } catch (Exception e) {
      return 1;
    }
    System.out.println("The event with ID " + "\"" + id + "\"" + " was updated successfully.");
    System.out.println(PrettyEvent.showEventsInDetail(new ArrayList<>(List.of(event))));
    return 0;
  }

  private static boolean isValidDate(String dateString) {
    if (dateString.isEmpty()) {
      return true;
    }
    try {
      LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      System.err.println(
          "Error: Invalid date format " + "\"" + dateString + "\". " + "Please use YYYY-MM-DD.");
      return false;
    } catch (Exception e) {
      System.err.println("Error: Unable to parse date.");
      System.err.println(e.getMessage());
      return false;
    }
    return true;
  }

  private static boolean isValidTime(String timeString) {
    if (timeString.isEmpty()) {
      return true;
    }
    try {
      LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME);
    } catch (DateTimeParseException e) {
      System.err.println(
          "Error: Invalid time format " + "\"" + timeString + "\". " + "Please use HH:MM.");
      return false;
    } catch (Exception e) {
      System.err.println("Error: Unable to parse date.");
      System.err.println(e.getMessage());
      return false;
    }
    return true;
  }
}
