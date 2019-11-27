/*
 * AnyIntegerDocument.java
 *
 * An IntegerDocument where the word "Any" can be placed
 * Can be used to enter a year, month...(a positive number) or the choice "Any" -> returns -1
 *
 * Created on 16 oktober 2003, 14:46
 */

package com.energyict.mdc.engine.offline.gui.core;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * @author pasquien
 */
public class AnyIntegerDocument extends IntegerDocument {

    private static String any = TranslatorProvider.instance.get().getTranslator().getTranslation("any");
    private boolean anySet = false;

    public void insertString(int index, String s, AttributeSet a)
            throws BadLocationException {
        anySet = false;
        super.insertString(index, s, a);
    }

    public void setAny() {
        try {
            remove(0, getLength());
            insertString(0, any, null);
            anySet = true;
        } catch (BadLocationException e) {
        }
    }

    public boolean isAny() {
        return anySet;
    }
}
