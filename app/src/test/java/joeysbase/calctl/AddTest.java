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

class AddTest {

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
  }

  @AfterEach
  void cleanUp() throws Exception {
    Path path = Path.of(EVENT_DATA_FILE);
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }

  @Test
  void testRunWithValidInputs() {
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

    assertEquals(0, exitcode);
    // assertTrue(out.toString().contains("added"));
  }

  @Test
  void testRunWithMissingRequiredFields() {

    int exitcode =
        cmd.execute(
            "add",
            "--title",
            "", // Missing title
            "--date",
            "2023-10-10",
            "--time",
            "14:00",
            "--duration",
            "1h 30m");

    assertEquals(1, exitcode);
    // assertTrue(err.toString().equals("Error: --title, --date, --time, and --duration are required
    // fields."));
  }

  @Test
  void testRunWithInvalidDateFormat() {

    int exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting",
            "--date",
            "10-10-2023", // Invalid format
            "--time",
            "14:00",
            "--duration",
            "1h 30m");

    assertEquals(1, exitcode);
    // assertTrue(err.toString().contains("Invalid date format"));
  }

  @Test
  void testRunWithInvalidTimeFormat() {

    int exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting",
            "--date",
            "2023-10-10",
            "--time",
            "2 PM", // Invalid format
            "--duration",
            "1h 30m");

    assertEquals(1, exitcode);
    // assertTrue(err.toString().contains("Invalid time format"));
  }

  @Test
  void testRunWithInvalidDurationFormat() {
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
            "90 minutes");
    assertEquals(1, exitcode);
    // assertTrue(err.toString().contains("Invalid duration format"));
  }

  @Test
  void testRunWithConflictingEvents() {

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

    assertEquals(0, exitcode);
    // assertTrue(out.toString().equals("Event added successfully."));
    exitcode =
        cmd.execute(
            "add",
            "--title",
            "Meeting2",
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
    assertEquals(1, exitcode);
    // assertTrue(err.toString().equals("events found conflicted"));
  }

  @Test
  void testRunWithForceOption() {
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

    assertEquals(0, exitcode);
    // assertTrue(out.toString().equals("Event added successfully."));
    exitcode =
        cmd.execute(
            "--force",
            "add",
            "--title",
            "Meeting2",
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
    assertEquals(0, exitcode);
    // assertTrue(err.toString().equals("events found conflicted"));
  }
}
