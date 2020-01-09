/*
 * JComboBoxCellEditor.java
 *
 * Created on 12 januari 2004, 9:27
 */

package com.energyict.mdc.engine.offline.gui.table.editor;

import javax.swing.*;

/**
 * @author Koen
 */
public class JComboBoxCellEditor extends DefaultCellEditor {

    JComboBox jComboBox;

    /**
     * Creates a new instance of JButtonCellEditor
     */
    public JComboBoxCellEditor(JComboBox jComboBox) {
        super(jComboBox);
    }

    public Object getCellEditorValue() {
        return jComboBox;
    }

    public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Can we return the real ComboBox object here?
        jComboBox = (JComboBox) value;
        // Edit the object!
        return jComboBox;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    public void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
