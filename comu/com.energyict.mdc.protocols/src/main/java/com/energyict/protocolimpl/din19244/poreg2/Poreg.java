package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregConnection;
import com.energyict.protocolimpl.din19244.poreg2.core.PoregMessages;
import com.energyict.protocolimpl.din19244.poreg2.factory.RegisterFactory;
import com.energyict.protocolimpl.din19244.poreg2.factory.RequestFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 11-mei-2011
 * Time: 11:59:59
 */
public abstract class Poreg extends AbstractProtocol implements MessageProtocol {

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

    public Poreg(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doDisConnect() throws IOException {
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
    public void validateSerialNumber() throws IOException {
        String serialNumber = getRegisterFactory().readSerialNumber();
        if (!getNodeId().equals(serialNumber)) {
            throw new IOException("Serial number mismatch! Expected " + getNodeId() + ", received " + serialNumber);
        }
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
}