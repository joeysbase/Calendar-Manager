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
import picocli.CommandLine.ParentCommand;

/**
 * The {@code Add} class is a command-line utility for adding a new event to the calendar. It
 * provides options to specify the event's title, date, start time, duration, description, and
 * location. The class validates the input and ensures that required fields are provided. It also
 * checks for event conflicts unless the {@code --force} option is specified.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>{@code --title}: Title of the event (required).
 *   <li>{@code --date}: Date of the event in YYYY-MM-DD format (required).
 *   <li>{@code --time}: Start time of the event in HH:MM format (required).
 *   <li>{@code --duration}: Duration of the event in Ww Dd Hh Mm format (required).
 *   <li>{@code --description}: Description of the event (optional).
 *   <li>{@code --location}: Location of the event (optional).
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Validates the date and time formats.
 *   <li>Checks for event conflicts unless the {@code --force} option is used.
 *   <li>Creates and adds the event to the calendar.
 *   <li>Persists the event data to a file.
 * </ul>
 *
 * <p>Example:
 *
 * <pre>
 *   java Add --title "Meeting" --date "2023-10-10" --time "14:00" --duration "1h 30m"
 * </pre>
 *
 * <p>Errors:
 *
 * <ul>
 *   <li>Missing required fields: {@code --title}, {@code --date}, {@code --time}, {@code
 *       --duration}.
 *   <li>Invalid date or time format.
 *   <li>Invalid duration format.
 *   <li>Event conflicts detected (unless {@code --force} is used).
 * </ul>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>{@code CalendarControl}: Parent command providing context for the calendar operations.
 *   <li>{@code Event}: Represents the event to be added.
 *   <li>{@code PrettyEvent}: Utility for formatting event details.
 * </ul>
 */
@Command(
    name = "add",
    description = "Add a new event to the calendar",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Add implements Callable<Integer> {

  @ParentCommand CalendarControl calendarControl;

  @Option(
      names = {"--title"},
      paramLabel = "TITLE",
      description = "Title of the event",
      defaultValue = "")
  String title;

  @Option(
      names = {"--date"},
      paramLabel = "DATE",
      description = "Date of the event in YYYY-MM-DD format",
      defaultValue = "")
  String date;

  @Option(
      names = {"--time"},
      paramLabel = "START_TIME",
      description = "Start time of the event in HH:MM format",
      defaultValue = "")
  String startTime;

  @Option(
      names = {"--duration"},
      paramLabel = "DURATION",
      description = "Duration of the event in Ww Dd Hh Mm format",
      defaultValue = "")
  String duration;

  @Option(
      names = {"--description"},
      paramLabel = "DESCRIPTION",
      description = "Description of the event",
      defaultValue = "")
  String description;

  @Option(
      names = {"--location"},
      paramLabel = "LOCATION",
      description = "Location of the event",
      defaultValue = "")
  String location;

  /**
   * Executes the process of adding a new event to the calendar.
   *
   * <p>This method validates the input fields, checks for event conflicts, and adds the event to
   * the calendar if all conditions are met. If the `--force` option is set, conflict checking is
   * skipped.
   *
   * <p>The required fields for the event are:
   *
   * <ul>
   *   <li>title
   *   <li>date
   *   <li>startTime
   *   <li>duration
   * </ul>
   *
   * <p>If any of these fields are missing, the program will terminate with an error message.
   *
   * <p>The method performs the following steps:
   *
   * <ol>
   *   <li>Validates the date and time formats.
   *   <li>Parses the date, time, and duration into appropriate objects.
   *   <li>Constructs an event object with the provided details.
   *   <li>Checks for conflicts with existing events unless the `--force` option is set.
   *   <li>Adds the event to the calendar and saves the updated calendar to a file.
   * </ol>
   *
   * <p>If conflicts are detected and the `--force` option is not set, the program will terminate
   * with a message listing the conflicting events.
   *
   * <p>Upon successful addition of the event, the method prints a success message and details of
   * the newly added event.
   *
   * <p>Note: The program terminates with `System.exit()` in case of errors or after successful
   * execution.
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
      System.err.println("Error: Unable to initialize event engine. Your event file maybe empty.");
      return 1;
    }

    if (title.isEmpty() || date.isEmpty() || startTime.isEmpty() || duration.isEmpty()) {
      System.err.println("Error: --title, --date, --time, and --duration are required fields.");
      return 1;
    }
    if (!isValidDate(date) || !isValidTime(startTime)) {
      return 1;
    }
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    LocalTime localTime = LocalTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_TIME);
    LocalDateTime startDateTime = LocalDateTime.of(localDate, localTime);
    Duration eventDuration = null;
    try {
      eventDuration = new Duration(duration);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println(
          "Error: Invalid duration format " + "\"" + duration + "\". " + "Please use Ww Dd Hh Mm.");
      return 1;
    }

    Event event =
        new Event.Builder(
                "evt-" + String.valueOf((title + startDateTime.toString()).hashCode()), title)
            .description(description)
            .startDateTime(startDateTime)
            .duration(eventDuration)
            .location(location)
            .timeCreated(LocalDateTime.now())
            .timeUpdated(LocalDateTime.now())
            .build();

    if (this.calendarControl.force) {
      System.out.println("Warning: --force option is set. Skipping event conflicts checking.");
    } else {
      List<String> eventIds = eventEngine.checkConflicts(event);
      if (!eventIds.isEmpty()) {
        System.err.println(eventIds.size() + " events found conflicted with the new event.");
        System.err.println(PrettyEvent.showConflicts(eventEngine.findByIds(eventIds)));
        System.err.println("Consider using calctl --force option to add anyway.");
        return 1;
      }
    }

    eventEngine.addEvent(event);
    try {
      eventEngine.toFile();
    } catch (Exception e) {
      System.err.println(e);
      return 1;
    }
    System.out.println("Event added successfully.");
    System.out.println(PrettyEvent.listEvents(new ArrayList<>(List.of(event))));
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
      return false;
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
