/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.MeterInfoParser;

/**
 *
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
	private static final int MODBUS_MAX_LENGTH		= 0x007D;
	private static final String METERINFO_PARSER	= "METERINFO_PARSER";
	
	public HoldingRegister meterInfo;
	public HoldingRegister writeFunctionReg;
	public HoldingRegister readProfileReg;
	
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {

    	setZeroBased(false);

    	meterInfo = (HoldingRegister) new HoldingRegister(0x0000, 0x001E).setParser(METERINFO_PARSER);
    	meterInfo.setRegisterFactory(this);
    	
    	writeFunctionReg = new HoldingRegister(0xD000, 0x0000);
    	writeFunctionReg.setRegisterFactory(this);
    	
    	readProfileReg = new HoldingRegister(0x2300, 0x0000);
    	readProfileReg.setRegisterFactory(this);

    	getRegisters().add(new HoldingRegister(0x0500, 2, ObisCode.fromString("1.1.1.1.1.1"), Unit.get(""), ""));
    	
    }
    
    private TimeZone getTimeZone() {
    	return getModBus().gettimeZone();
    }
    
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                BigDecimal bd = new BigDecimal(""+values[0]);
                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });
        
        getParserFactory().addParser("EnergyParser",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=(values[1]<<16)+values[0];
                BigDecimal bd = new BigDecimal(""+val);
                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });
        
        getParserFactory().addParser("FloatingPoint",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val=(values[0]<<16)+values[1];
                return new BigDecimal(""+Float.intBitsToFloat(val));
            }
        });
        
        getParserFactory().addParser("emptyParser",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return "emptyParser";
            }
        });

        getParserFactory().addParser(METERINFO_PARSER, new MeterInfoParser(getTimeZone()));
        
    } //private void initParsers()
    
} // public class RegisterFactory extends AbstractRegisterFactory
