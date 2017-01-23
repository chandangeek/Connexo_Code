/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.cutlerhammer.iq230;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.math.BigDecimal;
import java.util.Calendar;

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
        // options
        setZeroBased(false); // this means that reg2read = reg-1

        // registers
        getRegisters().add(new HoldingRegister(0x1876,2, ObisCode.fromString("1.1.16.8.0.255"), Unit.get("kWh")));
        getRegisters().add(new HoldingRegister(0x182A,2,ObisCode.fromString("1.1.1.7.0.255")));
        getRegisters().add(new HoldingRegister(0x182c,2,ObisCode.fromString("1.1.3.7.0.255")));
        getRegisters().add(new HoldingRegister(0x182e,2,ObisCode.fromString("1.1.9.7.0.255")));
        getRegisters().add(new HoldingRegister(0x1830,2,ObisCode.fromString("1.1.13.7.0.255")).setScale(2));
        getRegisters().add(new HoldingRegister(0x1814,2,ObisCode.fromString("1.1.12.7.0.255")));
        getRegisters().add(new HoldingRegister(0x181c,2,ObisCode.fromString("1.1.112.7.0.255")));
        getRegisters().add(new HoldingRegister(0x180a,2,ObisCode.fromString("1.1.11.7.0.255")));
        getRegisters().add(new HoldingRegister(0x183a,2,ObisCode.fromString("1.1.21.7.0.255")));
        getRegisters().add(new HoldingRegister(0x183c,2,ObisCode.fromString("1.1.41.7.0.255")));
        getRegisters().add(new HoldingRegister(0x183e,2,ObisCode.fromString("1.1.61.7.0.255")));
        getRegisters().add(new HoldingRegister(0x184c,2,ObisCode.fromString("1.1.33.7.0.255")).setScale(2));
        getRegisters().add(new HoldingRegister(0x184e,2,ObisCode.fromString("1.1.53.7.0.255")).setScale(2));
        getRegisters().add(new HoldingRegister(0x1850,2,ObisCode.fromString("1.1.73.7.0.255")).setScale(2));
        getRegisters().add(new HoldingRegister(0x180e,2,ObisCode.fromString("1.1.32.7.0.255")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1810,2,ObisCode.fromString("1.1.52.7.0.255")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1812,2,ObisCode.fromString("1.1.72.7.0.255")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1816,2,ObisCode.fromString("1.1.132.7.0.255"),Unit.get("V")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1818,2,ObisCode.fromString("1.1.152.7.0.255"),Unit.get("V")).setScale(1));
        getRegisters().add(new HoldingRegister(0x181A,2,ObisCode.fromString("1.1.172.7.0.255"),Unit.get("V")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1802,2,ObisCode.fromString("1.1.31.7.0.255")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1804,2,ObisCode.fromString("1.1.51.7.0.255")).setScale(1));
        getRegisters().add(new HoldingRegister(0x1806,2,ObisCode.fromString("1.1.71.7.0.255")).setScale(1));
        //getRegisters().add(new HoldingRegister(,2,ObisCode.fromString("1.1.1.4.0.255")));
        //getRegisters().add(new HoldingRegister(,2,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(0x1858,2,ObisCode.fromString("1.1.1.6.0.255")));


        getRegisters().add(new HoldingRegister(0x186e,2,"productid").setParser("productcode"));


        // mMINT modbus <-> INCOM interface  does allow reading the registerconfiguration registers for the word order by using address 247 or 248.
        // we use a custom property to change the order because this reading of interface configuration registers is not possible within the framework.
        //getRegisters().add(new HoldingRegister(2002,1,"fpwordorder").setParser("fixedpoint"));
    }
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                ParseUtils.signExtend(val, values.length*16);
                BigDecimal bd  =  new BigDecimal(val);
                return bd.movePointLeft(register.getScale());
            }
        });

        getParserFactory().addParser("productcode", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                return new BigDecimal(val);

            }
        });

        getParserFactory().addDateParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
                cal.set(Calendar.MONTH,((values[0]>>8)&0xFF)-1);
                cal.set(Calendar.DAY_OF_MONTH,(values[0]&0xFF));
                cal.set(Calendar.YEAR,((values[1]>>8)&0xFF)+1900);
                cal.set(Calendar.HOUR_OF_DAY,values[1]&0xFF);
                cal.set(Calendar.MINUTE,((values[2]>>8)&0xFF));
                cal.set(Calendar.SECOND,values[2]&0xFF);
                return cal.getTime();
            }
        });
    } //private void initParsers()

} // public class RegisterFactory extends AbstractRegisterFactory
