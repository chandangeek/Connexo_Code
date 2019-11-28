package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class DoubleDocument extends PlainDocument {

    public void insertString(int index, String s, AttributeSet a)
            throws BadLocationException {

        if (s == null || s.length() == 0) {
            return;
        }
        StringBuffer t = new StringBuffer(getLength() + s.length());
        t.append(getText(0, index));
        t.append(s);
        t.append(getText(index, getLength() - index));

        String strValue = t.toString().trim();
        strValue = strValue.replace(',', '.');
        if (strValue.equals("-")) {
            strValue += "0";
        }
        try {
            Double.parseDouble(strValue);
        } catch (NumberFormatException ex) {
            return;
        }
        super.insertString(index, s, a);
    }
}