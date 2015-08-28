package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedData is just a ValueObject that holds the {@link DLMSAttribute} from a DisconnectControl object
 */
public class ComposedDisconnectControl implements ComposedObject {

    private final DLMSAttribute outputStateAttribute;
    private final DLMSAttribute controlStateAttribute;
    private final DLMSAttribute controlModeAttribute;


    public ComposedDisconnectControl(DLMSAttribute outputStateAttribute, DLMSAttribute controlStateAttribute, DLMSAttribute controlModeAttribute) {
        this.outputStateAttribute = outputStateAttribute;
        this.controlStateAttribute = controlStateAttribute;
        this.controlModeAttribute = controlModeAttribute;
    }

    public DLMSAttribute getOutputStateAttribute() {
        return outputStateAttribute;
    }

    public DLMSAttribute getControlStateAttribute() {
        return controlStateAttribute;
    }

    public DLMSAttribute getControlModeAttribute() {
        return controlModeAttribute;
    }
}