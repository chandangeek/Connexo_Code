package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 4-nov-2010
 * Time: 11:28:30
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
     * @return the used {@link com.energyict.mdw.core.Rtu}
     */
    protected abstract Rtu getMeter();

    /**
     * Read the given object and return the scalerUnit.
     * If the unit is 0(not a valid value) then return a unitLess scalerUnit.
     * If you can not read the scalerUnit, then return an error.
     *
     * @param oc the {@link ObisCode} from which we want the ScalerUnit
     * @return the ScalerUnit
     * @throws java.io.IOException if something happened during the read or when the ScalerUnit is not correct
     */
    protected ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc) throws IOException {
        try {
            ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
            if (su != null) {
                if (su.getUnitCode() == 0) {
                    su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
                }
            } else {
                throw new ProtocolException("Meter does not report a proper scalerUnit for obiscode " + oc + " , data can not be interpreted correctly.");
            }
            return su;
        } catch (final IOException e) {
            getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
            throw e;
        }
    }
}
