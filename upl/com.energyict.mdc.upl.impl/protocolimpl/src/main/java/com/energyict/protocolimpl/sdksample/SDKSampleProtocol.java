/*
 * SDKSampleProtocol.java
 *
 * Created on 13 juni 2007, 11:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.sdksample;

import com.energyict.cbo.*;
import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.math.*;
import java.util.*;

/**
 *
 * @author kvds
 * com.energyict.protocolimpl.sdksample.SDKSampleProtocol
 */
public class SDKSampleProtocol extends AbstractProtocol implements MessageProtocol  {
    
    SDKSampleProtocolConnection connection;
    private int sDKSampleProperty;
    
    /** Creates a new instance of SDKSampleProtocol */
    public SDKSampleProtocol() {
    }
     
    /*******************************************************************************************
     M e s s a g e P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    // message protocol
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while(it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry)it.next();
            //System.out.println(messageEntry);
        }
    }
    
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
    	
    	getLogger().info("MessageEntry: "+messageEntry.getContent());
    	
        return MessageResult.createSuccess(messageEntry);
        //messageEntry.setTrackingId("tracking ID for "+messageEntry.);
        //return MessageResult.createQueued(messageEntry);
        //return MessageResult.createFailed(messageEntry);
        //return MessageResult.createUnknown(messageEntry);
    }
    
    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("SAMPLE");
        MessageSpec msgSpec = addBasicMsg("Disconnect meter", "DISCONNECT", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", "CONNECT", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Limit current to 6A", "LIMITCURRENT6A", false);
        cat.addMessageSpec(msgSpec);
        theCategories.add(cat);
        return theCategories;
    }    
    
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    public String writeMessage(Message msg) {
        return msg.write(this);
    }
    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append( msgTag.getName() );
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute)it.next();
            if (att.getValue()==null || att.getValue().length()==0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");
        
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag())
                buf.append( writeTag((MessageTag)elt) );
            else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if (value==null || value.length()==0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("</");
        buf.append( msgTag.getName() );
        buf.append(">");
        
        return buf.toString();    
    }
    
    public String writeValue(MessageValue value) {
        return value.getValue();
    }    
    protected void doConnect() throws IOException {
        getLogger().info("call abstract method doConnect()");      
        getLogger().info("--> at that point, we have a communicationlink with the meter (modem, direct, optical, ip, ...)");     
        getLogger().info("--> here the login and other authentication and setup should be done");     
    }
    
    protected void doDisConnect() throws IOException {
        getLogger().info("call abstract method doDisConnect()");        
        getLogger().info("--> here the logoff should be done");     
        getLogger().info("--> after that point, we will close the communicationlink with the meter");     
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        
        getLogger().info("call overrided method getProfileData("+lastReading+","+includeEvents+")");  
        getLogger().info("--> here we read the profiledata from the meter and construct a profiledata object");  
        
        ProfileData pd = new ProfileData();
        
        pd.addChannel(new ChannelInfo(0,0, "SDK sample channel 1", Unit.get("kWh")));
        pd.addChannel(new ChannelInfo(1,1, "SDK sample channel 2", Unit.get("kvarh")));
        
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastReading);
        if (getProfileInterval()<=0)
            throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
        ParseUtils.roundDown2nearestInterval(cal,getProfileInterval());
        Date now = new Date();
        while(cal.getTime().before(now)) { 
           IntervalData id = new IntervalData(cal.getTime());
           
           id.addValue(new BigDecimal(10000+Math.round(Math.random()*100)));
           id.addValue(new BigDecimal(1000+Math.round(Math.random()*10)));
           pd.addInterval(id);
           cal.add(Calendar.SECOND, getProfileInterval());
        }
        
        pd.addEvent(new MeterEvent(now,MeterEvent.APPLICATION_ALERT_START, "SDK Sample"));
        return pd;
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        getLogger().info("call overrided method getRegistersInfo("+extendedLogging+")");
        getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
        return "1.1.1.8.1.255 Active Import energy";
    }
    
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //getLogger().info("call overrided method translateRegister()");
        return new RegisterInfo(obisCode.getDescription());
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        getLogger().info("call overrided method readRegister("+obisCode+")");
        getLogger().info("--> request the register from the meter here");
        if (obisCode.equals(ObisCode.fromString("1.1.1.8.0.255")))
            return new RegisterValue(obisCode,new Quantity(new BigDecimal("1234687.64"),Unit.get("kWh")));
        throw new NoSuchRegisterException("Register "+obisCode+" not supported!");
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        // Override or add new properties here e.g. below
        setSDKSampleProperty(Integer.parseInt(properties.getProperty("SDKSampleProperty", "123")));
    }
    
    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        //add new properties here, e.g. below
        list.add("SDKSampleProperty");
        return list;
    }
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        getLogger().info("call doInit(...)");
        getLogger().info("--> construct the ProtocolConnection and all other object here");
        
        connection = new SDKSampleProtocolConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,getLogger());
        return connection;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
        getLogger().info("--> report the nr of load profile channels in the meter here");
        return 2;
    }
    
    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        long currenttime = new Date().getTime();
        return new Date(currenttime-(1000*15));
    }
    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
    }
    public String getProtocolVersion() {
        //getLogger().info("call getProtocolVersion()");
        return "$Revision: 1.5 $";
        //return "SDK Sample protocol version";  
    }
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "SDK Sample firmware version";
    }

    public int getSDKSampleProperty() {
        return sDKSampleProperty;
    }

    public void setSDKSampleProperty(int sDKSampleProperty) {
        this.sDKSampleProperty = sDKSampleProperty;
    }
}
