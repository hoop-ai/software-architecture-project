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
