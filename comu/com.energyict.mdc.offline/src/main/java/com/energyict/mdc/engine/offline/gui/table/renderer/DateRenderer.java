package com.energyict.mdc.engine.offline.gui.table.renderer;


import com.energyict.mdc.engine.offline.core.FormatProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRenderer extends DefaultTableCellRenderer implements ColumnWidthCalculator {

    DateFormat format;
    boolean showMilliseconds = false;
    int preferredWidth;

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public DateRenderer() {
        this(FormatProvider.instance.get().getFormatPreferences().getDateTimeFormat(true));
    }

    public DateRenderer(DateFormat format) {
        this.format = format;
        // Calculating the preferred width. The label's preferred width is used by
        // the Tablebuilder to determine's the columns default width
        preferredWidth = calcPreferredWidth();
    }

    public void setShowMilliseconds(boolean showMilliseconds){
        this.showMilliseconds = showMilliseconds;
    }

    /* ColumnWidthCalculator interface */
    // Calculates the preferred width using a default date
    public int calcPreferredWidth() {
        this.setText(getDisplayString(new Date()));
        return this.getPreferredSize().width + 5;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String displayString = null;
        if (value != null && value instanceof Date) {
            displayString = getDisplayString((Date) value);
        } else if (value != null && value instanceof String) {
            displayString = value.toString();
        }
        this.setPreferredSize(new Dimension(preferredWidth, table.getRowHeight(row)));
        return super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
    }

    private String getDisplayString(Date date) {
        if (showMilliseconds){
            return formatWithMilliseconds().format(date);
        }
        return format.format(date);
    }

    private DateFormat formatWithMilliseconds(){
        return new SimpleDateFormat(((SimpleDateFormat) format).toPattern()+".SSS");
    }

}
