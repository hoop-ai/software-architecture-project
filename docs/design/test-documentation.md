# Test Documentation — Task Management System

## How to Compile and Run

This project is pure Java with **zero external dependencies**. If you have a JDK installed, you can compile and run it in three commands — no build tool, no Maven, no Gradle.

### Prerequisites

**JDK 8 or later** (JDK 11+ recommended). Check with:

```sh
java -version
javac -version
```

If both print a version, you're ready. If not, install one of:

- [Eclipse Temurin](https://adoptium.net/) (recommended, cross-platform)
- `winget install Microsoft.OpenJDK.21` (Windows)
- `brew install openjdk` (macOS)
- `sudo apt install default-jdk` (Debian/Ubuntu)

**A terminal.** Any shell works — Windows CMD, PowerShell, Git Bash, macOS Terminal, or Linux shells. The commands below are identical across all of them.

### Step 0: Open a terminal in the project folder

Either open this repo in VS Code → `Terminal` → `New Terminal`, or navigate manually:

```sh
cd path/to/software-architecture-project
```

Verify you're in the right place:

```sh
ls src/main/java
```

You should see `Main.java`, `TaskManagementApp.java`, and 14 other `.java` files.

### Step 1: Compile

Run this single command (works in **any shell** — CMD, PowerShell, bash, zsh):

```sh
javac -d bin src/main/java/*.java
```

What it does:

- Creates a `bin/` folder automatically (no `mkdir` needed)
- Compiles all 16 Java source files into `.class` files
- No output on success — silence means it worked

**Verify it worked:**

```sh
ls bin
```

You should see ~16 `.class` files (e.g. `Main.class`, `TaskManager.class`, ...).

### Step 2: Run the Automated Test Suite

```sh
java -cp bin Main
```

This runs 6 automated test sections. Scroll to the bottom — you should see:

```text
##########################################################
#                  ALL TESTS PASSED                      #
##########################################################
```

If you see that banner, **everything works.** ✅

### Step 3: Run the Interactive Application

```sh
java -cp bin TaskManagementApp
```

This launches the **interactive task management system**. You will see a menu:

```text
  MAIN MENU
  1. Create a new task          ← Uses Factory Method pattern
  2. View all tasks
  3. View tasks (sorted by current strategy)  ← Uses Strategy pattern
  4. Change sorting strategy    ← Swap strategy at runtime
  5. Change task status         ← State machine transitions
  6. Filter tasks by status
  7. View task details
  8. Remove a task
  9. View summary
  0. Exit
```

Type a number and press Enter to use each feature. The app will ask you follow-up questions (task type, title, etc.) — just type your answers and press Enter.

**Quick demo walkthrough:**

| Step | What to type | What it demonstrates |
|---|---|---|
| `1` → `BUG` → `Login crash` → `App crashes` → `5` → `n` | Creates a high-priority bug | **Factory Method** — BugTaskFactory creates a BugTask |
| `1` → `FEATURE` → `Dark mode` → `Add dark theme` → `2` → `n` | Creates a low-priority feature | **Factory Method** — FeatureTaskFactory creates a FeatureTask |
| `3` | View sorted tasks | **Strategy** — UrgentFirst sorts by priority (bug appears first) |
| `4` → `2` | Switch to DeadlineFirst | **Strategy** — swap algorithm at runtime |
| `3` | View sorted tasks again | Same tasks, different order — strategy swap worked |
| `5` → `1` → `IN_PROGRESS` | Move bug to In Progress | **State Machine** — valid transition |
| `5` → `1` → `DONE` | Try to skip to Done | **State Machine** — invalid transition blocked! |
| `0` | Exit the application | |

### Step 3: Run the Automated Tests

```bash
java -cp bin Main
```

If you see `ALL TESTS PASSED` at the end, the system is working correctly.

---

## Test Sections Explained

The `Main.java` file contains 6 test sections that demonstrate the system works correctly. Each test prints `[PASS]` when successful.

### Test 1: Factory Method Pattern Demo
**What it tests:** That each factory creates the correct task type without the caller knowing which concrete class is used.

**What you should see:**
- A BugTask created via `BugTaskFactory` → type shows "BUG", has severity field
- A FeatureTask created via `FeatureTaskFactory` → type shows "FEATURE", has effort/value fields
- A DocumentationTask created via `DocumentationTaskFactory` → type shows "DOCUMENTATION", has docType field
- A task created with a deadline using the template method `createTaskWithDeadline()`
- A task created with full custom parameters via specialized factory method

**Why this matters:** Proves the Factory Method pattern works — client code uses `TaskFactory` (abstract) and gets the right concrete product back.

### Test 2: Strategy Pattern Demo
**What it tests:** That the same list of tasks is sorted differently depending on which strategy is active.

**What you should see:**
- **UrgentFirstStrategy:** Tasks ordered by priority (5 first, 1 last)
- **DeadlineFirstStrategy:** Tasks ordered by deadline (earliest date first, no-deadline tasks at the end)
- **SeverityFirstStrategy:** Bug tasks appear first (CRITICAL before MEDIUM), then non-bug tasks by priority

**Why this matters:** Proves the Strategy pattern works — swapping the strategy at runtime changes behavior without modifying any code.

### Test 3: Task Lifecycle (State Transitions) Demo
**What it tests:** That the TaskStatus state machine correctly allows valid transitions and blocks invalid ones.

**What you should see:**
- Valid path: OPEN → IN_PROGRESS → REVIEW → DONE (all succeed)
- Blocked path: OPEN → BLOCKED → OPEN → IN_PROGRESS (unblocking works)
- Invalid transition: OPEN → DONE → throws `IllegalArgumentException` with message "Cannot transition from OPEN to DONE"
- Terminal state: DONE → OPEN → throws `IllegalArgumentException` (DONE is final, no transitions out)

**Why this matters:** Proves the state machine prevents invalid workflows and enforces business rules.

### Test 4: TaskManager Integration Demo
**What it tests:** The full workflow — creating, filtering, transitioning, and summarizing tasks.

**What you should see:**
- 5 tasks created via `TaskManager.createTask()` using the factory registry
- Tasks filtered by status (OPEN, IN_PROGRESS, REVIEW)
- A task summary showing counts per status
- A task removed by ID, remaining count decreases

**Why this matters:** Proves all components work together as a system.

### Test 5: SOLID Principles Demo
**What it tests:** That each SOLID principle is satisfied by the implementation.

**What you should see:**
- **OCP:** A new anonymous strategy added at runtime without modifying any existing class
- **LSP:** All three factories used through a `TaskFactory[]` array — all substitutable
- **DIP:** TaskManager's field types are all abstractions (Task, PriorityStrategy, TaskFactory)
- **SRP:** Each class has exactly one responsibility
- **ISP:** Interfaces are minimal (PriorityStrategy has one method, Task has only common methods)

**Why this matters:** Proves the code follows software engineering best practices required by the project.

### Test 6: Edge Cases and Error Handling
**What it tests:** That the system gracefully handles invalid inputs and boundary conditions.

**What you should see:**
- **Invalid priority (0):** `IllegalArgumentException` with message "Priority must be between 1 and 5, got: 0"
- **Null title:** `IllegalArgumentException` with message "Title must not be null or blank."
- **Unknown task type ("UNKNOWN_TYPE"):** `IllegalArgumentException` listing available types
- **Non-existent task ID (99999):** `IllegalArgumentException` with message "No task found with ID: 99999"
- **Null strategy:** `IllegalArgumentException` with message "Strategy must not be null."
- **Case-insensitive type ("bug" lowercase):** Successfully creates a BugTask, proving case-insensitive lookup works

**Why this matters:** Proves the system is robust and doesn't crash on bad input — every error is caught and reported clearly.

---

## Edge Cases Handled

| Edge Case | How It's Handled | Where |
|---|---|---|
| Invalid priority (0 or 6) | `IllegalArgumentException` thrown | `AbstractTask` constructor |
| Null or blank title | `IllegalArgumentException` thrown | `AbstractTask` constructor |
| Null description | `IllegalArgumentException` thrown | `AbstractTask` constructor |
| Invalid state transition | `IllegalArgumentException` thrown | `AbstractTask.setStatus()` |
| Transition from terminal DONE state | Blocked, exception thrown | `TaskStatus.DONE.canTransitionTo()` |
| Unknown task type in factory registry | `IllegalArgumentException` with available types listed | `TaskManager.createTask()` |
| Task not found by ID | `IllegalArgumentException` thrown | `TaskManager.getTask()` |
| Null task added | `IllegalArgumentException` thrown | `TaskManager.addTask()` |
| Null strategy set | `IllegalArgumentException` thrown | `TaskManager.setPriorityStrategy()` |
| Null deadline in sorting | Pushed to end of list | `DeadlineFirstStrategy.sort()` |
| Case-insensitive task type | `.toUpperCase()` applied | `TaskManager.createTask()` |

---

## Expected Console Output

Running `java -cp bin Main` produces approximately 150 lines of formatted output. The output ends with:

```
##########################################################
#                  ALL TESTS PASSED                      #
##########################################################
```

If any test fails, the specific `[PASS]` marker will be missing and an error message will indicate what went wrong.

---

## Verification Checklist

After running the program, verify:

- [ ] All 6 test sections show `[PASS]`
- [ ] Test 1 creates 3 different task types correctly
- [ ] Test 2 shows 3 different orderings of the same 5 tasks
- [ ] Test 3 catches 2 expected exceptions (invalid transition + terminal state)
- [ ] Test 4 shows filtered views and task removal
- [ ] Test 5 demonstrates all 5 SOLID principles
- [ ] Test 6 catches all 6 edge cases (6/6 passed)
- [ ] Final summary shows "ALL TESTS PASSED"
- [ ] No compilation warnings or errors

---

## Troubleshooting

### `javac` or `java` is not recognized

Your JDK is not on the system `PATH`. Pick one:

- **Quickest fix (Windows, if you use VS Code):** VS Code's Red Hat Java extension bundles a JDK. In Git Bash you can point your current terminal at it:

  ```sh
  export PATH="$HOME/.vscode/extensions/redhat.java-1.53.0-win32-x64/jre/21.0.10-win32-x86_64/bin:$PATH"
  ```

  (Adjust the version number if your extension is newer.)

- **Proper fix:** Install a JDK — see [Prerequisites](#prerequisites) — and restart your terminal.

### `error: file not found: src/main/java/*.java`

You're not in the project root. Run `pwd` (bash/PowerShell) or `cd` (CMD) to check. Navigate to the folder that contains `src/` and `README.md`, then retry.

### `Error: Could not find or load main class Main`

The `bin/` folder is empty or missing — compilation didn't run or failed. Re-run Step 1:

```sh
javac -d bin src/main/java/*.java
```

Make sure it completes with no error output, then retry Step 2.

### Wildcards don't expand (rare, old shells)

If `src/main/java/*.java` isn't being recognized, pass the files explicitly using Java's built-in argument file support:

```sh
javac -d bin src/main/java/Task.java src/main/java/AbstractTask.java src/main/java/BugTask.java src/main/java/FeatureTask.java src/main/java/DocumentationTask.java src/main/java/TaskStatus.java src/main/java/TaskFactory.java src/main/java/BugTaskFactory.java src/main/java/FeatureTaskFactory.java src/main/java/DocumentationTaskFactory.java src/main/java/PriorityStrategy.java src/main/java/UrgentFirstStrategy.java src/main/java/DeadlineFirstStrategy.java src/main/java/SeverityFirstStrategy.java src/main/java/TaskManager.java src/main/java/TaskManagementApp.java src/main/java/Main.java
```

### Output looks garbled / strange characters

The tests use a few Unicode symbols. If your terminal shows `?` or boxes, set UTF-8:

- **Windows CMD:** `chcp 65001` before running
- **PowerShell:** `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8`
- **Git Bash / macOS / Linux:** already UTF-8 by default

### `Exception in thread "main" java.lang.UnsupportedClassVersionError`

Your `java` version is older than your `javac` version. Run `java -version` and `javac -version` — they should match. If not, install a matching JDK.
