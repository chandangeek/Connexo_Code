/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.elster.a1800;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.protocolimpl.ansi.c12.C12Layer2;
import com.energyict.protocolimpl.ansi.c12.PSEMServiceFactory;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.RtuPlusServerHalfDuplexController;
import com.energyict.protocolimpl.elster.a3.AlphaA3;
import com.energyict.protocolimpl.elster.a3.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.elster.a3.tables.ManufacturerTableFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class A1800 extends AlphaA3 implements MessageProtocol, HalfDuplexEnabler {

	@Override
	public String getProtocolDescription() {
		return "Elster Alpha A1800 ANSI";
	}

	private A1800LoadProfile a1800LoadProfile;

	private boolean messageSucceeded = true;

	private int rs485RtuPlusServer = 0;

	@Inject
	public A1800(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return a1800LoadProfile.getProfileData(from,to,includeEvents);
    }

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		if (halfDuplexController != null && this.isRS485RtuPlusServer()) {
			halfDuplexController = new RtuPlusServerHalfDuplexController(halfDuplexController);
		}

		c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
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

		this.rs485RtuPlusServer=Integer.parseInt(properties.getProperty("RS485RtuPlusServer","0").trim());
    }

	protected void doDisConnect() throws IOException {
		try {
			getPSEMServiceFactory().logOff();
		} catch (ResponseIOException e) {
			//if the message could not write the table, the logoff will fail. the message status
			//will not get updated, will try again next time, will fail, and loop again and again
			if (messageSucceeded) {
				throw (e);
			}
		}
    }

    /* The protocol version */
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
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

	private void importMessage(String message, DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
		byte[] bai = message.getBytes();
		InputStream i = new ByteArrayInputStream(bai);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(i, handler);
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
					int index = getNumberOfChannels()*3; //3 bytes per channel for LP_SEL_SET
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
				messageSucceeded = false;
				return MessageResult.createFailed(messageEntry);
			}
			else {
				throw e;
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log(Level.INFO, "Message " + messageEntry.getContent() + " has failed. " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}

		if (success) {
			return MessageResult.createSuccess(messageEntry);
		} else {
			return MessageResult.createFailed(messageEntry);
		}
	}

	public void log(Level level, String tekst){
		this.getLogger().log(level, tekst);
	}

	public List<MessageCategorySpec> getMessageCategories() {
		List<MessageCategorySpec> theCategories = new ArrayList<>();
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
		StringBuilder builder = new StringBuilder();

		// a. Opening tag
		builder.append("<");
		builder.append( msgTag.getName() );

		// b. Attributes
		for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = (MessageAttribute)it.next();
			if (att.getValue()==null || att.getValue().isEmpty()) {
				continue;
			}
			builder.append(" ").append(att.getSpec().getName());
			builder.append("=").append('"').append(att.getValue()).append('"');
		}
		builder.append(">");

		// c. sub elements
		for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
			MessageElement elt = (MessageElement)it.next();
			if (elt.isTag()) {
				builder.append(writeTag((MessageTag) elt));
			}
			else if (elt.isValue()) {
				String value = writeValue((MessageValue)elt);
				if (value==null || value.isEmpty()) {
					return "";
				}
				builder.append(value);
			}
		}

		// d. Closing tag
		builder.append("</");
		builder.append( msgTag.getName() );
		builder.append(">");

		return builder.toString();
	}

	public String writeValue(MessageValue value) {
		return value.getValue();
	}

	protected List<String> doGetOptionalKeys() {
        List<String> result = new ArrayList<>(super.doGetOptionalKeys());
        result.add("HalfDuplex");
		result.add("RS485RtuPlusServer");
        return result;
    }

	private boolean isRS485RtuPlusServer() {
		return (this.rs485RtuPlusServer  != 0);
	}

}