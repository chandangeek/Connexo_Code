package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.logging.Level;


public class LevelRenderer extends DefaultTableCellRenderer
        implements ColumnWidthCalculator {

    int preferredWidth;

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public LevelRenderer() {
        // The preferred size of the renderer's component is used by the TableBuilder
        // to set the column's width
        preferredWidth = calcPreferredWidth();
    }

    /* ColumnWidthCalculator interface */
    // Calculates the preferred width

    public int calcPreferredWidth() {
        int maxwidth = -1;
        Level[] levels = {Level.ALL, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.INFO, Level.OFF, Level.SEVERE, Level.WARNING};
        for (int i = 0; i < levels.length; i++) {
            this.setText(getDisplayString(levels[i]));
            maxwidth = Math.max(maxwidth, this.getPreferredSize().width);
        }
        return maxwidth + 1;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String displayString = null;
        if (value != null) {
            displayString = getDisplayString((Level) value);
        }

        this.setPreferredSize(new Dimension(preferredWidth, table.getRowHeight(row)));
        return super.getTableCellRendererComponent(
                table, displayString, isSelected, hasFocus, row, column);
    }

    private String getDisplayString(Level level) {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(level.toString());
    }

}
