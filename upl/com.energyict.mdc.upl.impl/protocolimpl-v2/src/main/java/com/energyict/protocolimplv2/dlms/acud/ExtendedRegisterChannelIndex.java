package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 * Helper class to store on which channel an extended register is stored on billing profile
 *
 * Eg. for 1.0.1.6.0.255 (max demand), there will be 2 channels captured, one for value and one for event time:
 *     ch 3:  4,1.0.1.6.0.255,2  -- value
 *     ch 4:  4,1.0.1.6.0.255,5  -- captured time
 *
 */
public class ExtendedRegisterChannelIndex {

    // channel where the value is stored
    private int valueIndex = 0;

    // channel where the event time is stored
    private int eventTimeIndex = 0;

    // the base obis code - should be an extended register
    ObisCode obisCode;

    // list of captured objects
    List<CapturedObject> captureObjects;

    public ExtendedRegisterChannelIndex(ObisCode baseObisCode, List<CapturedObject> captureObjects) throws IOException {
        this.obisCode = baseObisCode;
        this.captureObjects = captureObjects;

        checkIfObisCodeIsCaptured();
    }

    /**
     * @return the profile channel index where the value is captured
     */
    public int getValueIndex() {
        return valueIndex;
    }

    /**
     * Store the profile channel where the value is captured
     * @param valueIndex
     */
    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    /**
     * @return the profile channel where the captured time (event time) is captured
     */
    public int getEventTimeIndex() {
        return eventTimeIndex;
    }

    /**
     * Store the profile channel where the captured time (event time) is captured
     * @param eventTimeIndex
     */
    public void setEventTimeIndex(int eventTimeIndex) {
        this.eventTimeIndex = eventTimeIndex;
    }

    public boolean isValid() {
        return valueIndex>0;
    }


    protected boolean checkIfObisCodeIsCaptured() throws IOException {
        int channelIndex = 0;
        for (CapturedObject capturedObject : captureObjects) {
            if (capturedObject.getLogicalName().getObisCode().equals(getObisCode())) {
                if (capturedObject.getAttributeIndex() == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                    this.setEventTimeIndex(channelIndex);
                } else {
                    this.setValueIndex(channelIndex);
                }
            }
            channelIndex++;
        }

        if (this.isValid()) {
            return true;
        }

        throw new NoSuchRegisterException("Obiscode " + obisCode.toString() + " is not stored in the billing profile. The captured objects are: " + captureObjects.toString());
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public List<CapturedObject> getCaptureObjects() {
        return captureObjects;
    }
}
