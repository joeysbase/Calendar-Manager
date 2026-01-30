package joeysbase.calctl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * The ListCommand class is responsible for listing events in the calendar based on various
 * criteria. It supports filtering events by date range, today's events, this week's events, or
 * listing all events.
 *
 * <p>Command-line options:
 *
 * <ul>
 *   <li><b>--from</b>: Specifies the start date for filtering events. The date should be in the
 *       format YYYY-MM-DD.
 *   <li><b>--to</b>: Specifies the end date for filtering events. The date should be in the format
 *       YYYY-MM-DD.
 *   <li><b>--week</b>: Lists all events occurring in the current week.
 *   <li><b>--today</b>: Lists all events occurring today.
 * </ul>
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>If both <b>--from</b> and <b>--to</b> are provided, events within the specified date range
 *       are listed.
 *   <li>If only <b>--from</b> is provided, events starting from the specified date are listed.
 *   <li>If only <b>--to</b> is provided, events up to the specified date are listed.
 *   <li>If <b>--today</b> is provided, only today's events are listed.
 *   <li>If <b>--week</b> is provided, events for the current week are listed.
 *   <li>If no options are provided, all events are listed.
 * </ul>
 *
 * <p>Validation:
 *
 * <ul>
 *   <li>Dates provided with <b>--from</b> and <b>--to</b> are validated to ensure they are in the
 *       correct format (YYYY-MM-DD).
 *   <li>An error is displayed if the <b>--from</b> date is after the <b>--to</b> date.
 * </ul>
 *
 * <p>Output:
 *
 * <ul>
 *   <li>If no events are found, a message "No events found." is displayed.
 *   <li>Events are displayed in a formatted manner using the PrettyEvent utility.
 * </ul>
 *
 * <p>Exit Codes:
 *
 * <ul>
 *   <li>0: Successful execution.
 *   <li>1: Error due to invalid input or other issues.
 * </ul>
 */
@Command(
    name = "list",
    description = "List all events in the calendar",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class ListCommand implements Callable<Integer> {

  @Option(
      names = {"--from"},
      description = "The date from which the events start.",
      defaultValue = "",
      paramLabel = "DATE")
  String from;

  @Option(
      names = {"--to"},
      description = "The date until which the events end.",
      defaultValue = "",
      paramLabel = "DATE")
  String to;

  @Option(
      names = {"--week"},
      description = "List this week's events.")
  boolean week;

  @Option(
      names = {"--today"},
      description = "List today's events.")
  boolean today;

  /**
   * Executes the command to list events based on the specified criteria. The method processes the
   * input parameters to determine the date range or specific conditions for filtering events. It
   * supports the following scenarios:
   *
   * <p>- If both `from` and `to` dates are provided, it lists events within the specified range. -
   * If only `from` is provided, it lists events starting from the `from` date. - If only `to` is
   * provided, it lists events up to the `to` date. - If the `today` flag is set, it lists events
   * for the current day. - If the `week` flag is set, it lists events for the current week (Monday
   * to Sunday). - If no criteria are provided, it lists all events.
   *
   * <p>The method validates the input dates and ensures that the `from` date is not after the `to`
   * date. If no events are found, it prints a message and exits. Otherwise, it displays the events
   * in a formatted manner.
   *
   * <p>Note: The method terminates the program using `System.exit()` after execution.
   */
  @Override
  public Integer call() {
    if (!isValidDate(from) || !isValidDate(to)) {
      return 1;
    }

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

    List<Event> events;
    if (!from.isEmpty() && !to.isEmpty()) {

      LocalDate fromDate = LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
      LocalDate toDate = LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE);
      if (fromDate.isAfter(toDate)) {
        System.err.println("Error: --from date cannot be after --to date.");
        return 1;
      }
      events = eventEngine.findByDateRange(fromDate, toDate);
    } else if (!from.isEmpty()) {
      LocalDate fromDate = LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
      events = eventEngine.findByDateRange(fromDate, false);
    } else if (!to.isEmpty()) {
      LocalDate toDate = LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE);
      events = eventEngine.findByDateRange(toDate, true);
    } else if (today) {
      LocalDate todayDate = LocalDate.now();
      events = eventEngine.findByDate(todayDate);
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
    System.out.println(PrettyEvent.listEvents(events));
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
