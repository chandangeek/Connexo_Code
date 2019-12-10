/*
 * JTextAreaCellEditor.java
 *
 * Created on 27 juli 2004, 15:43
 */

package com.energyict.mdc.engine.offline.gui.table.editor;

import javax.swing.*;

/**
 * @author Koen
 */
public class JTextAreaCellEditor extends DefaultCellEditor {

    JTextArea jTextArea;

    /**
     * Creates a new instance of jTextAreaCellEditor
     */
    public JTextAreaCellEditor(JTextArea jTextArea) {
        super(new JTextField("test"));
    }


    public Object getCellEditorValue() {
        return jTextArea;
    }

    public java.awt.Component getTableCellEditorComponent(JTable jTable, Object value, boolean isSelected, int row, int column) {
        jTextArea = (JTextArea) value;
        // Edit the object!
        return jTextArea;
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    public void fireEditingStopped() {
        super.fireEditingStopped();
    }

}
