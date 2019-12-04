package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AnyIntegerBasicComboBoxEditor extends BasicComboBoxEditor {

    private AnyIntegerDocument document;

    public AnyIntegerBasicComboBoxEditor() {
        document = new AnyIntegerDocument();
        editor.setDocument(document);
    }

    public void setItem(Object o) {
        if (o == null) {
            return;
        }
        if (((Integer) o).intValue() == -1) {
            document.setAny();
        } else {
            super.setItem(o);
        }
    }

    public Object getItem() {
        try {
            if (document.isAny()) {
                return new Integer(-1);
            } else {
                return new Integer(super.getItem().toString().trim());
            }
        } catch (NumberFormatException e) {
            return new Integer(0);
        }
    }
}