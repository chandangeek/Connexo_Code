package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdw.cpo.PropertySpecFactory;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 19/03/12
 * Time: 14:50
 */
public abstract class AbstractDlmsSessionProtocol extends PluggableMeterProtocol implements MessageProtocol, RegisterProtocol {

    private DlmsSession session = null;

    protected abstract DlmsSessionProperties getProperties();

    protected abstract void doInit();

    protected abstract String readSerialNumber() throws IOException;

    protected void validateSerialNumber() throws IOException {
        String eisSerial = getProperties().getSerialNumber().trim();
        String meterSerialNumber = readSerialNumber().trim();
        getLogger().info("Meter serial number [" + meterSerialNumber + "]");
        if (eisSerial.length() != 0) {
            if (!eisSerial.equalsIgnoreCase(meterSerialNumber)) {
                String message = "Configured serial number [" + eisSerial + "] does not match with the meter serial number [" + meterSerialNumber + "]!";
                getLogger().severe(message);
                throw new IOException(message);
            }
        } else {
            getLogger().info("Skipping validation of meter serial number: No serial number found in EIServer.");
        }
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.session = new DlmsSession(inputStream, outputStream, logger, getProperties(), timeZone);
        doInit();
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        ((DlmsProtocolProperties) getProperties()).addProperties(properties);
    }

    public DlmsSession getSession() {
        return this.session;
    }

    public void connect() throws IOException {
        this.session.connect();
        validateSerialNumber();
    }

    public void disconnect() throws IOException {
        this.session.disconnect();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public List<String> getRequiredKeys() {
        return ((DlmsProtocolProperties) getProperties()).getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return ((DlmsProtocolProperties) getProperties()).getOptionalKeys();
    }

    protected Logger getLogger() {
        if (this.session == null || this.session.getLogger() == null) {
            return Logger.getLogger(getClass().getName());
        }
        return this.session.getLogger();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar lastReading = Calendar.getInstance();
        lastReading.add(Calendar.DAY_OF_MONTH, -1);
        return getProfileData(lastReading.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }

    public void initializeDevice() throws IOException {
        // No init required
    }

    public void release() throws IOException {
        // No release required
    }

    public void setCache(Object cacheObject) {
        // TODO: Implement this method
    }

    public Object getCache() {
        return null;  // TODO: Implement this method
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;  // TODO: Implement this method
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        // TODO: Implement this method
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public String writeTag(MessageTag tag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(tag.getName());

        // b. Attributes
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().length() == 0)) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.length() == 0)) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("\n\n</");
        buf.append(tag.getName());
        buf.append(">");

        return buf.toString();

    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return new ArrayList<MessageCategorySpec>();
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

}
