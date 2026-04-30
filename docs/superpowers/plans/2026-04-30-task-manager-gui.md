# Task Manager GUI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Swing-based GUI as an additive third entry point to the SEN3006 Task Management project, packaged as a runnable JAR with launcher scripts and updated README, without modifying any of the 16 existing engine source files.

**Architecture:** Five new Java classes in `src/main/java/gui/` (default package, organized in subfolder), consuming only the public API of `TaskManager`/`TaskFactory`/`PriorityStrategy`. Bundled into `TaskManagerGUI.jar`. Verified by ensuring `Main.java` and `TaskManagementApp.java` continue to work unchanged.

**Tech Stack:** Java 8+, Swing (javax.swing.*), AWT (java.awt.*), zero external dependencies. Manual smoke testing (no JUnit per project rule).

**Spec:** See `docs/superpowers/specs/2026-04-30-task-manager-gui-design.md`.

---

## Pre-flight: Establish baseline

### Task 0: Verify current project builds and tests

**Files:** None modified.

- [ ] **Step 1: Compile the existing project**

Run from project root:

```bash
javac -d bin src/main/java/*.java
```

Expected: silent success, no errors. `bin/` populated with `.class` files.

- [ ] **Step 2: Run Main and capture baseline output to verify later**

```bash
java -cp bin Main > docs/design/baseline-main-output.txt
```

Expected: command exits 0; file ends with `# ALL TESTS PASSED #`. This file is the regression-test oracle for later tasks.

- [ ] **Step 3: Confirm TaskManagementApp also still launches**

```bash
echo "0" | java -cp bin TaskManagementApp
```

Expected: prints menu, exits cleanly on input `0`. (`0` should be the exit option; if not, send whatever option exits.)

- [ ] **Step 4: Commit baseline**

```bash
git add docs/design/baseline-main-output.txt
git commit -m "chore: capture baseline Main output before GUI work"
```

---

## Phase 1: Foundation — empty window opens

### Task 1: Create `gui/` folder + minimal `TaskManagerGUI` skeleton

**Files:**

- Create: `src/main/java/gui/TaskManagerGUI.java`

- [ ] **Step 1: Create the skeleton file**

Write `src/main/java/gui/TaskManagerGUI.java`:

```java
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;

/**
 * Swing GUI entry point for the Task Management System.
 *
 * <p>This is the third entry point alongside {@code Main} (deterministic test demo)
 * and {@code TaskManagementApp} (interactive console menu). It provides a
 * graphical interface that consumes the same public API of {@link TaskManager},
 * {@link TaskFactory}, and {@link PriorityStrategy}, making the design patterns
 * visible to a user who clicks rather than reads terminal output.</p>
 *
 * <p><strong>Note:</strong> No {@code package} declaration is used so this class
 * can reference engine types in the default package (Java 8 forbids importing
 * default-package types from a named package). The {@code gui/} subfolder is
 * organizational only.</p>
 */
public class TaskManagerGUI extends JFrame {

    public TaskManagerGUI() {
        super("SEN3006 Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(new JLabel("Task Manager GUI — coming online", JLabel.CENTER), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
            // Fall back to default L&F silently.
        }
        SwingUtilities.invokeLater(() -> new TaskManagerGUI().setVisible(true));
    }
}
```

- [ ] **Step 2: Compile with the new file included**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success. `bin/TaskManagerGUI.class` is created (note: in `bin/` root, not `bin/gui/` — file has no `package` declaration).

- [ ] **Step 3: Verify class landed in default package**

```bash
ls bin/TaskManagerGUI.class
```

