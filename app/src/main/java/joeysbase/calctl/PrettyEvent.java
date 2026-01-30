package joeysbase.calctl;

import java.util.List;

import de.vandermeer.asciitable.AsciiTable;

/**
 * The PrettyEvent class provides utility methods for formatting and displaying event-related data
 * in a human-readable format. It uses ASCII tables for tabular representations and string builders
 * for listing conflicts.
 */
class PrettyEvent {

  static String listEvents(List<Event> events) {
    AsciiTable at = new AsciiTable();
    at.addRow("Title", "Start Datetime", "Duration", "Description", "Location");
    for (Event event : events) {
      at.addRow(
          event.getTitle(),
          event.getStartDateTime().toString(),
          event.getDuration().toString(),
          event.getDescription(),
          event.getLocation());
    }
    return at.render();
  }

  static String showAgenda(List<Event> events) {
    return listEvents(events);
  }

  static String showEventsInDetail(List<Event> events) {
    AsciiTable at = new AsciiTable();
    at.addRow(
        "ID",
        "Title",
        "Start Datetime",
        "Duration",
        "Description",
        "Location",
        "Created At",
        "Updated At");
    for (Event event : events) {
      at.addRow(
          event.getId(),
          event.getTitle(),
          event.getStartDateTime().toString(),
          event.getDuration().toString(),
          event.getDescription(),
          event.getLocation(),
          event.getTimeCreated().toString(),
          event.getTimeUpdated().toString());
    }
    return at.render();
  }

  static String showConflicts(List<Event> events) {
    StringBuilder sb = new StringBuilder();
    for (Event event : events) {
      sb.append("- ")
          .append("\"")
          .append(event.getTitle())
          .append("\" ")
          .append("(")
          .append(event.getStartDateTime())
          .append(" -> ")
          .append(event.getStartDateTime().plusSeconds(event.getDuration().toSeconds()))
          .append(")\n");
    }
    return sb.toString();
  }
}
