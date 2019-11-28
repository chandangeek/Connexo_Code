package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * User: gde
 * Date: 16/05/12
 */
public class EndDeviceEventTypeDocument extends PlainDocument {

    public EndDeviceEventTypeDocument() {
        super();
    }

    @Override
    public void insertString(int index, String s, AttributeSet a) throws BadLocationException {
        if (s == null || s.length() == 0) {
            return;
        }
        StringBuffer t = new StringBuffer(getLength() + s.length());
        t.append(getText(0, index));
        t.append(s);
        t.append(getText(index, getLength() - index));
        String validChars = "0123456789.";
        for (int i = 0; i < t.length(); i++) {
            if (validChars.indexOf(t.charAt(i)) == -1) {
                return; // don't allow other characters
            }
        }
        super.insertString(index, s, a);
    }

}
