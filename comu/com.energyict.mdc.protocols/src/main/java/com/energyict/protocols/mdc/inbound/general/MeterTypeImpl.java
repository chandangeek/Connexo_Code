/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterType.java
 *
 * Created on 30 oktober 2003, 16:55
 */

package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocols.util.ProtocolImplFactory;

import java.io.IOException;

/*
* @author  Koen
*/
public class MeterTypeImpl implements MeterType {

    String receivedIdent;
    String meter3letterId;
    Object instance = null;

    public MeterTypeImpl(String receivedIdent) throws IOException {
        this.receivedIdent = receivedIdent;
        if (receivedIdent.length() >= 4) {
            this.meter3letterId = receivedIdent.substring(1, 4);
        } else {
            throw new IOException("MeterType, invalid flag meter identification " + receivedIdent);
        }
    }

    public String toString() {
        return receivedIdent + ", " + meter3letterId;
    }

    @Override
    public int getBaudrateIndex() {
        return receivedIdent.charAt(4) - 0x30;
    }

    @Override
    public char getZ() {
        return receivedIdent.charAt(4);
    }

    @Override
    public SerialNumber getProtocolSerialNumberInstance() throws NestedIOException {
        return (SerialNumber) getInstance();
    }

    // KV 19012004
    @Override
    public MeterExceptionInfo getProtocolMeterExceptionInfoInstance() throws NestedIOException {
        return (MeterExceptionInfo) getInstance();
    }

    private Object getInstance() throws NestedIOException {
        try {
            if (instance == null) {
                return Class.forName(ProtocolImplFactory.getIdentificationFactory().getMeterProtocolClass(this)).newInstance();
            } else {
                return instance;
            }
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | IOException e) {
            throw new NestedIOException(e);
        }
    }

    @Override
    public String[] getSerialNumberRegisterNames() throws IOException {
        return ProtocolImplFactory.getIdentificationFactory().getMeterSerialNumberRegisters(this);
    }

    @Override
    public String getResourceName() throws IOException {
        return ProtocolImplFactory.getIdentificationFactory().getResourceName(this);
    }

    @Override
    public java.lang.String getMeter3letterId() {
        return meter3letterId;
    }

    @Override
    public void setMeter3letterId(java.lang.String meter3letterId) {
        this.meter3letterId = meter3letterId;
    }

    @Override
    public String getReceivedIdent() {
        return receivedIdent;
    }

}