/*
 * RestrictedTableBubbleSortDecorator.java
 *
 * Created on 22 oktober 2003, 16:29
 */

package com.energyict.mdc.engine.offline.gui.table;

import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * @author Koen
 */
public class RestrictedTableBubbleSortDecorator extends TableBubbleSortDecorator {

    TableModel model;

    /**
     * Creates a new instance of RestrictedTableBubbleSortDecorator
     */
    public RestrictedTableBubbleSortDecorator(TableModel model) {
        super(model);
        this.model = model;
    }

    public void sort(int column, boolean bAscending) {
        if (!(model.getColumnClass(column).isAssignableFrom(JCheckBox.class) ||
                model.getColumnClass(column).isAssignableFrom(JButton.class)))
            super.sort(column, bAscending);
    }
}
