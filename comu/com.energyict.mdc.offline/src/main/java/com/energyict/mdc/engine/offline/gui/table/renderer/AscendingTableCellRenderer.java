/*
 * AscendingTableCellRenderer.java
 *
 * Created on 16 december 2004, 13:35
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Geert
 */
public class AscendingTableCellRenderer extends DefaultTableCellRenderer {

    private static Icon ascendingIcon;
    private static Icon descendingIcon;
    private static String YES;
    private static String NO;

    static {
        ascendingIcon = new ImageIcon(AscendingTableCellRenderer.class.getResource("/images/up.gif"));
        descendingIcon = new ImageIcon(AscendingTableCellRenderer.class.getResource("/images/down.gif"));
        YES = TranslatorProvider.instance.get().getTranslator().getTranslation("yes");
        NO = TranslatorProvider.instance.get().getTranslator().getTranslation("no");
    }

    /**
     * Creates a new instance of AscendingTableCellRenderer
     */
    public AscendingTableCellRenderer() {
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        if (value == null) {
            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        }
        try {
            java.awt.Color color = null;
            java.awt.Color background = null;
            if (isSelected) {
                color = table.getSelectionForeground();
                background = table.getSelectionBackground();
            } else {
                color = table.getForeground();
                background = table.getBackground();
            }
            if (color != null) {
                setForeground(color);
            }
            if (background != null) {
                setBackground(background);
            }

            if (((Boolean) value).booleanValue()) {
                setIcon(ascendingIcon);
                setText(YES);
            } else {
                setIcon(descendingIcon);
                setText(NO);
            }
        } catch (ClassCastException ex) {
            setIcon(null);
            setText(null);
        }
        return this;
    }
}
