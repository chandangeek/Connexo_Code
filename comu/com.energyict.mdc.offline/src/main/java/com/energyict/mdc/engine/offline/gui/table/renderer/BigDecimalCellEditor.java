/*
 * IntervalStateTableCellRenderer.java
 *
 * Created on 4 juli 2003, 20:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.gui.core.JBigDecimalField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * @author Karel
 */
public class BigDecimalCellEditor extends DefaultCellEditor {

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public BigDecimalCellEditor() {
        super(BigDecimalCellEditor.createEditor());
        setClickCountToStart(1);
        JBigDecimalField field = getBigDecimalField();
        field.setBorder(new javax.swing.border.EmptyBorder(0, 1, 0, 0));
        field.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public BigDecimalCellEditor(JTable table) {
        this();
        getBigDecimalField().setFont(table.getFont());
    }

    public JBigDecimalField getBigDecimalField() {
        return (JBigDecimalField) getComponent();
    }

    public Object getCellEditorValue() {
        return getBigDecimalField().getValue();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        Component result = super.getTableCellEditorComponent(
                table,
                value,
                isSelected,
                row,
                column);
        ((JBigDecimalField) result).setValue((BigDecimal) value);
        return result;
    }

    static private JBigDecimalField createEditor() {
        JBigDecimalField editor = new JBigDecimalField();
        // (2004-jan-27) Geert 
        // For this component: remove the normal paste interception
        // Let the JTable this component is in decide what to do on Ctrl+V
        KeyStroke paste =
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        Action noAction = new AbstractAction() {
            public boolean isEnabled() {
                return false;
            }

            public void actionPerformed(ActionEvent e) {
            }
        };
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(paste, "none");
        editor.getActionMap().put("none", noAction);
        return editor;
    }

}
