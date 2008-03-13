/*
 * PQM2.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.ge.pqm2;

import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.modbus.core.connection.*;
import com.energyict.protocolimpl.modbus.core.discover.*;
import com.energyict.protocolimpl.modbus.core.functioncode.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
/**
 *
 * @author Koen
 */
public class PQM2 extends Modbus implements MessageProtocol {
    
    /** Creates a new instance of PQM2 */
    public PQM2() {
    }
    
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return (String)getRegisterFactory().findRegister("firmware version").objectValueWithParser("firmware version");
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.8 $";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister("clock").dateValue();
        //return new Date();
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

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }    
    
    static public void main(String[] args) {
        try {
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(9600,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_NONE,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"11");
            properties.setProperty("HalfDuplex", "1");
            
            // ********************** EictRtuModbus **********************
            PQM2 pmq2 = new PQM2();
            
            pmq2.setProperties(properties);
            pmq2.setHalfDuplexController(dialer.getHalfDuplexController());
            pmq2.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            pmq2.connect();
//            System.out.println(pmq2.getRegisterFactory().findRegister("clock").dateValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("clock").getReadHoldingRegistersRequest());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse1 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse1 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse2 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse2 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse3 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse3 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse4 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse4 input low").quantityValue());
            
//            System.out.println(pmq2.getRegisterFactory().findRegister(0).quantityValue());
//            
            //byte[] registerValues = new byte[]{(byte)0xa,(byte)0x2b,(byte)0xc3,(byte)0x1e,(byte)0x5,(byte)0x03,(byte)0x7,(byte)0xd7};
            //pmq2.getRegisterFactory().findRegister("clock").getWriteMultipleRegisters(registerValues);
            
            
            //System.out.println(pmq2.getRegisterFactory().findRegister("UserDefined1").quantityValue());
            //pmq2.getRegisterFactory().findRegister("UserDefined1").getWriteSingleRegister(0x241);
            //System.out.println(pmq2.getRegisterFactory().findRegister("UserDefined1").quantityValue());
            
            //System.out.println(pmq2.getTime());
            
           // System.out.println(pmq2.getRegistersInfo(1));
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.7.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.6.0.255")));
            System.out.println(pmq2.getRegisterFactory().findRegister("ProductDeviceCode").quantityValue());
            System.out.println(pmq2.getRegisterFactory().findRegister("SerialNumber").value());
            
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.14.7.0.255")));
            
            
//            System.out.println(pmq2.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.getRegistersInfo(1));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
