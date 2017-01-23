/*
 * RegisterMapping.java
 *
 * Created on 15 september 2006, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.itron.fulcrum.Fulcrum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Koen
 */
public class RegisterFactory {

    List registers=null;
    static public final int MAX_NR_OF_PEAKS=5;
    static public final int MAX_NR_OF_RATES=4;
    static public final int MAX_NR_OF_SELFREADS=4;


    static public int[] obisCFields=new int[]{ObisCode.CODE_C_ACTIVE_IMPORT,
            ObisCode.CODE_C_REACTIVE_Q1,
            ObisCode.CODE_C_APPARENT,
            ObisCode.CODE_C_REACTIVE_IMPORT,
            ObisCode.CODE_C_REACTIVE_Q4,
            ObisCodeExtensions.OBISCODE_C_VOLTSQUARE,
            ObisCode.CODE_C_CURRENTANYPHASE};

            Fulcrum fulcrum;

            /** Creates a new instance of RegisterMapping */
            public RegisterFactory(Fulcrum fulcrum) {
                this.fulcrum=fulcrum;


            }

            public void init() throws IOException {
                buildRegisters();
                createCoincidentRegisters();
                createSelfreadRegisters();
            }

            private void createCoincidentRegisters() throws IOException {
                // create the coincident registers, use the correct C field for the coincident registers
                ObisCode obisCode=null;

                // coincident when wattHour max
                try {
                    obisCode = findRegisterByAddress(fulcrum.getBasePagesFactory().getCoincidentDemandSetupTableBasePage().getAddressForkWPeak()).getObisCode();
                    registers.add(new Register(0x28DD+98, ObisCode.fromString("1.1."+obisCode.getC()+"."+ObisCodeExtensions.OBISCODE_D_COINCIDENT+".0.255"),"Coincident register for max wattHour")); // coincident register value
                } catch(IOException e) {
                    // absorb
                }
                // coincident when lag varHour max
                try {
                    obisCode = findRegisterByAddress(fulcrum.getBasePagesFactory().getCoincidentDemandSetupTableBasePage().getAddressForkvarPeak()).getObisCode();
                    registers.add(new Register(0x296B+98, ObisCode.fromString("1.1."+obisCode.getC()+"."+ObisCodeExtensions.OBISCODE_D_COINCIDENT+".0.255"),"Coincident register for varHour lag max")); // coincident register value
                } catch(IOException e) {
                    // absorb
                }
                // coincident when VAHour max
                try {
                    obisCode = findRegisterByAddress(fulcrum.getBasePagesFactory().getCoincidentDemandSetupTableBasePage().getAddressForkVAPeak()).getObisCode();
                    registers.add(new Register(0x29F9+98, ObisCode.fromString("1.1."+obisCode.getC()+"."+ObisCodeExtensions.OBISCODE_D_COINCIDENT+".0.255"),"Coincident register for VAHour max")); // coincident register value
                } catch(IOException e) {
                    // absorb
                }
                // coincident when PF minimum
                try {
                    obisCode = findRegisterByAddress(fulcrum.getBasePagesFactory().getCoincidentDemandSetupTableBasePage().getAddressForMinPFPeak()).getObisCode();
                    registers.add(new Register(0x2A87+65, ObisCode.fromString("1.1."+obisCode.getC()+"."+ObisCodeExtensions.OBISCODE_D_COINCIDENT+".0.255"),"Coincident register for PF minimum")); // average power factor
                } catch(IOException e) {
                    // absorb
                }
            }

