/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapping.java
 *
 * Created on 15 september 2006, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.protocolimpl.itron.quantum.Quantum;

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

    static public int PRESENT_REGISTERS=255;
    static public int BILLING_REGISTERS=0;
    static public int LAST_SEASON_REGISTERS=1;


            Quantum quantum;

            /** Creates a new instance of RegisterMapping */
            public RegisterFactory(Quantum quantum) {
                this.quantum=quantum;
            }

           public void init() throws IOException {
                buildRegisters();
            }

            private void buildRegisters() throws IOException {
                int count=0;
                registers=new ArrayList();
                Iterator it = quantum.getBasePagesFactory().getProgramTableBasePage(false).getProgramEntries().iterator();
                while(it.hasNext()) {
                    ProgramEntry p = (ProgramEntry)it.next();
                    if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0)) {

                         UnitTable ut = UnitTable.findUnitTable(p.getRegisterNr());
                         int cField = ut.getObisCField();
                         int dField = ut.getObisDField();
                         int eField = 0;

                         if ((p.getRegisterType() & 0x10) == 0x10) {
                             // tariff registers
                             if ((p.getRegisterType() & 0x8) == 0x8) {
                                 eField = 4;
                             }
                             else if ((p.getRegisterType() & 0x4) == 0x4) {
                                 eField = 3;
                             }
                             else if ((p.getRegisterType() & 0x2) == 0x2) {
                                 eField = 2;
                             }
                             else if ((p.getRegisterType() & 0x1) == 0x1) {
                                 eField = 1;
                             }
                         } // if ((p.getRegisterType() & 0x10) == 0x10)
                         else if ((p.getRegisterType() & 0x10) == 0x00) {
                             // per phase registers
                             if ((p.getRegisterType() & 0x8) == 0x8) {

                             }
                             else if ((p.getRegisterType() & 0x4) == 0x4) {
                                 cField+=60;
                             }
                             else if ((p.getRegisterType() & 0x2) == 0x2) {
                                 cField+=40;
                             }
                             else if ((p.getRegisterType() & 0x1) == 0x1) {
                                 cField+=20;
                             }

                         } // if ((p.getRegisterType() & 0x10) == 0x00)

                         registers.add(new Register(count, ObisCode.fromString("1.1." + cField + "." + dField + "." + eField + ".255"), ut));
                         registers.add(new Register(count, ObisCode.fromString("1.1."+cField+"."+dField+"."+eField+".0"), ut)); // billing registers ?
                         registers.add(new Register(count++, ObisCode.fromString("1.1."+cField+"."+dField+"."+eField+".1"), ut)); // previous season ?

                    } // if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0))

                } // while(it.hasNext())

            } // private void buildRegisters() throws IOException

            public Register findRegisterByIndex(int index) throws IOException {
                Iterator it = registers.iterator();
                while(it.hasNext()) {
                    Register r = (Register)it.next();
                    if (r.getIndex()==index)
                        return r;
                }
                throw new IOException("Register with index "+index+" does not exist!");
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

}  // public class RegisterFactory
