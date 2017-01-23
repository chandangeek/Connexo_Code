package com.energyict.protocolimpl.dlms.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

    @Inject
    public AbstractDlmsSessionProtocol(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected abstract DlmsSessionProperties getProperties();

    protected abstract void doInit();

    protected abstract String readSerialNumber() throws IOException;

    protected void validateSerialNumber() throws IOException {
        String eisSerial = getProperties().getSerialNumber().trim();
        String meterSerialNumber = readSerialNumber().trim();
        getLogger().info("Meter serial number [" + meterSerialNumber + "]");
        if (!eisSerial.isEmpty()) {
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
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
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

    public Object fetchCache(int rtuid) throws SQLException {
        return null;  // TODO: Implement this method
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
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
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(tag.getName());

        // b. Attributes
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
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

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return new ArrayList<>();
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    @Override
    public Optional<String> getActiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNameActive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getPassiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNamePassive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

}
