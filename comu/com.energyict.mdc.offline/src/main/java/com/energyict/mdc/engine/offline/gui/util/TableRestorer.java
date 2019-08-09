package com.energyict.mdc.engine.offline.gui.util;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;

public class TableRestorer {

    protected JTable table;
    private DefaultTableColumnModel columnModel;

    /**
     * Creates a new instance of TableRestorer
     */
    public TableRestorer(JTable table) {
        this.table = table;
        columnModel = new DefaultTableColumnModel();
        init();
    }

    private void init() {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            columnModel.addColumn(table.getColumnModel().getColumn(columnIndex));
        }
    }

    public void restore() {
        table.setColumnModel(columnModel);
    }

}
