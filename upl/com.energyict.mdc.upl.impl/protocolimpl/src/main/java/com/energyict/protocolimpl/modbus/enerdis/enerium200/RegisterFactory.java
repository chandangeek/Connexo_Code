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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.F15Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.F39Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.MeterInfoParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.Non_SignedParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.Non_Signed_1000_Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.Non_Signed_1_10000_Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.Non_Signed_1_100_Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.SignedParser;

/**
 *
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
	private static final int MODBUS_MAX_LENGTH		= 0x007D;
	
	public HoldingRegister meterInfo;
	public HoldingRegister writeFunctionReg;
	public HoldingRegister readProfileReg;
	public List enerium200Registers;
	
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {

    	
    	setZeroBased(false);

    	meterInfo = (HoldingRegister) new HoldingRegister(0x0000, 0x001E).setParser(MeterInfoParser.PARSER_NAME);
    	meterInfo.setRegisterFactory(this);
    	
    	writeFunctionReg = new HoldingRegister(0xD000, 0x0000);
    	writeFunctionReg.setRegisterFactory(this);
    	
    	readProfileReg = new HoldingRegister(0x2300, 0x0000);
    	readProfileReg.setRegisterFactory(this);

    	addReg(0x0500, 2, "1.1.32.7.0.255", "V", "Instantaneous V1", 1, Enerium200Register.NON_SIGNED_1_100);
    	addReg(0x0502, 2, "1.1.52.7.0.255", "V", "Instantaneous V2", 1, Enerium200Register.NON_SIGNED_1_100);
    	addReg(0x0504, 2, "1.1.72.7.0.255", "V", "Instantaneous V3", 1, Enerium200Register.NON_SIGNED_1_100);
    	
    	addReg(0x050E, 2, "1.1.31.7.0.255", "A", "Instantaneous I1", 1, Enerium200Register.NON_SIGNED_1_10000);
    	addReg(0x0510, 2, "1.1.51.7.0.255", "A", "Instantaneous I2", 1, Enerium200Register.NON_SIGNED_1_10000);
    	addReg(0x0512, 2, "1.1.71.7.0.255", "A", "Instantaneous I3", 1, Enerium200Register.NON_SIGNED_1_10000);
    	addReg(0x0514, 2, "1.1.91.7.0.255", "A", "Instantaneous In", 1, Enerium200Register.NON_SIGNED_1_10000);
    	
    	addReg(0x0516, 2, "1.1.21.7.0.255", "W", "Instantaneous P1", 1, Enerium200Register.SIGNED);
    	addReg(0x0518, 2, "1.1.41.7.0.255", "W", "Instantaneous P2", 1, Enerium200Register.SIGNED);
    	addReg(0x051A, 2, "1.1.61.7.0.255", "W", "Instantaneous P3", 1, Enerium200Register.SIGNED);
    	addReg(0x051C, 2, "1.1.1.7.0.255",  "W", "Instantaneous Pt", 1, Enerium200Register.SIGNED);
    	
    	addReg(0x051E, 2, "1.1.23.7.0.255", "var", "Instantaneous Q1", 1, Enerium200Register.SIGNED);
    	addReg(0x0520, 2, "1.1.43.7.0.255", "var", "Instantaneous Q2", 1, Enerium200Register.SIGNED);
    	addReg(0x0522, 2, "1.1.63.7.0.255", "var", "Instantaneous Q3", 1, Enerium200Register.SIGNED);
    	addReg(0x0524, 2, "1.1.3.7.0.255",  "var", "Instantaneous Qt", 1, Enerium200Register.SIGNED);

    	addReg(0x0526, 2, "1.1.29.7.0.255", "VA", "Instantaneous S1", 1, Enerium200Register.NON_SIGNED);
    	addReg(0x0528, 2, "1.1.49.7.0.255", "VA", "Instantaneous S2", 1, Enerium200Register.NON_SIGNED);
    	addReg(0x052A, 2, "1.1.69.7.0.255", "VA", "Instantaneous S3", 1, Enerium200Register.NON_SIGNED);
    	addReg(0x052C, 2, "1.1.9.7.0.255",  "VA", "Instantaneous St", 1, Enerium200Register.NON_SIGNED);
    	
    }
    
    private void addReg(int address, int size, String obisCode, String unit, String name, int scaler, int type) {
    	if (enerium200Registers == null) enerium200Registers = new ArrayList(0);
    	Enerium200Register eneriumReg = new Enerium200Register(address, size, ObisCode.fromString(obisCode), Unit.get(unit), name, scaler, type);
    	enerium200Registers.add(eneriumReg);
    	
	}
    
    public AbstractRegister findRegister(ObisCode obisCode) throws IOException {
    	for (int i = 0; i < getEnerium200Registers().size(); i++) {
			Enerium200Register er = (Enerium200Register) getEnerium200Registers().get(i);
			if (er.getObisCode().equals(obisCode)) {
				HoldingRegister hr = new HoldingRegister(er.getAddress(), er.getSize(), er.getObisCode(), er.getUnit(), er.getName());
				hr.setRegisterFactory(this);
				hr.setScale(er.getScaler());
				switch (er.getType()) {
					case Enerium200Register.F15					: hr.setParser(F15Parser.PARSER_NAME); break;
					case Enerium200Register.F39					: hr.setParser(F39Parser.PARSER_NAME); break;
					case Enerium200Register.NON_SIGNED			: hr.setParser(Non_SignedParser.PARSER_NAME); break;
					case Enerium200Register.NON_SIGNED_1000		: hr.setParser(Non_Signed_1000_Parser.PARSER_NAME); break;
					case Enerium200Register.NON_SIGNED_1_100	: hr.setParser(Non_Signed_1_100_Parser.PARSER_NAME); break;
					case Enerium200Register.NON_SIGNED_1_10000	: hr.setParser(Non_Signed_1_10000_Parser.PARSER_NAME); break;
					case Enerium200Register.SIGNED				: hr.setParser(SignedParser.PARSER_NAME); break;
				}
				return hr;
			}
		}
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
    private TimeZone getTimeZone() {
    	return getModBus().gettimeZone();
    }
    
    private List getEnerium200Registers() {
		return enerium200Registers;
	}
    
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                BigDecimal bd = new BigDecimal(""+values[0]);
                return bd;
//                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });
        
        getParserFactory().addParser("EnergyParser",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=(values[1]<<16)+values[0];
                BigDecimal bd = new BigDecimal(""+val);
                return bd;
//                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
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

        getParserFactory().addParser(MeterInfoParser.PARSER_NAME, new MeterInfoParser(getTimeZone()));
        getParserFactory().addParser(Non_Signed_1_100_Parser.PARSER_NAME, new Non_Signed_1_100_Parser());
        getParserFactory().addParser(Non_Signed_1_10000_Parser.PARSER_NAME, new Non_Signed_1_10000_Parser());
        getParserFactory().addParser(SignedParser.PARSER_NAME, new SignedParser());
        getParserFactory().addParser(Non_SignedParser.PARSER_NAME, new Non_SignedParser());
        
    } //private void initParsers()
    
} // public class RegisterFactory extends AbstractRegisterFactory
