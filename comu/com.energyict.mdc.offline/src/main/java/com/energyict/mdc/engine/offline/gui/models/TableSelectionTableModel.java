/*
 * TableSelectionTableModel.java
 *
 * Created on 13 juli 2005, 13:57
 */

package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pasquien
 */
public abstract class TableSelectionTableModel extends AbstractTableModel {

    public static final int MULTIPLE_SELECTION = 0;
    public static final int SINGLE_SELECTION = 1;

    TableModel realModel;
    private int selectionMode = MULTIPLE_SELECTION;
    private List selected = new ArrayList();


    public TableSelectionTableModel(TableModel tableModel) {
        this.realModel = tableModel;
    }

    public TableModel getRealModel() {
        return realModel;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        if (selectionMode == SINGLE_SELECTION) {
            if (!isSelectionEmpty()) {
                Integer firstSelected = (Integer) selected.get(0);
                selected = new ArrayList();
                selected.add(firstSelected);
            }
            fireTableDataChanged();
        }
    }

    public boolean isSelectionEmpty() {
        return selected.isEmpty();
    }

    public int[] getSelected() {
        int[] result = new int[selected.size()];
        for (int i = 0; i < selected.size(); i++) {
            result[i] = ((Integer) selected.get(i)).intValue();
        }
        return result;
    }

    public void setSelected(int[] columns) {
        selected = new ArrayList();
        if (columns == null) {
            return;
        }
        for (int i = 0; i < columns.length; i++) {
            selected.add(new Integer(columns[i]));
            if (selectionMode == SINGLE_SELECTION) {
                break;
            } // Only the first one !
        }
    }

    public void select(int col) {
        if (selectionMode == SINGLE_SELECTION) {
            selected = new ArrayList();
        }
        selected.add(new Integer(col));
    }

    public void selectAndNotify(int col) {
        select(col);
        fireTableCellUpdated(col, 0);
    }

    public void selectAll() {
        int rows = this.getRowCount();
        for (int i = 0; i < rows; i++) {
            Object value = getValueAt(i, 0);
            if (value == null) {
                continue;
            }
            if (((Boolean) value).booleanValue() == false) {
                setValueAt(Boolean.TRUE, i, 0);
            }
        }
    }

    public void deSelect(int col) {
        selected.remove(new Integer(col));
    }

    public void deSelectAll() {
        selected = new ArrayList();
        fireTableDataChanged();
    }

    protected boolean isSelected(int col) {
        return selected.indexOf(new Integer(col)) >= 0;
    }

    public int getColumnCount() {
        return 2;
    }

    public Class getColumnClass(int col) {
        if (col == 0) {
            return Boolean.class;
        } else {
            return Object.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return false;
        }
        return (col == 0);
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isCellEditable(row, col)) {
            return;
        }
        if (col == 0) {
            if (((Boolean) value).booleanValue()) {
                select(row);
                if (getSelectionMode() == SINGLE_SELECTION) {
                    fireTableDataChanged();
                } else {
                    fireTableCellUpdated(row, col);
                }
            } else {
                deSelect(row);
                fireTableCellUpdated(row, col);
            }
        }
    }

}
