package com.energyict.mdc.engine.offline.gui.selecting;

import com.energyict.mdc.engine.offline.gui.models.AspectTableModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MultiSelectTableItemPnl<T> extends SelectTableItemPnl<T> {

    private List<T> selectedItems = new ArrayList<>();

    public MultiSelectTableItemPnl(List<T> items, Class<T> classPara, T preselection, List<String> columns, boolean showNone) {
        super(items, classPara, preselection, columns, showNone);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public void setSelectionMode(int mode) {
        selectionTable.setSelectionMode(mode);
    }
    @SuppressWarnings("unchecked")
    protected void storeSelection() {
        selectedItems.clear();
        for (int row: selectionTable.getSelectedRows()){
            int realRow = rowSorter.convertRowIndexToModel(row);
            selectedItems.add(((AspectTableModel<T>) selectionTable.getModel()).getObjectAt(realRow));
        }
    }

    public List<T> getSelectedItems() {
        return selectedItems;
    }

    @Override
    protected void tableClicked(JTable table) {
        //do nothing!
    }
}
