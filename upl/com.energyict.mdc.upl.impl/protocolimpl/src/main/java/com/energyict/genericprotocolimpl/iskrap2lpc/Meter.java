package com.energyict.genericprotocolimpl.iskrap2lpc;

import com.energyict.cbo.*;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class Meter implements Messaging, MeterProtocol {
    
    private final static boolean ADVANCED = true;
     
    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageCategorySpec cat2 = new MessageCategorySpec("DLC communication");
        
        MessageSpec msgSpec = null;
        
        msgSpec = addBasicMsg("Read on demand", RtuMessageConstant.READ_ON_DEMAND, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg("Connect", RtuMessageConstant.CONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addThresholdParameters("Threshold parameters", RtuMessageConstant.THRESHOLD_PARAMETERS, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addRepeaterMode("Repeater mode", RtuMessageConstant.REPEATER_MODE, !ADVANCED);
        cat2.addMessageSpec(msgSpec);
        
        msgSpec = addPLCFreqChange("Change PLC Frequency", RtuMessageConstant.CHANGE_PLC_FREQUENCY, !ADVANCED);
        cat2.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        theCategories.add(cat2);
        return theCategories;
    }
    
    public String writeMessage(Message msg) {
        return msg.write(this);
    }
    
    private MessageSpec addRepeaterMode(String keyId, String tagName, boolean advanced){
    	// TODO can we allow only the 0 - 1 - and 2
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
    	MessageTagSpec tagSpec = new MessageTagSpec(tagName);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	return msgSpec;
    }
    
    private MessageSpec addPLCFreqChange(String keyId, String tagName, boolean advanced){
    	// TODO can we allow only the 0-1-2-3 or 4
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
    	MessageTagSpec tagSpec = new MessageTagSpec(tagName);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	return msgSpec;
    }
    
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    private MessageSpec addThresholdParameters(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_GROUPID);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_POWERLIMIT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.CONTRACT_POWERLIMIT);
        tagSpec.add(new MessageValueSpec());
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
    
    // MeterProtocol interface implementation
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
    }
    
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger)
            throws IOException {
    }
    
    public void connect() throws IOException { }
    
    public void disconnect() throws IOException { }
    
    public List getRequiredKeys() {
        return new ArrayList(0);
    }
    
    public List getOptionalKeys() {
        return new ArrayList(0);
    }
    
    public String getProtocolVersion() {
        return "$Date$";
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
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,
            UnsupportedException {
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
    
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException,
            UnsupportedException {
    }
    
    public void setTime() throws IOException { }
    
    public void initializeDevice() throws IOException, UnsupportedException { }
    
    public void setCache(Object cacheObject) {}
    
    public Object getCache() {
        return null;
    }
    
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;
    }
    
    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException { }
    
    public void release() throws IOException { }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        return null;
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return null;
    }
    

}
