package joeysbase.calctl;

import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * The {@code Search} class is a command-line utility for searching events in a calendar. It
 * supports searching for events based on keywords in their titles and descriptions.
 *
 * <p>This class is part of a command-line application and is annotated with the {@code @Command}
 * annotation to define its name, description, and help options. It provides two modes of search:
 *
 * <ul>
 *   <li>Search in event titles only using the {@code --title} option.
 *   <li>Search in both event titles and descriptions using a keyword.
 * </ul>
 *
 * <p>Usage examples:
 *
 * <ul>
 *   <li>{@code calctl search <KEYWORD>} - Searches for events with the specified keyword in their
 *       titles and descriptions.
 *   <li>{@code calctl search --title <TITLE>} - Searches for events with the specified keyword in
 *       their titles only.
 * </ul>
 *
 * <p>If no results are found, an appropriate message is displayed. If neither a keyword nor a title
 * keyword is provided, an error message is displayed, and the program exits with a non-zero status
 * code.
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>{@code CalendarControl} - The parent command that provides access to the event engine.
 *   <li>{@code Event} - Represents an event in the calendar.
 *   <li>{@code PrettyEvent} - Utility class for formatting event details for display.
 * </ul>
 *
 * <p>Annotations:
 *
 * <ul>
 *   <li>{@code @ParentCommand} - Links this command to its parent command.
 *   <li>{@code @Parameters} - Defines the main keyword parameter for searching.
 *   <li>{@code @Option} - Defines the optional title keyword parameter for searching in titles
 *       only.
 * </ul>
 *
 * <p>Exit Codes:
 *
 * <ul>
 *   <li>{@code 0} - Successful execution with results or no results.
 *   <li>{@code 1} - Error due to missing required parameters.
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@Command(
    name = "search",
    description = "Search for events in the calendar",
    mixinStandardHelpOptions = true)
@SuppressWarnings("PMD")
class Search implements Callable<Integer> {

  // @ParentCommand CalendarControl calendarControl;

  @Parameters(
      index = "0",
      description = "Keyword to search for in event titles and descriptions.",
      defaultValue = "")
  String keyword;

  @Option(
      names = {"--title"},
      description = "Search only in event titles.",
      defaultValue = "")
  String titleKeyword;

  /**
   * Executes the search operation based on the provided keywords.
   *
   * <p>This method performs a search for events in a calendar system. It supports two types of
   * searches:
   *
   * <ul>
   *   <li>Search by title keyword: If a title keyword is provided, it searches for events with
   *       titles containing the specified keyword.
   *   <li>Search by general keyword: If a general keyword is provided, it searches for events with
   *       titles or descriptions containing the specified keyword.
   * </ul>
   *
   * <p>If no keyword is provided, an error message is displayed.
   *
   * <p>The method outputs the search results in a detailed format if matches are found. If no
   * matches are found, it outputs an appropriate message and exits the program.
   *
   * <p>Usage examples:
   *
   * <ul>
   *   <li>Search in title only: <code>calctl search --title TITLE</code>
   *   <li>Search in title and description: <code>calctl search KEYWORD</code>
   * </ul>
   *
   * <p>Exit codes:
   *
   * <ul>
   *   <li>0: Successful execution (results found or no results).
   *   <li>1: Error due to missing keyword.
   * </ul>
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

    if (!titleKeyword.isEmpty()) {
      List<Event> results = eventEngine.searchInTitle(titleKeyword);
      if (results.isEmpty()) {
        System.out.println("No events found with title containing \"" + titleKeyword + "\".");
        return 0;
      } else {
        System.out.println("Events found with title containing \"" + titleKeyword + "\":");
        System.out.println(PrettyEvent.showEventsInDetail(results));
        return 0;
      }
    } else if (!keyword.isEmpty()) {
      List<Event> results = eventEngine.searchInTitleAndDescription(keyword);
      if (results.isEmpty()) {
        System.out.println(
            "No events found with keyword \"" + keyword + "\" in title or description.");
        return 0;
      } else {
        System.out.println(
            "Events found with keyword \"" + keyword + "\" in title or description:");
        System.out.println(PrettyEvent.showEventsInDetail(results));
        return 0;
      }
    } else {
      System.err.println(
          "Error: Please provide a keyword \"calctl search <KEYWORD>\" to search in title and"
              + " description, or a title keyword \"calctl search --title TITLE\" to search in"
              + " title only.");
      return 1;
    }
  }
}
