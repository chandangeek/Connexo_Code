/*
 * PM750.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm750;

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
public class PM750 extends Modbus  {

	ModbusConnection modbusConnection;
	private RegisterFactory registerFactory;
	private MultiplierFactory multiplierFactory = null;

	/**
	 * Creates a new instance of PM750
	 */
	public PM750() {
	}

	protected void doTheConnect() throws IOException {

	}

	protected void doTheDisConnect() throws IOException {

	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		
		setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "50").trim()));
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		// return
		// getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+",
		// "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
		return getRegisterFactory().getFunctionCodeFactory()
				.getMandatoryReadDeviceIdentification().toString();
	}

	protected List doTheGetOptionalKeys() {
		List result = new ArrayList();
		return result;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.1 $";
	}

	protected void initRegisterFactory() {
		setRegisterFactory(new RegisterFactory(this));
	}

	public Date getTime() throws IOException {
		return new Date();
	}



	public DiscoverResult discover(DiscoverTools discoverTools) {
		// discovery is implemented in the GenericModbusDiscover protocol
		return null;
	}

	static public void main(String[] args) {
		try {
			int countMax;
            if ((args==null) || (args.length<=3))
            	countMax=200;
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
				properties.setProperty(MeterProtocol.ADDRESS, "8");
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
				PM750 eictRtuModbus = new PM750();
				// System.out.println(eictRtuModbus.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));

				eictRtuModbus.setProperties(properties);
				eictRtuModbus.setHalfDuplexController(dialer
						.getHalfDuplexController());
				eictRtuModbus.init(dialer.getInputStream(), dialer
						.getOutputStream(), TimeZone.getTimeZone("ECT"), Logger
						.getLogger("name"));
				eictRtuModbus.connect();

				// System.out.println(eictRtuModbus.getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification());

				// System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).getReadHoldingRegistersRequest());
				// System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValue());
				// System.out.println(eictRtuModbus.getRegisterFactory().findRegister(3034).dateValue());
				// System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValueWithParser("BigDecimal"));
				// System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).objectValueWithParser("powerfactor"));

				// System.out.println(eictRtuModbus.getFirmwareVersion());
				// System.out.println(eictRtuModbus.getClass().getName());
				// System.out.println(eictRtuModbus.getTime());

				//System.out.println("****************************\n"+eictRtuModbus.getRegistersInfo(1));
				// System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.12.7.0.255")));
				// System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
				 System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.16.8.0.255")));
				// System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
				// System.out.println(eictRtuModbus.getRegistersInfo(0));
				 //System.out.println(eictRtuModbus.getRegistersInfo(1));

				eictRtuModbus.disconnect();
				dialer.disConnect();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public BigDecimal getRegisterMultiplier(int address) throws IOException,
			UnsupportedException {
		return getMultiplierFactory().getMultiplier(address);
	}

	public MultiplierFactory getMultiplierFactory() {
		if (multiplierFactory == null)
			multiplierFactory = new MultiplierFactory(this);
		return multiplierFactory;
	}

}
