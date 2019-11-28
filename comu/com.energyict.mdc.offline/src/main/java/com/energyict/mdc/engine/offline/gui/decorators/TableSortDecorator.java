package com.energyict.mdc.engine.offline.gui.decorators;

import javax.swing.table.TableModel;

public abstract class TableSortDecorator extends TableModelDecorator {

    protected boolean sortOnChange = true;

    // Extensions of TableSortDecorator must implement the
    // abstract sort method, in addition to tableChanged. The
    // latter is required because TableModelDecorator
    // implements the TableModelListener interface.
    abstract public void sort(int column);

    public TableSortDecorator(TableModel realModel) {
        super(realModel);
    }

    // to be overriden in subclasses where the sort can be activated/deActivated
    public boolean isActive() {
        return true;
    }

    public boolean getSortOnChange() {
        return sortOnChange;
    }

    public void setSortOnChange(boolean sortOnChange) {
        this.sortOnChange = sortOnChange;
    }


}
