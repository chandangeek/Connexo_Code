/*
 * ColumnChooserOwner.java
 *
 * Created on 17 mei 2004, 16:07
 */

package com.energyict.mdc.engine.offline.gui.util;

import java.util.List;

/**
 * @author Geert
 */
public interface ColumnChooserOwner {

    void refreshTableModel(List<String> colToShow, int sortingColumn);

}
