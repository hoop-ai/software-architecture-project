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
