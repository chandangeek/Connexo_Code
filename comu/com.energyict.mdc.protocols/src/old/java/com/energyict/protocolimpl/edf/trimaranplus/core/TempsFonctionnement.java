/*
 * TempsFonctionnement.java
 *
 * Created on 22 februari 2007, 10:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class TempsFonctionnement {

    private int variableName;
    private DateType DateDebutPeriode;
    private int CodeAF;
    private DateType DateFinPeriode;
    private Quantity[] valueTempsFonctionnement;

    /** Creates a new instance of TempsFonctionnement */
    public TempsFonctionnement(TrimaranDataContainer dc, TimeZone timezone, int variableName) throws IOException {
        int offset = 0;
        setVariableName(variableName);
        setDateDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timezone));
        setCodeAF(dc.getRoot().getInteger(offset++));
        setDateFinPeriode(new DateType(dc.getRoot().getLong(offset++), timezone)); // DateType, -- champ sans signification, date par defaut
        setValueTempsFonctionnement(new Quantity[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getValueTempsFonctionnement().length;i++) {
            getValueTempsFonctionnement()[i] = new Quantity(BigDecimal.valueOf(dc.getRoot().getStructure(offset).getLong(i)),VariableNameFactory.getVariableName(variableName).getUnit());
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        try {
            strBuff.append("TempsFonctionnement ("+VariableNameFactory.getVariableName(getVariableName())+"):\n");
        }
        catch(IOException e) {
            strBuff.append("TempsFonctionnement ("+e.toString()+"):\n");
        }
        strBuff.append("   codeAF="+getCodeAF()+"\n");
        strBuff.append("   dateDebutPeriode="+getDateDebutPeriode()+"\n");
        strBuff.append("   dateFinPeriode="+getDateFinPeriode()+"\n");
        for (int i=0;i<getValueTempsFonctionnement().length;i++) {
            strBuff.append("       TempsFonctionnement["+i+"]="+getValueTempsFonctionnement()[i]+"\n");
        }
        return strBuff.toString();
    }

    public DateType getDateDebutPeriode() {
        return DateDebutPeriode;
    }

    public void setDateDebutPeriode(DateType DateDebutPeriode) {
        this.DateDebutPeriode = DateDebutPeriode;
    }

    public int getCodeAF() {
        return CodeAF;
    }

    public void setCodeAF(int CodeAF) {
        this.CodeAF = CodeAF;
    }

    public DateType getDateFinPeriode() {
        return DateFinPeriode;
    }

    public void setDateFinPeriode(DateType DateFinPeriode) {
        this.DateFinPeriode = DateFinPeriode;
    }

    public Quantity[] getValueTempsFonctionnement() {
        return valueTempsFonctionnement;
    }

    public void setValueTempsFonctionnement(Quantity[] valueTempsFonctionnement) {
        this.valueTempsFonctionnement = valueTempsFonctionnement;
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

}
