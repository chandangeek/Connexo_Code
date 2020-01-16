package com.energyict.mdc.engine.offline.gui.core;

import com.energyict.mdc.engine.offline.core.FormatProvider;

import javax.swing.*;
import javax.swing.text.Document;

public class JDoubleField extends JTextField {

    private char decimalSeparator = FormatProvider.instance.get().getDecimalSeparator();

    public JDoubleField() {
    }

    public JDoubleField(double value) {
        this();
        setValue(value);
    }

    public JDoubleField(double value, int columns) {
        this(value);
        setColumns(columns);
    }

    public double getValue() {
        double retVal = 0;
        String strValue = getText().trim();
        strValue = strValue.replace(',', '.');
        try {
            retVal = Double.parseDouble(strValue);
        } catch (NumberFormatException ex) {
        }
        return retVal;
    }

    public void setValue(double value) {
        String strValue = "" + value;
        if (decimalSeparator != '.') {
            strValue = strValue.replace('.', decimalSeparator);
        }
        setText(strValue);
    }

    protected Document createDefaultModel() {
        return new DoubleDocument();
    }
}
