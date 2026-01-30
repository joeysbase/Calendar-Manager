package joeysbase.calctl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * The Delete command is responsible for removing events from the calendar. It supports deletion by
 * event ID or by date, with options for dry-run and confirmation prompts.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>Delete by event ID: <code>calctl delete EVENTID</code>
 *   <li>Delete by date: <code>calctl delete --date YYYY-MM-DD</code>
 * </ul>
 *
 * <p>Options:
 *
 * <ul>
 *   <li><code>--date</code>: Specify the date of the event(s) to delete in YYYY-MM-DD format.
 *   <li><code>--dry-run</code>: Simulate the delete operation without making any changes.
 *   <li><code>--force</code>: Skip confirmation prompts (inherited from the parent command).
 * </ul>
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>If an event ID is provided, the command attempts to delete the event with the specified ID.
 *   <li>If a date is provided, the command attempts to delete all events on that date.
 *   <li>If neither an ID nor a date is provided, an error message is displayed.
 *   <li>Confirmation prompts are displayed unless the <code>--force</code> option is used.
 *   <li>In dry-run mode, no changes are made, but the events to be deleted are displayed.
 * </ul>
 *
 * <p>Error Handling:
 *
 * <ul>
 *   <li>If no event is found with the specified ID or date, an error message is displayed.
 *   <li>If the date format is invalid, an error message is displayed.
 *   <li>If user input cannot be read during confirmation, an error message is displayed.
 * </ul>
 *
 * <p>Examples:
 *
 * <pre>
 *   calctl delete 12345
 *   calctl delete --date 2023-10-15
 *   calctl delete 12345 --dry-run
 *   calctl delete --date 2023-10-15 --force
 * </pre>
 */
@Command(
    name = "delete",
    description = "delete an event from the calendar",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Delete implements Callable<Integer> {

  @ParentCommand CalendarControl calendarControl;

  @Parameters(index = "0", description = "ID of the event to delete.", defaultValue = "")
  String id;

  @Option(
      names = {"--date"},
      description = "Date of the event to be deleted in YYYY-MM-DD format.",
      paramLabel = "DATE",
      defaultValue = "")
  String date;

  @Option(
      names = {"--dry-run"},
      description = "Simulate the delete operation without making any changes.",
      defaultValue = "false")
  boolean dryRun;

  /**
   * Executes the delete operation for events in the calendar.
   *
   * <p>This method performs the following: - Deletes an event by its unique ID if the `id` field is
   * not empty. - Deletes all events on a specific date if the `date` field is not empty. - Prompts
   * the user for confirmation unless the `force` flag is set to true. - Supports a dry-run mode
   * where no actual changes are made.
   *
   * <p>Behavior: - If an event ID is provided: - Finds the event by its ID. - If the event is not
   * found, prints an error message and exits. - If the event is found, prompts for confirmation
   * (unless `force` is true). - Deletes the event and optionally writes changes to a file. - If a
   * date is provided: - Validates the date format. - Finds all events on the specified date. - If
   * no events are found, prints an error message and exits. - If events are found, prompts for
   * confirmation (unless `force` is true). - Deletes the events and optionally writes changes to a
   * file. - If neither an event ID nor a date is provided, prints an error message and exits.
   *
   * <p>Error Handling: - Handles invalid user input during confirmation prompts. - Exits with an
   * error message if operations fail.
   *
   * <p>Preconditions: - The `id` or `date` field must be set before calling this method. - The
   * `calendarControl` object must be properly initialized.
   *
   * <p>Postconditions: - Events matching the specified criteria are deleted, unless in dry-run
   * mode. - Changes are persisted to a file unless in dry-run mode.
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

    if (!id.isEmpty()) {
      Event event = eventEngine.findById(id);
      if (event == null) {
        System.err.println("Error: No event found with ID " + "\"" + id + "\".");
        return 1;
      } else {
        if (calendarControl.force == false) {
          System.out.println("Are you sure you want to delete the following event? (y/n)");
          System.out.println("You can use calctl --force to skip this confirmation.");
          System.out.println(PrettyEvent.listEvents(new ArrayList<>(List.of(event))));
          try {
            int inputChar = System.in.read();
            if (inputChar != 'y' && inputChar != 'Y') {
              System.out.println("Aborting delete operation.");
              return 0;
            }
          } catch (Exception e) {
            System.err.println("Error: Unable to read user input.");
            System.err.println(e);
            return 1;
          }
        }
        boolean success = eventEngine.removeByEvent(event);
        if (success) {
          if (!dryRun) {
            try {
              eventEngine.toFile();
            } catch (Exception e) {
              return 1;
            }
            System.out.println("The following events were deleted successfully.");
          } else {
            System.out.println("Dry run enabled. No changes were made to the following events.");
          }
          System.out.println(PrettyEvent.listEvents(new ArrayList<>(List.of(event))));
          return 0;
        } else {
          System.err.println("Error: Unable to delete event with ID " + "\"" + id + "\".");
          return 1;
        }
      }
    } else if (!date.isEmpty()) {
      isValidDate(date);
      LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
      List<Event> eventsToDelete = eventEngine.findByDate(localDate);
      if (eventsToDelete.isEmpty()) {
        System.err.println("Error: No events found on date " + "\"" + date + "\".");
        return 1;

      } else {
        if (calendarControl.force == false) {
          System.out.println("Are you sure you want to delete the following events? (y/n)");
          System.out.println("You can use calctl --force to skip this confirmation.");
          System.out.println(PrettyEvent.listEvents(eventsToDelete));
          try {
            int inputChar = System.in.read();
            if (inputChar != 'y' && inputChar != 'Y') {
              System.out.println("Aborting delete operation.");
              return 0;
            }
          } catch (Exception e) {
            System.err.println("Error: Unable to read user input.");
            return 1;
          }
        }
        boolean success = eventEngine.removeByDate(localDate);
        if (success) {
          if (!dryRun) {
            try {
              eventEngine.toFile();
            } catch (Exception e) {
              return 1;
            }
            System.out.println("The following events were deleted successfully.");
          } else {
            System.out.println("Dry run enabled. No changes were made to the following events.");
          }
          System.out.println(PrettyEvent.listEvents(eventsToDelete));
          return 0;
        } else {
          System.err.println("Error: Unable to delete events on date " + "\"" + date + "\".");
          return 1;
        }
      }
    } else {
      System.err.println(
          "Error: Please provide either an event ID \"calctl delete <EVENTID>\" or a date \"calctl"
              + " delete --date DATE\" to delete events.");
      return 1;
    }
  }

  private static boolean isValidDate(String dateString) {
    if (dateString.isEmpty()) {
      return false;
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
