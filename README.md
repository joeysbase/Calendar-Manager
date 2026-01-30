# Calendar Manager

**calctl** (calendar control) is a command-line calendar management tool that helps users organize, track, and manage appointments and events.

## Command Overview

```text
calctl [global-options] <command> [command-options]
```

### Commands

| Command  | Description              |
| -------- | ------------------------ |
| `add`    | Add a new event          |
| `list`   | List events with filters |
| `show`   | Show full event details  |
| `edit`   | Edit an existing event   |
| `delete` | Delete event(s)          |
| `search` | Search events            |
| `agenda` | Display agenda view      |

### Global Options

| Option          | Description            |
| --------------- | ---------------------- |
| `-h, --help`    | Show help              |
| `-v, --version` | Show version           |
| `--json`        | Output JSON            |
| `--plain`       | Disable formatting     |
| `--no-color`    | Disable colored output |

## Event Model

Each event includes the following fields:

| Field       | Required | Description                       |
| ----------- | -------- | --------------------------------- |
| ID          | Yes      | Auto-generated (e.g. `evt-7d3f`)  |
| Title       | Yes      | Event title                       |
| Description | No       | Optional description              |
| Date        | Yes      | Event date (ISO format)           |
| Start Time  | Yes      | 24-hour time                      |
| Duration    | Yes      | Time duration                     |
| Location    | No       | Optional location                 |
| Created     | Yes      | Creation timestamp                |
| Updated     | Yes      | Last update timestamp             |

---

## Usage Examples

### Add Event

```bash
calctl add \
  --title "Team Meeting" \
  --date 2024-03-15 \
  --time 14:00 \
  --duration 60m
```

### List Events

```bash
calctl list
calctl list --today
calctl list --week
calctl list --from 2024-03-01 --to 2024-03-31
```

### Agenda View

```bash
calctl agenda
calctl agenda --week
calctl agenda --date 2024-03-15
```

### Edit Event

```bash
calctl edit evt-8a2f --time 14:30 --duration 45m
```

### Delete Event

```bash
calctl delete evt-8a2f
calctl delete --date 2024-03-15 --force
```

### Search Events

```bash
calctl search "meeting"
calctl search --title "standup"
```

## Data Storage

Events are stored locally in JSON format:

```text
~/.calctl/events.json
```

The file is created automatically if it does not exist. Writes are performed atomically to prevent corruption.

## Conflict Detection

Events are considered conflicting if their time ranges overlap:

```
A.start < B.end && B.start < A.end
```

* Conflicts are detected during `add` and `edit`
* Conflicts are shown during `show`
* Use `--force` to bypass conflict validation

## Build Instructions

### Prerequisites

* Java 21+
* Gradle (or `./gradlew`)

### Build and Test
```shell
# this command will run the test as well
./gradlew build 

# only run test. This command will generate jacoco report at /lib/build/jacocoHtml
./gradlew test 
```
### Generate Javadoc
```shell
# generate javadoc to /lib/build/doc/javadoc
./gradlew javadoc
```
### Style Check
```shell
# generate style check report at /lib/build/reports/checkstyle
./gradlew checkstyleMain
```
### Static Analysis
```shell
# generate analysis report at /lib/build/reports/pmd
./gradlew pmdMain
```
### Generate JAR
```shell
# generate .jar file at /lib/build/libs
./gradlew jar
```

## Docker Usage

### Build Image

```bash
# You may need to change permission if access denied
chmod 755 ./build.sh

./build.sh
```

### Run with Persistent Storage

```bash
# Add an event
docker run -v calctl-data:/home/appuser/.calctl calctl:latest add --title test1 --date 2026-01-30 --time 14:00 --duration 1h

# List all event
docker run -v calctl-data:/home/appuser/.calctl calctl:latest list

# Delete operation needs to be ran in interactive mode unless --force option specified
docker run -it -v calctl-data:/home/appuser/.calctl calctl:latest list
```

### Convenience Alias

```bash
alias calctl='docker run -v calctl-data:/home/appuser/.calctl calctl:latest'
```

