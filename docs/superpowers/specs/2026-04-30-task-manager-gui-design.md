# Task Manager GUI — Design Spec

**Date:** 2026-04-30
**Project:** SEN3006 Task Management System
**Goal:** Add a Swing-based graphical user interface as an additive demo wrapper around the existing Java engine, packaged for one-click use by the professor, without modifying any existing source file.

---

## 1. Motivation

The existing project is a pure-Java Task Management System demonstrating the Factory Method + Strategy design patterns. It already has two terminal entry points:

- `Main.java` — deterministic 6-section pattern walkthrough (test demo).
- `TaskManagementApp.java` — interactive menu-driven console application.

For the in-person demo, a graphical interface adds a third, visual entry point that will:

1. Make the design patterns *visible* — Strategy swap re-sorts a live table, Factory dispatch creates rows of the right concrete type, the state machine is rendered as enable/disable on transition buttons.
2. Let the professor click around freely instead of reading scrolling terminal output.
3. Provide canned scenario loaders so the deterministic test scenarios from `Main.java` can still be reproduced inside the GUI on demand.
4. Ship as a one-click runnable JAR for trivial setup.

## 2. Non-Goals

- No modification of any of the 16 existing Java files in `src/main/java/`.
- No new external dependencies — Swing only (Java standard library).
- No persistence (tasks live in memory; closing the window discards them, same as the terminal demo).
- No network/multi-user features.
- No replacement of the existing entry points — the GUI is a *third alternative* entry point. `java -cp bin Main` and `java -cp bin TaskManagementApp` both continue to work identically.

## 3. Architecture

### 3.1 Layering

```text
┌─────────────────────────────────────────────────┐
│  gui/  (NEW — Swing presentation layer)         │
│  ├─ TaskManagerGUI       (main window, entry)   │
│  ├─ TaskFormPanel        (create-task form)     │
│  ├─ TaskTablePanel       (table + controls)     │
│  ├─ TaskTableModel       (Swing TableModel)     │
│  └─ DemoScenarios        (canned data loaders)  │
└────────────────────┬────────────────────────────┘
                     │ public API only
                     ▼
┌─────────────────────────────────────────────────┐
│  default package  (UNCHANGED — engine)          │
│  TaskManager, TaskFactory, PriorityStrategy,    │
│  Task, TaskStatus, BugTask, FeatureTask, ...    │
└─────────────────────────────────────────────────┘
```

The GUI layer **only consumes the public API** of the engine — `manager.createTask(...)`, `manager.setPriorityStrategy(...)`, `manager.getPrioritizedTasks()`, `manager.transitionTask(...)`, `TaskStatus.canTransitionTo(...)`. No reflection, no internal state access. This means the GUI is itself a faithful demonstration of the patterns rather than a parallel implementation.

### 3.2 File layout (additions only)

```text
src/main/java/
├── Main.java                   ← UNTOUCHED
├── TaskManager.java            ← UNTOUCHED
├── ... 14 other engine files   ← UNTOUCHED
└── gui/                        ← NEW package
    ├── TaskManagerGUI.java     ← second main(); builds JFrame
    ├── TaskTableModel.java     ← AbstractTableModel over TaskManager
    ├── TaskFormPanel.java      ← create-task form (top of window)
    ├── TaskTablePanel.java     ← table, sort dropdown, status filter, action buttons
    └── DemoScenarios.java      ← reusable canned-data loaders
```

```text
project root/
├── README.md                   ← updated (additive sections)
├── SUBMISSION_README.md        ← UNTOUCHED (academic submission text)
├── SEN3006_Task_Management_System.zip ← rebuilt to include GUI + JAR
├── TaskManagerGUI.jar          ← NEW (built artifact, in zip)
├── run-gui.bat                 ← NEW (Windows launcher)
├── run-gui.sh                  ← NEW (Mac/Linux launcher)
└── build-jar.sh                ← NEW (one-time build helper for student)
```

### 3.3 Why a new sub-package

The existing engine classes are all in the default package (per project rule: "No package declarations — all files in default package"). To avoid touching them, the GUI lives in a new `gui` package. From `gui`, default-package types are accessed via the unnamed-package import workaround (since Java 8 forbids importing default-package types directly): the GUI files do **not** declare `package gui;` either — they live in default package too, but in a `gui/` subdirectory only for organizational clarity, and prefix their class names with `Gui` to namespace by convention.

