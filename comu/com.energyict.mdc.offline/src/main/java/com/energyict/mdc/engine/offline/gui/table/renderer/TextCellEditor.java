/*
 * TextCellEditor.java
 *
 * Created on 14 oktober 2003, 9:43
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author pasquien, Geert
 */
public class TextCellEditor extends DefaultCellEditor {

    /**
     * Creates a new instance of TextCellEditor
     */
    public TextCellEditor(JTextField textField) {
        super(textField);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));

        // (2004-jan-28) Geert
        // For this component: remove the normal paste interception
        // Let the JTable (this component is in) decide what to do on 
        // Ctrl+V/Ctrl+Insert
        Action noAction = new AbstractAction() {
            public boolean isEnabled() {
                return false;
            }

            public void actionPerformed(ActionEvent e) {
            }
        };
        KeyStroke paste =
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke paste2 =
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.CTRL_MASK, false);
        textField.getInputMap(JComponent.WHEN_FOCUSED).put(paste, "none");
        textField.getInputMap(JComponent.WHEN_FOCUSED).put(paste2, "none");
        textField.getActionMap().put("none", noAction);

        this.setClickCountToStart(1);
    }
}