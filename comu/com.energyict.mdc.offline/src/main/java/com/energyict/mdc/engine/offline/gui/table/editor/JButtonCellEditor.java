/*
 * JButtonCellEditor.java
 *
 * Created on 26 september 2003, 11:24
 */

package com.energyict.mdc.engine.offline.gui.table.editor;

import javax.swing.*;

/**
 * @author Koen
 */
public class JButtonCellEditor extends DefaultCellEditor {

    JButton jButton;

    /**
     * Creates a new instance of JButtonCellEditor
     */
    public JButtonCellEditor(JCheckBox jCheckBox) {
        super(jCheckBox);
    }

    public Object getCellEditorValue() {
        return jButton;
    }

    public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Can we return the real button object here?
        jButton = (JButton) value;
        // Edit the object!
        return jButton;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    public void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
