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
