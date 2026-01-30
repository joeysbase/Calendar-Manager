package joeysbase.calctl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * The Agenda class is a command-line utility for viewing the agenda of events for a specific day or
 * week. It is part of the CalendarControl application and uses the PicoCLI library for command-line
 * parsing.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>View events for a specific date using the `--date` option.
 *   <li>View events for the current week using the `--week` option.
 *   <li>View all events if no specific option is provided.
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 *   agenda --date YYYY-MM-DD
 *   agenda --week
 * </pre>
 *
 * <p>Options:
 *
 * <ul>
 *   <li>`--date`: Specifies the date for which to view the agenda in `YYYY-MM-DD` format.
 *   <li>`--week`: Specifies that the agenda for the current week should be displayed.
 * </ul>
 *
 * <p>Validation:
 *
 * <ul>
 *   <li>The `--date` option validates the date format and ensures it is in `YYYY-MM-DD` format.
 *   <li>If an invalid date is provided, an error message is displayed, and the program exits.
 * </ul>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>Uses `java.time` for date parsing and manipulation.
 *   <li>Uses `picocli` for command-line argument parsing.
 *   <li>Relies on `CalendarControl` and its `eventEngine` for fetching events.
 *   <li>Uses `PrettyEvent` for formatting and displaying the agenda.
 * </ul>
 *
 * <p>Exit Codes:
 *
 * <ul>
 *   <li>`0`: Successful execution with or without events.
 *   <li>`1`: Invalid date format or other errors during execution.
 * </ul>
 */
@Command(
    name = "agenda",
    description = "View the agenda for a specific day or week",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Agenda implements Callable<Integer> {

  @ParentCommand CalendarControl calendarControl;

  @Option(
      names = {"--date"},
      description = "The date for which to view the agenda in YYYY-MM-DD format",
      defaultValue = "",
      paramLabel = "DATE")
  String date;

  @Option(
      names = {"--week"},
      description = "View the agenda for the current week")
  boolean week;

  /**
   * Executes the agenda retrieval process based on the specified date or week flag.
   *
   * <p>This method determines the events to display based on the provided date or whether the week
   * flag is set. If a valid date is provided, it retrieves events for that specific date. If the
   * week flag is set, it retrieves events for the current week. If neither is specified, it
   * retrieves all events.
   *
   * <p>The method performs the following steps:
   *
   * <ul>
   *   <li>Validates the provided date.
   *   <li>Retrieves events based on the date or week flag.
   *   <li>Prints the events in a formatted manner using {@code PrettyEvent.showAgenda}.
   *   <li>Exits the program if no events are found or after displaying the events.
   * </ul>
   *
   * @throws DateTimeParseException if the provided date is not in ISO_DATE_TIME format.
   * @throws NullPointerException if the calendar control or event engine is not initialized.
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

    if (!isValidDate(date)) {
      return 1;
    }
    List<Event> events;
    if (!date.isEmpty()) {
      LocalDate agendaDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
      events = eventEngine.findByDate(agendaDate);
    } else if (week) {
      int dayOfWeekValue = LocalDate.now().getDayOfWeek().getValue();
      LocalDate weekStart = LocalDate.now().minusDays(dayOfWeekValue - 1);
      LocalDate weekEnd = weekStart.plusDays(6);
      events = eventEngine.findByDateRange(weekStart, weekEnd);
    } else {
      events = eventEngine.getAllEvents();
    }

    if (events.isEmpty()) {
      System.out.println("No events found.");
      return 0;
    }
    System.out.println(PrettyEvent.showAgenda(events));
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
}
