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
