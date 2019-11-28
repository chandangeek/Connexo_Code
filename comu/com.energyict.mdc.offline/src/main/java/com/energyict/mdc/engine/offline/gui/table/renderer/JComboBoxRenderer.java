/*
 * JComboBoxCellRenderer.java
 *
 * Created on 12 januari 2004, 9:24
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Koen
 */
public class JComboBoxRenderer extends JComboBox implements TableCellRenderer {

    /**
     * Creates a new instance of JButtonRenderer
     */
    public JComboBoxRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("ComboBox.background"));
        }
        return (Component) value;
    }

}

