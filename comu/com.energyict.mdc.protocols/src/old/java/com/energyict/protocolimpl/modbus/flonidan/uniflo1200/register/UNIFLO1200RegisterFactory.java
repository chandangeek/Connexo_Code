/**
 * RegisterFactory.java
 *
 * Created on 4-dec-2008, 15:00:50 by jme
 *
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jme
 */
public class UNIFLO1200RegisterFactory extends AbstractRegisterFactory {

	private static final int DEBUG 						= 0;
	private UNIFLO1200Registers fwRegisters 			= null;

	public static final String REG_TIME 				= "Time";						// meter time and date
    public static final String REG_DEVICE_TYPE			= "Device type";				// device type string
    public static final String REG_LOGIN				= "Login";						// register to write password to during login
    public static final String REG_ACTUAL_SECLEVEL		= "ActualSecurityLevel";		// the actual security level after logon
    public static final String REG_SERIAL_NUMBER 		= "SerialNumber";				// the serial number of the device

    public static final String REG_INTERVAL_LOG_REG 	= "Interval_Log_REG";
    public static final String REG_INTERVAL_LOG_INDEX 	= "Interval_Log_INDEX";
    public static final String REG_INTERVAL_LOG_SIZE 	= "Interval_Log_SIZE";
    public static final String REG_INTERVAL_LOG_WIDTH 	= "Interval_Log_WIDTH";
    public static final String REG_INTERVAL_LOG_EEPROM	= "Interval_Log_EEPROM";

    public static final String REG_MONTH_LOG_REG 		= "Month_Log_REG";
    public static final String REG_MONTH_LOG_INDEX 		= "Month_Log_INDEX";
    public static final String REG_MONTH_LOG_SIZE 		= "Month_Log_SIZE";
    public static final String REG_MONTH_LOG_WIDTH 		= "Month_Log_WIDTH";

    public static final String REG_DAILY_LOG_REG 		= "Daily_Log_REG";
    public static final String REG_DAILY_LOG_INDEX 		= "Daily_Log_INDEX";
    public static final String REG_DAILY_LOG_SIZE 		= "Daily_Log_SIZE";
    public static final String REG_DAILY_LOG_WIDTH 		= "Daily_Log_WIDTH";

    public static final String REG_ALARM_LOG_INDEX 		= "Alarm_Log_INDEX";
    public static final String REG_INTERVAL				= "Interval_Time";

