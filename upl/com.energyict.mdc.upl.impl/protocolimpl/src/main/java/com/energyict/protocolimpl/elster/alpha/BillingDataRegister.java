package com.energyict.protocolimpl.elster.alpha;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-08 (15:16)
 */
public interface BillingDataRegister {

    String getDescription();

    ObisCode getObisCode();

    RegisterValue getRegisterValue();

}