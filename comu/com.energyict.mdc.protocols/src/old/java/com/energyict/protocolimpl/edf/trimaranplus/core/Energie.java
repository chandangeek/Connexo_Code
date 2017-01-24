/*
 * Energie.java
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
public class Energie {

    private int variableName;
    private DateType DateDebutPeriode;
    private int CodeAF;
    private DateType DateFinPeriode;
    private Quantity[] indexEnergie;

    /** Creates a new instance of Energie */
    public Energie(TrimaranDataContainer dc, TimeZone timezone, int variableName) throws IOException {
        int offset = 0;
        setVariableName(variableName);
        setDateDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timezone));
        setCodeAF(dc.getRoot().getInteger(offset++));
        setDateFinPeriode(new DateType(dc.getRoot().getLong(offset++), timezone)); // DateType, -- champ sans signification, date par defaut
        setIndexEnergie(new Quantity[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getIndexEnergie().length;i++) {
            getIndexEnergie()[i] = new Quantity(BigDecimal.valueOf(dc.getRoot().getStructure(offset).getLong(i)),VariableNameFactory.getVariableName(variableName).getUnit());
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        try {
            strBuff.append("Energie ("+VariableNameFactory.getVariableName(getVariableName())+"):\n");
        }
        catch(IOException e) {
            strBuff.append("Energie ("+e.toString()+"):\n");
        }
        strBuff.append("   codeAF="+getCodeAF()+"\n");
        strBuff.append("   dateDebutPeriode="+getDateDebutPeriode()+"\n");
        strBuff.append("   dateFinPeriode="+getDateFinPeriode()+"\n");
        for (int i=0;i<getIndexEnergie().length;i++) {
            strBuff.append("       indexEnergie["+i+"]="+getIndexEnergie()[i]+"\n");
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

    public Quantity[] getIndexEnergie() {
        return indexEnergie;
    }

    public void setIndexEnergie(Quantity[] indexEnergie) {
        this.indexEnergie = indexEnergie;
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

}
