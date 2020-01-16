package com.energyict.mdc.engine.offline.gui.models;


import javax.swing.table.TableModel;

public abstract class TableFilterDecorator extends
        TableModelDecorator {

    // Extensions of TableModelDecorator must implement the
    // abstract filter method, in addition to tableChanged.
    // The latter is required because TableModelDecorator
    // implements the TableModelListener interface.
    abstract public void filter();

    public TableFilterDecorator(TableModel realModel) {
        super(realModel);
    }
}
