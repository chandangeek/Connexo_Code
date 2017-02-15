package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregMessages;
import com.energyict.protocolimpl.din19244.poreg2.factory.RegisterFactory;
import com.energyict.protocolimpl.din19244.poreg2.factory.RequestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 19-apr-2011
 * Time: 14:37:53
 */
public class Poreg2P extends Poreg {

    public Poreg2P(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {
            to = new Date();
        }
        return getProfileDataReader().getProfileData(from, to, includeEvents);
    }

    @Override
    protected PoregMessages getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new PoregMessages(this);
        }
        return messageHandler;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        isPoreg2 = false;
        connection = new PoregConnection(this, inputStream, outputStream, timeoutProperty, protocolRetriesProperty);
        registerFactory = new RegisterFactory(this);
        requestFactory = new RequestFactory(this);
        profileDataReader = new ProfileDataReader(this);
        obisCodeMapper = new ObisCodeMapper(this);
        messageHandler = new PoregMessages(this);
        return connection;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    @Override
    public Date getTime() throws IOException {
        return getRegisterFactory().readTime();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getRegisterFactory().getNumberOfChannels();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}