Expected: file exists. (If it landed in `bin/gui/`, the spec's package strategy is broken — abort and revisit §3.3 of the spec.)

- [ ] **Step 4: Launch the empty window**

```bash
java -cp bin TaskManagerGUI
```

Expected: a 900x650 window titled "SEN3006 Task Manager" opens with the placeholder label. Close the window to exit.

- [ ] **Step 5: Verify zero regression on Main**

```bash
java -cp bin Main > /tmp/main-output-task1.txt
diff docs/design/baseline-main-output.txt /tmp/main-output-task1.txt
```

Expected: no output from `diff` (files are identical).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/gui/TaskManagerGUI.java
git commit -m "feat(gui): add empty Swing window skeleton"
```

---

## Phase 2: Data binding — table shows tasks

### Task 2: Create `TaskTableModel`

**Files:**

- Create: `src/main/java/gui/TaskTableModel.java`

- [ ] **Step 1: Create the table model**

Write `src/main/java/gui/TaskTableModel.java`:

```java
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Swing table model backed by a {@link TaskManager}'s prioritized task list.
 *
 * <p>Re-reads from the manager whenever {@link #refresh()} is called. The current
 * priority strategy held by the manager governs row order, so switching strategies
 * and calling refresh() makes the Strategy pattern visibly swap before the user.</p>
 */
public class TaskTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID", "Type", "Title", "Priority", "Status", "Deadline"};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TaskManager manager;
    private List<Task> rows;

    public TaskTableModel(TaskManager manager) {
        this.manager = manager;
        this.rows = manager.getPrioritizedTasks();
    }

    /** Re-pulls the prioritized list from the manager and fires a full refresh. */
    public void refresh() {
        this.rows = manager.getPrioritizedTasks();
        fireTableDataChanged();
    }

    /** Returns the task at the given view row, or null if out of range. */
    public Task getTaskAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) return null;
        return rows.get(rowIndex);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
        Task t = rows.get(row);
        switch (col) {
            case 0: return t.getId();
            case 1: return t.getType();
            case 2: return t.getTitle();
            case 3: return t.getPriority();
            case 4: return t.getStatus().toString();
            case 5: return t.getDeadline() == null ? "—" : t.getDeadline().format(DATE_FMT);
            default: return "";
        }
    }
}
```

- [ ] **Step 2: Compile**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/gui/TaskTableModel.java
git commit -m "feat(gui): add TaskTableModel reading from TaskManager"
```

---

### Task 3: Create `TaskTablePanel` with sort + filter dropdowns and live table

**Files:**

- Create: `src/main/java/gui/TaskTablePanel.java`

- [ ] **Step 1: Write the panel**

Write `src/main/java/gui/TaskTablePanel.java`:

```java
import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Center region of the GUI: a strip of sort/filter dropdowns over a JTable.
 *
 * <p>The Sort dropdown calls {@code manager.setPriorityStrategy(...)} (Strategy
 * pattern, engine-side). The Filter dropdown applies a view-side
 * {@link RowFilter} so the engine retains all tasks; only the table view hides
 * rows that don't match the selected status.</p>
 */
public class TaskTablePanel extends JPanel {

    private final TaskManager manager;
    private final TaskTableModel model;
    private final JTable table;
    private final TableRowSorter<TaskTableModel> rowSorter;
    private final JComboBox<String> sortCombo;
    private final JComboBox<String> filterCombo;
    private Runnable onSelectionChanged = () -> {};

    public TaskTablePanel(TaskManager manager) {
        super(new BorderLayout());
        this.manager = manager;
        this.model = new TaskTableModel(manager);

        // Sort + filter strip.
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Sort by:"));
        sortCombo = new JComboBox<>(new String[]{"Urgent First", "Deadline First", "Severity First"});
        sortCombo.addActionListener(e -> applySort());
        controls.add(sortCombo);

        controls.add(Box.createHorizontalStrut(20));
        controls.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All Statuses", "OPEN", "IN_PROGRESS", "REVIEW", "BLOCKED", "DONE"});
        filterCombo.addActionListener(e -> applyFilter());
        controls.add(filterCombo);

        add(controls, BorderLayout.NORTH);

        // Table with view-side row sorter (used for filtering only — engine handles ordering).
        table = new JTable(model);
        rowSorter = new TableRowSorter<>(model);
        // Disable per-column click-to-sort: we want order to come from the engine's Strategy.
        for (int i = 0; i < model.getColumnCount(); i++) rowSorter.setSortable(i, false);
        table.setRowSorter(rowSorter);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelectionChanged.run();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void applySort() {
        String choice = (String) sortCombo.getSelectedItem();
        PriorityStrategy strategy;
        if ("Deadline First".equals(choice))      strategy = new DeadlineFirstStrategy();
        else if ("Severity First".equals(choice)) strategy = new SeverityFirstStrategy();
        else                                       strategy = new UrgentFirstStrategy();
        manager.setPriorityStrategy(strategy);
        refreshTable();
    }

    private void applyFilter() {
        String choice = (String) filterCombo.getSelectedItem();
        if (choice == null || "All Statuses".equals(choice)) {
            rowSorter.setRowFilter(null);
        } else {
            // Column 4 is "Status" in TaskTableModel; case-insensitive exact match on the enum name.
            rowSorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(choice) + "$", 4));
        }
    }

    /** Rebuilds the table from the engine's current prioritized list. */
    public void refreshTable() {
        model.refresh();
        applyFilter(); // re-apply so newly added rows respect current filter
    }

    public Task getSelectedTask() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getTaskAt(modelRow);
    }

    public JTable getTable() { return table; }

    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback == null ? () -> {} : callback;
    }
}
```

