/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.GatewaySetupAttributes;
import com.energyict.dlms.cosem.methods.GatewaySetupMethods;

import java.io.IOException;

public class GatewaySetup extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.8.255");

    private Array meterWhitelist;
    private OctetString operatingWindowStartTime;
    private OctetString operatingWindowEndTime;
    private BooleanObject whitelistEnabled;
    private BooleanObject operatingWindowEnabled;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public GatewaySetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.GATEWAY_SETUP.getClassId();
    }

    /**
     * Read the meter whitelist from the device
     * @return the whitelist containing all allowed meters
     * @throws java.io.IOException
     */
	public Array readMeterWhitelist() throws IOException {
        this.meterWhitelist = new Array(getResponseData(GatewaySetupAttributes.METER_WHITELIST), 0, 0);
        return this.meterWhitelist;
	}

    /**
     * Getter for the meter whitelist
     * @return the whitelist containing all allowed meters
     * @throws java.io.IOException
     */
    public Array getMeterWhitelist() throws IOException {
        if (this.meterWhitelist == null) {
            readMeterWhitelist();
        }
        return this.meterWhitelist;
    }

    /**
     * Read the operating window start time from the device
     *
     * @return the time at which the operating window gets active (i.e. when the PlC board is activated)
     * @throws java.io.IOException
     */
    public OctetString readOperatingWindowStartTime() throws IOException {
        this.operatingWindowStartTime = new OctetString(getResponseData(GatewaySetupAttributes.OPERATING_WINDOW_START_TIME), 0);
        return this.operatingWindowStartTime;
    }

    /**
     * Getter for the operating window start time
     *
     * @return the time at which the operating window gets active (i.e. when the PlC board is activated)
     * @throws java.io.IOException
     */
    public OctetString getOperatingWindowStartTime() throws IOException {
        if (this.operatingWindowStartTime == null) {
            readOperatingWindowStartTime();
        }
        return this.operatingWindowStartTime;
    }

    /**
     * Setter for the operating window start time
     *
     * @param operatingWindowStartTime the time at which the operating window gets active (i.e. when the PlC board is activated)
     * @throws java.io.IOException
     */
    public void writeOperatingWindowStartTime(OctetString operatingWindowStartTime) throws IOException {
        write(GatewaySetupAttributes.OPERATING_WINDOW_START_TIME, operatingWindowStartTime.getBEREncodedByteArray());
        this.operatingWindowStartTime = operatingWindowStartTime;
    }

    /**
     * Read the operating window end time from the device
     *
     * @return the time at which the operating window gets deactive (i.e. when the PlC board is deactivated)
     * @throws java.io.IOException
     */
    public OctetString readOperatingWindowEndTime() throws IOException {
        this.operatingWindowEndTime = new OctetString(getResponseData(GatewaySetupAttributes.OPERATING_WINDOW_END_TIME), 0);
        return this.operatingWindowEndTime;
    }

    /**
     * Getter for the operating window end time
     *
     * @return the time at which the operating window gets deactive (i.e. when the PlC board is deactivated)
     * @throws java.io.IOException
     */
    public OctetString getOperatingWindowEndTime() throws IOException {
        if (this.operatingWindowEndTime == null) {
            readOperatingWindowEndTime();
        }
        return this.operatingWindowEndTime;
    }

    /**
     * Setter for the operating window end time
     *
     * @param operatingWindowEndTime the time at which the operating window gets deactive (i.e. when the PlC board is deactivated)
     * @throws java.io.IOException
     */
    public void writeOperatingWindowEndTime(OctetString operatingWindowEndTime) throws IOException {
        write(GatewaySetupAttributes.OPERATING_WINDOW_END_TIME, operatingWindowEndTime.getBEREncodedByteArray());
        this.operatingWindowEndTime = operatingWindowEndTime;
    }

    /**
     * Read the whitelist status from the device
     *
     * @return the status of the whitelist (disabled / activated)
     * @throws java.io.IOException
     */
    public BooleanObject readWhiteListEnabled() throws IOException {
        this.whitelistEnabled = new BooleanObject(getResponseData(GatewaySetupAttributes.WHITELIST_ENABLED), 0);
        return this.whitelistEnabled;
    }

    /**
     * Getter for the whitelist status
     *
     * @return the status of the whitelist (disabled / activated)
     * @throws java.io.IOException
     */
    public BooleanObject getWhitelistEnabled() throws IOException {
        if (this.whitelistEnabled == null) {
            readWhiteListEnabled();
        }
        return this.whitelistEnabled;
    }

    /**
     * Read the operating window status from the device
     *
     * @return the status of the operating window (disabled / activated)
     * @throws java.io.IOException
     */
    public BooleanObject readOperatingWindowEnabled() throws IOException {
        this.operatingWindowEnabled = new BooleanObject(getResponseData(GatewaySetupAttributes.OPERATING_WINDOW_ENABLED), 0);
        return this.operatingWindowEnabled;
    }

    /**
     * Getter for the operating window status
     *
     * @return the status of the operating window (disabled / activated)
     * @throws java.io.IOException
     */
    public BooleanObject getOperatingWindowEnabled() throws IOException {
        if (this.operatingWindowEnabled == null) {
            readOperatingWindowEnabled();
        }
        return this.operatingWindowEnabled;
    }

    /**
     * Clear the whitelist
     *
     * @throws java.io.IOException
     */
    public final void clearWhitelist() throws IOException {
        methodInvoke(GatewaySetupMethods.CLEAR_WHITELIST);
    }

    /**
     * Activate the whitelist
     *
     * @throws java.io.IOException
     */
    public final void activateWhitelist() throws IOException {
        methodInvoke(GatewaySetupMethods.ACTIVATE_WHITELIST);
    }

    /**
     * Deactivate the whitelist
     *
     * @throws java.io.IOException
     */
    public final void deactivateWhitelist() throws IOException {
        methodInvoke(GatewaySetupMethods.DEACTIVATE_WHITELIST);
    }

    /**
     * Activate the operating window
     *
     * @throws java.io.IOException
     */
    public final void activateOperatingWindow() throws IOException {
        methodInvoke(GatewaySetupMethods.ACTIVATE_OPERATING_WINDOW);
    }

    /**
     * Deactivate the operating window
     *
     * @throws java.io.IOException
     */
    public final void deactivateOperatingWindow() throws IOException {
        methodInvoke(GatewaySetupMethods.DEACTIVATE_OPERATING_WINDOW);
    }
}
