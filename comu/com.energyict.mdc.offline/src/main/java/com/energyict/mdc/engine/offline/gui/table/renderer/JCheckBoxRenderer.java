/*
 * JCheckBoxRenderer.java
 *
 * Created on 26 september 2003, 9:16
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Koen
 */
public class JCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

    /**
     * Creates a new instance of JButtonRenderer
     */
    public JCheckBoxRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }
        return (Component) value;
    }

}
