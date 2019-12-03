/*
 * IntervalStateTableCellRenderer.java
 *
 * Created on 4 juli 2003, 20:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.FormatPreferences;
import com.energyict.mdc.engine.offline.core.FormatProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Karel
 */
public class BigDecimalRenderer extends DefaultTableCellRenderer {

    protected int scale = -1;
    protected char decimalSeparator = FormatProvider.instance.get().getDecimalSeparator();
    protected DecimalFormat format;

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public BigDecimalRenderer() {
        setHorizontalAlignment(RIGHT);
        format = getFormatPreferences().getNumberFormat();
    }

    public BigDecimalRenderer(int scale) {
        setHorizontalAlignment(RIGHT);
        format = getFormatPreferences().getNumberFormat(scale, true);
        this.scale = scale;
    }

    private FormatPreferences getFormatPreferences() {
        return FormatProvider.instance.get().getFormatPreferences();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        String displayString = null;
        try {
            displayString = format.format(value);
        } catch (IllegalArgumentException x) {
            displayString = value.toString();
        }
        return super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
    }

    public NumberFormat getFormat() {
        return format;
    }

    public String format(Object value) {
        return format.format(value);
    }
}
