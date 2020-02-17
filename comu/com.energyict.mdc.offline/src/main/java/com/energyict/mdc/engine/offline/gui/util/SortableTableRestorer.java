/*
 * SortableTableRestorer.java
 * 
 * is a TableRestorer used with tables where the model is 
 * instanceof com.enenergyict.cso.models.TableBubbleSortDecorator
 * 
 * restore resets the sorting...
 *
 * Created on 10 juni 2004, 11:25
 */

package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.gui.actions.SortSettings;
import com.energyict.mdc.engine.offline.gui.decorators.TableBubbleSortDecorator;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * @author pasquien
 */
public class SortableTableRestorer extends TableRestorer {

    int sortColumn = 0;
    boolean isSortingAscending = true;
    SortSettings sortSettings = null;

    /**
     * Creates a new instance of TableRestorer
     */
    public SortableTableRestorer(JTable table) {
        super(table);
        TableBubbleSortDecorator currentModel = (TableBubbleSortDecorator) table.getModel();
        sortColumn = currentModel.getSortingColumn();
        isSortingAscending = currentModel.isSortingAscending();
        sortSettings = currentModel.getSortSettings();

    }

    public void restore(TableModel realModel) {
        TableBubbleSortDecorator currentModel = (TableBubbleSortDecorator) table.getModel();
        currentModel.removeMouseListenersFromHeaderInTable();

        TableBubbleSortDecorator newModel = new TableBubbleSortDecorator(realModel);
        newModel.addMouseListenerToHeaderInTable(table);
        if (sortSettings != null) {
            newModel.setSortSettings(sortSettings);
            newModel.resort();
        } else {
            newModel.sortInitially(sortColumn, isSortingAscending);
        }
        table.setModel(newModel);
        super.restore();
    }

}
