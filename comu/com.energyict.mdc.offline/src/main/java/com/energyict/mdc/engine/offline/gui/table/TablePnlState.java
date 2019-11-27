package com.energyict.mdc.engine.offline.gui.table;

import com.energyict.mdc.engine.offline.gui.actions.SortSettings;

import java.util.HashSet;
import java.util.Set;

public class TablePnlState {

    private Set<Integer> selectedRows = new HashSet<Integer>(); // indices of all selected rows
    private SortSettings sortSettings = null;
    private int sortingCol = -1;
    private boolean ascending = true;

    public TablePnlState() {

    }

    public TablePnlState(Set<Integer> rows, SortSettings sortSettings) {
        this.selectedRows = rows;
        this.sortSettings = sortSettings;
    }

    public TablePnlState(Set<Integer> rows, int col, boolean asc) {
        this.selectedRows = rows;
        this.sortSettings = new SortSettings();
        sortSettings.addIndexToSortOn(col, asc);
    }

    public Set<Integer> getSelectedRows() {
        return selectedRows;
    }

    public SortSettings getSortSettings() {
        return sortSettings;
    }

    public void setSortSettings(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
    }


    public int getSortingColumn() {
        return sortingCol;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void addRow(int index) {
        selectedRows.add(new Integer(index));
    }

    public void removeRow(int index) {
        selectedRows.remove(new Integer(index));
    }

    public void setSortingColumn(int col) {
        sortingCol = col;
    }

    public void setAscending(boolean asc) {
        ascending = asc;
    }

    public void clearSelectedRows() {
        selectedRows.clear();
    }
}