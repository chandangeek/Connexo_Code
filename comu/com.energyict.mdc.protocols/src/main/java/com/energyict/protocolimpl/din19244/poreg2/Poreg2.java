/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregMessages;
import com.energyict.protocolimpl.din19244.poreg2.factory.RegisterFactory;
import com.energyict.protocolimpl.din19244.poreg2.factory.RequestFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class Poreg2 extends Poreg {

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Poreg 2 DIN19244";
    }

    @Inject
    public Poreg2(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        if (to == null) {
            to = new Date();
        }
        return getProfileDataReader().getProfileData(from, to, includeEvents);
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        isPoreg2 = true;
        connection = new PoregConnection(this, inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, halfDuplexController);
        registerFactory = new RegisterFactory(this);
        requestFactory = new RequestFactory(this);
        profileDataReader = new ProfileDataReader(this);
        obisCodeMapper = new ObisCodeMapper(this);
        messageHandler = new PoregMessages(this);
        return connection;
    }

    protected PoregMessages getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new PoregMessages(this);
        }
        return messageHandler;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    @Override
    public Date getTime() throws IOException {
        return getRegisterFactory().readTime();
    }

    public int getNumberOfChannels() throws IOException {
        return getRegisterFactory().getNumberOfChannels();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterInfo(obisCode);
    }

    /**
     * The protocol verion
     */
    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}