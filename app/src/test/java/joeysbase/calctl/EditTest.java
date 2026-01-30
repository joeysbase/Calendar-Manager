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

class EditTest {

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
  }

  @AfterEach
  void cleanUp() throws Exception {
    Path path = Path.of(EVENT_DATA_FILE);
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }

  @Test
  void testRunWithValidIdAndNoConflicts() {
    int exitcode =
        cmd.execute("edit", "evt-931530237", "--title", "new Meeting", "--date", "2027-12-10");

    assertEquals(0, exitcode);
  }

  @Test
  void testRunWithNonExistentEventId() {
    int exitcode =
        cmd.execute("edit", "evt-930237", "--title", "new Meeting", "--date", "2027-12-10");

    assertEquals(1, exitcode);
  }

  @Test
  void testRunWithInvalidDateFormat() {
    int exitcode =
        cmd.execute("edit", "evt-931530237", "--title", "new Meeting", "--date", "2027-12*10");

    assertEquals(1, exitcode);
  }

  @Test
  void testRunWithInvalidTimeFormat() {
    int exitcode =
        cmd.execute(
            "edit",
            "evt-931530237",
            "--title",
            "new Meeting",
            "--date",
            "2027-12-10",
            "--time",
            "2 PM");

    assertEquals(1, exitcode);
  }

  @Test
  void testRunWithConflictsAndNoForceOption() {
    int exitcode =
        cmd.execute(
            "edit",
            "evt-931530237",
            "--title",
            "new Meeting",
            "--date",
            "2026-01-28",
            "--time",
            "14:00");

    assertEquals(1, exitcode);
  }

  @Test
  void testRunWithConflictsAndForceOption() {
    int exitcode =
        cmd.execute(
            "--force",
            "edit",
            "evt-931530237",
            "--title",
            "new Meeting",
            "--date",
            "2026-01-28",
            "--time",
            "14:00");

    assertEquals(0, exitcode);
  }

  @Test
  void testRunWithValidDateAndTime() {
    int exitcode =
        cmd.execute(
            "edit",
            "evt-931530237",
            "--title",
            "new Meeting",
            "--date",
            "2026-01-29",
            "--time",
            "14:00");

    assertEquals(0, exitcode);
  }
}
