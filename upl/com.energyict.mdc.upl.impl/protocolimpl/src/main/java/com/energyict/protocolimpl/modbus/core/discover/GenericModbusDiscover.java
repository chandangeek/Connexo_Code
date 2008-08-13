/*
 * GenericModbusDiscover.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.discover; 

 
import com.energyict.protocol.discover.*; 
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.modbus.core.connection.*;
import com.energyict.protocolimpl.modbus.core.functioncode.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
/**
 *
 * @author Koen
 */
public class GenericModbusDiscover extends Modbus implements MessageProtocol {
    
    final int DEBUG=0;
    
    /**
     * Creates a new instance of GenericModbusDiscover 
     */
    public GenericModbusDiscover() {
    }
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","25").trim()));
    }
    
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "THIS PROTOCOL IS ONLY FOR DISCOVERY";
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    public Date getTime() throws IOException {
        //return getRegisterFactory().findRegister("clock").dateValue();
        return new Date();
    }
    
    /*******************************************************************************************
     M e s s a g e P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    // message protocol
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while(it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry)it.next();
            System.out.println(messageEntry);
        }
    }
    
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createSuccess(messageEntry);
        //messageEntry.setTrackingId("tracking ID for "+messageEntry.);
        //return MessageResult.createQueued(messageEntry);
        //return MessageResult.createFailed(messageEntry);
        //return MessageResult.createUnknown(messageEntry);
    }
    
    
    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("PM800Messages");
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
    
    
    public DiscoverResult discoverHoldingRegister(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        discoverResult.setDiscovered(false);
        discoverResult.setResult("");
        
        try {
            if (DEBUG>=1) System.out.println("GenericModbusDiscover, discoverHoldingRegister...");
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodHoldingRegister()) {
                    int value=0;    
                    try {
                        value = ((BigDecimal)getRegisterFactory().findRegister(dpi.getDeviceType()).value()).intValue();
                    }
                    catch(ModbusException e) {
                        //e.printStackTrace();
                    }
                    boolean found=false;
                    StringTokenizer strTok = new StringTokenizer(dpi.getDetectionString(),";");
                    while(strTok.countTokens()>0) {
                        String detectionToken = strTok.nextToken();
                        if (value==Integer.parseInt(detectionToken)) {
                            discoverResult.setDiscovered(true);
                            discoverResult.setProtocolName(dpi.getProtocolName());
                            discoverResult.setAddress(discoverTools.getAddress());
                            discoverResult.setResult(""+value);
                            discoverResult.setDeviceTypeName(dpi.getDeviceType());
                            found=true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }            
        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;        
    }    
    
    private DiscoverResult discoverSlaveId(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        
        discoverResult.setDiscovered(false);
        discoverResult.setResult("");
        
        try {
            if (DEBUG>=1) System.out.println("GenericModbusDiscover, discoverSlaveId...");
            
            String str = getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
            if (DEBUG>=1) System.out.println("getReportSlaveId().getAdditionalDataAsString()="+str);
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodSlaveId()) {
                    if (str.toLowerCase().indexOf(dpi.getDetectionString().toLowerCase())>=0) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(str);
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());
                        
                        discoverResult.setShortDeviceTypeName("");
                        discoverResult.setDeviceName(str.replace('.','-').replace('/',' ')); // '.' and '/' are not allowed in EIServer as character in a device name!
                        
                        break;
                    }
                }
            }
        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }
    
    private DiscoverResult discoverMeterId(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        
        discoverResult.setDiscovered(false);
        discoverResult.setResult("");
        
        try {
            if (DEBUG>=1) System.out.println("GenericModbusDiscover, discoverMeterId...");
            MandatoryDeviceIdentification mdi = getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification();
            
            if (DEBUG>=1) System.out.println(mdi);
            
            Iterator it = DiscoverProtocolInfo.getSupportedDevicesList().iterator();
            while(it.hasNext()) {
                DiscoverProtocolInfo dpi = (DiscoverProtocolInfo)it.next();
                if (dpi.isDiscoverMethodMeterId()) {
                    if ((mdi.getVendorName().toLowerCase().indexOf(dpi.getMeterId()[0].toLowerCase())>=0) && (mdi.getProductCode().toLowerCase().indexOf(dpi.getMeterId()[1].toLowerCase())>=0)) {
                        discoverResult.setDiscovered(true);
                        discoverResult.setProtocolName(dpi.getProtocolName());
                        discoverResult.setAddress(discoverTools.getAddress());
                        discoverResult.setResult(mdi.getVendorName()+", "+mdi.getProductCode());
                        discoverResult.setDeviceTypeName(dpi.getDeviceType());
                        break;
                    }
                }
            }
            
            
        }
        catch(Exception e) {
            discoverResult.setResult(e.toString());
        }
        return discoverResult;
    }
    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        
        if (DEBUG>=1) System.out.println("GenericModbusDiscover, discover("+discoverTools+")");
        
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        
        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0)
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            connect();

            discoverResult = discoverSlaveId(discoverTools);
            if (!discoverResult.isDiscovered())
                discoverResult = discoverMeterId(discoverTools);
            if (!discoverResult.isDiscovered())
                discoverResult = discoverHoldingRegister(discoverTools);
            
            
            return discoverResult;
        }
        catch(Exception e) {
            discoverResult.setDiscovered(false);
            discoverResult.setResult(e.toString());
            return discoverResult;
        }
        finally {
           try { 
              disconnect();
           }
           catch(IOException e) {
               // absorb
           }
        }
    }   
    
    private Properties getProperties(int address) {
        Properties properties = new Properties();
        properties.setProperty(MeterProtocol.ADDRESS,""+address);
        properties.setProperty("ProfileInterval", "900");
        properties.setProperty("HalfDuplex", "-1");
        return properties;        
    }        
    
    static public void main(String[] args) { 
        
        try { 
            // ********************** EictRtuModbus **********************
            GenericModbusDiscover genericModbusDiscover = new GenericModbusDiscover();
            DiscoverTools discoverTools = null;
            
            if ((args==null) || (args.length<=1))
                discoverTools = new DiscoverTools("COM1");
            else
                discoverTools = new DiscoverTools(args[1]); //"/dev/ttyXR0";
            discoverTools.setProperties(genericModbusDiscover.getProperties(5));
            discoverTools.setAddress(5);
            discoverTools.init();
            discoverTools.connect();
            
            System.out.println(genericModbusDiscover.discover(discoverTools));
            discoverTools.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }    
}
