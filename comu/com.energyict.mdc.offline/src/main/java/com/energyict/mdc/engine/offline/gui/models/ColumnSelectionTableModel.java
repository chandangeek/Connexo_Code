/*
 * ColumnSelectionTableModel.java
 *
 * TableModel that can be used to select/deselect columns from another TableModel
 * 
 */

package com.energyict.mdc.engine.offline.gui.models;

import javax.swing.table.TableModel;

/**
 * @author pasquien
 */
public class ColumnSelectionTableModel extends TableSelectionTableModel {

    public ColumnSelectionTableModel(TableModel tableModel) {
        super(tableModel);
    }

    public int getRowCount() {
        return realModel.getColumnCount();
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return null;
        }
        switch (col) {
            case 0:
                return Boolean.valueOf(isSelected(row));
            case 1:
                return realModel.getColumnName(row);
        }
        return null;
    }

}
