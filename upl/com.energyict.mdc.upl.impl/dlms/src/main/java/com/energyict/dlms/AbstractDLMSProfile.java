package com.energyict.dlms;

import com.energyict.cbo.ApplicationException;
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
            unit = scalerUnit != null ? scalerUnit.getUnit() : null;
        } catch (IOException e) {
            throw new IOException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. " + e.getMessage());
        } catch (ApplicationException e) {
            throw new IOException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. " + e.getMessage());
        }
        if (unit == null) {
            throw new IOException("Unable to read the scaler and/or unit for channel with obiscode [" + oc + "]. Unit was 'null'.");
        }
        return unit;
    }


}
