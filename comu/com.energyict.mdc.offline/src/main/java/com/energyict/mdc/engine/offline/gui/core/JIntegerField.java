/*
 * JIntegerField.java
 *
 * Created on 18 juni 2003, 13:01
 */

package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.*;
import javax.swing.text.Document;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @author GDE
 */
public class JIntegerField extends JTextField {

    private NumberFormat integerFormatter;

    public JIntegerField() {
        integerFormatter = NumberFormat.getIntegerInstance();
        integerFormatter.setGroupingUsed(false);
        integerFormatter.setParseIntegerOnly(true);
    }

    public JIntegerField(int value) {
        this();
        setValue(value);
    }

    public JIntegerField(int value, int columns) {
        this(value);
        setColumns(columns);
    }

    public int getValue() {
        int retVal = 0;
        try {
            retVal = integerFormatter.parse(getText()).intValue();
        } catch (ParseException e) {
            // This should never happen because IntegerDocument:insertString
            // allows only properly formatted data to get in the field.
        }
        return retVal;
    }

    public void setValue(int value) {
        setText(integerFormatter.format(value));
    }

    public void setAllowNegativeValues(boolean flag){
        ((IntegerDocument) getDocument()).setAllowNegativeValues(flag);
    }

    protected Document createDefaultModel() {
        return new IntegerDocument();
    }
}