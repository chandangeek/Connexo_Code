package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 14:50
 */
public abstract class AbstractDlmsSessionProtocol extends PluggableMeterProtocol implements MessageProtocol, RegisterProtocol {

    private DlmsSession session = null;

    protected abstract DlmsProtocolProperties getProperties();

    protected abstract void doInit();

    protected abstract String readSerialNumber() throws IOException;

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.session = new DlmsSession(inputStream, outputStream, logger, getProperties(), timeZone);
        doInit();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties().getPropertySpecs();
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        getProperties().setProperties(properties);
    }

    public DlmsSession getSession() {
        return this.session;
    }

    @Override
    public void connect() throws IOException {
        this.session.connect();
    }

    @Override
    public void disconnect() throws IOException {
        this.session.disconnect();
    }

    protected Logger getLogger() {
        if (this.session == null || this.session.getLogger() == null) {
            return Logger.getLogger(getClass().getName());
        }
        return this.session.getLogger();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar lastReading = Calendar.getInstance();
        lastReading.add(Calendar.DAY_OF_MONTH, -1);
        return getProfileData(lastReading.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public void setRegister(String name, String value) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() {
        // No init required
    }

    @Override
    public void release() {
        // No release required
    }

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
    }

    @Override
    public String writeTag(MessageTag tag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(tag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("\n\n</");
        builder.append(tag.getName());
        builder.append(">");

        return builder.toString();

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return new ArrayList<>();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

}