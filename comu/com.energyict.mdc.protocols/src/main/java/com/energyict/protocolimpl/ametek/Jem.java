package com.energyict.protocolimpl.ametek;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class Jem extends AbstractProtocol implements MessageProtocol {

	protected static final int REGULAR = 0;
	protected static final int ALTERNATE = 1;

	protected JemProtocolConnection connection;
	protected InputStream inputStream;
	protected OutputStream outputStream;

	protected int profileInterval = 900;
	protected int channelCount=0;
	protected Date time;
	protected ProfileData pd;
	protected byte[] ack = {0x10,0x01};
	protected byte[] cmdStart = {0x10,0x02};
	protected byte[] cmdEnd = {0x10,0x03};
	protected Map registerValues=null;

	public Jem(PropertySpecService propertySpecService) {
		super(propertySpecService);
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
		System.out.println(messageEntry);
		//return MessageResult.createSuccess(messageEntry);
		//messageEntry.setTrackingId("sampleTrackingId");
		return MessageResult.createSuccess(messageEntry);
		//return MessageResult.createQueued(messageEntry);
		//return MessageResult.createFailed(messageEntry);
		//return MessageResult.createUnknown(messageEntry);
	}

	public List getMessageCategories() {
		List theCategories = new ArrayList();
		// General Parameters
		MessageCategorySpec cat = new MessageCategorySpec("sampleCategoryName");
		MessageSpec msgSpec = addBasicMsg("sampleId", "SAMPLETAG", false);
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
		//getLogger().info("call abstract method doConnect()");
		//getLogger().info("--> at that point, we have a communicationlink with the meter (modem, direct, optical, ip, ...)");
		//getLogger().info("--> here the login and other authentication and setup should be done");

		if(getInfoTypeNodeAddress() == null || getInfoTypeNodeAddressNumber() < 1)
			throw new IOException("Invalid Node Address");

		if(getInfoTypePassword() == null || (getInfoTypePassword()!=null && getInfoTypePassword().length()<1))
			throw new IOException("Invalid Password");


		byte[] send = new byte[13];

		send[0] = (byte)getInfoTypeNodeAddressNumber();
		send[1] = 0x50;
		send[2] = 0x01;
		send[3] = cmdStart[0];
		send[4] = cmdStart[1];

		char pass[] = getInfoTypePassword().toCharArray();

		int i=0;
		for(; i <pass.length; i++)
			send[i+5] = (byte)pass[i];

		i+=5;
		send[i] = cmdEnd[0];
		i++;
		send[i] = cmdEnd[1];

		byte[] check = connection.getCheck(send, send.length);

//		delayAndFlush(1000);
		outputStream.write(ack);
		outputStream.write(send);
		outputStream.write(check);

		ByteArrayInputStream bais = new ByteArrayInputStream(connection.receiveResponse().toByteArray());
		int inval=0;

		inval = bais.read();
		if(inval!=0x06)
			throw new IOException("Invalid Response from Send Password.");
	}


	protected void doDisConnect() throws IOException {
		//getLogger().info("call abstract method doDisConnect()");
		//getLogger().info("--> here the logoff should be done");
		//getLogger().info("--> after that point, we will close the communicationlink with the meter");
	}



	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, null, includeEvents);
	}

	/*******************************************************************************************
   R e g i s t e r P r o t o c o l  i n t e r f a c e
	 *******************************************************************************************/
	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		////getLogger().info("call overrided method translateRegister()");
		return new RegisterInfo(obisCode.getDescription());
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		//getLogger().info("call overrided method readRegister("+obisCode+")");
		//getLogger().info("--> request the register from the meter here");

		if(obisCode.getA()!=1)
			throw new NoSuchRegisterException("Register "+obisCode+" not supported!");

		if(registerValues==null)
			retrieveRegisters();

//		if(obisCode.getB()<1 || obisCode.getB()>channelCount)
//		throw new NoSuchRegisterException("Register "+obisCode+" not supported!");

		RegisterValue rv = (RegisterValue)registerValues.get(obisCode.toString());

		if(rv!=null)
			return new RegisterValue(obisCode, rv.getQuantity(), rv.getEventTime(), rv.getFromTime(), rv.getToTime(), rv.getReadTime(), rv.getRegisterSpecId(), rv.getText());

		throw new NoSuchRegisterException("Register "+obisCode+" not supported!");

		//return new RegisterValue(obisCode,new Quantity(new BigDecimal("1234687.64"),Unit.get("kWh")));
	}

	protected abstract void retrieveRegisters() throws IOException;

	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		// Override or add new properties here e.g. below
//		setSDKSampleProperty(Integer.parseInt(properties.getProperty("SDKSampleProperty", "123")));
	}



	protected List doGetOptionalKeys() {
		List list = new ArrayList();
		//add new properties here, e.g. below
//		list.add("SDKSampleProperty");
		return list;
	}


	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		//getLogger().info("call doInit(...)");
		//getLogger().info("--> construct the ProtocolConnection and all other object here");

		connection = new JemProtocolConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,getLogger());
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		return connection;
	}







	protected long convertHexToLong(InputStream byteStream, int length) throws IOException
	{
		return ProtocolUtils.getLong((ByteArrayInputStream)byteStream,length);
	}

	protected long convertHexToLongLE(InputStream byteStream, int length) throws IOException
	{
		return ProtocolUtils.getLongLE((ByteArrayInputStream)byteStream,length);
	}


	protected SimpleDateFormat getDateFormatter() {
		SimpleDateFormat format = new SimpleDateFormat("yyMMddhhmmss");
		format.setTimeZone(getTimeZone());
		return format;
	}

	protected SimpleDateFormat getShortDateFormatter() {
		SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmm");
		format.setTimeZone(getTimeZone());
		return format;
	}


//	public int getSDKSampleProperty() {
//	return sDKSampleProperty;
//	}

//	public void setSDKSampleProperty(int sDKSampleProperty) {
//	this.sDKSampleProperty = sDKSampleProperty;
//	}


	protected void logData(int val)
	{
		if(getInfoTypeExtendedLogging()<1)
			return;

		String zeropad = "";
		if(Integer.toHexString(val).length()<2)
			zeropad = "0";
		System.out.print(",0x" + zeropad + Integer.toHexString(val));

	}

	protected void processList(ArrayList dataList, Calendar c, Date startDate, Date now) {
		for (int i = dataList.size()-1; i>=0; i--) {
			c.add(Calendar.SECOND, (getProfileInterval()*-1));
			ArrayList vals = (ArrayList)dataList.get(i);
			if(c.getTime().getTime() >= startDate.getTime() && c.getTime().before(now)) {
				IntervalData id = new IntervalData(c.getTime());
				id.addValues(vals);
				pd.addInterval(id);
			}
		}

	}

	public int getProfileInterval() {
		return profileInterval;
	}


}
