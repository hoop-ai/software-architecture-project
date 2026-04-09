# SEN3006 – Software Architecture
# Java Design Pattern Project

---

## GENERAL INFORMATION

**Applicants Name:**
- Mohammad Houjeirat — 2285763
- Abdul Rahman Malak — 2285310

**Project Title:** Task Management System for Software Development Teams — A Factory Method and Strategy Pattern Implementation

**Course:** SEN3006 – Software Architecture

---

## SUMMARY

This project delivers a Task Management System built in pure Java that demonstrates two complementary Gang-of-Four design patterns — **Factory Method** (creational) and **Strategy** (behavioral) — applied to a realistic software-engineering problem: coordinating heterogeneous work items (bugs, features, documentation) under changing prioritization needs.

- **Originality of the project:** Unlike textbook examples that showcase a single pattern in isolation, this project composes two patterns inside one coherent system. A single coordinator class (`TaskManager`) simultaneously acts as a Factory Method *client* and a Strategy *context*, making the system extensible along two independent axes — new task types and new prioritization algorithms — without modifying existing code.
- **Methodology (design patterns used):** Factory Method is used for polymorphic task creation; Strategy is used for runtime-swappable prioritization. The design is driven by SOLID principles and validated through seven UML diagrams (class, sequence, use case, activity, state, component, deployment).
- **Project management approach:** Work is structured into five work packages (WP1–WP5) — problem definition, design & UML, implementation, testing, and reporting — each with defined deliverables and risk-mitigation strategies.
- **Expected impact:** The project demonstrates that high-quality architecture can be achieved without heavy frameworks. It serves as an educational reference for applying GoF patterns and SOLID principles in real Java code, and provides a foundation that can be extended toward a production-grade task tracking tool.

---

## 1. ORIGINALITY

### 1.1 Importance and Originality

**The problem.** Software development teams operate on heterogeneous work items — bug reports, feature requests, and documentation tasks — each with distinct attributes, workflows, and prioritization needs. A bug has a severity level and reproduction steps; a feature has an effort estimate and business value; a documentation task has a document type and target audience. On top of this, the *right* order in which to work on these items depends on context: crisis mode demands urgent-first sorting, sprint planning demands deadline-first sorting, and a stabilization phase demands severity-first sorting.

**Why it is important.** Every software team — from a two-person startup to a thousand-engineer organization — needs to coordinate such work. The quality of the underlying architecture directly determines how easily the team can adapt when new task types or new prioritization rules are introduced. Poor architecture translates into brittle code, regression bugs, and slower delivery.

**Why current solutions are insufficient.** Naive implementations embed task-creation logic in long `if/else` or `switch` blocks and hard-code sorting algorithms inside the manager class. This tightly couples the system to its current task types and strategies: introducing a new task type (e.g., `ResearchTask`) or a new prioritization rule forces modifications across the codebase, violating the Open/Closed Principle and creating a brittle maintenance burden. Existing commercial tools (Jira, Trello, Linear) solve the *user* problem but hide their architecture and therefore cannot serve as a pedagogical reference for students learning design patterns.

**Research question.** *How can design patterns be composed to build a task management system that is simultaneously extensible in its product hierarchy and flexible in its algorithmic behavior, without sacrificing clarity or introducing external dependencies?*

The original contribution of this project is the demonstration of **double extensibility** through the deliberate composition of Factory Method and Strategy inside a single coordinator class, accompanied by a complete UML model, a working Java implementation, and test sections that explicitly exercise each SOLID principle.

### 1.2 Aim and Scope

**System objectives:**
1. Design and implement a Task Management System in pure Java using Factory Method and Strategy patterns.
2. Demonstrate that both patterns compose naturally inside a single coordinator.
3. Validate the design against all five SOLID principles through an explicit test suite.
4. Produce a complete UML model and written report suitable for architectural review.

**Scope — Included:**
- Three task types: Bug, Feature, Documentation (each with type-specific fields).
- Three prioritization strategies: Urgent-First, Deadline-First, Severity-First.
- A validated task lifecycle state machine (OPEN, IN_PROGRESS, REVIEW, DONE, BLOCKED).
- A `TaskManager` that coordinates creation, prioritization, filtering, and reporting.
- Seven UML diagrams and a complete test suite in `Main.java` (six sections).
- A separate interactive console application (`TaskManagementApp.java`) for live demonstration.
- Zero external dependencies — only `java.util` and `java.time`.