    /** Creates a new instance of RegisterFactory */
    public UNIFLO1200RegisterFactory(Modbus modBus) {
        super(modBus);
        init();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		Unit returnUnit = null;
		String returnText = null;
		Date returnEventTime = null;
		Date returnFromTime = new Date();
		Date returnToTime = new Date();
		Date returnReadTime = new Date();
		Quantity returnQuantity = null;
		int returnRtuRegisterID = 0;
		int returnDecimals = 0;

		try {

			UNIFLO1200HoldingRegister hr = (UNIFLO1200HoldingRegister) findRegister(obisCode);

        	returnUnit = hr.getUnit();
        	returnDecimals = hr.getScale();

        	returnRtuRegisterID = hr.getReg();
        	Object result = hr.value();
        	Class rc = result.getClass();

        	if (DEBUG >= 1) System.out.println("Result class type: " + result.getClass().getName());

        	if (rc == String.class)	{
        		returnText = (String)result;
        	}
        	else if (rc == Date.class) {
        		returnEventTime = (Date)result;
        		returnQuantity = new Quantity(new Long(returnEventTime.getTime()), returnUnit);
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
        		returnQuantity = new Quantity((Integer)result, returnUnit);
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

	protected void init() {
        try {
        	this.fwRegisters  = new UNIFLO1200Registers(UNIFLO1200Registers.UNIFLO1200_FW_28);

        	setZeroBased(false); // this means that reg2read = reg-1

        	// unmapped registers, only for internal use
        	add(UNIFLO1200Registers.V28.WR_SET_PASS, null, REG_LOGIN);
        	add(UNIFLO1200Registers.V28.ACTUAL_SECLEVEL, null, REG_ACTUAL_SECLEVEL);

        	add(UNIFLO1200Registers.V28.INTERVAL_LOG_REG, null, REG_INTERVAL_LOG_REG);
        	add(UNIFLO1200Registers.V28.INTERVAL_LOG_INDEX, null, REG_INTERVAL_LOG_INDEX);
        	add(UNIFLO1200Registers.V28.INTERVAL_LOG_SIZE, null, REG_INTERVAL_LOG_SIZE);
        	add(UNIFLO1200Registers.V28.INTERVAL_LOG_WIDTH, null, REG_INTERVAL_LOG_WIDTH);
        	add(UNIFLO1200Registers.V28.INTERVAL_LOG_EEPROM, null, REG_INTERVAL_LOG_EEPROM);

        	add(UNIFLO1200Registers.V28.DAILY_LOG_REG, null, REG_DAILY_LOG_REG);
        	add(UNIFLO1200Registers.V28.DAILY_LOG_INDEX, null, REG_DAILY_LOG_INDEX);
        	add(UNIFLO1200Registers.V28.DAILY_LOG_SIZE, null, REG_DAILY_LOG_SIZE);
        	add(UNIFLO1200Registers.V28.DAILY_LOG_WIDTH, null, REG_DAILY_LOG_WIDTH);

        	add(UNIFLO1200Registers.V28.MONTH_LOG_REG, null, REG_MONTH_LOG_REG);
        	add(UNIFLO1200Registers.V28.MONTH_LOG_INDEX, null, REG_MONTH_LOG_INDEX);
        	add(UNIFLO1200Registers.V28.MONTH_LOG_SIZE, null, REG_MONTH_LOG_SIZE);
        	add(UNIFLO1200Registers.V28.MONTH_LOG_WIDTH, null, REG_MONTH_LOG_WIDTH);

        	add(UNIFLO1200Registers.V28.ALARM_LOG_INDEX, null, REG_ALARM_LOG_INDEX);

        	add(UNIFLO1200Registers.V28.LOG_INTERVAL, fwRegisters.getDataLength(UNIFLO1200Registers.V28.LOG_INTERVAL),
					"1.1.1.1.1.1", fwRegisters.getUnitString(UNIFLO1200Registers.V28.LOG_INTERVAL),
					REG_INTERVAL, UNIFLO1200Parsers.PARSER_INTERVAL);

        	// registers mapped to obiscode
        	add(UNIFLO1200Registers.V28.TIME, "7.1.0.1.2.255", REG_TIME);
        	add(UNIFLO1200Registers.V28.FW_VERSION_TYPE, "7.1.0.2.1.255", REG_DEVICE_TYPE);

        	add(UNIFLO1200Registers.V28.SERIAL_NUMBER, "7.1.96.50.1.255", REG_SERIAL_NUMBER);

        	add(UNIFLO1200Registers.V28.PRESS_SERIAL, "7.1.0.2.11.255");
        	add(UNIFLO1200Registers.V28.PRESS_SENSOR, "7.1.0.2.11.0");
        	add(UNIFLO1200Registers.V28.TEMP_SENSOR, "7.1.0.2.12.255");
        	add(UNIFLO1200Registers.V28.FLOW_SENSOR, "7.1.0.2.13.255");
        	add(UNIFLO1200Registers.V28.ZA, "7.1.53.2.0.255");
			add(UNIFLO1200Registers.V28.ZB, "7.1.53.11.0.255");
			add(UNIFLO1200Registers.V28.BATTERY_REMAINING, "0.1.96.6.0.255");

			add(UNIFLO1200Registers.V28.TEMPERATURE, "7.1.41.0.0.255");
			add(UNIFLO1200Registers.V28.FALLBACK_TEMP, "7.1.41.3.0.255");

			add(UNIFLO1200Registers.V28.PRESSURE, "7.1.42.0.0.255");
			add(UNIFLO1200Registers.V28.FALLBACK_PRESS, "7.1.42.3.0.255");

			add(UNIFLO1200Registers.V28.CONVERSION_FACTOR, "7.1.52.0.0.255");
			add(UNIFLO1200Registers.V28.CORRECTION_FACTOR, "7.1.51.0.0.255");

			add(
				UNIFLO1200Registers.V28.GAS_CALC_FORMULA,
				fwRegisters.getDataLength(UNIFLO1200Registers.V28.GAS_CALC_FORMULA),
				"7.1.53.12.0.255",
				fwRegisters.getUnitString(UNIFLO1200Registers.V28.GAS_CALC_FORMULA),
				fwRegisters.getAddressName(UNIFLO1200Registers.V28.GAS_CALC_FORMULA),
				UNIFLO1200Parsers.PARSER_GAS_FORM
			);

			add(UNIFLO1200Registers.V28.FLOW_MEASURED, "7.1.43.0.0.255");
			add(UNIFLO1200Registers.V28.FLOW_CORRECTED, "7.1.43.1.0.255");
			add(UNIFLO1200Registers.V28.FLOW_CONVERTED, "7.1.43.2.0.255");

			add(UNIFLO1200Registers.V28.DENSITY, "7.1.45.0.0.255");

			add(UNIFLO1200Registers.V28.VOLUME_MEASURED, "7.1.0.3.0.255");
			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED, "7.1.96.50.0.255");
			add(UNIFLO1200Registers.V28.VOLUME_CONVERTED, "7.1.0.3.3.255");

			add(UNIFLO1200Registers.V28.TEMP_LOW_LIMIT, "7.1.0.5.11.255");
			add(UNIFLO1200Registers.V28.TEMP_HIGH_LIMIT, "7.1.0.5.12.255");
			add(UNIFLO1200Registers.V28.PRESS_LOW_LIMIT, "7.1.0.5.13.255");
			add(UNIFLO1200Registers.V28.PRESS_HIGH_LIMIT, "7.1.0.5.14.255");

			add(UNIFLO1200Registers.V28.METHANE, "7.1.98.1.0.255");
			add(UNIFLO1200Registers.V28.NITROGEN, "7.1.98.1.1.255");
			add(UNIFLO1200Registers.V28.CO2, "7.1.98.1.2.255");
			add(UNIFLO1200Registers.V28.ETHANE, "7.1.98.1.3.255");
			add(UNIFLO1200Registers.V28.PROPANE, "7.1.98.1.4.255");
			add(UNIFLO1200Registers.V28.WATER, "7.1.98.1.5.255");
			add(UNIFLO1200Registers.V28.HYDRG_SUL, "7.1.98.1.6.255");
			add(UNIFLO1200Registers.V28.HYDROGEN, "7.1.98.1.7.255");
			add(UNIFLO1200Registers.V28.CARBON_MONOXIDE, "7.1.98.1.8.255");
			add(UNIFLO1200Registers.V28.OXYGEN, "7.1.98.1.9.255");
			add(UNIFLO1200Registers.V28.I_BUTANE, "7.1.98.1.10.255");
			add(UNIFLO1200Registers.V28.N_BUTANE, "7.1.98.1.11.255");
			add(UNIFLO1200Registers.V28.I_PETANE, "7.1.98.1.12.255");
			add(UNIFLO1200Registers.V28.N_PETANE, "7.1.98.1.13.255");
			add(UNIFLO1200Registers.V28.N_HEXANE, "7.1.98.1.14.255");
			add(UNIFLO1200Registers.V28.N_HEPTANE, "7.1.98.1.15.255");
			add(UNIFLO1200Registers.V28.N_NOCTANE, "7.1.98.1.16.255");
			add(UNIFLO1200Registers.V28.N_OCTANE, "7.1.98.1.17.255");
			add(UNIFLO1200Registers.V28.N_DECANE, "7.1.98.1.18.255");
			add(UNIFLO1200Registers.V28.HELIUM, "7.1.98.1.19.255");
			add(UNIFLO1200Registers.V28.ARGON, "7.1.98.1.20.255");


			// The following registers were removed from the obismapping because they contain garbage in the meter.
			// They are used as temporary registers for the average calculations

//			add(UNIFLO1200Registers.V28.INT_LOG_POWER_AVG, "7.0.0.8.3.1");
//			add(UNIFLO1200Registers.V28.INT_LOG_TEMP_AVG, "7.0.0.8.3.2");
//			add(UNIFLO1200Registers.V28.INT_LOG_PRESS_AVG, "7.0.0.8.3.3");
//			add(UNIFLO1200Registers.V28.INT_LOG_CORR_FLOW_AVG, "7.0.0.8.3.4");
//			add(UNIFLO1200Registers.V28.INT_LOG_CONV_FLOW_AVG, "7.0.0.8.3.5");
//
//			add(UNIFLO1200Registers.V28.DAILY_LOG_POWER_AVG, "7.0.0.8.4.1");
//			add(UNIFLO1200Registers.V28.DAILY_LOG_TEMP_AVG, "7.0.0.8.4.2");
//			add(UNIFLO1200Registers.V28.DAILY_LOG_PRESS_AVG, "7.0.0.8.4.3");
//			add(UNIFLO1200Registers.V28.DAILY_LOG_CORR_FLOW_AVG, "7.0.0.8.4.4");
//			add(UNIFLO1200Registers.V28.DAILY_LOG_CONV_FLOW_AVG, "7.0.0.8.4.5");



			if (DEBUG >= 1) {
				for (int i = 0; i < 255; i++) {
					if (i != 244) add(i, new ObisCode(7, 128, 0, 0, 0, i).toString());
				}
			}

//			add(UNIFLO1200Registers.V28.TURN_OFF_DIAPLAY_AFTER, "0.0.0.0.0.2", "", REG_TURN_OFF_DISP_AFTER);

//			add(UNIFLO1200Registers.V28.ENERGY, "0.0.0.0.0.3", REG_ENERGY);
//			add(UNIFLO1200Registers.V28.VOLUME_MEASURED, "0.0.0.0.0.4", REG_VOLUME_MEASURED);
//			add(UNIFLO1200Registers.V28.VOLUME_CONVERTED, "0.0.0.0.0.5", REG_VOLUME_CONVERTED);
//			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED, "0.0.0.0.0.6", REG_VOLUME_CORRECTED);
//			add(UNIFLO1200Registers.V28.VOLUME_CORRECTED_I, "0.0.0.0.0.10", REG_VOLUME_CORRECTED_I);

        } catch (IOException e) {
			e.printStackTrace();
		}

        for (Iterator iterator = getRegisters().iterator(); iterator.hasNext();) {
			UNIFLO1200HoldingRegister reg = (UNIFLO1200HoldingRegister) iterator.next();
			reg.fixParser();
		}
    }


	private void add(int registerIndex, String obisString) throws IOException {
		this.add(
				registerIndex,
				obisString,
				fwRegisters.getAddressName(registerIndex)
		);
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
    	UNIFLO1200HoldingRegister hr;
    	Unit unit = Unit.get(unitString);
    	int registerAddress = fwRegisters.getWordAddr(registerIndex);
    	int slaveID = fwRegisters.getSlaveID(registerIndex);

    	if (obisString == null) {
        	hr = new UNIFLO1200HoldingRegister(registerAddress, numberOfWords, registerName, slaveID, getModBus().getModbusConnection());
    	} else {
        	ObisCode obis = ObisCode.fromString(obisString);
        	hr = new UNIFLO1200HoldingRegister(registerAddress, numberOfWords, obis, unit, registerName, slaveID, getModBus().getModbusConnection());
    	}

    	hr.setScale(fwRegisters.getDecimals(registerIndex));
    	hr.setParser(parserString);
    	hr.setOddAddress(fwRegisters.isOddAddr(registerIndex));
    	getRegisters().add(hr);
    }

    private ObisCode toObis(String obis) {
        return ObisCode.fromString( obis );
    }

    public List getRegisters() {
        return super.getRegisters();
    }

    public UNIFLO1200Registers getFwRegisters() {
		return fwRegisters;
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
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_INTERVAL, up.new IntervalParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STRING, up.new StringParser());

        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR1, up.new STR1Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR22, up.new STR22Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR29, up.new STR29Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_REAL32, up.new REAL32Parser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_INTREAL, up.new INTREALParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_DATABLOCK, up.new DATABLOCKParser());

        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_UINT8_SWP, up.new UINT8SwappedParser());
        getParserFactory().addParser(UNIFLO1200Parsers.PARSER_STR1_SWP, up.new STR1SwappedParser());

    }

}

