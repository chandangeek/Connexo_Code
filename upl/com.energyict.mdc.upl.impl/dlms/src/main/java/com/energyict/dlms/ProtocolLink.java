/*
 * ProtocolLink.java
 * Created on 18 augustus 2004, 12:01
 */

package com.energyict.dlms;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdc.upl.MeterProtocol;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * DLMS protocols should implement this interface.
 * It is used to communicate with DLMS devices.
 *
 * @author Koen
 */
public interface ProtocolLink {

    /**
     * Used by com.energyict.dlms.ProtocolLink.getReference() for short name
     */
    int SN_REFERENCE = 1;

    /**
     * Used by com.energyict.dlms.ProtocolLink.getReference() for long name
     */
    int LN_REFERENCE = 0;

    /**
     * Getter for property {@link DLMSConnection}.
     *
     * @return the {@link DLMSConnection}.
     */
    DLMSConnection getDLMSConnection();

    /**
     * Getter for property meterConfig.
     *
     * @return Value of property meterConfig.
     */
    DLMSMeterConfig getMeterConfig();

    /**
     * @return the {@link TimeZone}
     */
    TimeZone getTimeZone();

    /**
     * Check if the {@link TimeZone} is read from the DLMS device, or if the
     * {@link TimeZone} from the {@link MeterProtocol} should be used.
     *
     * @return true is the {@link TimeZone} is read from the device
     */
    boolean isRequestTimeZone();

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    int getRoundTripCorrection();

    /**
     * Getter for the {@link Logger}
     *
     * @return the current {@link Logger}
     */
    Logger getLogger();

    /**
     * Getter for the type of reference used in the DLMS protocol. This can be
     * {@link ProtocolLink}.SN_REFERENCE or {@link ProtocolLink}.LN_REFERENCE
     *
     * @return {@link ProtocolLink}.SN_REFERENCE for short name or
     * {@link ProtocolLink}.LN_REFERENCE for long name
     */
    int getReference();

    /**
     * Getter for the {@link StoredValues} object
     *
     * @return the {@link StoredValues} object
     */
    StoredValues getStoredValues();

    /**
     * The ApplicationServiceObject. It is used by most protocols to setup, maintain and release the application association to the device.
     * Some old protocols don't have this (they manually build and send the AA requests), they return null.
     */
    ApplicationServiceObject getAso();

}