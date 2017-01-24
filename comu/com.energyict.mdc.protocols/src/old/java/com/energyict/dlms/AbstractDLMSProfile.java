package com.energyict.dlms;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/04/11
 * Time: 11:53
 */
public abstract class AbstractDLMSProfile {

    protected abstract CosemObjectFactory getCosemObjectFactory();
    protected abstract ObisCode getCorrectedObisCode(ObisCode baseObisCode);

    /**
     * Read the given object and return the Unit.
     *
     * @param oc
     * @return
     * @throws java.io.IOException
     */
    protected Unit getUnit(ObisCode oc) throws IOException {
        Unit unit;
        try {
            CosemObject capturedObject = getCosemObjectFactory().getCosemObject(oc);
            ScalerUnit scalerUnit = capturedObject != null ? capturedObject.getScalerUnit() : null;
            unit = scalerUnit != null ? scalerUnit.getEisUnit() : null;
        } catch (IOException e) {
            throw new ProtocolException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. " + e.getMessage());
        } catch (ApplicationException e) {
            throw new ProtocolException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. " + e.getMessage());
        }
        if (unit == null) {
            throw new ProtocolException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. Unit was 'null'.");
        }
        return unit;
    }


    /**
     * Check if it is a valid channel Obiscode
     *
     * @param obisCode - the {@link ObisCode} to check
     * @return true if you know it is a valid channelData oc, false otherwise
     */
    protected boolean isValidChannelObisCode(final ObisCode obisCode) {
        ObisCode oc = getCorrectedObisCode(obisCode);
        if ((oc.getA() == 1) && (((oc.getB() >= 0) && (oc.getB() <= 64)) || (oc.getB() == 128))) {    // Energy channels - Pulse channels (C == 82)
            return true;
        } else if (oc.getC() == 96) {    // Temperature and Humidity
            if ((oc.getA() == 0) && ((oc.getB() == 0) || (oc.getB() == 1)) && (oc.getD() == 9) && ((oc.getE() == 0) || (oc.getE() == 2))) {
                return true;
            } else {
                return false;
            }
        } else if ((oc.getA() == 4) || (oc.getA() == 5) || (oc.getA() == 6) || (oc.getA() == 7) || (oc.getA() == 8) || (oc.getA() == 9)) {    // Allow heat, gas, water, cooling, ...
            return true;
        } else {
            return false;
        }
    }


}