**Decision:** keep all GUI files in the default package as well (no `package gui;` declaration), but place them in `src/main/java/gui/` for folder-level separation. The compile command `javac -d bin src/main/java/*.java src/main/java/gui/*.java` flattens them into `bin/`. This sidesteps Java 8's default-package import restriction while preserving folder-level organization.

## 4. UI Design

### 4.1 Window layout

Single `JFrame` (~900x650), `BorderLayout`:

```text
┌─ TaskManagerGUI ─────────────────────────────────────────────┐
│  Menu bar: Demo ▾  |  Help ▾                                 │
├──────────────────────────────────────────────────────────────┤
│  TaskFormPanel (NORTH)                                       │
│    Type: [BUG ▾]  Title: [____]  Priority: [3 ▾]             │
│    Description: [____________]  [+ Add Task]                 │
├──────────────────────────────────────────────────────────────┤
│  Sort/Filter strip (CENTER top)                              │
│    Sort: [Urgent First ▾]   Filter: [All Statuses ▾]         │
├──────────────────────────────────────────────────────────────┤
│  TaskTable (CENTER)                                          │
│    ID | Type | Title | Priority | Status | Deadline          │
│    (color-coded rows by status)                              │
├──────────────────────────────────────────────────────────────┤
│  Action buttons (CENTER bottom, only enabled when row sel'd) │
│    [→ IN_PROGRESS] [→ REVIEW] [→ DONE] [Block] [Delete]      │
├──────────────────────────────────────────────────────────────┤
│  Status bar (SOUTH)                                          │
│    "5 tasks | 2 OPEN, 1 IN_PROGRESS, 1 REVIEW, 1 DONE"       │
└──────────────────────────────────────────────────────────────┘
```

### 4.2 Pattern visibility (the demo value)

| User action                         | Engine call                          | Pattern shown          |
|-------------------------------------|--------------------------------------|------------------------|
| Pick type + click "Add Task"        | `manager.createTask(type, ...)`      | Factory Method (via Service Locator registry) |
| Switch "Sort by" dropdown           | `manager.setPriorityStrategy(new X)` then refresh | Strategy (live swap)   |
| Click a transition button           | `manager.transitionTask(id, status)` | State machine validation |
| Transition buttons auto-disable     | `TaskStatus.canTransitionTo(...)`    | Invariants made visible |
| Demo menu → "Load Strategy Demo"    | `DemoScenarios.loadStrategyDemo(m)`  | Same data as `Main.java` Test 2 |

### 4.3 Error handling

The engine already throws `IllegalArgumentException` with descriptive messages (null title, priority out of range, unknown type, invalid transition, etc.). The GUI catches these at the action-listener boundary and displays them via `JOptionPane.showMessageDialog(frame, ex.getMessage(), "Validation error", ERROR_MESSAGE)`. **No new validation logic** — the GUI delegates entirely to the existing engine's validation.

### 4.4 Look and feel

`UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel())` at startup so the window matches the host OS (Windows on the professor's machine, presumably). Status row colors:

- `OPEN` → light gray
- `IN_PROGRESS` → light blue
- `REVIEW` → light yellow
- `BLOCKED` → light red
- `DONE` → light green

## 5. Demo Scenarios

`DemoScenarios.java` exposes static methods, each taking a `TaskManager` and pre-populating it with the same data the corresponding section of `Main.java` builds:

- `loadStrategyDemo(TaskManager m)` — 5 tasks with deadlines and severities; matches Test 2 setup so the professor can see the three sortings live.
- `loadLifecycleDemo(TaskManager m)` — single task ready for state transitions; matches Test 3.
- `loadIntegrationDemo(TaskManager m)` — 5-task mix with transitions applied; matches Test 4.
- `clearAll(TaskManager m)` — removes every task (calls `removeTask` for each id).

These methods construct **new** task data using the same factory and manager APIs `Main.java` uses. We are **not** extracting code from `Main.java` — we are duplicating the data setup inside the GUI package. `Main.java` stays byte-identical.

## 6. Packaging

### 6.1 Runnable JAR

`build-jar.sh` (and a parallel `build-jar.bat` for Windows convenience):

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
echo "Main-Class: TaskManagerGUI" > manifest.txt
jar cfm TaskManagerGUI.jar manifest.txt -C bin .
rm manifest.txt
```

(Note: class name is `TaskManagerGUI` not `gui.TaskManagerGUI` because the GUI files live in default package per §3.3.)

The student runs this once before zipping. Result: a self-contained `TaskManagerGUI.jar` the professor can double-click on Windows/Mac, or launch with `java -jar TaskManagerGUI.jar`.

### 6.2 Launcher scripts

`run-gui.bat`:

```bat
@echo off
java -jar TaskManagerGUI.jar
```

`run-gui.sh`:

```bash
#!/bin/bash
java -jar TaskManagerGUI.jar
```

### 6.3 README updates (additive)

A new "Quick Start (GUI)" section at the top of `README.md`, followed by the existing terminal-demo content unchanged:

````markdown
## Quick Start (GUI demo)

The easiest way to see the project in action:

1. Unzip `SEN3006_Task_Management_System.zip`
2. Double-click `TaskManagerGUI.jar` (or `run-gui.bat` on Windows / `run-gui.sh` on Mac/Linux)
3. The Task Manager window opens. Try the **Demo** menu to load preset scenarios, or use the form to create your own tasks.

Requires Java 8 or newer.

## Quick Start (terminal demo)

For the original 6-section pattern walkthrough:

```bash
javac -d bin src/main/java/*.java
java -cp bin Main
```

Both entry points use the same engine — the GUI is a Swing wrapper that consumes the public API of `TaskManager`, `TaskFactory`, and `PriorityStrategy`.
````

The `SUBMISSION_README.md` (separate file for the formal academic submission) is **not** modified.

### 6.4 Zip rebuild

The existing `SEN3006_Task_Management_System.zip` is regenerated to include:

- All existing files (unchanged)
- `src/main/java/gui/*.java` (5 new files)
- `TaskManagerGUI.jar` (built artifact)
- `run-gui.bat`, `run-gui.sh`
- Updated `README.md`

A safety copy of the original zip is preserved as `SEN3006_Task_Management_System-original.zip` in case the student wants to revert.

## 7. Testing Strategy

### 7.1 Engine tests (unchanged)

`Main.java` keeps acting as the integration test for the engine. It must continue to print `ALL TESTS PASSED` after the GUI files are added — proving zero regression in the existing classes.

### 7.2 GUI smoke tests

Manual checklist (executed once after build, documented in spec):

- [ ] `java -jar TaskManagerGUI.jar` opens a window with no console errors.
- [ ] Adding a task via the form puts a new row in the table.
- [ ] Switching the "Sort by" dropdown reorders the table.
- [ ] Selecting a row and clicking → IN_PROGRESS updates the Status column.
- [ ] Clicking → DONE on an OPEN task is disabled (or shows the validation dialog).
- [ ] Demo → Load Strategy Demo populates 5 tasks; switching strategies reorders them.
- [ ] Closing and reopening: tasks reset (no persistence — by design).
- [ ] Double-clicking `run-gui.bat` (Windows) opens the window without a terminal command.

### 7.3 No automated GUI tests

JUnit / AssertJ-Swing are excluded — they would add external dependencies, violating the "0 external dependencies" project rule. Manual smoke tests are sufficient for a course demo of this size.

## 8. Risks and Open Questions

| Risk                                              | Mitigation                                                  |
|---------------------------------------------------|-------------------------------------------------------------|
| Default-package import workaround feels hacky     | Documented in §3.3; Java 8 limitation, not project-specific |
| Double-click on `.jar` may not work on some OSes  | Launcher scripts (`run-gui.bat` / `run-gui.sh`) as fallback |
| Professor's Java version is < 8                   | Course requires Java 8+; same prereq as the terminal demo   |
| Rebuilding zip might miss new files               | Document the zip rebuild step explicitly in the impl plan   |

## 9. Success Criteria

1. `Main.java` produces byte-identical output before and after the GUI is added (proves zero regression).
2. Double-clicking `TaskManagerGUI.jar` on a Windows machine with Java 8+ opens the window.
3. The Strategy swap, Factory dispatch, and state machine are all visibly demonstrable in the GUI.
4. README's new "Quick Start (GUI)" section gives the professor a 2-step path to the running app.
5. No modification of any existing `.java` file in `src/main/java/` (verified by `git diff` showing only additions in `gui/` and a single additive section in `README.md`).
