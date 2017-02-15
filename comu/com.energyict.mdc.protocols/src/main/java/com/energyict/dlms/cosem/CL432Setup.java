/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.CL432SetupAttributes;
import com.energyict.dlms.cosem.methods.CL432SetupMethods;

import java.io.IOException;

public class CL432Setup extends AbstractCosemObject {

    /**
     * The default {@link ObisCode} used in most case for this CL432Setup cosem object
     */
    public static final ObisCode DEFAULT_OBIS = ObisCode.fromString("0.0.28.0.0.255");

    /**
     * Creates a new instance of {@link CL432Setup}
     *
     * @param protocolLink    The protocol link to use for this object
     * @param objectReference The object reference to use, containing the obisCode
     */
    public CL432Setup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.CL_432_SETUP.getClassId();
    }

    /**
     * The default {@link ObisCode} used in most case for this CL432Setup cosem object
     *
     * @return The default {@link ObisCode}
     * @see CL432Setup#DEFAULT_OBIS
     */
    public static ObisCode getDefaultObis() {
        return DEFAULT_OBIS;
    }

    public final OctetString getLogicalName() throws IOException {
        return readDataType(CL432SetupAttributes.LOGICAL_NAME, OctetString.class);
    }

    /**
     * Holds the value of the address assigned to the service node during its registration by the base node.
     *
     * @return The device address
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned16 getDeviceAddress() throws IOException {
        return readDataType(CL432SetupAttributes.DEVICE_ADDRESS, Unsigned16.class);
    }

    /**
     * Holds the value of the address assigned to the base node.
     *
     * @return The base node address
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned16 getBaseNodeAddress() throws IOException {
        return readDataType(CL432SetupAttributes.BASE_NODE_ADDRESS, Unsigned16.class);
    }

    /**
     * This method is activated as a processing of CL_432_leave or CL_432_RELEASE services.
     *
     * @throws java.io.IOException If the reset failed
     */
    public final void reset() throws IOException {
        methodInvoke(CL432SetupMethods.RESET);
    }

}
