package com.energyict.mdc.engine.offline.gui.core;


import com.energyict.mdc.common.HexString;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexStringField extends JTextField {

    public HexStringField() {
        super(20);
    }

    public HexString getValue() {
        return new HexString(getText());
    }

    public void setValue(HexString value) {
        setText( value==null || value.isEmpty() ? "" : value.toString() );
    }

    @Override
    protected Document createDefaultModel() {
        return new HexStringDocument();
    }
}
