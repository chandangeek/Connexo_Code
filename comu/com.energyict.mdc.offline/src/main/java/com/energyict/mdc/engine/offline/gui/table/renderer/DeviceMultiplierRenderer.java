/*
 * IntervalStateTableCellRenderer.java
 *
 * Created on 4 juli 2003, 20:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.model.DeviceMultiplier;

import javax.swing.*;
import java.awt.*;

/**
 * @author Karel
 */
public class DeviceMultiplierRenderer extends BigDecimalRenderer {

    /**
     * Creates a new instance of DeviceMultiplierRenderer
     */
    public DeviceMultiplierRenderer() {
        super();
    }

    public DeviceMultiplierRenderer(int scale) {
        super(scale);
    }


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof DeviceMultiplier) {
            return super.getTableCellRendererComponent(table, ((DeviceMultiplier)value).getMultiplier(), isSelected, hasFocus, row, column);
        } else {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,column);
        }

    }

}