            private void createSelfreadRegisters() throws IOException {
                // self read registers
                int selfReadRegisterOffset=0;
                List selfReadRegisters = fulcrum.getBasePagesFactory().getRegisterAddressTable().getSelfReadRegisterAddress();
                Iterator it = selfReadRegisters.iterator();
                while(it.hasNext()) {
                    SelfReadRegisterAddress sra = (SelfReadRegisterAddress)it.next();
                    try {
                        Register register = findRegisterByAddress(sra.getRegisterAddress());
                        // The 4 selfread register sets (0..3) are in a circular configuration. vzSelfReadIndex is the most recent set. this has to be obis billingSet 0
                        int vzSelfReadIndex = (MAX_NR_OF_SELFREADS + (fulcrum.getBasePagesFactory().getSelfreadIndexBasePage().getIndex()-1)) % MAX_NR_OF_SELFREADS;
                        for (int obisBillingSet=0;obisBillingSet<MAX_NR_OF_SELFREADS;obisBillingSet++) {
                            int address = 0x34AD + (vzSelfReadIndex%MAX_NR_OF_SELFREADS)*414 + 6 + selfReadRegisterOffset;
                            vzSelfReadIndex++;
                            registers.add(new Register(register, address, obisBillingSet, selfReadRegisterOffset));
                        }
                    }
                    catch(IOException e) {
                        // absorb
                    }
                    selfReadRegisterOffset+=sra.getRegisterLength();
                }
            }


