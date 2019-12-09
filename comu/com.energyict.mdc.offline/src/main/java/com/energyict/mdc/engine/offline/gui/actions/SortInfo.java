/*
 * SortInfo.java
 *
 * Created on 16 december 2004, 10:31
 */

package com.energyict.mdc.engine.offline.gui.actions;

import javax.swing.*;

/**
 * @author Geert
 */
public class SortInfo {

    private String name;
    private SortOrder sortOrder;

    public SortInfo(String name) {
        this(name, SortOrder.ASCENDING);
    }
    @Deprecated // use SortInfo(String name, SortOrder order)
    public SortInfo(String name, boolean ascending) {
        this(name, ascending ? SortOrder.ASCENDING: SortOrder.DESCENDING);
    }

    public SortInfo(String name, SortOrder order){
        this.name = name;
        this.sortOrder = order;
    }

    public String getName() {
        return name;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Deprecated // use getSortOrder
    public boolean getAscending() {
        return this.sortOrder.equals(SortOrder.ASCENDING);
    }
    @Deprecated // use setSortOrder
    public void setAscending(boolean asc) {
        this.sortOrder = asc ? SortOrder.ASCENDING : SortOrder.DESCENDING;
    }
}