**Scope — Excluded:** persistent storage, multi-user concurrency, GUI, authentication, network services. These are acknowledged as future work.

**Expected outcomes:**
1. A fully working Java console application demonstrating both patterns.
2. UML documentation suitable for architectural review.
3. A written report explaining design decisions, trade-offs, and counterfactual analysis.
4. A reusable educational artifact illustrating SOLID principles in action.

---

## 2. METHOD

### System Architecture

The system follows a layered object-oriented architecture organized into five logical components:

- **Core:** `Task` interface, `AbstractTask` abstract class, `TaskStatus` enum — foundational abstractions.
- **Products:** `BugTask`, `FeatureTask`, `DocumentationTask` — concrete task types.
- **Factories:** `TaskFactory` abstract class and three concrete factories — the Factory Method hierarchy.
- **Strategies:** `PriorityStrategy` interface and three concrete strategies — the Strategy hierarchy.
- **Coordinator:** `TaskManager` — the central class that acts as client of Factory Method and context of Strategy.

### Selected Design Patterns (at least two)

**1. Factory Method (Creational).** Defers instantiation to subclasses. `TaskFactory` declares the abstract `createTask(...)` method; `BugTaskFactory`, `FeatureTaskFactory`, and `DocumentationTaskFactory` each override it to produce their specific product with sensible defaults. `TaskFactory` additionally provides a `createTaskWithDeadline(...)` template method layered on top of the factory method.

**2. Strategy (Behavioral).** Encapsulates a family of algorithms behind a common interface. `PriorityStrategy.sort(List<Task>)` is implemented by `UrgentFirstStrategy` (priority descending), `DeadlineFirstStrategy` (deadline ascending, nulls last), and `SeverityFirstStrategy` (two-tier sort: bugs first by severity rank, then non-bugs by priority). `TaskManager` holds a `PriorityStrategy` reference (`currentStrategy`) and delegates sorting to it; strategies are swappable at runtime via `setPriorityStrategy(...)`.

These two patterns were chosen because they address orthogonal concerns — *object creation* and *algorithmic behavior* — and their composition inside `TaskManager` illustrates how patterns can coexist naturally in a single design.

**Additional creative twist — Simplified Service Locator.** `TaskManager` keeps an internal `Map<String, TaskFactory>` registry that maps type strings (`"BUG"`, `"FEATURE"`, `"DOCUMENTATION"`) to factory instances. This layers a simplified Service Locator on top of the Factory Method hierarchy, so clients can request task creation by name without knowing which factory to use.

### UML Diagrams (Class, Sequence, Use Case, and more)

Seven diagrams are produced as PlantUML sources in [docs/uml/](../uml/):

| Diagram | Location | Purpose |
|---|---|---|
| **Class** | [class/task-management-class.puml](../uml/class/task-management-class.puml) | Complete static structure — product, creator, strategy hierarchies, and coordinator. |
| **Sequence** | [sequence/task-creation-sequence.puml](../uml/sequence/task-creation-sequence.puml) | Runtime flow of `TaskManager.createTask("BUG",...)`. |
| **Use Case** | [usecase/task-management-usecase.puml](../uml/usecase/task-management-usecase.puml) | Developer and Team Lead interactions. |
| **Activity** | [activity/task-lifecycle-activity.puml](../uml/activity/task-lifecycle-activity.puml) | End-to-end workflow from creation to completion. |
| **State** | [activity/task-state.puml](../uml/activity/task-state.puml) | `TaskStatus` finite state machine. |
| **Component** | [class/component-diagram.puml](../uml/class/component-diagram.puml) | Module-level dependencies. |
| **Deployment** | [class/deployment-diagram.puml](../uml/class/deployment-diagram.puml) | JVM runtime environment. |

### Implementation Approach in Java

- **Language level:** Java 8+ (developed and tested on JDK 21).
- **Package structure:** single default package (17 source files in [src/main/java/](../../src/main/java/)).
- **Abstraction layering:** interfaces and abstract classes at the top of each hierarchy; concrete implementations at the bottom.
- **Immutability:** strategies return *new* sorted lists without mutating the caller's list.
- **State machine:** enforced by per-constant `allowedTransitions()` overrides in `TaskStatus`, with `AbstractTask.setStatus(...)` throwing `IllegalArgumentException` on invalid transitions.
- **Extensibility hook:** `TaskManager.registerFactory(...)` enables adding new task types at runtime without modifying the coordinator.

### Tools and Techniques Used

