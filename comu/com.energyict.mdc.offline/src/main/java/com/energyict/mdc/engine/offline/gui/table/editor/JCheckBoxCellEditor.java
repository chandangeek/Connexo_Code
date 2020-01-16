/*
 * JCheckBoxCellEditor.java
 *
 * Created on 26 september 2003, 11:01
 */

package com.energyict.mdc.engine.offline.gui.table.editor;

import javax.swing.*;

/**
 * @author Koen
 */
public class JCheckBoxCellEditor extends DefaultCellEditor {

    JCheckBox jCheckBox;

    /**
     * Creates a new instance of JCheckBoxCellEditor
     */
    public JCheckBoxCellEditor(JCheckBox jCheckBox) {
        super(jCheckBox);
    }

    public Object getCellEditorValue() {
        return jCheckBox;
    }

    public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Edit the object!
        boolean state = ((JCheckBox) value).isSelected();
        ((JCheckBox) value).setSelected(state);
        jCheckBox = (JCheckBox) value;
        return jCheckBox;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    public void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
