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

	public List getCosemClasses() {
		List classes = new ArrayList();
		CosemClass data = new CosemClass(1, "Data");
		data.addAttribute(new CosemAttribute(1, "Logical name", CosemDataType.STRING_TYPE));
		data.addAttribute(new CosemAttribute(2, "Value", CosemDataType.INTEGER_TYPE));
		classes.add(data);
		CosemClass register = new CosemClass(3, "Register");
		register.addAttribute(new CosemAttribute(1, "Logical name", CosemDataType.STRING_TYPE));
		register.addAttribute(new CosemAttribute(2, "Value", CosemDataType.INTEGER_TYPE));
		register.addAttribute(new CosemAttribute(3, "Scaler Unit", CosemDataType.OTHER_TYPE));
		CosemMethod reset = new CosemMethod(1, "Reset");
		reset.addParameter(new CosemParameter(CosemDataType.INTEGER_TYPE));
		data.addMethod(reset);
		classes.add(register);
		CosemClass demandRegister = new CosemClass(5, "Demand Register");
		demandRegister.addAttribute(new CosemAttribute(1, "Logical name", CosemDataType.STRING_TYPE));
		demandRegister.addAttribute(new CosemAttribute(2, "Current average value", CosemDataType.INTEGER_TYPE));
		demandRegister.addAttribute(new CosemAttribute(3, "Last average value", CosemDataType.INTEGER_TYPE));
		demandRegister.addAttribute(new CosemAttribute(4, "Scaler Unit", CosemDataType.OTHER_TYPE));
		demandRegister.addAttribute(new CosemAttribute(5, "Status", CosemDataType.INTEGER_TYPE));
		demandRegister.addAttribute(new CosemAttribute(6, "Capture time", CosemDataType.DATE_TYPE));
		demandRegister.addAttribute(new CosemAttribute(7, "Start time current", CosemDataType.DATE_TYPE));
		demandRegister.addAttribute(new CosemAttribute(8, "Period", CosemDataType.INTEGER_TYPE));
		demandRegister.addAttribute(new CosemAttribute(9, "Number of periods", CosemDataType.INTEGER_TYPE));
		CosemMethod reset2 = new CosemMethod(1, "Reset");
		reset2.addParameter(new CosemParameter(CosemDataType.INTEGER_TYPE));
		data.addMethod(reset2);
		CosemMethod nextPeriod = new CosemMethod(1, "Next period");
		nextPeriod.addParameter(new CosemParameter(CosemDataType.INTEGER_TYPE));
		data.addMethod(nextPeriod);
		classes.add(demandRegister);
		return classes;
	}

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
