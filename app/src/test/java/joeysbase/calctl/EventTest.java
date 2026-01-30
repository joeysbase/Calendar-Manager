package joeysbase.calctl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.northeastern.timeduration.Duration;

class EventTest {

  @Test
  void testEventBuilderAndGetters() {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.fromMinutes(120);

    Event event =
        new Event.Builder("1", "Meeting")
            .description("Team meeting")
            .startDateTime(now)
            .duration(duration)
            .location("Conference Room")
            .timeCreated(now)
            .timeUpdated(now)
            .build();

    assertEquals("1", event.getId());
    assertEquals("Meeting", event.getTitle());
    assertEquals("Team meeting", event.getDescription());
    assertEquals(now, event.getStartDateTime());
    assertEquals(duration, event.getDuration());
    assertEquals("Conference Room", event.getLocation());
    assertEquals(now, event.getTimeCreated());
    assertEquals(now, event.getTimeUpdated());
  }

  @Test
  void testEventConflict() {
    LocalDateTime now = LocalDateTime.now();
    Duration duration1 = Duration.fromMinutes(120);
    Duration duration2 = Duration.fromMinutes(60);

    Event event1 =
        new Event.Builder("1", "Meeting 1").startDateTime(now).duration(duration1).build();

    Event event2 =
        new Event.Builder("2", "Meeting 2")
            .startDateTime(now.plusHours(1))
            .duration(duration2)
            .build();

    Event event3 =
        new Event.Builder("3", "Meeting 3")
            .startDateTime(now.plusHours(3))
            .duration(duration2)
            .build();

    assertTrue(event1.isConflict(event2)); // Overlapping events
    assertFalse(event1.isConflict(event3)); // Non-overlapping events
  }

  @Test
  void testToJsonString() {
    LocalDateTime now = LocalDateTime.now();
    Duration duration = Duration.fromMinutes(120);

    Event event =
        new Event.Builder("1", "Meeting")
            .description("Team meeting")
            .startDateTime(now)
            .duration(duration)
            .location("Conference Room")
            .timeCreated(now)
            .timeUpdated(now)
            .build();

    String json = event.toJsonString();
    // System.out.println(json);
    assertTrue(json.contains("\"id\":\"1\""));
    assertTrue(json.contains("\"title\":\"Meeting\""));
    assertTrue(json.contains("\"description\":\"Team meeting\""));
    assertTrue(json.contains("\"location\":\"Conference Room\""));
  }

  @Test
  void testCompareTo() {
    LocalDateTime now = LocalDateTime.now();

    Event event1 = new Event.Builder("1", "Meeting 1").startDateTime(now).build();

    Event event2 = new Event.Builder("2", "Meeting 2").startDateTime(now.plusHours(1)).build();

    assertTrue(event1.compareTo(event2) < 0); // event1 is earlier than event2
    assertTrue(event2.compareTo(event1) > 0); // event2 is later than event1
    assertEquals(0, event1.compareTo(event1)); // Same event
  }
}
