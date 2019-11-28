package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TimeDurationTableCellRenderer extends DefaultTableCellRenderer {

    public TimeDurationTableCellRenderer() {
        setHorizontalAlignment(LEFT);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TimeDuration td = (TimeDuration) value;
        if (td != null) {
            String key = TimeDuration.getTimeUnitDescription(td.getTimeUnitCode());
            if (key.length() <= 0) { // for unknown/undefined time unit codes, show 0 seconds
                td = new TimeDuration(0,0);
                key = TimeDuration.getTimeUnitDescription(td.getTimeUnitCode());
            }
            setText(td.getCount() + " " + TranslatorProvider.instance.get().getTranslator().getTranslation(key));
        } else {
            setText("");
        }
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return this;
    }
}
