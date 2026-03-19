import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console application for the Task Management System.
 *
 * <p>This class provides a menu-driven interface that allows users to interact
 * with the system in real-time: create tasks, view them, change their status,
 * switch sorting strategies, and see summaries.</p>
 *
 * <p><strong>How to run:</strong></p>
 * <pre>
 *   javac -d bin src/main/java/*.java
 *   java -cp bin TaskManagementApp
 * </pre>
 *
 * <p><strong>Design Pattern Usage:</strong></p>
 * <ul>
 *   <li><strong>Factory Method:</strong> When you create a task, the system uses
 *       the registered factory for that type (BUG/FEATURE/DOCUMENTATION) to create
 *       the correct task object — you never need to know which class is used.</li>
 *   <li><strong>Strategy:</strong> When you change the sorting strategy, the same
 *       list of tasks is re-sorted using a different algorithm — no code changes,
 *       just swap the strategy object at runtime.</li>
 * </ul>
 */
public class TaskManagementApp {

    // The core system that manages all tasks
    private final TaskManager manager;

    // Scanner for reading user input from the console
    private final Scanner scanner;

    // Flag to control the main loop
    private boolean running;

    /**
     * Creates a new application instance with a fresh TaskManager.
     */
    public TaskManagementApp() {
        this.manager = new TaskManager();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    // -----------------------------------------------------------------------
    // Main entry point
    // -----------------------------------------------------------------------

    /**
     * Application entry point. Creates the app and starts the menu loop.
     */
    public static void main(String[] args) {
        TaskManagementApp app = new TaskManagementApp();
        app.run();
    }

    // -----------------------------------------------------------------------
    // Main application loop
    // -----------------------------------------------------------------------

    /**
     * Runs the main menu loop until the user chooses to exit.
     */
    public void run() {
        printWelcome();

        while (running) {
            printMenu();
            int choice = readInt("Your choice: ");

            switch (choice) {
                case 1:
                    createTask();
                    break;
                case 2:
                    viewAllTasks();
                    break;
                case 3:
                    viewPrioritizedTasks();
                    break;
                case 4:
                    changeSortingStrategy();
                    break;
                case 5:
                    changeTaskStatus();
                    break;
                case 6:
                    viewTasksByStatus();
                    break;
                case 7:
                    viewTaskDetails();
                    break;
                case 8:
                    removeTask();
                    break;
                case 9:
                    viewSummary();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nGoodbye! Thank you for using the Task Management System.");
                    break;
                default:
                    System.out.println("\nInvalid option. Please enter a number from the menu.");
            }
        }

        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Menu display
    // -----------------------------------------------------------------------

    private void printWelcome() {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("       TASK MANAGEMENT SYSTEM - Interactive Mode");
        System.out.println("       SEN3006 Software Architecture Project");
        System.out.println("==========================================================");
        System.out.println("  Patterns: Factory Method (Creational) + Strategy (Behavioral)");
        System.out.println("  Type 'help' at any prompt for guidance.");
        System.out.println("==========================================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("  MAIN MENU");
        System.out.println("----------------------------------------------------------");
        System.out.println("  1. Create a new task");
        System.out.println("  2. View all tasks");
        System.out.println("  3. View tasks (sorted by current strategy)");
        System.out.println("  4. Change sorting strategy");
        System.out.println("  5. Change task status");
        System.out.println("  6. Filter tasks by status");
        System.out.println("  7. View task details");
        System.out.println("  8. Remove a task");
        System.out.println("  9. View summary");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    // Menu actions
    // -----------------------------------------------------------------------

    /**
     * Creates a new task by asking the user for type and details.
     *
     * FACTORY METHOD PATTERN IN ACTION:
     * The user picks a type (BUG/FEATURE/DOCUMENTATION), and the system
     * looks up the correct factory from the registry to create the task.
     * The user (and this code) never directly calls "new BugTask()" —
     * the factory handles it.
     */
    private void createTask() {
        System.out.println("\n--- Create New Task ---");
        System.out.println("  Task types: BUG, FEATURE, DOCUMENTATION");

        String type = readString("  Enter task type: ").toUpperCase();

        // Validate type before asking for more details
        if (!type.equals("BUG") && !type.equals("FEATURE") && !type.equals("DOCUMENTATION")) {
            System.out.println("  Error: Unknown type '" + type + "'. Use BUG, FEATURE, or DOCUMENTATION.");
            return;
        }

        String title = readString("  Enter title: ");
        String description = readString("  Enter description: ");
        int priority = readIntInRange("  Enter priority (1=lowest, 5=highest): ", 1, 5);

        try {
            // This is where Factory Method happens:
            // manager.createTask() looks up the factory for this type and delegates creation
            Task task = manager.createTask(type, title, description, priority);
            System.out.println("\n  Task created successfully!");
            System.out.println("  " + task);

            // Ask for optional deadline
            String addDeadline = readString("  Add a deadline? (y/n): ");
            if (addDeadline.equalsIgnoreCase("y")) {
                String dateStr = readString("  Enter deadline (YYYY-MM-DD): ");
                try {
                    LocalDate deadline = LocalDate.parse(dateStr);
                    task.setDeadline(deadline);
                    System.out.println("  Deadline set to: " + deadline);
                } catch (DateTimeParseException e) {
                    System.out.println("  Invalid date format. Deadline not set.");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    /**
     * Displays all tasks in the order they were created.
     */
    private void viewAllTasks() {
        System.out.println("\n--- All Tasks ---");
        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks yet. Create one from the menu!");
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + tasks.get(i));
        }
        System.out.println("  Total: " + tasks.size() + " task(s)");
    }

    /**
     * Displays tasks sorted by the current prioritization strategy.
     *
     * STRATEGY PATTERN IN ACTION:
     * The same list of tasks is sorted differently depending on which
     * strategy is currently set. The TaskManager delegates sorting to
     * the strategy object — it doesn't contain any sorting logic itself.
     */
    private void viewPrioritizedTasks() {
        System.out.println("\n--- Prioritized Tasks ---");
        System.out.println("  Current strategy: " + manager.getCurrentStrategyName());

        List<Task> tasks = manager.getPrioritizedTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks yet.");
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Lets the user switch between different sorting strategies at runtime.
     *
     * STRATEGY PATTERN IN ACTION:
     * This is the "swap" — we create a new strategy object and hand it
     * to the TaskManager. The next time tasks are sorted, the new algorithm
     * is used. No code in TaskManager changes — only the strategy object.
     */
    private void changeSortingStrategy() {
        System.out.println("\n--- Change Sorting Strategy ---");
        System.out.println("  Current: " + manager.getCurrentStrategyName());
        System.out.println();
        System.out.println("  1. UrgentFirst   - Highest priority number first");
        System.out.println("  2. DeadlineFirst - Earliest deadline first");
        System.out.println("  3. SeverityFirst - Bugs by severity, then others by priority");

        int choice = readInt("  Select strategy: ");

        switch (choice) {
            case 1:
                manager.setPriorityStrategy(new UrgentFirstStrategy());
                System.out.println("  Strategy changed to: UrgentFirstStrategy");
                break;
            case 2:
                manager.setPriorityStrategy(new DeadlineFirstStrategy());
                System.out.println("  Strategy changed to: DeadlineFirstStrategy");
                break;
            case 3:
                manager.setPriorityStrategy(new SeverityFirstStrategy());
                System.out.println("  Strategy changed to: SeverityFirstStrategy");
                break;
            default:
                System.out.println("  Invalid choice. Strategy unchanged.");
        }
    }

    /**
     * Lets the user transition a task to a new status.
     * The state machine in TaskStatus validates the transition.
     */
    private void changeTaskStatus() {
        System.out.println("\n--- Change Task Status ---");

        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks available.");
            return;
        }

        // Show current tasks with their status
        for (Task task : tasks) {
            System.out.println("  ID " + task.getId() + ": " + task.getTitle()
                    + " [" + task.getStatus() + "]");
        }

        int taskId = readInt("\n  Enter task ID: ");

        System.out.println("  Available statuses: OPEN, IN_PROGRESS, REVIEW, DONE, BLOCKED");
        String statusStr = readString("  Enter new status: ").toUpperCase();

        try {
            TaskStatus newStatus = TaskStatus.valueOf(statusStr);
            manager.transitionTask(taskId, newStatus);
            Task task = manager.getTask(taskId);
            System.out.println("  Status changed! " + task.getTitle() + " is now " + task.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    /**
     * Filters and displays tasks that have a specific status.
     */
    private void viewTasksByStatus() {
        System.out.println("\n--- Filter by Status ---");
        System.out.println("  1. OPEN");
        System.out.println("  2. IN_PROGRESS");
        System.out.println("  3. REVIEW");
        System.out.println("  4. DONE");
        System.out.println("  5. BLOCKED");

        int choice = readInt("  Select status: ");

        TaskStatus status;
        switch (choice) {
            case 1: status = TaskStatus.OPEN; break;
            case 2: status = TaskStatus.IN_PROGRESS; break;
            case 3: status = TaskStatus.REVIEW; break;
            case 4: status = TaskStatus.DONE; break;
            case 5: status = TaskStatus.BLOCKED; break;
            default:
                System.out.println("  Invalid choice.");
                return;
        }

        List<Task> filtered = manager.getTasksByStatus(status);
        System.out.println("\n  Tasks with status " + status + ":");
        if (filtered.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Task task : filtered) {
                System.out.println("  - " + task);
            }
        }
    }

    /**
     * Shows detailed information about a specific task.
     */
    private void viewTaskDetails() {
        System.out.println("\n--- Task Details ---");

        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks available.");
            return;
        }

        // Show task IDs for reference
        for (Task task : tasks) {
            System.out.println("  ID " + task.getId() + ": " + task.getTitle());
        }

        int taskId = readInt("\n  Enter task ID: ");

        try {
            Task task = manager.getTask(taskId);
            System.out.println();
            System.out.println("  ================================================");
            System.out.println("  Task #" + task.getId());
            System.out.println("  ================================================");
            System.out.println("  Type:        " + task.getType());
            System.out.println("  Title:       " + task.getTitle());
            System.out.println("  Description: " + task.getDescription());
            System.out.println("  Status:      " + task.getStatus());
            System.out.println("  Priority:    " + task.getPriority() + "/5");
            System.out.println("  Deadline:    " + (task.getDeadline() != null ? task.getDeadline() : "Not set"));
            System.out.println("  Created:     " + task.getCreatedAt());

            // Show type-specific details
            if (task instanceof BugTask) {
                BugTask bug = (BugTask) task;
                System.out.println("  Severity:    " + bug.getSeverity());
                System.out.println("  Steps:       " + bug.getStepsToReproduce());
            } else if (task instanceof FeatureTask) {
                FeatureTask feature = (FeatureTask) task;
                System.out.println("  Effort:      " + feature.getEstimatedEffort() + " hours");
                System.out.println("  Biz Value:   " + feature.getBusinessValue() + "/10");
            } else if (task instanceof DocumentationTask) {
                DocumentationTask doc = (DocumentationTask) task;
                System.out.println("  Doc Type:    " + doc.getDocumentType());
                System.out.println("  Audience:    " + doc.getTargetAudience());
            }
            System.out.println("  ================================================");
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    /**
     * Removes a task by ID.
     */
    private void removeTask() {
        System.out.println("\n--- Remove Task ---");

        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks available.");
            return;
        }

        for (Task task : tasks) {
            System.out.println("  ID " + task.getId() + ": " + task.getTitle() + " [" + task.getStatus() + "]");
        }

        int taskId = readInt("\n  Enter task ID to remove: ");
        String confirm = readString("  Are you sure? (y/n): ");

        if (confirm.equalsIgnoreCase("y")) {
            boolean removed = manager.removeTask(taskId);
            if (removed) {
                System.out.println("  Task removed.");
            } else {
                System.out.println("  No task found with ID " + taskId);
            }
        } else {
            System.out.println("  Cancelled.");
        }
    }

    /**
     * Displays a summary of all tasks grouped by status.
     */
    private void viewSummary() {
        System.out.println("\n--- Task Summary ---");
        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks yet.");
            return;
        }
        System.out.print("  " + manager.getTaskSummary().replace("\n", "\n  "));
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Input helper methods
    // -----------------------------------------------------------------------

    /**
     * Reads a non-empty string from the user.
     */
    private String readString(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print("  Input cannot be empty. " + prompt);
            input = scanner.nextLine().trim();
        }
        return input;
    }

    /**
     * Reads an integer from the user, re-prompting on invalid input.
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    /**
     * Reads an integer within a specific range from the user.
     */
    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  Please enter a number between " + min + " and " + max + ".");
        }
    }
}
