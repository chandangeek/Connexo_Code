/*
 * SortInfoTableModel.java
 *
 * Created on 16 december 2004, 11:51
 */

package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.actions.SortInfo;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author Geert
 */
public class SortInfoTableModel extends AbstractTableModel {

    private List info;  // List of SortInfo objects

    final String[] columnNames = {
            TranslatorProvider.instance.get().getTranslator().getTranslation("name"),
            TranslatorProvider.instance.get().getTranslator().getTranslation("ascending")
    };

    /**
     * Creates a new instance of SortInfoTableModel
     */
    public SortInfoTableModel(List info) {
        this.info = info;
    }

    public int getRowCount() {
        return info.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getObjectAt(int row) {
        return info.get(row);
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= info.size()) {
            return null;
        }
        SortInfo theInfo = (SortInfo) info.get(row);
        switch (col) {
            case 0:
                return theInfo.getName();
            case 1:
                return Boolean.valueOf(theInfo.getAscending());
            default:
                return null;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (col != 1) {
            return;
        }
        SortInfo theInfo = (SortInfo) info.get(row);
        theInfo.setAscending(((Boolean) value).booleanValue());
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void removeElementAt(int index) {
        if (index < info.size()) {
            info.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    public void insertElementAt(SortInfo theInfo, int index) {
        info.add(index, theInfo);
        fireTableRowsInserted(info.size() - 1, info.size());
    }

}