- [ ] **Step 2: Compile**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/gui/TaskTablePanel.java
git commit -m "feat(gui): add TaskTablePanel with sort + filter dropdowns"
```

---

### Task 4: Create `TaskFormPanel` for adding tasks

**Files:**

- Create: `src/main/java/gui/TaskFormPanel.java`

- [ ] **Step 1: Write the form panel**

Write `src/main/java/gui/TaskFormPanel.java`:

```java
import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * North region of the GUI: a form for creating new tasks.
 *
 * <p>Demonstrates the Factory Method pattern: the type dropdown drives
 * {@code manager.createTask(type, ...)}, which routes through the registered
 * factory for that type without the GUI knowing the concrete class.</p>
 */
public class TaskFormPanel extends JPanel {

    private final TaskManager manager;
    private final JComboBox<String> typeCombo;
    private final JTextField titleField;
    private final JComboBox<Integer> priorityCombo;
    private final JTextField descriptionField;
    private Runnable onTaskAdded = () -> {};

    public TaskFormPanel(TaskManager manager) {
        super(new GridBagLayout());
        this.manager = manager;
        setBorder(BorderFactory.createTitledBorder("Create Task"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Type, Title, Priority
        c.gridy = 0;
        c.gridx = 0; add(new JLabel("Type:"), c);
        typeCombo = new JComboBox<>(new String[]{"BUG", "FEATURE", "DOCUMENTATION"});
        c.gridx = 1; c.weightx = 0; add(typeCombo, c);

        c.gridx = 2; add(new JLabel("Title:"), c);
        titleField = new JTextField(20);
        c.gridx = 3; c.weightx = 1; add(titleField, c);

        c.gridx = 4; c.weightx = 0; add(new JLabel("Priority:"), c);
        priorityCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        priorityCombo.setSelectedItem(3);
        c.gridx = 5; add(priorityCombo, c);

        // Row 1: Description, Add button
        c.gridy = 1;
        c.gridx = 0; add(new JLabel("Description:"), c);
        descriptionField = new JTextField(40);
        c.gridx = 1; c.gridwidth = 4; c.weightx = 1; add(descriptionField, c);

        JButton addBtn = new JButton("+ Add Task");
        addBtn.addActionListener(e -> handleAdd());
        c.gridx = 5; c.gridwidth = 1; c.weightx = 0; add(addBtn, c);
    }

    private void handleAdd() {
        String type = (String) typeCombo.getSelectedItem();
        String title = titleField.getText().trim();
        Integer priority = (Integer) priorityCombo.getSelectedItem();
        String description = descriptionField.getText().trim();

        try {
            manager.createTask(type, title, description, priority);
            // Clear form on success
            titleField.setText("");
            descriptionField.setText("");
            onTaskAdded.run();
        } catch (IllegalArgumentException ex) {
            // Engine validation surfaced to the user (no GUI-side validation).
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Validation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setOnTaskAdded(Runnable callback) {
        this.onTaskAdded = callback == null ? () -> {} : callback;
    }
}
```

- [ ] **Step 2: Compile**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/gui/TaskFormPanel.java
git commit -m "feat(gui): add TaskFormPanel for creating tasks via Factory Method"
```

---

## Phase 3: Wire it together

### Task 5: Wire panels into `TaskManagerGUI` main window + transition buttons

**Files:**

- Modify: `src/main/java/gui/TaskManagerGUI.java`

- [ ] **Step 1: Replace the placeholder with the wired-up window**

Overwrite `src/main/java/gui/TaskManagerGUI.java`:

```java
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Swing GUI entry point for the Task Management System.
 *
 * <p>Third entry point alongside {@code Main} (deterministic test demo) and
 * {@code TaskManagementApp} (interactive console menu). Consumes the same
 * public API as both, demonstrating that all three views are powered by the
 * same Factory Method + Strategy engine.</p>
 *
 * <p><strong>No package declaration</strong> — keeps this class in the default
 * package so it can reference engine types directly (Java 8 forbids importing
 * default-package classes from a named package). The {@code gui/} subfolder
 * is organizational only.</p>
 */
public class TaskManagerGUI extends JFrame {

    private final TaskManager manager = new TaskManager();
    private final TaskFormPanel formPanel = new TaskFormPanel(manager);
    private final TaskTablePanel tablePanel = new TaskTablePanel(manager);
    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JLabel statusBar = new JLabel(" ");

    private final JButton btnInProgress = new JButton("→ IN_PROGRESS");
    private final JButton btnReview     = new JButton("→ REVIEW");
    private final JButton btnDone       = new JButton("→ DONE");
    private final JButton btnBlock      = new JButton("Block");
    private final JButton btnUnblock    = new JButton("Unblock (→ OPEN)");
    private final JButton btnDelete     = new JButton("Delete");

    public TaskManagerGUI() {
        super("SEN3006 Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(formPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(tablePanel, BorderLayout.CENTER);

        actionsPanel.setBorder(BorderFactory.createTitledBorder("Selected Task Actions"));
        actionsPanel.add(btnInProgress);
        actionsPanel.add(btnReview);
        actionsPanel.add(btnDone);
        actionsPanel.add(btnBlock);
        actionsPanel.add(btnUnblock);
        actionsPanel.add(btnDelete);
        center.add(actionsPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);

        wireActions();
        formPanel.setOnTaskAdded(this::refreshAll);
        tablePanel.setOnSelectionChanged(this::updateActionButtons);
        refreshAll();
    }

    private void wireActions() {
        btnInProgress.addActionListener(e -> transitionSelected(TaskStatus.IN_PROGRESS));
        btnReview    .addActionListener(e -> transitionSelected(TaskStatus.REVIEW));
        btnDone      .addActionListener(e -> transitionSelected(TaskStatus.DONE));
        btnBlock     .addActionListener(e -> transitionSelected(TaskStatus.BLOCKED));
        btnUnblock   .addActionListener(e -> transitionSelected(TaskStatus.OPEN));
        btnDelete    .addActionListener(e -> deleteSelected());
    }

    private void transitionSelected(TaskStatus target) {
        Task t = tablePanel.getSelectedTask();
        if (t == null) return;
        try {
            manager.transitionTask(t.getId(), target);
            refreshAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Invalid transition", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Task t = tablePanel.getSelectedTask();
        if (t == null) return;
        manager.removeTask(t.getId());
        refreshAll();
    }

    private void refreshAll() {
        tablePanel.refreshTable();
        updateActionButtons();
        updateStatusBar();
    }

    private void updateActionButtons() {
        Task t = tablePanel.getSelectedTask();
        if (t == null) {
            btnInProgress.setEnabled(false);
            btnReview.setEnabled(false);
            btnDone.setEnabled(false);
            btnBlock.setEnabled(false);
            btnUnblock.setEnabled(false);
            btnDelete.setEnabled(false);
            return;
        }
        TaskStatus s = t.getStatus();
        btnInProgress.setEnabled(s.canTransitionTo(TaskStatus.IN_PROGRESS));
        btnReview    .setEnabled(s.canTransitionTo(TaskStatus.REVIEW));
        btnDone      .setEnabled(s.canTransitionTo(TaskStatus.DONE));
        btnBlock     .setEnabled(s.canTransitionTo(TaskStatus.BLOCKED));
        btnUnblock   .setEnabled(s.canTransitionTo(TaskStatus.OPEN));
        btnDelete    .setEnabled(true);
    }

    private void updateStatusBar() {
        int total = manager.getAllTasks().size();
        StringBuilder sb = new StringBuilder();
        sb.append(total).append(" task").append(total == 1 ? "" : "s");
        if (total > 0) {
            sb.append(" | ");
            boolean first = true;
            for (TaskStatus s : TaskStatus.values()) {
                int count = manager.getTasksByStatus(s).size();
                if (count == 0) continue;
                if (!first) sb.append(", ");
                sb.append(count).append(" ").append(s);
                first = false;
            }
        }
        sb.append("  |  Strategy: ").append(manager.getCurrentStrategyName());
        statusBar.setText(sb.toString());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { /* fall back silently */ }
        SwingUtilities.invokeLater(() -> new TaskManagerGUI().setVisible(true));
    }
}
```

- [ ] **Step 2: Compile**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success.

- [ ] **Step 3: Smoke test — launch the wired GUI**

```bash
java -cp bin TaskManagerGUI
```

Expected behavior:

- Window opens with form at top, table in middle (empty), action buttons row, status bar at bottom showing `0 tasks | Strategy: UrgentFirstStrategy`.
- Type "Login crash" in title, click `+ Add Task`. A row appears in the table.
- Click the row. The `→ IN_PROGRESS` button becomes enabled, `→ DONE` stays disabled (state machine).
- Click `→ IN_PROGRESS`. Status column updates; `→ DONE` is still disabled, `→ REVIEW` is enabled.
- Switch the Sort dropdown to `Deadline First`. Status bar updates to `Strategy: DeadlineFirstStrategy`.
- Add 2 more tasks with different priorities. Switch Sort back to `Urgent First`. Watch the table reorder.
- Close the window.

- [ ] **Step 4: Verify zero regression on Main**

```bash
java -cp bin Main > /tmp/main-output-task5.txt
diff docs/design/baseline-main-output.txt /tmp/main-output-task5.txt
```

Expected: no diff output.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/gui/TaskManagerGUI.java
git commit -m "feat(gui): wire form, table, transition buttons, status bar"
```

---

## Phase 4: Demos + polish

### Task 6: Add `DemoScenarios` and Demo menu

**Files:**

- Create: `src/main/java/gui/DemoScenarios.java`
- Modify: `src/main/java/gui/TaskManagerGUI.java` (add menu bar)

- [ ] **Step 1: Create the scenario loader**

Write `src/main/java/gui/DemoScenarios.java`:

```java
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-built scenarios that mirror the data setups in {@link Main}'s test sections.
 *
 * <p>Each method takes a {@link TaskManager}, clears its tasks, and populates it
 * with the same data the corresponding {@code Main.java} test builds. This lets
 * the professor see the canonical demos inside the GUI on demand without losing
 * the freedom to add custom tasks.</p>
 *
 * <p><strong>Independent of {@link Main}:</strong> these methods do not call
 * {@code Main} or extract code from it. They duplicate the data setup here so
 * {@code Main.java} stays untouched.</p>
 */
public final class DemoScenarios {

    private DemoScenarios() {}

    /** Removes every task currently in the manager. */
    public static void clearAll(TaskManager manager) {
        // Copy IDs first to avoid concurrent-modification issues.
        List<Integer> ids = new ArrayList<>();
        for (Task t : manager.getAllTasks()) ids.add(t.getId());
        for (int id : ids) manager.removeTask(id);
    }

    /** Mirrors Main.java Test 2: 5 tasks with deadlines + severities for sorting demos. */
    public static void loadStrategyDemo(TaskManager manager) {
        clearAll(manager);
        manager.createTask("BUG", "Fix payment bug", "Payment fails for some users", 5);
        manager.createTask("FEATURE", "Add search", "Implement full-text search", 2);
        manager.createTask("BUG", "UI glitch", "Button misaligned on mobile", 1);
        manager.createTask("DOCUMENTATION", "Setup guide", "Write installation guide", 3);
        manager.createTask("FEATURE", "Export CSV", "Export reports as CSV", 4);

        List<Task> all = manager.getAllTasks();
        all.get(0).setDeadline(LocalDate.of(2026, 5, 1));
        all.get(1).setDeadline(LocalDate.of(2026, 6, 15));
        all.get(2).setDeadline(null);
        all.get(3).setDeadline(LocalDate.of(2026, 4, 20));
        all.get(4).setDeadline(LocalDate.of(2026, 5, 10));

        if (all.get(0) instanceof BugTask) {
            ((BugTask) all.get(0)).setSeverity("CRITICAL");
        }
    }

    /** Mirrors Main.java Test 3: a single task ready for state-transition demos. */
    public static void loadLifecycleDemo(TaskManager manager) {
        clearAll(manager);
        manager.createTask("BUG", "Test bug", "A bug for lifecycle testing", 3);
    }

    /** Mirrors Main.java Test 4: 5-task mix with some transitions pre-applied. */
    public static void loadIntegrationDemo(TaskManager manager) {
        clearAll(manager);
        Task t1 = manager.createTask("BUG", "Server timeout", "API times out under load", 5);
        Task t2 = manager.createTask("FEATURE", "User profiles", "Add user profile pages", 3);
        Task t3 = manager.createTask("BUG", "Typo in footer", "Footer says 'Copyrigth'", 1);
        manager.createTask("DOCUMENTATION", "API reference", "Write API docs", 2);
        Task t5 = manager.createTask("FEATURE", "Notifications", "Push notification system", 4);

        t1.setDeadline(LocalDate.of(2026, 4, 1));
        t2.setDeadline(LocalDate.of(2026, 5, 15));
        t5.setDeadline(LocalDate.of(2026, 4, 30));

        manager.transitionTask(t1.getId(), TaskStatus.IN_PROGRESS);
        manager.transitionTask(t3.getId(), TaskStatus.IN_PROGRESS);
        manager.transitionTask(t3.getId(), TaskStatus.REVIEW);
    }
}
```

- [ ] **Step 2: Add the menu bar to `TaskManagerGUI`**

Edit `src/main/java/gui/TaskManagerGUI.java`. After the constructor's `setLayout(new BorderLayout());` line, add:

```java
        setJMenuBar(buildMenuBar());
```

Then add the following methods before `main(...)`:

```java
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu demoMenu = new JMenu("Demo");
        demoMenu.add(menuItem("Load Strategy Demo (Main.java Test 2)",
                () -> { DemoScenarios.loadStrategyDemo(manager); refreshAll(); }));
        demoMenu.add(menuItem("Load Lifecycle Demo (Main.java Test 3)",
                () -> { DemoScenarios.loadLifecycleDemo(manager); refreshAll(); }));
        demoMenu.add(menuItem("Load Integration Demo (Main.java Test 4)",
                () -> { DemoScenarios.loadIntegrationDemo(manager); refreshAll(); }));
        demoMenu.addSeparator();
        demoMenu.add(menuItem("Clear All Tasks",
                () -> { DemoScenarios.clearAll(manager); refreshAll(); }));
        bar.add(demoMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuItem("About", () -> JOptionPane.showMessageDialog(this,
                "SEN3006 Task Management System — GUI demo\n"
                        + "Patterns: Factory Method + Strategy\n"
                        + "Engine: pure Java, zero external dependencies\n"
                        + "Use the form to create tasks, click rows to transition them,\n"
                        + "switch the Sort dropdown to see Strategy swap live.",
                "About", JOptionPane.INFORMATION_MESSAGE)));
        bar.add(helpMenu);

        return bar;
    }

    private JMenuItem menuItem(String label, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> action.run());
        return item;
    }
```

- [ ] **Step 3: Compile**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Expected: silent success.

- [ ] **Step 4: Smoke test the menu**

```bash
java -cp bin TaskManagerGUI
```

Expected:

- Menu bar shows `Demo` and `Help`.
- `Demo → Load Strategy Demo` populates the table with 5 tasks.
- Switching the Sort dropdown reorders them visibly.
- `Demo → Clear All Tasks` empties the table.
- `Help → About` shows the about dialog.

- [ ] **Step 5: Verify zero regression**

```bash
java -cp bin Main > /tmp/main-output-task6.txt
diff docs/design/baseline-main-output.txt /tmp/main-output-task6.txt
```

Expected: no diff.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/gui/DemoScenarios.java src/main/java/gui/TaskManagerGUI.java
git commit -m "feat(gui): add demo scenarios and menu bar"
```

---

### Task 7: Color-coded rows by status

**Files:**

- Modify: `src/main/java/gui/TaskTablePanel.java`

- [ ] **Step 1: Add a status color renderer**

Edit `src/main/java/gui/TaskTablePanel.java`. Add these imports at the top:

```java
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
```

In the constructor, after the line `table.setRowHeight(24);` add:

```java
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());
```

At the bottom of the class, before the closing brace, add the inner renderer:

```java
    /** Renders rows with a background color based on the task's status. */
    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (selected) return c;
            Task task = model.getTaskAt(row);
            if (task == null) return c;
            switch (task.getStatus()) {
                case OPEN:        c.setBackground(new Color(0xEEEEEE)); break;
                case IN_PROGRESS: c.setBackground(new Color(0xCFE2FF)); break;
                case REVIEW:      c.setBackground(new Color(0xFFF3CD)); break;
                case BLOCKED:     c.setBackground(new Color(0xF8D7DA)); break;
                case DONE:        c.setBackground(new Color(0xD1E7DD)); break;
                default:          c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
```

- [ ] **Step 2: Compile and smoke test**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin TaskManagerGUI
```

Expected: load `Demo → Load Integration Demo`. Rows show color-coded backgrounds (light blue for IN_PROGRESS, light yellow for REVIEW, light gray for OPEN, etc.).

- [ ] **Step 3: Verify zero regression**

```bash
java -cp bin Main > /tmp/main-output-task7.txt
diff docs/design/baseline-main-output.txt /tmp/main-output-task7.txt
```

Expected: no diff.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/gui/TaskTablePanel.java
git commit -m "feat(gui): color-code table rows by task status"
```

---

## Phase 5: Packaging

### Task 8: Build script + manifest for runnable JAR

**Files:**

- Create: `build-jar.sh`
- Create: `build-jar.bat`

- [ ] **Step 1: Create the bash build script**

Write `build-jar.sh`:

```bash
#!/usr/bin/env bash
# Builds TaskManagerGUI.jar — a self-contained runnable JAR for the GUI demo.
# Run this once before zipping for the professor.

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "==> Cleaning bin/"
rm -rf bin
mkdir -p bin

echo "==> Compiling all sources (engine + GUI)"
javac -d bin src/main/java/*.java src/main/java/gui/*.java

echo "==> Writing JAR manifest"
echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: TaskManagerGUI" >> manifest.txt
echo "" >> manifest.txt

echo "==> Building TaskManagerGUI.jar"
jar cfm TaskManagerGUI.jar manifest.txt -C bin .
rm manifest.txt

echo "==> Done. To launch:"
echo "    java -jar TaskManagerGUI.jar"
```

- [ ] **Step 2: Create the Windows build script**

Write `build-jar.bat`:

```bat
@echo off
REM Builds TaskManagerGUI.jar — runnable JAR for the GUI demo.
REM Run this once before zipping for the professor.

setlocal

cd /d "%~dp0"

echo ==^> Cleaning bin\
if exist bin rmdir /s /q bin
mkdir bin

echo ==^> Compiling all sources (engine + GUI)
javac -d bin src\main\java\*.java src\main\java\gui\*.java
if errorlevel 1 goto :error

echo ==^> Writing JAR manifest
echo Manifest-Version: 1.0> manifest.txt
echo Main-Class: TaskManagerGUI>> manifest.txt
echo.>> manifest.txt

echo ==^> Building TaskManagerGUI.jar
jar cfm TaskManagerGUI.jar manifest.txt -C bin .
del manifest.txt

echo ==^> Done. To launch:
echo     java -jar TaskManagerGUI.jar
goto :eof

:error
echo BUILD FAILED
exit /b 1
```

- [ ] **Step 3: Make the bash script executable**

```bash
chmod +x build-jar.sh
```

- [ ] **Step 4: Run the build**

On Windows (Git Bash): `bash build-jar.sh`
On Mac/Linux: `./build-jar.sh`

Expected: ends with `==> Done. To launch:` and `TaskManagerGUI.jar` exists in the project root.

- [ ] **Step 5: Smoke test the JAR**

```bash
java -jar TaskManagerGUI.jar
```

Expected: GUI window opens (no `-cp bin` needed because the JAR is self-contained).

- [ ] **Step 6: Commit**

```bash
git add build-jar.sh build-jar.bat TaskManagerGUI.jar
git commit -m "build: add JAR build scripts and runnable TaskManagerGUI.jar"
```

(Note: `TaskManagerGUI.jar` is checked in deliberately so the professor doesn't need a JDK to run it via double-click — only a JRE.)

---

### Task 9: Launcher scripts for double-click runs

**Files:**

- Create: `run-gui.bat`
- Create: `run-gui.sh`

- [ ] **Step 1: Create the Windows launcher**

Write `run-gui.bat`:

```bat
@echo off
REM Double-click this file to launch the Task Manager GUI.
cd /d "%~dp0"
java -jar TaskManagerGUI.jar
if errorlevel 1 (
    echo.
    echo Could not launch the GUI. Make sure Java 8 or newer is installed.
    echo Try running:  java -version
    pause
)
```

- [ ] **Step 2: Create the Mac/Linux launcher**

Write `run-gui.sh`:

```bash
#!/usr/bin/env bash
# Run this script (or double-click it on systems that support it) to launch the GUI.
cd "$( dirname "${BASH_SOURCE[0]}" )"
java -jar TaskManagerGUI.jar
```

- [ ] **Step 3: Make the bash launcher executable**

```bash
chmod +x run-gui.sh
```

- [ ] **Step 4: Smoke test by double-clicking (or invoking) each script**

On Windows: double-click `run-gui.bat` in Explorer. Expected: GUI opens.
On Mac/Linux Git Bash: `./run-gui.sh`. Expected: GUI opens.

- [ ] **Step 5: Commit**

```bash
git add run-gui.bat run-gui.sh
git commit -m "build: add double-click launcher scripts for the GUI"
```

---

### Task 10: Update README with GUI quick-start

**Files:**

- Modify: `README.md`

- [ ] **Step 1: Read the current README to know where to insert**

```bash
head -60 README.md
```

Identify the location of the existing "Quick Start" or "Build & Run" section so the new GUI block can be inserted directly before it.

- [ ] **Step 2: Insert the new GUI section**

Add the following block immediately after the project-overview / heading section of `README.md` and before the existing terminal-build section. Use the Edit tool with the existing first-section content as the `old_string` anchor (do not delete or reorder existing content):

````markdown
## Quick Start (GUI demo)

The fastest way to see the project run:

1. Unzip `SEN3006_Task_Management_System.zip`.
2. Double-click **`TaskManagerGUI.jar`** (or **`run-gui.bat`** on Windows / **`run-gui.sh`** on Mac/Linux).
3. The Task Manager window opens. Use the **Demo** menu to load preset scenarios, or fill in the form to create your own tasks. Switch the **Sort by** dropdown to see the Strategy pattern swap the order live.

Requires Java 8 or newer. If double-click does not work on your OS, run:

```bash
java -jar TaskManagerGUI.jar
```

## Quick Start (terminal demo)

For the original 6-section pattern walkthrough used during development:

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main
```

For the interactive console menu:

```bash
java -cp bin TaskManagementApp
```

All three entry points (`TaskManagerGUI`, `Main`, `TaskManagementApp`) share the same engine — the GUI is a Swing wrapper consuming the public API of `TaskManager`, `TaskFactory`, and `PriorityStrategy`.

## Building from source

To rebuild the JAR after editing source files:

```bash
bash build-jar.sh    # Mac/Linux/Git Bash
build-jar.bat        # Windows cmd
```
````

- [ ] **Step 3: Verify Markdown renders cleanly**

```bash
head -80 README.md
```

Expected: the new section is present, fenced code blocks balance, no garbled output.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: add GUI quick-start section to README"
```

---

### Task 11: Rebuild the submission zip

**Files:**

- Backup: `SEN3006_Task_Management_System.zip` → `SEN3006_Task_Management_System-original.zip` (only if not already saved)
- Regenerate: `SEN3006_Task_Management_System.zip`

- [ ] **Step 1: Preserve a safety copy of the current zip**

```bash
if [ ! -f SEN3006_Task_Management_System-original.zip ]; then
  cp SEN3006_Task_Management_System.zip SEN3006_Task_Management_System-original.zip
fi
```

Expected: file `SEN3006_Task_Management_System-original.zip` exists. (No action if it already exists from a previous attempt.)

- [ ] **Step 2: Rebuild the JAR fresh**

```bash
bash build-jar.sh
```

Expected: `==> Done.` line; `TaskManagerGUI.jar` is updated.

- [ ] **Step 3: Rebuild the submission zip**

```bash
rm -f SEN3006_Task_Management_System.zip
zip -r SEN3006_Task_Management_System.zip \
  README.md \
  SUBMISSION_README.md \
  src/ \
  docs/ \
  TaskManagerGUI.jar \
  run-gui.bat \
  run-gui.sh \
  build-jar.sh \
  build-jar.bat \
  -x "docs/superpowers/*" -x "*/.DS_Store"
```

Expected: `SEN3006_Task_Management_System.zip` is regenerated. The `-x "docs/superpowers/*"` exclusion keeps internal planning docs out of the professor's zip.

- [ ] **Step 4: Verify zip contents**

```bash
unzip -l SEN3006_Task_Management_System.zip | head -40
```

Expected to see (among others):

- `README.md`
- `TaskManagerGUI.jar`
- `run-gui.bat`, `run-gui.sh`
- `src/main/java/Main.java`
- `src/main/java/TaskManager.java`
- `src/main/java/gui/TaskManagerGUI.java`
- `src/main/java/gui/TaskTableModel.java`
- `src/main/java/gui/TaskTablePanel.java`
- `src/main/java/gui/TaskFormPanel.java`
- `src/main/java/gui/DemoScenarios.java`

And NOT see `docs/superpowers/...`.

- [ ] **Step 5: Final smoke test — extract the zip into a scratch dir and run the JAR**

```bash
mkdir -p /tmp/sen3006-test && rm -rf /tmp/sen3006-test/*
cp SEN3006_Task_Management_System.zip /tmp/sen3006-test/
cd /tmp/sen3006-test
unzip -q SEN3006_Task_Management_System.zip
java -jar TaskManagerGUI.jar
cd -
```

Expected: GUI opens from the freshly extracted zip. This simulates exactly what the professor will do.

- [ ] **Step 6: Final regression check on Main**

```bash
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main > /tmp/main-output-final.txt
diff docs/design/baseline-main-output.txt /tmp/main-output-final.txt
```

Expected: no diff. Existing terminal demo is fully intact.

- [ ] **Step 7: Commit**

```bash
git add SEN3006_Task_Management_System.zip SEN3006_Task_Management_System-original.zip
git commit -m "build: rebuild submission zip to include GUI JAR + launchers"
```

---

## Self-review checklist (before declaring done)

- [ ] All 16 existing engine `.java` files in `src/main/java/` are byte-identical to their state at Task 0 (verify with `git diff <baseline-commit> -- src/main/java/*.java` showing no changes outside `gui/`).
- [ ] `Main.java` output matches the baseline (Task 11 Step 6).
- [ ] `TaskManagerGUI.jar` double-click on Windows opens the window.
- [ ] `Demo → Load Strategy Demo` + Sort dropdown switching reorders the table visibly.
- [ ] Selecting a task shows correctly enabled/disabled transition buttons (e.g., `→ DONE` disabled for OPEN tasks).
- [ ] Adding a task with empty title shows the engine's validation dialog.
- [ ] README's new "Quick Start (GUI)" section is present and renders cleanly.
- [ ] The rebuilt zip extracts to a clean directory and the JAR launches from it.
- [ ] No internal planning docs (`docs/superpowers/`) leaked into the submission zip.
