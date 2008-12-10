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
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.*;

/**
 *
 * @author jme
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
	private BigDecimal eScale;
    private BigDecimal ampsScale;
    private BigDecimal phaseVoltsScale;
    private BigDecimal lineVoltsScale;
    private BigDecimal powerScale;
    
    private Map scaleMap;
	private UNIFLO1200Registers fwRegisters = null;
    
    public static final String REG_ZA					= "Za"; 						// Actual supercompressibilitifactor
    public static final String REG_ZB					= "Zb"; 						// Base supercompressibilitifactor

	public static final String REG_SLAVE_ADDRESS 		= "SlaveAddress";				// Slave address
    public static final String REG_TIME 				= "Time";						// meter time and date
    public static final String REG_DEVICE_TYPE			= "TDeviceId";					// device type string 
    public static final String REG_BATTERY_REMAINING	= "BatteryRemaining"; 			// remaining battery time in days
    public static final String REG_SECURITY_LEVEL		= "ActualSecurityLevel"; 		// remaining battery time in days
    public static final String REG_CONVERSION_FACTOR	= "ConversionFactor"; 			// conversion factor from last measurement
    
    public static final String REG_VOLUME_MEASURED		= "VolumeMeasured"; 			// Volume measured
    public static final String REG_VOLUME_CONVERTED		= "VolumeConverted"; 			// Volume converted
    public static final String REG_VOLUME_CORRECTED		= "VolumeCorrected"; 			// Volume corrected
    public static final String REG_PULSE_OUT_REG_1		= "PulseOutReg_1";				// Pulse out reg 1
    public static final String REG_PULSE_OUT_REG_2		= "PulseOutReg_2";				// Pulse out reg 2

    public static final String REG_ENERGY				= "Energy"; 					// Energy measured
    public static final String REG_TURN_OFF_DISP_AFTER	= "TurnOffDisplayAfter";		// Turn off display after delay in seconds)
    

    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {
        try {
        	this.fwRegisters  = new UNIFLO1200Registers(UNIFLO1200Registers.UNIFLO1200_FW_28);
    	
        	setZeroBased(false); // this means that reg2read = reg-1
        
        	add(UNIFLO1200Registers.V28.TIME, "0.0.0.0.0.0", REG_TIME);
			add(UNIFLO1200Registers.V28.VER_TYPE, "0.0.0.0.0.1", REG_DEVICE_TYPE);
			
			add(UNIFLO1200Registers.V28.TURN_OFF_DIAPLAY_AFTER, "0.0.0.0.0.2", "", REG_TURN_OFF_DISP_AFTER);
			
			add(UNIFLO1200Registers.V28.ENERGY, "0.0.0.0.0.3", REG_ENERGY);
			add(UNIFLO1200Registers.V28.VOLUME_MEASURED, "0.0.0.0.0.4", REG_VOLUME_MEASURED);
			add(UNIFLO1200Registers.V28.VOLUME_CONVERTED, "0.0.0.0.0.5", REG_VOLUME_CONVERTED);
			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED, "0.0.0.0.0.6", REG_VOLUME_CORRECTED);
			add(UNIFLO1200Registers.V28.ZA, "0.0.0.0.0.7", REG_ZA);
			add(UNIFLO1200Registers.V28.ZB, "0.0.0.0.0.8", REG_ZB);
			add(UNIFLO1200Registers.V28.BATTERY_REMAINING, "0.0.0.0.0.9", REG_BATTERY_REMAINING);
		
        } catch (IOException e) {
			e.printStackTrace();
		}
        
    }
    
    private void add(int registerIndex, String obisString, String unitString, String registerName) throws IOException {
    	this.add(	
    			fwRegisters.getWordAddr(registerIndex), 
    			fwRegisters.getDataLength(registerIndex), 
    			obisString, unitString, registerName, 
    			fwRegisters.getParser(registerIndex)
    	);
    }

    private void add(int registerIndex, String obisString, String registerName) throws IOException {
    	this.add(	
    			fwRegisters.getWordAddr(registerIndex), 
    			fwRegisters.getDataLength(registerIndex), 
    			obisString, fwRegisters.getUnitString(registerIndex), registerName, 
    			fwRegisters.getParser(registerIndex)
    	);
    }

    private void add(int registerIndex, String registerName) throws IOException {
    	this.add(
    			fwRegisters.getWordAddr(registerIndex), 
    			fwRegisters.getDataLength(registerIndex), 
    			registerName, 
    			fwRegisters.getParser(registerIndex)
    	);
    }
    
    private void add(int registerAddress, int numberOfWords, String obisString, String unitString, String registerName, String parserString) {
    	ObisCode obis = ObisCode.fromString(obisString);
    	Unit unit = Unit.get(unitString);
    	HoldingRegister hr = new HoldingRegister(registerAddress, numberOfWords, obis, unit, registerName);
    	hr.setParser(parserString);
    	getRegisters().add(hr);
    }
    
    private void add(int registerAddress, int numberOfWords, String registerName, String parserString) {
    	HoldingRegister hr = new HoldingRegister(registerAddress, numberOfWords, registerName);
    	hr.setParser(parserString);
    	getRegisters().add(hr);
    }

    private ObisCode toObis(String obis) {
        return ObisCode.fromString( obis );
    }
    
    //------------------------------------------------------------------------------------------------------------
    // Parser classes
    //------------------------------------------------------------------------------------------------------------
    
    protected void initParsers() {
    	UNIFLO1200Parsers up = new UNIFLO1200Parsers(getModBus().gettimeZone());
    	
    	getParserFactory().addBigDecimalParser(up.new BigDecimalParser());
        getParserFactory().addDateParser(up.new TimeParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STRING, up.new StringParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR22, up.new STR22Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR29, up.new STR29Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT8, up.new UINT8Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT16, up.new UINT16Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_REAL32, up.new REAL32Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_INTREAL, up.new INTREALParser());
        
    } 


} 

