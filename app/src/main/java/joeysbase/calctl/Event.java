package joeysbase.calctl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.northeastern.timeduration.Duration;

/**
 * The Event class represents an event with various attributes such as title, description, start
 * time, duration, location, and timestamps for creation and updates. It also provides functionality
 * to check for conflicts with other events based on their time intervals.
 *
 * <p>This class is immutable once built using the {@link Builder} class, which allows for flexible
 * construction of Event objects.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Comparison of events based on their start time.
 *   <li>Conversion of event details to JSON format.
 *   <li>Conflict detection with other events based on overlapping time intervals.
 * </ul>
 *
 * <p>Usage Example:
 *
 * <pre>
 * Event event = new Event.Builder("1", "Meeting")
 *     .description("Team meeting")
 *     .startDateTime(LocalDateTime.of(2023, 10, 1, 10, 0))
 *     .duration(Duration.ofHours(1))
 *     .location("Conference Room")
 *     .timeCreated(LocalDateTime.now())
 *     .timeUpdated(LocalDateTime.now())
 *     .build();
 * </pre>
 *
 * <p>Note: The {@link Interval} class is used internally to represent time intervals for conflict
 * detection.
 *
 * @see Builder
 */
@SuppressWarnings("PMD")
class Event implements Comparable<Event> {

  private String id;
  private String title;
  private String description;
  private LocalDateTime startDateTime;
  private Duration duration;
  private String location;
  private LocalDateTime timeCreated;
  private LocalDateTime timeUpdated;

  Event(Builder builder) {
    this.id = builder.id;
    this.title = builder.title;
    this.description = builder.description;
    this.startDateTime = builder.startDateTime;
    this.duration = builder.duration;
    this.location = builder.location;
    this.timeCreated = builder.timeCreated;
    this.timeUpdated = builder.timeUpdated;
  }

  private class Interval {

    private final LocalDateTime start;
    private final LocalDateTime end;

    Interval(LocalDateTime start, LocalDateTime end) {
      this.start = start;
      this.end = end;
    }

    boolean isOverlap(Interval other) {
      return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }
  }

  static class Builder {

    private String id;
    private String title;
    private String description = null;
    private LocalDateTime startDateTime;
    private Duration duration;
    private String location = null;
    private LocalDateTime timeCreated;
    private LocalDateTime timeUpdated;

    Builder(String id, String title) {
      this.id = id;
      this.title = title;
    }

    Builder description(String description) {
      this.description = description;
      return this;
    }

    Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    Builder duration(Duration duration) {
      this.duration = duration;
      return this;
    }

    Builder location(String location) {
      this.location = location;
      return this;
    }

    Builder timeCreated(LocalDateTime timeCreated) {
      this.timeCreated = timeCreated;
      return this;
    }

    Builder timeUpdated(LocalDateTime timeUpdated) {
      this.timeUpdated = timeUpdated;
      return this;
    }

    Event build() {
      return new Event(this);
    }
  }

  String toJsonString() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();

    return gson.toJson(this);
  }

  boolean isConflict(Event other) {
    if (other == null) {
      return false;
    }
    Interval thisInterval =
        new Interval(this.startDateTime, this.startDateTime.plusSeconds(this.duration.toSeconds()));
    Interval otherInterval =
        new Interval(
            other.startDateTime, other.startDateTime.plusSeconds(other.duration.toSeconds()));
    return thisInterval.isOverlap(otherInterval);
  }

  String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  String getDescription() {
    return description;
  }

  void setDescription(String description) {
    this.description = description;
  }

  LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  Duration getDuration() {
    return duration;
  }

  void setDuration(Duration duration) {
    this.duration = duration;
  }

  String getLocation() {
    return location;
  }

  void setLocation(String location) {
    this.location = location;
  }

  LocalDateTime getTimeCreated() {
    return timeCreated;
  }

  void setTimeCreated(LocalDateTime timeCreated) {
    this.timeCreated = timeCreated;
  }

  LocalDateTime getTimeUpdated() {
    return timeUpdated;
  }

  void setTimeUpdated(LocalDateTime timeUpdated) {
    this.timeUpdated = timeUpdated;
  }

  @Override
  public int compareTo(Event o) {
    return this.startDateTime.compareTo(o.startDateTime);
  }
}
