package joeysbase.calctl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class ListCommandTest {

  private PrintWriter err;
  private CommandLine cmd;
  private PrintWriter out;
  private static final String EVENT_DATA_FILE =
      System.getProperty("user.home") + "/.calctl/events.json";

  @BeforeEach
  void setUp() throws Exception {
    err = new PrintWriter(new ByteArrayOutputStream(), true);
    out = new PrintWriter(new ByteArrayOutputStream(), true);
    cmd = new CommandLine(new CalendarControl()).setErr(err).setOut(out);
    Path path = Path.of(EVENT_DATA_FILE);
    if (Files.exists(path)) {
      Files.delete(path);
    }
    int exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting",
            "--date",
            "2023-10-10",
            "--time",
            "14:00",
            "--duration",
            "1h 30m",
            "--description",
            "Team meeting",
            "--location",
            "Conference Room");

    exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting2",
            "--date",
            "2026-01-28",
            "--time",
            "14:00",
            "--duration",
            "1h 30m",
            "--description",
            "Team meeting",
            "--location",
            "Conference Room");
    exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting7",
            "--date",
            "2026-01-30",
            "--time",
            "14:00",
            "--duration",
            "1h 30m",
            "--description",
            "Team meeting",
            "--location",
            "Conference Room");

    exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting3",
            "--date",
            "2026-02-28",
            "--time",
            "14:00",
            "--duration",
            "1h 30m",
            "--description",
            "Team meeting",
            "--location",
            "Conference Room");
  }

  @AfterEach
  void cleanUp() throws Exception {
    Path path = Path.of(EVENT_DATA_FILE);
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }

  @Test
  void testListAllEvents() {
    int exitcode = cmd.execute("list");

    assertEquals(0, exitcode);
  }

  @Test
  void testListEventsFromDate() {
    int exitcode = cmd.execute("list", "--from", "2026-01-27");

    assertEquals(0, exitcode);
  }

  @Test
  void testListEventsToDate() {
    int exitcode = cmd.execute("list", "--to", "2025-01-27");

    assertEquals(0, exitcode);
  }

  @Test
  void testListEventsFromAndToDate() {
    int exitcode = cmd.execute("list", "--from", "2025-01-27", "--to", "2026-03-02");
    assertEquals(0, exitcode);
  }

  @Test
  void testListTodayEvents() {
    int exitcode = cmd.execute("list", "--today");

    assertEquals(0, exitcode);
  }

  @Test
  void testListThisWeekEvents() {
    int exitcode = cmd.execute("list", "--week");

    assertEquals(0, exitcode);
  }

  @Test
  void testInvalidFromDate() {
    int exitcode = cmd.execute("list", "--from", "2026-0-27");

    assertEquals(1, exitcode);
  }

  @Test
  void testInvalidToDate() {
    int exitcode = cmd.execute("list", "--to", "2026-0-27");

    assertEquals(1, exitcode);
  }

  @Test
  void testFromDateAfterToDate() {
    int exitcode = cmd.execute("list", "--from", "2027-01-27", "--to", "2026-02-29");

    assertEquals(1, exitcode);
  }

  @Test
  void testNoEventsFound() {
    int exitcode = cmd.execute("list", "--from", "2027-01-27", "--to", "2028-02-29");

    assertEquals(0, exitcode);
  }
}
