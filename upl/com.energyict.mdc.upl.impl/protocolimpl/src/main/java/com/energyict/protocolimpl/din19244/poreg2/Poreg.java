package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregMessages;
import com.energyict.protocolimpl.din19244.poreg2.factory.RegisterFactory;
import com.energyict.protocolimpl.din19244.poreg2.factory.RequestFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 11-mei-2011
 * Time: 11:59:59
 */
public abstract class Poreg extends AbstractProtocol implements MessageProtocol, SerialNumberSupport {

    protected PoregConnection connection;
    protected RegisterFactory registerFactory;
    protected RequestFactory requestFactory;
    protected ProfileDataReader profileDataReader;
    protected ObisCodeMapper obisCodeMapper;
    protected PoregMessages messageHandler;
    protected MeterType meterType;
    boolean isPoreg2;
    private int apparentEnergyResultLevel;
    private String systemAddress = "00000000";

    @Override
    protected void doDisconnect() throws IOException {
    }

    protected abstract PoregMessages getMessageHandler();

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
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.integer("ApparentEnergyResultLevel", false));
        propertySpecs.add(UPLPropertySpecFactory.string("SystemAddress", false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        apparentEnergyResultLevel = Integer.parseInt(properties.getTypedProperty("ApparentEnergyResultLevel", "0").trim());
        systemAddress = properties.getTypedProperty("SystemAddress", "00000000").trim();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessageHandler().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageHandler().queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return getMessageHandler().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessageHandler().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessageHandler().writeTag(tag);
    }

    @Override
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

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

}