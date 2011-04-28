package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Profile for all Generic NTA protocols
 */
public abstract class AbstractNTAProfile {

    /**
     * @return the used {@link java.util.logging.Logger}
     */
    protected abstract Logger getLogger();

    /**
     * @return the used {@link com.energyict.dlms.cosem.CosemObjectFactory}
     */
    protected abstract CosemObjectFactory getCosemObjectFactory();

    /**
     * Read the given object and return the scalerUnit.
     * If the unit is 0(not a valid value) then return an exception
     * If you can not read the scalerUnit, then return an error.
     *
     * @param oc the {@link ObisCode} from which we want the ScalerUnit
     * @return the ScalerUnit
     * @throws java.io.IOException if something happened during the read or when the ScalerUnit is not correct
     */
    protected ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc) throws IOException {
        return getCosemObjectScalerUnit(getCosemObjectFactory().getCosemObject(oc));
    }

    /**
     * Read the scalerUnit from the given object.
     *
     * @param oc      the {@link ObisCode} from which we want the ScalerUnit
     * @param classId the classId of the object
     * @return the ScalerUnit
     * @throws IOException if something happened during the read or when the ScalerUnit is not correct
     */
    protected ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc, final int classId) throws IOException {
        return getCosemObjectScalerUnit(getCosemObjectFactory().getCosemObjectFromObisAndClassId(oc, classId));
    }

    /**
     * Collect the SclaerUnit from the given CosemObject
     *
     * @param cosemObject the given Object
     * @return the ScalerUnit from the Object
     * @throws IOException if something happened during the read or when the ScalerUnit is not correct
     */
    private ScalerUnit getCosemObjectScalerUnit(CosemObject cosemObject) throws IOException {
        try {
            ScalerUnit su = cosemObject.getScalerUnit();
            if (su != null) {
                if (su.getUnitCode() == 0) {
                    su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
                }
            } else {
                throw new ProtocolException("Meter does not report a proper scalerUnit, data can not be interpreted correctly.");
            }
            return su;
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Could not collect the scalerUnit.");
            throw e;
        }
    }
}
