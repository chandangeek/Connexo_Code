/*
 * IntegerEditor.java
 *
 * Created on 18 juni 2003, 10:14
 */

package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * @author GDE
 */
public class IntegerBasicComboBoxEditor extends BasicComboBoxEditor {

    public IntegerBasicComboBoxEditor() {
        editor.setDocument(new IntegerDocument());
    }

    public Object getItem() {
        try {
            return new Integer(super.getItem().toString().trim());
        } catch (NumberFormatException e) {
            return new Integer(0);
        }
    }
}