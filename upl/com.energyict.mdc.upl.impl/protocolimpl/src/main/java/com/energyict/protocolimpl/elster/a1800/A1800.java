package com.energyict.protocolimpl.elster.a1800;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.elster.a3.AlphaA3;
import com.energyict.protocolimpl.elster.a3.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.elster.a3.tables.ManufacturerTableFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class A1800 extends AlphaA3 implements MessageProtocol, HalfDuplexEnabler {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(A1800.class.getName());

	private A1800LoadProfile a1800LoadProfile;

	private boolean messageFailed = false;
	
	private HalfDuplexController halfDuplexController;
	private long halfDuplex;

	private int rs485RtuPlusServer = 0;
	
	public A1800() {
		
	}
	
	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return a1800LoadProfile.getProfileData(from,to,includeEvents);
    }
	
	@Override
	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, this.halfDuplexController);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        a1800LoadProfile = new 	A1800LoadProfile(this);
        return c12Layer2;
    }
	
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"0"));
        c12User = properties.getProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getProperty("C12UserId","0").trim());
        passwordBinary = Integer.parseInt(properties.getProperty("PasswordBinary","0").trim());
        setRetrieveExtraIntervals(Integer.parseInt(properties.getProperty("RetrieveExtraIntervals","0").trim()));
        this.halfDuplex=Integer.parseInt(properties.getProperty("HalfDuplex","0").trim());
		this.rs485RtuPlusServer=Integer.parseInt(properties.getProperty("RS485RtuPlusServer","0").trim());
    }
	
	protected void doDisConnect() throws IOException {  
		try {
			getPSEMServiceFactory().logOff();
		} catch (ResponseIOException e) {
			//if the message could not write the table, the logoff will fail. the message status
			//will not get updated, will try again next time, will fail, and loop again and again
			if (!messageFailed)
				throw(e);
		}
    }
	
	/*******************************************************************************************
    M e s s a g e P r o t o c o l  i n t e r f a c e 
	 *******************************************************************************************/
	// message protocol
	public void applyMessages(List messageEntries) throws IOException {
//		Iterator it = messageEntries.iterator();
//		while(it.hasNext()) {
//			MessageEntry messageEntry = (MessageEntry)it.next();
//			messageSuccess.put(messageEntry.hashCode(), messageEntry);
//			System.out.println(messageEntry);
//		}
	}
	private void importMessage(String message, DefaultHandler handler) throws BusinessException{
        try {
            
            byte[] bai = message.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);
            
        } catch (ParserConfigurationException thrown) {
            throw new BusinessException(thrown);
        } catch (SAXException thrown) {
            throw new BusinessException(thrown);
        } catch (IOException thrown) {
            throw new BusinessException(thrown);
        }
	}



	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		MessageHandler messageHandler = new MessageHandler();
		String content = messageEntry.getContent();
		
		boolean success = false;
		
		try {
			
			importMessage(content, messageHandler);
			boolean lpDiv = messageHandler.getType().equals(MessageHandler.SETPDIVISOR);
			if(lpDiv){
				
				// Execute the message
				int channel = messageHandler.getChannel();
				int divisor = messageHandler.getDivisor();
				if(divisor < 1 || divisor > 255){
					String error = "" + divisor + ".";
					log(Level.INFO, error);
				} else if (channel < 0 || channel > this.getNumberOfChannels()){
					String error = "" + channel + ".";
					log(Level.INFO, error);
				} else {
					//Execute
					int tableId = 62;
					int index = 0;
					index = getNumberOfChannels()*3; //3 bytes per channel for LP_SEL_SET
					index += 1; //1 byte for INT_FMT_CODE1
					index += getNumberOfChannels() * 2; //2 bytes per channel for SCALARS_SET
					index += (channel-1) * 2; //2 bytes per channel for DIVISORS_SET
					
					
					byte[] tableData = ParseUtils.getArrayLE(divisor, 2);
					getPSEMServiceFactory().partialWriteOffset(tableId, index, tableData);
					success = true;
				}
				
			} 
		}
		catch (ResponseIOException e) {
			if (e.getMessage().contains("Table 62, Inappropriate Action Requested.")) {
				log(Level.INFO, "Message " + messageEntry.getContent() + " has failed. Could not write to table. " + e.getMessage());
				messageFailed  = true;
				return MessageResult.createFailed(messageEntry);
			}
			else {
				throw e;
			}
		}
		catch (BusinessException e) {
			log(Level.INFO, "Message " + messageEntry.getContent() + " has failed. " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
		
		if(success){
			return MessageResult.createSuccess(messageEntry);
		} else {
			return MessageResult.createFailed(messageEntry);
		}
	}
	
	public void log(Level level, String tekst){
		this.getLogger().log(level, tekst);
	}

	public List getMessageCategories() {
		List theCategories = new ArrayList();
		// General Parameters
		MessageCategorySpec cat = new MessageCategorySpec("LP Configuration");
		MessageSpec msgSpec = addBasicMsg("setLPDivisor", "SETLPDIVISOR", true);
		cat.addMessageSpec(msgSpec);
		theCategories.add(cat);
		return theCategories;
	}

	private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
		tagSpec.add(new MessageAttributeSpec("Channel", true));
		tagSpec.add(new MessageAttributeSpec("Divisor", true));
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
	
	protected List<String> doGetOptionalKeys() {
        List<String> result = new ArrayList<String>(super.doGetOptionalKeys());

        result.add("HalfDuplex");
		result.add("RS485RtuPlusServer");
        
        return result;
    }
	
	private boolean isRS485RtuPlusServer() {
		return (this.rs485RtuPlusServer  != 0);
	}
	
	public void setHalfDuplexController(HalfDuplexController controller) {
		if (isRS485RtuPlusServer()) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Running on an RTU+Server, using inverted logic for RTS.");
			}

			this.halfDuplexController = new RtuPlusServerHalfDuplexController(controller);
		} else {
			this.halfDuplexController = controller;
		}

		this.halfDuplexController.setDelay(this.halfDuplex);
	}

}
