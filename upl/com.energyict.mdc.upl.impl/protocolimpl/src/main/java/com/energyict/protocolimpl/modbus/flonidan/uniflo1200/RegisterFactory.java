/**
 * RegisterFactory.java
 * 
 * Created on 4-dec-2008, 15:00:50 by jme
 * 
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;

/**
 *
 * @author jme
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
	private UNIFLO1200Registers fwRegisters = null;
	private int baseSlaveID = -1;
    
	public static final String REG_TIME 				= "Time";						// meter time and date
    public static final String REG_DEVICE_TYPE			= "Device type";				// device type string 
    public static final String REG_LOGIN				= "Login";						// register to write password to during login


    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		Unit returnUnit = null;
		String returnText = null;
		Date returnEventTime = null;
		Date returnFromTime = null;
		Date returnToTime = null;
		Date returnReadTime = null;
		Quantity returnQuantity = null;
		int returnRtuRegisterID = 0;
		int returnDecimals = 0;
		int slaveID = 0;
        	
		try {

			UNIFLO1200HoldingRegister hr = (UNIFLO1200HoldingRegister) findRegister(obisCode);
        	
        	slaveID = hr.getSlaveID();
        	returnUnit = hr.getUnit();
        	returnDecimals = hr.getScale();

        	getModBus().getModbusConnection().setAddress(getBaseSlaveID()  + slaveID);
        	
        	returnRtuRegisterID = hr.getReg();
        	Object result = hr.value();
        	Class rc = result.getClass();
        	
        	System.out.println("Result class type: " + result.getClass().getName());
            
        	if (rc == String.class)	{
        		returnText = (String)result;
        	}
        	else if (rc == Date.class) {
        		returnEventTime = (Date)result;
        		returnQuantity = new Quantity(returnEventTime.getTime(), returnUnit);
        		returnText = returnEventTime.toString();
        	}
        	else if (rc == Quantity.class) {
        		returnQuantity = (Quantity)result;
        		returnQuantity.convertTo(returnUnit, true);
        	}
        	else if (rc == BigDecimal.class) {
        		BigDecimal value = ((BigDecimal)result).setScale(returnDecimals, BigDecimal.ROUND_HALF_UP);
        		returnQuantity = new Quantity((BigDecimal)value, returnUnit);
        	}
        	else if (rc == Integer.class) {
        		returnQuantity = new Quantity(new BigDecimal((Integer)result), returnUnit);
        	}
        	else {
        		returnText = result.toString();
        	}
        	
        	return new RegisterValue(obisCode, returnQuantity, returnEventTime, returnFromTime, returnToTime, returnReadTime, returnRtuRegisterID, returnText);
        }
        catch(ModbusException e) {
            if ((e.getExceptionCode()==0x02) && (e.getFunctionErrorCode()==0x83))
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            else
                throw e;
        }

	}

    
    private int getBaseSlaveID() {
    	if (this.baseSlaveID == -1) {
        	this.baseSlaveID = getModBus().getModbusConnection().getAddress();
    	}
    	return this.baseSlaveID;
	}

	protected void init() {
        try {
        	this.fwRegisters  = new UNIFLO1200Registers(UNIFLO1200Registers.UNIFLO1200_FW_28);
    	
        	setZeroBased(false); // this means that reg2read = reg-1
        
        	getRegisters().add(new UNIFLO1200HoldingRegister(UNIFLO1200Registers.V28.WR_SET_PASS, 0, REG_LOGIN));

        	add(UNIFLO1200Registers.V28.TIME, "7.0.0.1.2.255", REG_TIME);
        	add(UNIFLO1200Registers.V28.FW_VERSION_TYPE, "7.0.0.2.1.255", REG_DEVICE_TYPE);
        	
        	add(UNIFLO1200Registers.V28.PRESS_SERIAL, "7.0.0.2.11.255", "Temperature sensor serial");
        	add(UNIFLO1200Registers.V28.PRESS_SENSOR, "7.0.0.2.11.0", "Pressure sensor type");
        	add(UNIFLO1200Registers.V28.TEMP_SENSOR, "7.0.0.2.12.255", "Temperature sensor type");
        	add(UNIFLO1200Registers.V28.FLOW_SENSOR, "7.0.0.2.13.255", "Flow sensor type");
        	add(UNIFLO1200Registers.V28.ZA, "7.0.53.2.0.255", "Za (Actual supercompressibilitifactor)");
			add(UNIFLO1200Registers.V28.ZB, "7.0.53.11.0.255", "Zb (Base supercompressibilitifactor)");
			add(UNIFLO1200Registers.V28.BATTERY_REMAINING, "0.1.96.6.0.255", "Battery remaining days");
			
			add(UNIFLO1200Registers.V28.TEMPERATURE, "7.0.41.0.0.255", "Temperature (current value)");
			add(UNIFLO1200Registers.V28.FALLBACK_TEMP, "7.0.41.3.0.255", "Fallback temperature");

			add(UNIFLO1200Registers.V28.PRESSURE, "7.0.42.0.0.255", "Pressure (current value)");
			add(UNIFLO1200Registers.V28.FALLBACK_PRESS, "7.0.42.3.0.255", "Fallback pressure");
        	
			add(UNIFLO1200Registers.V28.CONVERSION_FACTOR, "7.0.52.0.0.255", "Conversion factor");
			add(UNIFLO1200Registers.V28.CORRECTION_FACTOR, "7.0.51.0.0.255", "Correction factor");

			add(UNIFLO1200Registers.V28.GAS_CALC_FORMULA, fwRegisters.getDataLength(UNIFLO1200Registers.V28.GAS_CALC_FORMULA), 
					"7.0.53.12.0.255", fwRegisters.getUnitString(UNIFLO1200Registers.V28.GAS_CALC_FORMULA),	
					"Gas calculation formula", UNIFLO1200Parsers.PARSER_GAS_FORM);
			
			add(UNIFLO1200Registers.V28.FLOW_MEASURED, "7.0.43.0.0.255", "Flow measured");
			add(UNIFLO1200Registers.V28.FLOW_CORRECTED, "7.0.43.1.0.255", "Flow corrected");
			add(UNIFLO1200Registers.V28.FLOW_CONVERTED, "7.0.43.2.0.255", "Flow converted");

			add(UNIFLO1200Registers.V28.DENSITY, "7.0.45.0.0.255", "Density");
			
			add(UNIFLO1200Registers.V28.VOLUME_MEASURED, "7.0.0.3.0.255", "Volume measured");
			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED, "7.0.96.50.0.255", "Volume corrected");
			add(UNIFLO1200Registers.V28.VOLUME_CONVERTED, "7.0.0.3.3.255", "Volume converted");
			
			add(UNIFLO1200Registers.V28.TEMP_LOW_LIMIT, "7.0.0.5.11.255", "Temperature low limit");
			add(UNIFLO1200Registers.V28.TEMP_HIGH_LIMIT, "7.0.0.5.12.255", "Temperature high limit");
			add(UNIFLO1200Registers.V28.PRESS_LOW_LIMIT, "7.0.0.5.13.255", "Pressure low limit");
			add(UNIFLO1200Registers.V28.PRESS_HIGH_LIMIT, "7.0.0.5.14.255", "Pressure high limit");
			
			add(UNIFLO1200Registers.V28.METHANE, "7.0.98.1.0.255", "METHANE");
			add(UNIFLO1200Registers.V28.NITROGEN, "7.0.98.1.1.255", "NITROGEN");
			add(UNIFLO1200Registers.V28.CO2, "7.0.98.1.2.255", "CO2");
			add(UNIFLO1200Registers.V28.ETHANE, "7.0.98.1.3.255", "ETHANE");
			add(UNIFLO1200Registers.V28.PROPANE, "7.0.98.1.4.255", "PROPANE");
			add(UNIFLO1200Registers.V28.WATER, "7.0.98.1.5.255", "WATER");
			add(UNIFLO1200Registers.V28.HYDRG_SUL, "7.0.98.1.6.255", "HYDRG_SUL");
			add(UNIFLO1200Registers.V28.HYDROGEN, "7.0.98.1.7.255", "HYDROGEN");
			add(UNIFLO1200Registers.V28.CARBON_MONOXIDE, "7.0.98.1.8.255", "CARBON_MONOXIDE");
			add(UNIFLO1200Registers.V28.OXYGEN, "7.0.98.1.9.255", "OXYGEN");
			add(UNIFLO1200Registers.V28.I_BUTANE, "7.0.98.1.10.255", "I_BUTANE");
			add(UNIFLO1200Registers.V28.N_BUTANE, "7.0.98.1.11.255", "N_BUTANE");
			add(UNIFLO1200Registers.V28.I_PETANE, "7.0.98.1.12.255", "I_PETANE");
			add(UNIFLO1200Registers.V28.N_PETANE, "7.0.98.1.13.255", "N_PETANE");
			add(UNIFLO1200Registers.V28.N_HEXANE, "7.0.98.1.14.255", "N_HEXANE");
			add(UNIFLO1200Registers.V28.N_HEPTANE, "7.0.98.1.15.255", "N_HEPTANE");
			add(UNIFLO1200Registers.V28.N_NOCTANE, "7.0.98.1.16.255", "N_NOCTANE");
			add(UNIFLO1200Registers.V28.N_OCTANE, "7.0.98.1.17.255", "N_OCTANE");
			add(UNIFLO1200Registers.V28.N_DECANE, "7.0.98.1.18.255", "N_DECANE");
			add(UNIFLO1200Registers.V28.HELIUM, "7.0.98.1.19.255", "HELIUM");
			add(UNIFLO1200Registers.V28.ARGON, "7.0.98.1.20.255", "ARGON");

//			add(UNIFLO1200Registers.V28.TURN_OFF_DIAPLAY_AFTER, "0.0.0.0.0.2", "", REG_TURN_OFF_DISP_AFTER);
			
//			add(UNIFLO1200Registers.V28.ENERGY, "0.0.0.0.0.3", REG_ENERGY);
//			add(UNIFLO1200Registers.V28.VOLUME_MEASURED, "0.0.0.0.0.4", REG_VOLUME_MEASURED);
//			add(UNIFLO1200Registers.V28.VOLUME_CONVERTED, "0.0.0.0.0.5", REG_VOLUME_CONVERTED);
//			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED, "0.0.0.0.0.6", REG_VOLUME_CORRECTED);
//			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED_I, "0.0.0.0.0.10", REG_VOLUME_CORRECTED_I);

        } catch (IOException e) {
			e.printStackTrace();
		}
        
    }
    

    private void add(int registerIndex, String obisString, String registerName) throws IOException {
    	this.add(	
    			registerIndex, 
    			fwRegisters.getDataLength(registerIndex), 
    			obisString, fwRegisters.getUnitString(registerIndex), registerName, 
    			fwRegisters.getParser(registerIndex)
    	);
    }

    private void add(int registerIndex, int numberOfWords, String obisString, String unitString, String registerName, String parserString) throws IOException {
    	ObisCode obis = ObisCode.fromString(obisString);
    	Unit unit = Unit.get(unitString);
    	int registerAddress = fwRegisters.getWordAddr(registerIndex);
    	int slaveID = fwRegisters.getSlaveID(registerIndex);
    	
    	UNIFLO1200HoldingRegister hr = new UNIFLO1200HoldingRegister(registerAddress, numberOfWords, obis, unit, registerName, slaveID);
    	hr.setScale(fwRegisters.getDecimals(registerIndex));
    	hr.setParser(parserString);
    	getRegisters().add(hr);
    }
    
    private ObisCode toObis(String obis) {
        return ObisCode.fromString( obis );
    }
    
    protected List getRegisters() {
        return super.getRegisters();
    }

    //------------------------------------------------------------------------------------------------------------
    // Parser classes
    //------------------------------------------------------------------------------------------------------------
    
    protected void initParsers() {
    	UNIFLO1200Parsers up = new UNIFLO1200Parsers(getModBus().gettimeZone());
    	
    	getParserFactory().addBigDecimalParser(up.new BigDecimalParser());
        getParserFactory().addDateParser(up.new TimeParser());

        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT8, up.new UINT8Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT16, up.new UINT16Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT32, up.new UINT32Parser());

        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT160, up.new UINT160Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT320, up.new UINT320Parser());
        
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_GAS_FORM, up.new GasFormulaParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STRING, up.new StringParser());
        
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR1, up.new STR1Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR22, up.new STR22Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR29, up.new STR29Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_REAL32, up.new REAL32Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_INTREAL, up.new INTREALParser());
        
    } 


} 

