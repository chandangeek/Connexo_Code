/*
 * PropertyValueCellEditor.java
 *
 * Created on 17 februari 2003, 10:25
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.Association;
import com.energyict.mdc.engine.offline.core.PropertiesMetaData;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Karel
 */
public class PropertyValueCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private JComponent editor;
    private JTextField textField = new JTextField();
    private PropertiesMetaData metaData;

    private JTable table;
    private int row;
    private int column;

    /**
     * Creates a new instance of PropertyValueCellEditor
     */
    public PropertyValueCellEditor(PropertiesMetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    public Object getCellEditorValue() {
        if (editor == null) {
            return null;
        }
        if (editor instanceof JTextField) {
            return ((JTextField) editor).getText();
        }
        if (editor instanceof JComboBox) {
            return ((JComboBox) editor).getSelectedItem();
        }
        return null;
    }


    /**
     * Sets an initial <code>value</code> for the editor.  This will cause
     * the editor to <code>stopEditing</code> and lose any partially
     * edited value if the editor is editing when this method is called. <p>
     * <p/>
     * Returns the component that should be added to the client's
     * <code>Component</code> hierarchy.  Once installed in the client's
     * hierarchy this component will then be able to draw and receive
     * user input.
     *
     * @param table      the <code>JTable</code> that is asking the
     *                   editor to edit; can be <code>null</code>
     * @param value      the value of the cell to be edited; it is
     *                   up to the specific editor to interpret
     *                   and draw the value.  For example, if value is
     *                   the string "true", it could be rendered as a
     *                   string or it could be rendered as a check
     *                   box that is checked.  <code>null</code>
     *                   is a valid value
     * @param isSelected true if the cell is to be rendered with
     *                   highlighting
     * @param row        the row of the cell being edited
     * @param column     the column of the cell being edited
     * @return the component for editing
     */
    public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //save info
        this.table = table;
        this.row = row;
        this.column = column;

        TableModel model = table.getModel();
        String key = (String) model.getValueAt(row, 0);
        List possibleValues = metaData.getPossibleValues(key);
        if (possibleValues == null || possibleValues.isEmpty()) {
            textField.setText(value == null ? "" : value.toString());
            editor = textField;
            editor.setFont(table.getFont());
            editor.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
            return editor;
        }
        Collections.sort(possibleValues, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        Iterator it = possibleValues.iterator();
        JComboBox combo = new JComboBox();
        while (it.hasNext()) {
            Association association = (Association) it.next();
            combo.addItem(association.getKey());
        }
        combo.setSelectedItem(value);
        combo.addActionListener(this);
        editor = combo;
        editor.setFont(table.getFont());
        editor.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
        return editor;
    }

    /**
     * Invoked when an action occurs.
     */

    public void actionPerformed(ActionEvent e) {
        if (table == null) {
            return;
        }
        table.getModel().setValueAt(
                getCellEditorValue(),
                row,
                column);
    }

}
