/*
 * RegisterFactory.java
 *
 * Created on 27 juni 2006, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;
import com.energyict.protocolimpl.edf.trimaranplus.core.VariableNameFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterFactory {


    TrimaranPlus trimaranPlus;
    private List registers;

    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(TrimaranPlus trimaranPlus) throws IOException {
        this.trimaranPlus=trimaranPlus;
        buildRegisters();
    }

    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register reg = (Register)it.next();
            strBuff.append(reg.getObisCode()+", "+reg.getDescription()+"\n");
        }
        return strBuff.toString();
    }

    public Register findRegister(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Iterator it = getRegisters().iterator();
        while(it.hasNext()) {
            Register register = (Register)it.next();
            if (register.getObisCode().equals(obisCode)) {
                return register;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public List getRegisters() {
        return registers;
    }

    public void setRegisters(List registers) throws IOException {
        this.registers = registers;
    }

    // Periode
    final int[] PERIODE_BASE_2_OBISE= new int[]{1,12,5,10,13,11,4,9};
    final int[] PERIODE_EJP_2_OBISE= new int[]{6,12,7,8,13,11};
    final int[] PERIODE_MODULABLE_2_OBISE= new int[]{6,14,15,16};

    // Poste
    final int[] POSTE_BASE_2_OBISE= new int[]{1,3,2};
    final int[] POSTE_EJP_2_OBISE= new int[]{1,3,2};
    final int[] POSTE_MODULABLE_2_OBISE= new int[]{1,3,2};

    int[] periode2ObisE;
    int[] poste2ObisE;

    private void buildRegisters() throws IOException {
        registers = new ArrayList();

        if (trimaranPlus.getVDEType().isVDEBASE()) {
            periode2ObisE=PERIODE_BASE_2_OBISE;
            poste2ObisE=POSTE_BASE_2_OBISE;
        }
        else if (trimaranPlus.getVDEType().isVDEEJP()) {
            periode2ObisE=PERIODE_EJP_2_OBISE;
            poste2ObisE=POSTE_EJP_2_OBISE;
        }
        else if (trimaranPlus.getVDEType().isVDEMODULABLE()) {
            periode2ObisE=PERIODE_MODULABLE_2_OBISE;
            poste2ObisE=POSTE_MODULABLE_2_OBISE;
        }

/*   (F=255)(F=0)
        (56)(176)  EnergieActivePeriodeP
        (64)(184)  EnergieReactivePositivePeriodeP
        (72)(192)  EnergieReactiveNegativePeriodeP
        (80)(200)  EnergieActivePosteP
        (88)(208)  EnergieReactivePositivePosteP
        (96)(216)  EnergieReactiveNegativePosteP
 */
        buildEnergieRegisters();
/*
        (104)(224) PmaxPeriodeP
        (112)(232) PmaxPosteP
*/
        buildPMaxRegisters();
/*
        (120)(240) DureeDepassementPeriodeP
        (128)(248) DureeDepassementPosteP
*/
        buildDureeDepassementRegisters();
/*
        (136)(256) DepassementQuadratiquePeriodeP
        (144)(264) DepassementQuadratiquePosteP
*/
        buildDepassementQuadratiqueRegisters();
/*
        (152)(272) TempsFonctionnementPeriodeP
        (160)(280) TempsFonctionnementPosteP
*/
        buildTempsFonctionnemenRegisters();

