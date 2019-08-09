package com.energyict.mdc.engine.offline.gui.table.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.TimeZone;

/**
 * @author Karel
 */
public class TimeZoneRenderer extends DefaultTableCellRenderer {

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public TimeZoneRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        }
        String displayString = ((TimeZone) value).getID();
        return super.getTableCellRendererComponent(
                table, displayString, isSelected, hasFocus, row, column);
    }
}