            private void buildRegisters() {
                registers=new ArrayList();
                addEnergyRegisters(0x2819, ObisCode.CODE_C_ACTIVE_IMPORT);
                addEnergyRegisters(0x2835, ObisCode.CODE_C_REACTIVE_Q1);
                addEnergyRegisters(0x2851, ObisCode.CODE_C_APPARENT);
                addEnergyRegisters(0x286D, ObisCode.CODE_C_REACTIVE_IMPORT);
                addEnergyRegisters(0x2889, ObisCode.CODE_C_REACTIVE_Q4);
                addEnergyRegisters(0x28A5, ObisCodeExtensions.OBISCODE_C_VOLTSQUARE);
                addEnergyRegisters(0x28C1, ObisCode.CODE_C_CURRENTANYPHASE);

                addDemandRegisters(0x28DD, ObisCode.CODE_C_ACTIVE_IMPORT);
                addDemandRegisters(0x296B, ObisCode.CODE_C_REACTIVE_Q1);
                addDemandRegisters(0x29F9, ObisCode.CODE_C_APPARENT);

                addPowerFactorRegisters(0x2A87, ObisCode.CODE_C_POWERFACTOR);
                // page 50 self read registers are independend of the max demand reset.
                registers.add(new Register(0x2A87+57, ObisCode.fromString("1.1."+ObisCode.CODE_C_ACTIVE_IMPORT+"."+ObisCode.CODE_D_TIME_INTEGRAL1+"."+ObisCodeExtensions.OBISCODE_E_VALUEATDEMANDRESET+".0"), "Wh value at demand reset")); // Watthour reading at demand reset
                registers.add(new Register(0x2A87+61, ObisCode.fromString("1.1."+ObisCode.CODE_C_APPARENT+"."+ObisCode.CODE_D_TIME_INTEGRAL1+"."+ObisCodeExtensions.OBISCODE_E_VALUEATDEMANDRESET+".0"), "VAh value at demand reset")); // VAhour reading at demand reset

                // total energy registers
                // page 33 of fulcrum base page document. Are these the same registers as the energy registers totals?
                registers.add(new Register(0x49CB, ObisCode.fromString("1.2."+ObisCode.CODE_C_ACTIVE_IMPORT+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x49DF, ObisCode.fromString("1.2."+ObisCode.CODE_C_REACTIVE_Q1+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x49F3, ObisCode.fromString("1.2."+ObisCode.CODE_C_APPARENT+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x4A07, ObisCode.fromString("1.2."+ObisCode.CODE_C_REACTIVE_IMPORT+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x4A1B, ObisCode.fromString("1.2."+ObisCode.CODE_C_REACTIVE_Q4+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x4A2F, ObisCode.fromString("1.2."+ObisCodeExtensions.OBISCODE_C_VOLTSQUARE+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255"), "volt square hour"));
                registers.add(new Register(0x4A43, ObisCode.fromString("1.2."+ObisCode.CODE_C_CURRENTANYPHASE+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));

                // KYZ registers
                registers.add(new Register(0x25C2+1, Register.FORMAT_INTEGER_16BIT, ObisCode.fromString("1.1."+ObisCode.CODE_C_UNITLESS+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x25C2+4, Register.FORMAT_INTEGER_16BIT, ObisCode.fromString("1.2."+ObisCode.CODE_C_UNITLESS+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(0x25C2+7, Register.FORMAT_INTEGER_16BIT, ObisCode.fromString("1.3."+ObisCode.CODE_C_UNITLESS+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));


            }


            private void addEnergyRegisters(int address,int obisCField) {
                registers.add(new Register(address, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255")));
                registers.add(new Register(address+4, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1.255")));
                registers.add(new Register(address+8, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".2.255")));
                registers.add(new Register(address+12, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".3.255")));
                registers.add(new Register(address+16, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".4.255")));

            }

            private void addDemandRegisters(int address,int obisCField) {
                registers.add(new Register(address, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_INSTANTANEOUS+".0.255"))); // instantaneous demand
                registers.add(new Register(address+4, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_LAST_AVERAGE+".0.255"))); // total previous interval demand
                registers.add(new Register(address+8, Register.FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0.255"))); // total maximum demand
                for (int i=0;i<MAX_NR_OF_RATES;i++) { // maximum demand rates
                    registers.add(new Register(address+17+i*9, Register.FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+"."+(i+1)+".255")));
                }
                for (int i=0;i<MAX_NR_OF_PEAKS;i++) { // maximum demand peaks
                    registers.add(new Register(address+53+i*9, Register.FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP, ObisCode.fromString("1.1."+obisCField+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+i)+".0.255"), "Max demand peak "+(i+1)));
                }


                registers.add(new Register(address+102, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0.255"))); // total cumulative maximum demand
                for (int i=0;i<MAX_NR_OF_RATES;i++) { // cumulative maximum demand rates
                    registers.add(new Register(address+106+i*4, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+"."+(i+1)+".255")));
                }
                registers.add(new Register(address+122, ObisCode.fromString("1.1."+obisCField+"."+ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND+".0.255"),"Continuous cumulative demand register value")); // total continuous cumulative maximum demand
                for (int i=0;i<MAX_NR_OF_RATES;i++) { // continuous cumulative maximum demand rates
                    registers.add(new Register(address+126+i*4, ObisCode.fromString("1.1."+obisCField+"."+ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND+"."+(i+1)+".255"),"Continuous cumulative demand register value"));
                }
            }

            private void addPowerFactorRegisters(int address,int obisCField) {
                registers.add(new Register(address, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_INSTANTANEOUS+".0.255"))); // instantaneous power factor
                registers.add(new Register(address+4, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_CURRENT_AVERAGE5+".0.255"))); // average power factor
                registers.add(new Register(address+8, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_LAST_AVERAGE+".0.255"))); // previous power factor
                registers.add(new Register(address+12, Register.FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_MINIMUM+".0.255"))); // minimum power factor
                for (int i=0;i<MAX_NR_OF_RATES;i++) { // minimum power factor rates
                    registers.add(new Register(address+21+i*9, Register.FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP, ObisCode.fromString("1.1."+obisCField+"."+ObisCode.CODE_D_MINIMUM+"."+(i+1)+".255")));
                }
            }

            public Register findRegisterByAddress(int address) throws IOException {
                Iterator it = registers.iterator();
                while(it.hasNext()) {
                    Register r = (Register)it.next();
                    if (r.getAddress()==address)
                        return r;
                }
                throw new IOException("Register with address 0x"+Integer.toHexString(address)+" does not exist!");
            }

            public Register findRegisterByObisCode(ObisCode obisCode) throws IOException {
                Iterator it = registers.iterator();
                while(it.hasNext()) {
                    Register r = (Register)it.next();
                    if (r.getObisCode().equals(obisCode))
                        return r;
                }
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            }

            public String getRegisterInfo() {
                StringBuffer strBuff = new StringBuffer();
                Iterator it = registers.iterator();
                while(it.hasNext()) {
                    Register r = (Register)it.next();
                    strBuff.append(""+r);
                }
                return strBuff.toString();
            }

} // public class RegisterFactory
