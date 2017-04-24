/*
 * DepassementQuadratique.java
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
public class DepassementQuadratique {

    private int variableName;
    private DateType DateDebutPeriode;
    private int CodeAF;
    private DateType DateFinPeriode;
    private Quantity[] valueDepassementQuadratique;
    private Quantity EnergiesDepassement; // volume van de unit

    /** Creates a new instance of DepassementQuadratique */
    public DepassementQuadratique(TrimaranDataContainer dc, TimeZone timezone, int variableName) throws IOException {
        int offset = 0;
        setVariableName(variableName);
        setDateDebutPeriode(new DateType(dc.getRoot().getLong(offset++), timezone));
        setCodeAF(dc.getRoot().getInteger(offset++));
        setDateFinPeriode(new DateType(dc.getRoot().getLong(offset++), timezone)); // DateType, -- champ sans signification, date par defaut
        setValueDepassementQuadratique(new Quantity[dc.getRoot().getStructure(offset).getNrOfElements()]);
        for (int i=0;i<getValueDepassementQuadratique().length;i++) {
            getValueDepassementQuadratique()[i] = new Quantity(BigDecimal.valueOf(dc.getRoot().getStructure(offset).getLong(i)),VariableNameFactory.getVariableName(variableName).getUnit());
        }
        offset++;
        if (dc.getRoot().getNrOfElements() == 5){
           setEnergiesDepassement(new Quantity(BigDecimal.valueOf(dc.getRoot().getLong(offset)), VariableNameFactory.getVariableName(variableName).getUnit().getVolumeUnit()));
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        try {
            strBuff.append("DepassementQuadratique ("+VariableNameFactory.getVariableName(getVariableName())+"):\n");
        }
        catch(IOException e) {
            strBuff.append("DepassementQuadratique ("+e.toString()+"):\n");
        }
        strBuff.append("   codeAF="+getCodeAF()+"\n");
        strBuff.append("   dateDebutPeriode="+getDateDebutPeriode()+"\n");
        strBuff.append("   dateFinPeriode="+getDateFinPeriode()+"\n");
        for (int i=0;i<getValueDepassementQuadratique().length;i++) {
            strBuff.append("       DepassementQuadratique["+i+"]="+getValueDepassementQuadratique()[i]+"\n");
        }
        if (getEnergiesDepassement() != null) {
			strBuff.append("   EnergiesDepassement="+getEnergiesDepassement()+"\n");
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

    public Quantity[] getValueDepassementQuadratique() {
        return valueDepassementQuadratique;
    }

    public void setValueDepassementQuadratique(Quantity[] valueDepassementQuadratique) {
        this.valueDepassementQuadratique = valueDepassementQuadratique;
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public Quantity getEnergiesDepassement() {
        return EnergiesDepassement;
    }

    public void setEnergiesDepassement(Quantity EnergiesDepassement) {
        this.EnergiesDepassement = EnergiesDepassement;
    }

}
