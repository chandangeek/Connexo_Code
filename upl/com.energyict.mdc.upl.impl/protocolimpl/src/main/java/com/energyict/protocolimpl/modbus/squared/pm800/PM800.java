/*
 * PM800.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm800;

import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.modbus.core.connection.*;
import com.energyict.protocolimpl.modbus.core.discover.*;
import com.energyict.protocolimpl.modbus.core.functioncode.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.discover.DiscoverResult;
/**
 *
 * @author Koen
 */
public class PM800 extends Modbus implements MessageProtocol {
    
    ModbusConnection modbusConnection;
    private RegisterFactory registerFactory;
    private MultiplierFactory multiplierFactory=null;
    
    /** Creates a new instance of PM800 */
    public PM800() {
    }

    
    
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        //return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
        return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
    }
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.7 $";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister(3034).dateValue();
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
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        
        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0)
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            connect();

            MandatoryDeviceIdentification mdi = getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification();
            
            if ((mdi.getVendorName().toLowerCase().indexOf("square d")>=0) && (mdi.getProductCode().indexOf("15210")>=0)) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName(this.getClass().getName());
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else
                discoverResult.setDiscovered(false);
            
            discoverResult.setResult(mdi.toString());
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
    
    
    static public void main(String[] args) {
		try {
			int countMax;
            if ((args==null) || (args.length<=3))
            	countMax=1;
            else
            	countMax=Integer.parseInt(args[3]);				
			
			int count = 0;
			while (count++ < countMax) {
				
				// ********************** Dialer **********************
				Dialer dialer = DialerFactory.getDirectDialer().newDialer();
	            String comport;
	            if ((args==null) || (args.length<=1))
	                comport="COM1";
	            else
	                comport=args[1]; //"/dev/ttyXR0";			
				dialer.init(comport);
				
				dialer.getSerialCommunicationChannel().setParams(9600,
						SerialCommunicationChannel.DATABITS_8,
						SerialCommunicationChannel.PARITY_NONE,
						SerialCommunicationChannel.STOPBITS_1);
				dialer.connect();

				// ********************** Properties **********************
				Properties properties = new Properties();
				properties.setProperty("ProfileInterval", "900");
				// properties.setProperty(MeterProtocol.NODEID,"0");
				properties.setProperty(MeterProtocol.ADDRESS, "1");
				properties.setProperty("Timeout", "2000");

	            int ift;
	            if ((args==null) || (args.length==0))
	                ift=50;
	            else
	                ift=Integer.parseInt(args[0]);				
	            properties.setProperty("InterframeTimeout", ""+ift);
	            

	            int hdt;
	            if ((args==null) || (args.length<=2))
	            	hdt=-1;
	            else
	            	hdt=Integer.parseInt(args[2]);				
				properties.setProperty("HalfDuplex", ""+hdt); 

	            // ********************** EictRtuModbus **********************
	            PM800 eictRtuModbus = new PM800();
	            //System.out.println(eictRtuModbus.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));
	            
	            eictRtuModbus.setProperties(properties);
	            eictRtuModbus.setHalfDuplexController(dialer.getHalfDuplexController());
	            eictRtuModbus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
	            eictRtuModbus.connect();
	            
	            //System.out.println(eictRtuModbus.getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification());
	            
	//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).getReadHoldingRegistersRequest());
	//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValue());
	//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(3034).dateValue());
	//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValueWithParser("BigDecimal"));
	//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).objectValueWithParser("powerfactor"));
	            
	            //System.out.println(eictRtuModbus.getFirmwareVersion());
	            //System.out.println(eictRtuModbus.getClass().getName());
	            //System.out.println(eictRtuModbus.getTime());
	           System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.16.8.0.255")));
	            //System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
	            //System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
	            //System.out.println(eictRtuModbus.getRegistersInfo(0));
	            //System.out.println(eictRtuModbus.getRegistersInfo(1));
	            
	            
				eictRtuModbus.disconnect();
				dialer.disConnect();
			}
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        return getMultiplierFactory().getMultiplier(address);
    }    
    
    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null)
            multiplierFactory = new MultiplierFactory(this);
        return multiplierFactory;
    }

    
}
