package com.energyict.protocolimpl.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dynamicattributes.ValueFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.*;

public class SmartMeteringSampleProtocol extends AbstractProtocol implements Messaging, ConnectMessaging, DisconnectMessaging, CosemAttributeMessaging, CosemMethodMessaging, TimeOfUseMessaging{

	public List getMessageCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeMessage(Message msg) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeTag(MessageTag tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeValue(MessageValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void doConnect() throws IOException {
	}

	protected void doDisConnect() throws IOException {
	}

	protected List doGetOptionalKeys() {
		return new ArrayList();
	}

	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		return null;
	}

	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return "";
	}

	public String getProtocolVersion() {
		return "$Revision: 1.1 $";
	}

	public Date getTime() throws IOException {
		return new Date();
	}

	public void setTime() throws IOException {
	}
	
	
	public int getNumberOfParameters(int classId, ObisCode logicalName) {
		return 2;
	}
	
	public ValueFactory getType(int classId, ObisCode logicalName, int parameterId) {
		if (parameterId == 0)
			return new com.energyict.dynamicattributes.StringFactory();
		else if (parameterId == 1)
			return new com.energyict.dynamicattributes.BigDecimalFactory();
		else
			throw new ApplicationException("Only 2 parameters");
	}

	public boolean needsName() {
		return true;
	}
	
	public boolean supportsCodeTables() {
		return true;
	}
	
	public boolean supportsUserFiles() {
		return false;
	}
	
	public CosemAttributeMessageBuilder getCosemAttributeMessageBuilder() {
		return new CosemAttributeMessageBuilder();
	}

	public DisconnectMessageBuilder getDisconnectMessageBuilder() {
		return new DisconnectMessageBuilder();
	}

	public CosemMethodMessageBuilder getCosemMethodMessageBuilder() {
		return new CosemMethodMessageBuilder();
	}

	public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
		return new TimeOfUseMessageBuilder();
	}

	public ConnectMessageBuilder getConnectMessageBuilder() {
		return new ConnectMessageBuilder();
	}

	
	

}
