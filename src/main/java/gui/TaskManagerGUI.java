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
