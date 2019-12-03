package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.model.MeterReadingType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MeterReadingTypeTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null && value instanceof MeterReadingType) {
            MeterReadingType type = (MeterReadingType)value;
            setIcon(type.getIcon());
            setText(null);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        return this;
    }

}