package com.energyict.protocolimpl.elster.a1800;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

public class A1800 extends AlphaA3 implements MessageProtocol, HalfDuplexEnabler {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(A1800.class.getName());

	private A1800LoadProfile a1800LoadProfile;
	private boolean messageFailed = false;
	private int rs485RtuPlusServer = 0;

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

	@Override
	public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("RS485RtuPlusServer", false));
        return propertySpecs;
	}

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay", "10").trim()));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "0"));
        c12User = properties.getTypedProperty("C12User", "");
        c12UserId = Integer.parseInt(properties.getTypedProperty("C12UserId", "0").trim());
        passwordBinary = Integer.parseInt(properties.getTypedProperty("PasswordBinary", "0").trim());
        setRetrieveExtraIntervals(Integer.parseInt(properties.getTypedProperty("RetrieveExtraIntervals", "0").trim()));

		this.rs485RtuPlusServer = Integer.parseInt(properties.getTypedProperty("RS485RtuPlusServer", "0").trim());
    }

	protected void doDisconnect() throws IOException {
		try {
			getPSEMServiceFactory().logOff();
		} catch (ResponseIOException e) {
			//if the message could not write the table, the logoff will fail. the message status
			//will not get updated, will try again next time, will fail, and loop again and again
			if (!messageFailed) {
				throw (e);
			}
		}
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
	public void applyMessages(List messageEntries) throws IOException {
	}

	private void importMessage(String message, DefaultHandler handler) {
        try {
            byte[] bai = message.getBytes();
            InputStream i = new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (ParserConfigurationException | SAXException | IOException thrown) {
            throw new IllegalArgumentException(thrown);
        }
	}

    @Override
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
				messageFailed  = true;
				return MessageResult.createFailed(messageEntry);
			}
			else {
				throw e;
			}
		}
		catch (IllegalArgumentException e) {
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

    @Override
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
		tagSpec.add(new MessageValueSpec(" "));
		tagSpec.add(new MessageAttributeSpec("Channel", true));
		tagSpec.add(new MessageAttributeSpec("Divisor", true));
		msgSpec.add(tagSpec);
		return msgSpec;
	}

    @Override
	public String writeMessage(Message msg) {
		return msg.write(this);
	}

    @Override
	public String writeTag(MessageTag msgTag) {
		StringBuilder builder = new StringBuilder();

		// a. Opening tag
		builder.append("<");
		builder.append( msgTag.getName() );

		// b. Attributes
		for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = it.next();
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
			} else if (elt.isValue()) {
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

    @Override
	public String writeValue(MessageValue value) {
		return value.getValue();
	}

	private boolean isRS485RtuPlusServer() {
		return (this.rs485RtuPlusServer  != 0);
	}

}