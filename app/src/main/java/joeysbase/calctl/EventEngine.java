package joeysbase.calctl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * The EventEngine class manages a collection of events, providing functionality to load, save, add,
 * remove, sort, and search events. It also supports conflict detection between events.
 */
@SuppressWarnings("PMD")
class EventEngine {

  private List<Event> events;
  private static final String EVENT_DATA_FILE = System.getProperty("user.home") + "/.calctl/events.json";

  private EventEngine(List<Event> events) {
    this.events = events;
  }

  static EventEngine load() throws Exception {

    EventEngine eventEngine = null;
    try {
      JsonReader reader = new JsonReader(new FileReader(EVENT_DATA_FILE));
      Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
              .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
              .create();
      eventEngine = gson.fromJson(reader, EventEngine.class);
      reader.close();

    } catch (FileNotFoundException e) {
      eventEngine = new EventEngine(new ArrayList<>());
    } catch (Exception e) {
      throw e;
    }

    return eventEngine;
  }

  private void sortEvents() {
    this.events.sort((Event o1, Event o2) -> o1.compareTo(o2));
  }

  List<String> checkConflicts(Event newEvent) {
    List<String> conflictingEventIds = new ArrayList<>();
    for (Event event : this.events) {
      if (event.isConflict(newEvent) && !event.getId().equals(newEvent.getId())) {
        conflictingEventIds.add(event.getId());
      }
    }
    return conflictingEventIds;
  }

  void addEvent(Event event) {
    this.events.add(event);
    sortEvents();
  }

  void toFile() throws Exception {
    sortEvents();
    try {
      Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
              .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
              .create();
      Path path = Path.of(EVENT_DATA_FILE);
      Files.createDirectories(path.getParent());
      if (Files.notExists(path)) {
        Files.createFile(path);
        // System.out.println("Created new event data file at " + EVENT_DATA_FILE);
      }

      FileWriter writer = new FileWriter(EVENT_DATA_FILE);
      gson.toJson(this, writer);
      writer.close();
    } catch (Exception e) {
      System.err.println("Error: Unable to write events to file.");
      throw e;
    }
  }

  boolean removeById(String eventId) {
    return removeByIds(new ArrayList<>(List.of(eventId)));
  }

  boolean removeByDate(LocalDate date) {
    return removeByDates(new ArrayList<>(List.of(date)));
  }

  boolean removeByEvent(Event event) {
    return removeByEvents(new ArrayList<>(List.of(event)));
  }

  boolean removeByEvents(List<Event> eventsToRemove) {
    for (Event event : eventsToRemove) {
      if (!this.events.contains(event)) {
        return false;
      }
    }
    for (Event event : eventsToRemove) {
      this.events.remove(event);
    }
    return true;
  }

  boolean removeByIds(List<String> eventIds) {
    Set<String> idSet = new HashSet<>(eventIds);
    int count = 0;
    for (Event event : this.events) {
      if (idSet.contains(event.getId())) {
        count++;
      }
    }
    if (count != eventIds.size()) {
      return false;
    }
    List<Event> newEvents = new ArrayList<>();
    for (Event event : this.events) {
      if (!idSet.contains(event.getId())) {
        newEvents.add(event);
      }
    }
    this.events = newEvents;
    return true;
  }

  boolean removeByDates(List<LocalDate> dates) {
    Set<LocalDate> dateSet = new HashSet<>(dates);
    int count = 0;
    for (Event event : this.events) {
      if (dateSet.contains(event.getStartDateTime().toLocalDate())) {
        count++;
      }
    }
    if (count != dates.size()) {
      return false;
    }
    List<Event> newEvents = new ArrayList<>();
    for (Event event : this.events) {
      if (!dateSet.contains(event.getStartDateTime().toLocalDate())) {
        newEvents.add(event);
      }
    }
    this.events = newEvents;
    return true;
  }

  List<Event> findByIds(List<String> eventIds) {
    Set<String> idSet = new HashSet<>(eventIds);
    List<Event> foundEvents = new ArrayList<>();
    for (Event event : this.events) {
      if (idSet.contains(event.getId())) {
        foundEvents.add(event);
      }
    }
    return foundEvents;
  }

  Event findById(String eventId) {
    List<Event> foundEvents = findByIds(new ArrayList<>(List.of(eventId)));
    if (foundEvents.isEmpty()) {
      return null;
    }
    return foundEvents.get(0);
  }

  List<Event> findByDate(LocalDate date) {
    return findByDateRange(date, date);
  }

  List<Event> findByDateRange(LocalDate from, LocalDate to) {
    List<Event> foundEvents = new ArrayList<>();
    for (Event event : this.events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      if (eventDate.compareTo(from) >= 0 && eventDate.compareTo(to) <= 0) {
        foundEvents.add(event);
      }
    }
    return foundEvents;
  }

  List<Event> findByDateRange(LocalDate bound, boolean startLeft) {
    List<Event> foundEvents = new ArrayList<>();
    if (startLeft) {
      for (Event event : this.events) {
        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        if (eventDate.compareTo(bound) <= 0) {
          foundEvents.add(event);
        } else {
          break;
        }
      }
    } else {
      for (Event event : this.events.reversed()) {
        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        if (eventDate.compareTo(bound) >= 0) {
          foundEvents.add(event);
        } else {
          break;
        }
      }
    }
    return foundEvents;
  }

  List<Event> searchInTitle(String keyword) {
    keyword = keyword.toLowerCase();
    List<Event> result = new ArrayList<>();
    for (Event event : this.events) {
      if (event.getTitle().toLowerCase().contains(keyword)) {
        result.add(event);
      }
    }
    return result;
  }

  List<Event> searchInTitleAndDescription(String keyword) {
    keyword = keyword.toLowerCase();
    List<Event> result = new ArrayList<>();
    for (Event event : this.events) {
      if (event.getTitle().toLowerCase().contains(keyword)
          || event.getDescription().toLowerCase().contains(keyword)) {
        result.add(event);
      }
    }
    return result;
  }

  List<Event> getAllEvents() {
    return this.events;
  }
}
