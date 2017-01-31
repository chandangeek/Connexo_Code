/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.discover;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {
        setZeroBased(false); // this means that reg2read = reg-1
        getRegisters().add(new HoldingRegister(3590,1,"PNP EIMeter"));
        getRegisters().add(new HoldingRegister(0x186e,2,"PNP Cutler Hammer IQ200").setParser("PNP Cutler Hammer IQ200"));
        getRegisters().add(new HoldingRegister(0x186e,2,"PNP Cutler Hammer IQ230").setParser("PNP Cutler Hammer IQ230"));
        getRegisters().add(new HoldingRegister(257,1,"PNP Socomec Diris A20"));
        getRegisters().add(new HoldingRegister(257,1,"PNP Socomec Diris A40"));
        getRegisters().add(new HoldingRegister(0,1,"PNP GE PQM2"));
    }
    
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                return new BigDecimal(val);
            }
        });
        
        getParserFactory().addParser("value0", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return new BigDecimal( values[0] );
            }
        });        
        
        getParserFactory().addParser("PNP Cutler Hammer IQ200", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return new BigDecimal( values[0]&0x3f );
            }
        });        
        getParserFactory().addParser("PNP Cutler Hammer IQ230", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                return new BigDecimal(val&0x003F0000);
            }
        });        
        
    } //private void initParsers()
    
} // public class RegisterFactory extends AbstractRegisterFactory
