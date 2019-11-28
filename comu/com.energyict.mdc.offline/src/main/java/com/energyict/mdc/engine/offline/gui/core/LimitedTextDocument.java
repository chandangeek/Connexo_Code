package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Created by IntelliJ IDEA.
 * User: bbl
 * Date: 21-jan-2010
 * Time: 10:44:51
 */
public class LimitedTextDocument extends PlainDocument {

    private int limit;
    // optional uppercase conversion
    private boolean toUppercase = false;

    public LimitedTextDocument(int limit) {
        super();
        this.limit = limit;
    }

    public LimitedTextDocument(int limit, boolean upper) {
        super();
        this.limit = limit;
        this.toUppercase = upper;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        // All input accepted until the maximum allowed length is reached
        if (str.length() > limit - getLength()){
            str = str.substring(0, limit - getLength());
        }
        if (toUppercase) {
            str = str.toUpperCase();
        }
        super.insertString(offset, str, attr);
    }
}
