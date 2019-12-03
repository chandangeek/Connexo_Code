package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.cim.EndDeviceEventType;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * User: gde
 * Date: 16/05/12
 */
public class EndDeviceEventTypeField extends JTextField {

    public EndDeviceEventTypeField() {
        super();
    }

    public EndDeviceEventTypeField(int width) {
        super(width);
    }

    public void setValue(EndDeviceEventType eventType) {
        setText(eventType == null ? "" : eventType.toString());
    }

    public EndDeviceEventType getValue() {
        try {
            return EndDeviceEventType.fromString(getText());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean hasValue() {
        return (getValue() != null);
    }

    protected Document createDefaultModel() {
        return new EndDeviceEventTypeDocument();
    }

}
