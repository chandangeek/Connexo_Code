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
public class RowSelectionTableModel extends TableSelectionTableModel {

    private int rowIdentificationColumn = 0;


    /**
     * Creates a new instance of ColumnSelectionTableModel
     */
    public RowSelectionTableModel(TableModel tableModel) {
        super(tableModel);
    }

    public void setRowIdentificationColumn(int column) {
        rowIdentificationColumn = column;
    }

    public int getRowCount() {
        return realModel.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return null;
        }
        switch (col) {
            case 0:
                return Boolean.valueOf(isSelected(row));
            case 1:
                return realModel.getValueAt(row, rowIdentificationColumn);
        }
        return null;
    }

}