/*
        (48)(168) puissance souscrites periode
 */
        buildPuissanceSouscrite();

    } // private void buildRegisters() throws IOException

    private void buildPuissanceSouscrite() throws IOException {
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(48);
           registers.add(new Register(variableName,tariff,obisE));
       }
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(168);
           registers.add(new Register(variableName,tariff,obisE));
       }

    }

    private void buildTempsFonctionnemenRegisters() throws IOException {
        // DepassementQuadratique current poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(160);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DepassementQuadratique current periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(152);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DepassementQuadratique previous month poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(280);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DepassementQuadratique previous month periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(272);
           registers.add(new Register(variableName,tariff,obisE));
       }
    }


    private void buildDepassementQuadratiqueRegisters() throws IOException {

        // DepassementQuadratique current poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(144);
           registers.add(new Register(variableName,tariff,obisE));
       }
       if (trimaranPlus.getVDEType().isVDEEJP()) {
          VariableName variableName = VariableNameFactory.getVariableName(1144);
          registers.add(new Register(variableName,-1,6));
       }

        // DepassementQuadratique current periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(136);
           registers.add(new Register(variableName,tariff,obisE));
       }
       if (trimaranPlus.getVDEType().isVDEEJP() || trimaranPlus.getVDEType().isVDEMODULABLE()) {
          VariableName variableName = VariableNameFactory.getVariableName(1136);
          registers.add(new Register(variableName,-1,6));
       }

        // DepassementQuadratique previous month poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(264);
           registers.add(new Register(variableName,tariff,obisE));
       }
       if (trimaranPlus.getVDEType().isVDEEJP()) {
          VariableName variableName = VariableNameFactory.getVariableName(1264);
          registers.add(new Register(variableName,-1,6));
       }

        // DepassementQuadratique previous month periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(256);
           registers.add(new Register(variableName,tariff,obisE));
       }
       if (trimaranPlus.getVDEType().isVDEEJP() || trimaranPlus.getVDEType().isVDEMODULABLE()) {
          VariableName variableName = VariableNameFactory.getVariableName(1256);
          registers.add(new Register(variableName,-1,6));
       }

    }

    private void buildDureeDepassementRegisters() throws IOException {
        // DureeDepassement current poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(128);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DureeDepassement current periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(120);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DureeDepassement previous month poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(248);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // DureeDepassement previous month periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(240);
           registers.add(new Register(variableName,tariff,obisE));
       }
    }

    private void buildPMaxRegisters() throws IOException {
        // PMax current poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(112);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // PMax current periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(104);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // PMax previous month poste
       for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
           int obisE = poste2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(232);
           registers.add(new Register(variableName,tariff,obisE));
       }

        // PMax previous month periode
       for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
           int obisE = periode2ObisE[tariff];
           VariableName variableName = VariableNameFactory.getVariableName(224);
           registers.add(new Register(variableName,tariff,obisE));
       }
    }

    private void buildEnergieRegisters() throws IOException {
        // actief kWh, reactive import & reactive export Energie registers current poste
        for (int variableNameId=80;variableNameId<=96;variableNameId+=8) {
           for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
               int obisE = poste2ObisE[tariff];
               VariableName variableName = VariableNameFactory.getVariableName(variableNameId);
               registers.add(new Register(variableName,tariff,obisE));
           }
        }

        // actief kWh, reactive import & reactive export Energie registers current periode
        for (int variableNameId=56;variableNameId<=72;variableNameId+=8) {
           for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
               int obisE = periode2ObisE[tariff];
               VariableName variableName = VariableNameFactory.getVariableName(variableNameId);
               registers.add(new Register(variableName,tariff,obisE));
           }
        }

        // actief kWh, reactive import & reactive export Energie registers previous month poste
        for (int variableNameId=200;variableNameId<=216;variableNameId+=8) {
           for (int tariff=0;tariff<poste2ObisE.length;tariff++) {
               int obisE = poste2ObisE[tariff];
               VariableName variableName = VariableNameFactory.getVariableName(variableNameId);
               registers.add(new Register(variableName,tariff,obisE));
           }
        }

        // actief kWh, reactive import & reactive export Energie registers previous month periode
        for (int variableNameId=176;variableNameId<=192;variableNameId+=8) {
           for (int tariff=0;tariff<periode2ObisE.length;tariff++) {
               int obisE = periode2ObisE[tariff];
               VariableName variableName = VariableNameFactory.getVariableName(variableNameId);
               registers.add(new Register(variableName,tariff,obisE));
           }
        }
    } // buildEnergieRegisters()


} // public class RegisterFactory
