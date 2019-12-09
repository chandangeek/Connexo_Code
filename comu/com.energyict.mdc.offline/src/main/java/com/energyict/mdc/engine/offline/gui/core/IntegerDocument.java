package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntegerDocument extends PlainDocument {

    private boolean allowNegativeValues = true;

    public void insertString(int index, String s, AttributeSet a)
            throws BadLocationException {

        if (s == null || s.length() == 0) {
            return;
        }
        StringBuilder t = new StringBuilder(getLength() + s.length());
        t.append(getText(0, index));
        t.append(s);
        t.append(getText(index, getLength() - index));
        try {
            // gde (2003-nov-14) Fix
            // Although "-" doesn't lead to a successful (integer)parsing, it must
            // be accepted since otherwise the user can't enter negative values
            String tmp = t.toString().trim();
            if (!tmp.equals("-")) {
                int value = Integer.parseInt(tmp);
                if (!allowNegativeValues && value < 0) {
                    return; // reject negative value
                }
            } else if (!allowNegativeValues) {
                return; // reject "-" since no negative values allowed
            }
        } catch (NumberFormatException e) {
            return;
        }
        super.insertString(index, s, a);
    }

    public void setAllowNegativeValues(boolean allowNegativeValues) {
        this.allowNegativeValues = allowNegativeValues;
    }
}