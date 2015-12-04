package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregMessages;
import com.energyict.protocolimpl.din19244.poreg2.factory.RegisterFactory;
import com.energyict.protocolimpl.din19244.poreg2.factory.RequestFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 11-mei-2011
 * Time: 11:59:59
 */
public abstract class Poreg extends AbstractProtocol implements MessageProtocol,SerialNumberSupport {

    protected PoregConnection connection;
    protected RegisterFactory registerFactory;
    protected RequestFactory requestFactory;
    protected ProfileDataReader profileDataReader;
    protected ObisCodeMapper obisCodeMapper;
    protected PoregMessages messageHandler;
    protected MeterType meterType;
    protected boolean isPoreg2;
    private int apparentEnergyResultLevel;
    private String systemAddress = "00000000";

    @Override
    protected void doDisConnect() throws IOException {
    }

    abstract protected PoregMessages getMessageHandler();

    public PoregConnection getConnection() {
        return connection;
    }

    @Override
    public String getStrID() {
        if (null == super.getStrID() || "".equals(super.getStrID())) {
            return "00000000";
        }
        return super.getStrID();
    }

    @Override
    public MeterType getMeterType() {
        return meterType;
    }

    public boolean isPoreg2() {
        return isPoreg2;
    }

    public RegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public ObisCodeMapper getObisCodeMapper() {
        return obisCodeMapper;
    }

    public ProfileDataReader getProfileDataReader() {
        return profileDataReader;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return requestFactory.readFirmwareVersion();
    }

    @Override
    public void setTime() throws IOException {
        getRequestFactory().setTime();
    }

    @Override
    protected void doConnect() throws IOException {
    }

    public int getApparentEnergyResultLevel() {
        return apparentEnergyResultLevel;
    }

    public String getSystemAddress() {
        return systemAddress;
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        apparentEnergyResultLevel = Integer.parseInt(properties.getProperty("ApparentEnergyResultLevel", "0").trim());
        systemAddress = properties.getProperty("SystemAddress", "00000000").trim();
    }

    @Override
    protected List doGetOptionalKeys() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("ApparentEnergyResultLevel");
        arrayList.add("SystemAddress");
        return arrayList;
    }

    public void applyMessages(List messageEntries) throws IOException {
        getMessageHandler().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageHandler().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageHandler().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessageHandler().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessageHandler().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessageHandler().writeValue(value);
    }

    @Override
    public String getSerialNumber() {
        try {
            return getRegisterFactory().readSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }
}