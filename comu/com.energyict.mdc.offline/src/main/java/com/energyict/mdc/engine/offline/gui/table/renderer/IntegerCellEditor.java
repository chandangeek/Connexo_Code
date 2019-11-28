/*
 * IntervalStateTableCellRenderer.java
 *
 * Created on 4 juli 2003, 20:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.gui.core.JIntegerField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Karel
 */
public class IntegerCellEditor extends DefaultCellEditor {

    /**
     * Creates a new instance of IntervalStateTableCellRenderer
     */
    public IntegerCellEditor() {
        super(IntegerCellEditor.createEditor());
        setClickCountToStart(1);
        JIntegerField field = getIntegerField();
        field.setBorder(new javax.swing.border.EmptyBorder(0, 1, 0, 0));
        field.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public IntegerCellEditor(JTable table) {
        this();
        getIntegerField().setFont(table.getFont());
    }

    public JIntegerField getIntegerField() {
        return (JIntegerField) getComponent();
    }

    public Object getCellEditorValue() {
        return new Integer(getIntegerField().getValue());
    }

    static private JIntegerField createEditor() {
        JIntegerField editor = new JIntegerField();
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