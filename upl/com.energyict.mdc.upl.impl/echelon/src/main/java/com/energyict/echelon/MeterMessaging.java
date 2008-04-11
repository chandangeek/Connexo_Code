package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class MeterMessaging implements Messaging, MeterProtocol {

    private final static boolean ADVANCED = true;

    public final static String READ_REGISTERS = "readRegisters";
    public final static String READ_REGISTERS_ON_DEMAND = "readRegistersOnDemand";
    public final static String CONNECT_LOAD = "connectLoad";
    public final static String DISCONNECT_LOAD = "disconnectLoad";
    public final static String LOAD_PROFILE = "loadProfile";
    public final static String LOAD_PROFILE_DELTA = "loadProfileDelta";
    public final static String READ_EVENTS = "readEvents";
    public final static String CONTINUOUS_DELTA_LOAD_PROFILE = "continuousDeltaLoadProfile";

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageSpec msgSpec = null;

        msgSpec = addBasicMsg("Read registers on demand", READ_REGISTERS_ON_DEMAND, !ADVANCED);
        cat.addMessageSpec(msgSpec);

//        msgSpec = addBasicMsg("Read full load profile", LOAD_PROFILE, !ADVANCED);
//        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg("Read events", READ_EVENTS, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg("Connect", CONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg("Disconnect", DISCONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag())
                buf.append(writeTag((MessageTag) elt));
            else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0)
                    return "";
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }

    // MeterProtocol interface implementation
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
    }

    public void init(InputStream is, OutputStream os, TimeZone tz, Logger l) throws IOException {
    }

    public void connect() throws IOException {
    }

    public void disconnect() throws IOException {
    }

    public List getRequiredKeys() {
        return new ArrayList(0);
    }

    public List getOptionalKeys() {
        return new ArrayList(0);
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return null;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Date getTime() throws IOException {
        return new Date();
    }

    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return null;
    }

    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
    }

    public void setTime() throws IOException {
    }

    public void initializeDevice() throws IOException, UnsupportedException {
    }

    public void setCache(Object cacheObject) {
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
    }

    public void release() throws IOException {
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        return null;
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return null;
    }

}
