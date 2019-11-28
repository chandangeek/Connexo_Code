package com.energyict.mdc.engine.offline.gui.table.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * pdo: 23/08/12
 * Changes made so to minimize the impact on the UI delegate's appearance
 */

public class HeaderRenderer implements TableCellRenderer {
    // Delegating the renderer to the default TableHeaderRenderer
    protected TableCellRenderer delegate;

    /**
     * Creates a new instance of HeaderRenderer
     * @param table Jtable the renderer will be used
     */
    public HeaderRenderer(JTable table) {
        // As sort icon we use our own SortableTableHeader.SortOrderIcon
        UIManager.put("Table.ascendingSortIcon", new NullIcon());
        UIManager.put("Table.descendingSortIcon", new NullIcon());

        delegate = table.getTableHeader().getDefaultRenderer();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (result instanceof JLabel){
            ((DefaultTableCellRenderer) result).getUI().uninstallUI((DefaultTableCellRenderer) result);
            ((JLabel) result).setHorizontalAlignment(JLabel.CENTER);
            ((JLabel) result).setHorizontalTextPosition(JLabel.LEADING);
        }
        if (column >= 0){
            String tooltipText = getToolTipText(table.getColumnModel().getColumn(column), result);
            if (result instanceof JComponent){
                ((JComponent) result).setToolTipText(tooltipText);
            }
        }
        return result;
    }

    protected String getToolTipText(TableColumn column, Component headerComponent){
        // Tooltip set when column not wide enough to display the whole header text
        if (column.getWidth() < headerComponent.getPreferredSize().width) {
            return column.getHeaderValue()!=null ? column.getHeaderValue().toString() : null;
        }
        return null;
    }

    public class NullIcon implements Icon{

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // draw nothing
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 5;
        }
    }
}
