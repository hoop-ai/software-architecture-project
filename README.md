# Task Management System

**SEN3006 — Software Architecture Project**

A task management system built in pure Java demonstrating two design patterns: **Factory Method** (Creational) and **Strategy** (Behavioral). The system manages different types of software development tasks (bugs, features, documentation) with swappable prioritization algorithms.

**Submission Deadline:** June 05, 2026 | **Last Updated:** March 19, 2026

---

## Quick Start (GUI demo) — easiest

The fastest way to see the project running:

1. Unzip `SEN3006_Task_Management_System.zip` (or clone the repo).
2. Double-click **`TaskManagerGUI.jar`** (or **`run-gui.bat`** on Windows / **`run-gui.sh`** on Mac/Linux).
3. The Task Manager window opens. Use the **Demo** menu to load preset scenarios, or fill in the form to add your own. Switch the **Sort by** dropdown to see the Strategy pattern reorder the table live.

Requires Java 8 or newer. If double-click does not work on your OS, run from a terminal:

```sh
java -jar TaskManagerGUI.jar
```

---

## Quick Start

Pure Java, zero dependencies. Three commands and you're running.

### Prerequisites

- **JDK 8 or later** — verify with `java -version` and `javac -version`. If missing, install [Eclipse Temurin](https://adoptium.net/) (cross-platform), or `winget install Microsoft.OpenJDK.21` on Windows, `brew install openjdk` on macOS, or `sudo apt install default-jdk` on Linux.
- **Any terminal** — Windows CMD, PowerShell, Git Bash, macOS Terminal, or Linux shells. The commands below are identical in all of them.

### Step 1: Compile

From the project root, run:

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

This creates a `bin/` folder and compiles all source files (engine + GUI). No output means success.

### Step 2: Run the Automated Tests

```sh
java -cp bin Main
```

Look for `ALL TESTS PASSED` at the bottom — that confirms everything works.

### Step 3: Run the Interactive Application

```sh
java -cp bin TaskManagementApp
```

This launches the **interactive task management system** with a menu:

```
  MAIN MENU
  1. Create a new task          ← Uses Factory Method pattern
  2. View all tasks
  3. View tasks (sorted)        ← Uses Strategy pattern
  4. Change sorting strategy    ← Swap strategy at runtime
  5. Change task status         ← State machine transitions
  6. Filter tasks by status
  7. View task details
  8. Remove a task
  9. View summary
  0. Exit
```

**Quick demo to try:**
1. Type `1` → Enter → `BUG` → Enter → type a title → Enter → type a description → Enter → `5` → Enter → `n` → Enter
2. Type `1` → Enter → `FEATURE` → Enter → type a title → Enter → type a description → Enter → `2` → Enter → `n` → Enter
3. Type `3` → Enter → see tasks sorted by priority (highest first)
4. Type `4` → Enter → `2` → Enter → switch to DeadlineFirst sorting
5. Type `5` → Enter → pick a task ID → Enter → `IN_PROGRESS` → Enter → move a task forward
6. Type `0` → Enter → exit

> **Troubleshooting:** see [docs/design/test-documentation.md](docs/design/test-documentation.md#troubleshooting) for fixes to common issues (`javac` not found, wrong folder, garbled output, etc.).

---

## Project Structure

```
├── src/
│   └── main/
│       └── java/                    ← All 17 Java source files
│           ├── Task.java                 (Product interface)
│           ├── AbstractTask.java         (Abstract base class)
│           ├── BugTask.java              (Concrete task — bugs)
│           ├── FeatureTask.java          (Concrete task — features)
│           ├── DocumentationTask.java    (Concrete task — docs)
│           ├── TaskStatus.java           (Lifecycle state machine)
│           ├── TaskFactory.java          (Abstract creator)
│           ├── BugTaskFactory.java       (Creates bug tasks)
│           ├── FeatureTaskFactory.java   (Creates feature tasks)
│           ├── DocumentationTaskFactory.java (Creates doc tasks)
│           ├── PriorityStrategy.java     (Strategy interface)
│           ├── UrgentFirstStrategy.java  (Sort by priority)
│           ├── DeadlineFirstStrategy.java (Sort by deadline)
│           ├── SeverityFirstStrategy.java (Sort bugs by severity)
│           ├── TaskManager.java          (Central coordinator)
│           ├── TaskManagementApp.java    (Interactive console application)
│           └── Main.java                 (Automated tests — 6 test sections)
│
├── docs/
│   ├── report/
│   │   └── report.md                ← Full project report (10-14 pages)
│   │
│   ├── design/
│   │   ├── design-spec.md           ← System design specification
│   │   ├── test-documentation.md    ← How to run and verify tests
│   │   ├── study-guide.md           ← Presentation preparation guide
│   │   └── presentation-outline.md  ← Slide-by-slide presentation plan
│   │
│   └── uml/                        ← All UML diagrams
│       ├── class/
│       │   ├── task-management-class.puml    ← Class Diagram (PlantUML)
│       │   ├── class-diagram.md             ← Class Diagram (visual, Mermaid)
│       │   ├── component-diagram.puml       ← Component Diagram (PlantUML)
│       │   ├── component-diagram.md         ← Component Diagram (visual)
│       │   ├── deployment-diagram.puml      ← Deployment Diagram (PlantUML)
│       │   └── deployment-diagram.md        ← Deployment Diagram (visual)
│       ├── sequence/
│       │   ├── task-creation-sequence.puml   ← Sequence Diagram (PlantUML)
│       │   └── sequence-diagram.md          ← Sequence Diagram (visual)
│       ├── usecase/
│       │   ├── task-management-usecase.puml  ← Use Case Diagram (PlantUML)
│       │   └── usecase-diagram.md           ← Use Case Diagram (visual)
│       └── activity/
│           ├── task-lifecycle-activity.puml  ← Activity Diagram (PlantUML)
│           ├── activity-diagram.md          ← Activity Diagram (visual)
│           ├── task-state.puml              ← State Diagram (PlantUML)
│           └── state-diagram.md             ← State Diagram (visual)
│
└── guide.md                         ← Original project assignment
```

---

## Design Patterns Used

### 1. Factory Method (Creational Pattern)

**Problem:** Creating different task types (Bug, Feature, Documentation) without hardcoding `new BugTask()` everywhere.

**Solution:** An abstract `TaskFactory` defines a `createTask()` method. Each concrete factory (`BugTaskFactory`, `FeatureTaskFactory`, `DocumentationTaskFactory`) overrides it to produce the right task type.

**Benefit:** To add a new task type, you create two new files. Zero changes to existing code.

```
TaskFactory (abstract)
├── BugTaskFactory         → creates BugTask
├── FeatureTaskFactory     → creates FeatureTask
└── DocumentationTaskFactory → creates DocumentationTask
```

### 2. Strategy (Behavioral Pattern)

**Problem:** Tasks need to be sorted by different criteria (priority, deadline, severity) depending on the situation.

**Solution:** A `PriorityStrategy` interface defines a `sort()` method. Three concrete strategies implement different sorting algorithms. The `TaskManager` can swap strategies at runtime.

**Benefit:** To add a new sorting method, you create one new file. Zero changes to existing code.

```
PriorityStrategy (interface)
├── UrgentFirstStrategy     → sorts by priority (5 highest → 1 lowest)
├── DeadlineFirstStrategy   → sorts by deadline (earliest first)
└── SeverityFirstStrategy   → sorts bugs by severity, then others by priority
```

---

## UML Diagrams

All diagrams are available in two formats:
- **`.puml` files** — PlantUML source code (version-controlled, precise)
- **`.md` files** — Mermaid diagrams (viewable on GitHub, renders visually)

### Mandatory Diagrams (required by assignment)

| Diagram | Description | View |
|---|---|---|
| Class Diagram | All classes, interfaces, inheritance, and associations | [View](docs/uml/class/class-diagram.md) |
| Sequence Diagram | Task creation and prioritization flow | [View](docs/uml/sequence/sequence-diagram.md) |
| Use Case Diagram | All system features and user interactions | [View](docs/uml/usecase/usecase-diagram.md) |

### Optional Diagrams (recommended by assignment)

| Diagram | Description | View |
|---|---|---|
| Activity Diagram | Task lifecycle workflow from creation to completion | [View](docs/uml/activity/activity-diagram.md) |
| State Diagram | TaskStatus transitions (OPEN → IN_PROGRESS → REVIEW → DONE) | [View](docs/uml/activity/state-diagram.md) |
| Component Diagram | System modules and their dependencies | [View](docs/uml/class/component-diagram.md) |
| Deployment Diagram | Three-layer JVM architecture | [View](docs/uml/class/deployment-diagram.md) |

---

## Three Ways to Use the System

| Program | Command | Purpose |
|---|---|---|
| **Graphical UI** | `java -jar TaskManagerGUI.jar` | Click around — visual demo of all patterns, easiest for showing the project |
| **Interactive App** | `java -cp bin TaskManagementApp` | Console menu — create tasks, sort them, change status, view details |
| **Automated Tests** | `java -cp bin Main` | Verify the system — runs 6 test sections, all should show `[PASS]` |

## Automated Test Details

The `Main.java` file contains 6 test sections that verify the system works correctly:

| Test | What It Proves |
|---|---|
| Test 1: Factory Method Demo | Each factory creates the correct task type polymorphically |
| Test 2: Strategy Demo | Same task list sorted 3 different ways by swapping strategy |
| Test 3: Lifecycle Demo | Valid state transitions succeed, invalid ones are blocked |
| Test 4: Integration Demo | Full workflow — create, filter, transition, summarize |
| Test 5: SOLID Demo | All 5 SOLID principles demonstrated with code |
| Test 6: Edge Cases | Invalid priority, null title, unknown type, missing ID, null strategy |

Every test prints `[PASS]` when successful. The program ends with `ALL TESTS PASSED`.

For detailed test documentation, see [docs/design/test-documentation.md](docs/design/test-documentation.md).

---

## SOLID Principles

| Principle | How It's Applied |
|---|---|
| **S**ingle Responsibility | Each class has one job (Factory creates, Strategy sorts, Manager coordinates) |
| **O**pen/Closed | New task types or strategies added via new classes, no existing code modified |
| **L**iskov Substitution | All factories work through `TaskFactory` reference, all strategies through `PriorityStrategy` |
| **I**nterface Segregation | `PriorityStrategy` has one method; type-specific methods only on concrete classes |
| **D**ependency Inversion | `TaskManager` depends on interfaces (`Task`, `PriorityStrategy`), not concrete classes |

---

## Task Lifecycle

Tasks follow a state machine with 5 states:

```
OPEN ──────→ IN_PROGRESS ──────→ REVIEW ──────→ DONE (final)
  │               │                 │
  └──→ BLOCKED    └──→ BLOCKED      └──→ IN_PROGRESS (rejected)
         │
         └──→ OPEN (unblocked)
```

Invalid transitions (e.g., OPEN → DONE) throw an `IllegalArgumentException`.

---

## Documentation

| Document | Purpose |
|---|---|
| [Project Report](docs/report/report.md) | Full 10-14 page report with all 9 required sections |
| [Test Documentation](docs/design/test-documentation.md) | How to run tests, what each test does, edge cases |
| [Study Guide](docs/design/study-guide.md) | Presentation prep — every class explained, Q&A cheat sheet |
| [Presentation Outline](docs/design/presentation-outline.md) | Slide-by-slide plan with speaker notes |
| [Design Specification](docs/design/design-spec.md) | Technical design with all class signatures |
| [Assignment Guide](guide.md) | Original project requirements from the professor |

---

## Tools and Software

| Tool | Purpose | How to Install |
|---|---|---|
| Java JDK 21 | Compile and run Java code | `winget install Microsoft.OpenJDK.21` or [adoptium.net](https://adoptium.net/) |
| VS Code | Code editor | [code.visualstudio.com](https://code.visualstudio.com/) |
| Java Extension Pack | Java IntelliSense, debugging, testing in VS Code | Search "Java Extension Pack" in VS Code extensions |
| PlantUML Extension | Preview `.puml` diagram files in VS Code | Search "PlantUML" in VS Code extensions |
| Draw.io Extension | Visual diagram editor inside VS Code | Search "Draw.io" in VS Code extensions |
| Git | Version control | [git-scm.com](https://git-scm.com/) |

### How to View UML Diagrams

**Option 1 — GitHub (easiest):** Open the `.md` diagram files on GitHub — Mermaid diagrams render automatically.

**Option 2 — VS Code with PlantUML:**
1. Install the PlantUML extension
2. Open any `.puml` file
3. Press `Alt+D` to preview the diagram

**Option 3 — Online:** Copy the `.puml` content and paste at [plantuml.com](https://www.plantuml.com/plantuml/uml/)

---

## Building from source

To rebuild `TaskManagerGUI.jar` after editing source files:

```sh
bash build-jar.sh    # Mac/Linux/Git Bash
build-jar.bat        # Windows cmd
```

This recompiles all classes (engine + GUI) and packages them with a `Main-Class: TaskManagerGUI` manifest. The resulting JAR is the one shipped to the professor.