- **JDK 21** (bundled with the Red Hat Java extension for VS Code).
- **Visual Studio Code** with Red Hat Java, Debugger for Java, and Test Runner for Java extensions.
- **PlantUML** and **Mermaid** for UML diagram generation.
- **Git / GitHub** for version control ([hoop-ai/software-architecture-project](https://github.com/hoop-ai/software-architecture-project)).
- **Manual test harness** (`Main.java`) structured into six labeled sections that produce PASS/FAIL output.
- **Interactive console** (`TaskManagementApp.java`) with a menu-driven CLI for live classroom demonstration (separate entry point).

---

## 3. PROJECT MANAGEMENT

### 3.1 Work Plan

| WP | Name | Deliverables | Status |
|---|---|---|---|
| **WP1** | Problem Definition | Problem statement, functional and non-functional requirements, scope document, pattern selection rationale | Complete |
| **WP2** | Design & UML | Seven PlantUML diagrams, full design specification ([docs/design/design-spec.md](../design/design-spec.md)), class-hierarchy decisions | Complete |
| **WP3** | Implementation | 17 Java source files implementing Factory Method, Strategy, the state machine, the `TaskManager` coordinator, and a separate interactive console application | Complete |
| **WP4** | Testing | Six test sections in `Main.java` covering pattern behavior, state transitions, integration, and SOLID compliance; test documentation in [docs/design/test-documentation.md](../design/test-documentation.md) | Complete |
| **WP5** | Reporting | Full project report ([docs/report/report.md](report.md)), this filled template, presentation outline, submission package | Complete |

### 3.2 Risk Management

| Risk | Likelihood | Impact | Mitigation Strategy |
|---|---|---|---|
| Over-engineering (applying patterns where simpler code would suffice) | Medium | Medium | Keep scope minimal; every pattern use must be justified by a concrete extensibility requirement. |
| Pattern misapplication (e.g., confusing Factory Method with Abstract Factory) | Medium | High | Follow GoF definitions strictly; cross-validate with Refactoring Guru and *Head First Design Patterns*. |
| UML diagrams drifting from the code | High | Medium | Generate diagrams from PlantUML sources checked into the repo; update diagrams whenever class structure changes. |
| Java / JDK environment issues on target machines | Medium | Low | Document build/run commands in [CLAUDE.md](../../CLAUDE.md) and use the JDK bundled with the VS Code Java extension. |
| Scope creep toward persistence or GUI features | Medium | High | Explicitly exclude these from scope; record them under "Potential Improvements" instead. |
| Loss of work / accidental file deletion | Low | High | Git commits after every work package; remote backup on GitHub. |
| Deadline slippage (June 5, 2026) | Low | High | Clear milestones per WP; reporting (WP5) started in parallel with implementation (WP3). |
| Teammate coordination (two-person team) | Low | Medium | Clear WP ownership and daily sync via Git commit history. |

---

## 4. IMPLEMENTATION

### Class Structure

The system consists of **17 Java files** in [src/main/java/](../../src/main/java/):

- **2 interfaces:** `Task`, `PriorityStrategy`
- **1 enum:** `TaskStatus` (with per-constant state-machine overrides)
- **2 abstract classes:** `AbstractTask`, `TaskFactory`
- **3 concrete tasks:** `BugTask`, `FeatureTask`, `DocumentationTask`
- **3 concrete factories:** `BugTaskFactory`, `FeatureTaskFactory`, `DocumentationTaskFactory`
- **3 concrete strategies:** `UrgentFirstStrategy`, `DeadlineFirstStrategy`, `SeverityFirstStrategy`
- **1 coordinator:** `TaskManager`
- **2 entry points:** `Main` (six automated test sections) and `TaskManagementApp` (interactive menu-driven console)

### Interfaces and Abstract Classes

- **`Task` interface** — the universal contract: `getId`, `getTitle`, `getDescription`, `getStatus`/`setStatus`, `getPriority`, `getDeadline`/`setDeadline`, `getType`, `getCreatedAt`. Deliberately lean to satisfy Interface Segregation.
- **`AbstractTask` abstract class** — skeletal implementation managing shared fields, an auto-incrementing ID counter, state-transition validation, and a `toString()` that concrete subclasses extend via `super.toString()`.
- **`TaskFactory` abstract class** — declares the factory method `createTask(title, description, priority)` and provides the template method `createTaskWithDeadline(...)`.
- **`PriorityStrategy` interface** — single method `List<Task> sort(List<Task> tasks)`.
- **`TaskStatus` enum** — per-constant `allowedTransitions()` overrides encoding the finite state machine (OPEN, IN_PROGRESS, REVIEW, DONE, BLOCKED).

### Pattern Implementation

**Factory Method (+ Simplified Service Locator).** `TaskManager` maintains a `Map<String, TaskFactory>` registry (`factoryRegistry`) pre-populated with the three concrete factories in its constructor. `createTask(type, ...)` looks up the factory by type string and delegates the actual instantiation. New task types are added via `registerFactory(...)` — no modification to existing classes required.

```java
public Task createTask(String type, String title, String description, int priority) {
    TaskFactory factory = factoryRegistry.get(type.toUpperCase());
    if (factory == null) {
        throw new IllegalArgumentException(
                "No factory registered for task type: " + type
                + ". Available types: " + factoryRegistry.keySet());
    }
    Task task = factory.createTask(title, description, priority);
    tasks.add(task);
    return task;
}
```Yeah, I designed it in a nice Word document. 

**Strategy.** `TaskManager` holds a `PriorityStrategy` reference named `currentStrategy` (default: `UrgentFirstStrategy`, set in the constructor). `getPrioritizedTasks()` delegates to `currentStrategy.sort(tasks)`. `setPriorityStrategy(...)` swaps the algorithm at runtime and rejects `null`.

```java
public List<Task> getPrioritizedTasks() {
    return currentStrategy.sort(tasks);
}

public void setPriorityStrategy(PriorityStrategy strategy) {
    if (strategy == null) {
        throw new IllegalArgumentException("Strategy must not be null.");
    }
    this.currentStrategy = strategy;
}
```

### Key Methods and Code Logic

- `AbstractTask.setStatus(...)` — validates transitions against the `TaskStatus` state machine and rejects illegal moves.
- `BugTaskFactory.createTask(...)` — builds a `BugTask` with default severity `"MEDIUM"`.
- `SeverityFirstStrategy.sort(...)` — two-tier sort: bug tasks first by severity rank (CRITICAL=4, HIGH=3, MEDIUM=2, LOW=1), then non-bug tasks by priority descending. Uses `instanceof BugTask` as the partitioning predicate.
- `TaskManager.transitionTask(id, targetStatus)` — lifecycle management delegated to the state machine in `TaskStatus`.
- `TaskManager.registerFactory(type, factory)` — runtime extensibility hook for new task types.

A full code walkthrough is provided in [docs/report/report.md](report.md).

---

## 5. TESTING AND DEMONSTRATION

All tests are executed by running `Main.java`, which contains **six labeled sections**. Full sample output is captured in [docs/design/test-output.txt](../design/test-output.txt).

### Test Cases

| # | Section | Validates |
|---|---|---|
| 1 | Factory Method Demo | Polymorphic creation through the `TaskFactory` reference; the template method `createTaskWithDeadline(...)`. |
| 2 | Strategy Demo | The same task list sorted three different ways by swapping strategies at runtime. |
| 3 | Lifecycle / State Transitions | Valid path OPEN→IN_PROGRESS→REVIEW→DONE; blocked path; invalid-transition rejection; terminal-state enforcement. |
| 4 | TaskManager Integration | Mixed task creation, status transitions, filtering by status, summary report, task removal. |
| 5 | SOLID Principles Demo | OCP (custom anonymous "lowest-first" strategy added at runtime), LSP (factories substitutable via `TaskFactory` reference), DIP (only abstractions used), SRP, ISP. |
| 6 | Edge Cases and Error Handling | Six edge cases: invalid priority (out of 1–5), null title, unknown task type, task not found by ID, null strategy, case-insensitive type lookup. |

A separate interactive menu-driven console application (`TaskManagementApp.java`) is also provided for hands-on demonstration — it runs independently of the automated test suite.

### Sample Inputs / Outputs

**Strategy pattern (Test 2) — same 5 tasks sorted three different ways:**
```
--- Strategy 1: UrgentFirstStrategy (highest priority first) ---
  1. Task[...priority=5...] Fix payment bug
  2. Task[...priority=4...] Export CSV
  3. Task[...priority=3...] Setup guide
  4. Task[...priority=2...] Add search
  5. Task[...priority=1...] UI glitch

--- Strategy 2: DeadlineFirstStrategy (earliest deadline first) ---
  1. Task[...deadline=2026-04-20...] Setup guide
  2. Task[...deadline=2026-05-01...] Fix payment bug
  3. Task[...deadline=2026-05-10...] Export CSV
  4. Task[...deadline=2026-06-15...] Add search
  5. Task[...deadline=none...] UI glitch

[PASS] Same tasks, three different orderings via Strategy swap.
```

**State machine rejection (Test 3):**
```
--- Invalid transition: OPEN -> DONE (should fail) ---
  Caught expected error: Cannot transition from OPEN to DONE
  [PASS] State machine correctly rejects invalid transitions.
```

### Main.java Execution Explanation

Build once, then run either entry point:

```bash
# Compile all sources
javac -d bin src/main/java/*.java

# Run the automated six-section test suite
java -cp bin Main

# Run the interactive menu-driven console (separate application)
java -cp bin TaskManagementApp
```

Each test section in `Main` produces human-readable headers, tabular task output, and `[PASS]` markers, ending with a final summary banner. `TaskManagementApp` provides a menu that lets the user create tasks, switch strategies, transition task states, and view summaries by hand — ideal for live classroom demonstration.

---

## 6. RESULTS AND EVALUATION

### Advantages of the Design Patterns Used

1. **Separation of concerns** — creation, prioritization, lifecycle, and coordination each live in dedicated classes.
2. **Double extensibility** — new task types *and* new prioritization algorithms can be added independently, each through pure addition.
3. **Runtime flexibility** — both strategies and factories can be swapped or registered at runtime.
4. **SOLID compliance** — all five principles are exercised by the design and demonstrated explicitly in Test 5.
5. **Clean abstractions** — `Task`, `TaskFactory`, and `PriorityStrategy` provide boundaries that prevent implementation details from leaking.
6. **Testability** — because high-level code depends only on abstractions, each layer can be tested in isolation.

### Limitations

1. **No persistence** — all state lives in memory and is lost on exit.
2. **No GUI** — console application only.
3. **Single-user** — no concurrency control; `idCounter` is not thread-safe.
4. **No authentication / authorization** — any caller can perform any operation.
5. **String-based severity** — `BugTask.severity` is a `String` rather than a dedicated enum.
6. **No dependency injection framework** — factories are registered manually in `TaskManager`'s constructor.

### Possible Improvements

1. **Observer Pattern** — notify team members when a task changes status.
2. **Command Pattern** — support undo/redo of task operations.
3. **Repository Pattern** — introduce file or database backends behind a common interface.
4. **State Pattern** — replace the enum with state objects when states begin carrying behavior.
5. **Builder Pattern** — handle tasks with many optional parameters more cleanly.
6. **Persistence layer** — JSON or SQLite storage for real-world usage.
7. **REST API + web UI** — expose the coordinator over HTTP.

### Alternative Solutions

- **Abstract Factory instead of Factory Method** — appropriate only if each task type came with a *family* of related objects (template, validator, renderer). Overkill for this project.
- **Enum-only state representation** — the current approach, sufficient because states carry no behavior beyond transition rules.
- **Simple if/else dispatch** — rejected because it violates OCP and scales poorly (see counterfactual analysis below).
- **Reflection-based instantiation** — powerful but sacrifices type safety and readability.

### Justification of Design Pattern Usage

**Why Factory Method was necessary.** Each task type has distinct construction requirements — a bug needs severity and reproduction steps, a feature needs effort and business value, a documentation task needs document type and target audience. Without a factory abstraction, every place that creates tasks would need conditional logic over task type, duplicating knowledge across the codebase. Factory Method centralizes creation in one class per type and eliminates direct coupling between `TaskManager` and the concrete task classes. It also enables runtime registration of new task types via `registerFactory(...)` — which would be impossible with hard-coded constructors.

**Why Strategy was necessary.** Prioritization is fundamentally context-dependent. Crisis mode demands urgent-first; sprint planning demands deadline-first; release stabilization demands severity-first. These are different algorithms operating on the same data. Embedding them in `TaskManager` with `if/else` branches would bloat the class, violate SRP, and make adding a new algorithm a modification rather than an extension. Strategy isolates each algorithm, enables runtime swapping, and lets `TaskManager` focus purely on coordination.

### Counterfactual Analysis — What If the Patterns Were Not Used?

**Without Factory Method:**
- **Tight coupling:** `TaskManager.createTask(...)` would reference `BugTask`, `FeatureTask`, and `DocumentationTask` directly — pulling concrete classes into a high-level module and violating the Dependency Inversion Principle.
- **Increased code complexity:** a long `switch` statement on a `type` string would duplicate default-value logic, validation, and construction for every task type.
- **Reduced scalability and maintainability:** adding a `ResearchTask` would require editing `TaskManager`, editing every place that enumerates task types, and re-testing all existing creation paths. A change that should be purely additive becomes invasive.
- **Difficulty in extending the system:** new task types could not be registered at runtime; the set of types would be hard-coded at compile time.
- **Reduced testability:** mocking task creation for unit tests would require either reflection or subclassing `TaskManager`, because the `new` keywords would be hard-wired.

**Without Strategy:**
- **Monolithic sorting logic:** `TaskManager` would contain three (or more) private methods `sortByUrgency`, `sortByDeadline`, `sortBySeverity`, plus a `sortMode` field and a dispatching `switch`.
- **Tight coupling and SRP violation:** `TaskManager` would simultaneously be responsible for coordination *and* for implementing every sorting algorithm.
- **Reduced scalability:** adding a fourth algorithm would mean editing the central class, widening the blast radius of every future change.
- **Difficulty in extending the system:** a user-supplied sorting rule — such as the anonymous "lowest-first" strategy injected in Test 5 — could not be added at runtime; the set of algorithms would be fixed at compile time.
- **Reduced maintainability:** each sorting method would mix its algorithm with `TaskManager`'s other responsibilities, making unit testing awkward and encouraging regressions whenever one algorithm is changed.

In both counterfactuals the core issue is the same: structural decisions that *should* be open to extension become closed. Factory Method and Strategy directly neutralize these risks by design.

---

## 7. CONCLUSION

### Achievements

This project successfully designed, implemented, documented, and tested a Task Management System that composes two GoF design patterns — Factory Method and Strategy — in pure Java with zero external dependencies. The final deliverable comprises 17 Java source files, seven UML diagrams, a full written report, a structured six-section test suite (`Main.java`), and a separate interactive menu-driven console application (`TaskManagementApp.java`). Every SOLID principle is explicitly exercised by the tests, and both patterns coexist naturally inside a single coordinator class that also layers a simplified Service Locator registry for runtime factory lookup.

### Lessons Learned

1. **Patterns are a vocabulary.** They communicate design intent far more efficiently than ad-hoc descriptions and accelerate team discussions.
2. **SOLID enables extensibility in practice, not just theory.** Throughout implementation, new classes were added by pure extension — no existing class had to be opened for modification after its initial creation.
3. **Patterns compose.** Factory Method and Strategy address orthogonal concerns and live comfortably in the same system without interference.
4. **Simplicity has architectural value.** Pure Java, no frameworks, 16 files — and the system is still fully extensible. Good architecture comes from design decisions, not from tools.
5. **UML earns its place when it stays synchronized with code.** Keeping PlantUML sources in the repo prevented diagrams from drifting during implementation.
6. **Two-person collaboration** benefits enormously from clearly assigned work packages and frequent Git commits.

### Importance of Design Patterns

Design patterns are a cornerstone of professional software engineering. They appear in every major framework (Spring, Android, JavaFX), every enterprise codebase, and every technical interview. Mastery of patterns — combined with SOLID principles — marks the transition from programmer to software architect: someone who can design systems that not only work today but remain maintainable and extensible for years. Factory Method and Strategy, the two patterns demonstrated here, are among the most widely used in industry, and the skills practiced in this project transfer directly to real-world software development.

---

## 8. REFERENCES

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley Professional.
2. Freeman, E., & Robson, E. (2020). *Head First Design Patterns: Building Extensible and Maintainable Object-Oriented Software* (2nd ed.). O'Reilly Media.
3. Martin, R. C. (2003). *Agile Software Development, Principles, Patterns, and Practices*. Pearson Education.
4. Martin, R. C. (2009). *Clean Code: A Handbook of Agile Software Craftsmanship*. Pearson Education.
5. Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
6. Bloch, J. (2018). *Effective Java* (3rd ed.). Addison-Wesley Professional.
7. Fowler, M. (2018). *Refactoring: Improving the Design of Existing Code* (2nd ed.). Addison-Wesley Professional.
8. Oracle. *Java SE Documentation*. https://docs.oracle.com/javase/
9. Refactoring Guru. *Design Patterns*. https://refactoring.guru/design-patterns
10. Object Management Group. *Unified Modeling Language (UML) Specification*. https://www.omg.org/spec/UML/
