/*
 * QuantityRenderer.java
 *
 * Created on 12 mei 2005, 16:04
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.engine.offline.core.FormatPreferences;
import com.energyict.mdc.engine.offline.core.FormatProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author fbo
 */
public class QuantityRenderer extends DefaultTableCellRenderer {

    protected int scale = -1;
    protected char decimalSeparator = FormatProvider.instance.get().getDecimalSeparator();
    protected DecimalFormat format;

    /**
     * Creates a new instance of QuantityRenderer
     */
    public QuantityRenderer() {
        setHorizontalAlignment(RIGHT);
        format = getFormatPreferences().getNumberFormat();
    }

    public QuantityRenderer(int scale) {
        setHorizontalAlignment(RIGHT);
        format = getFormatPreferences().getNumberFormat(scale, true);
        this.scale = scale;
    }

    private FormatPreferences getFormatPreferences() {
        return FormatProvider.instance.get().getFormatPreferences();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        }
        Quantity q = (Quantity) value;
        String unit = (q.getUnit() != null) ? " " + q.getUnit() : "";
        String displayString = format.format(value) + unit;
        return super.getTableCellRendererComponent(
                table, displayString, isSelected, hasFocus, row, column);
    }

    public String format(Object value) {
        return format.format(value) + " " + ((Quantity) value).getUnit();
    }

}
