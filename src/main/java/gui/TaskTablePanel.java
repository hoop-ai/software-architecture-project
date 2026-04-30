import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
        table.setDefaultRenderer(Object.class, new StatusColorRenderer());
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

    /** Renders rows with a background color based on the task's status. */
    private class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected, boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (selected) return c;
            Task task = model.getTaskAt(t.convertRowIndexToModel(row));
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
}
