/*
 * EnergieActivePeriodeP.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;


/**
 *
 * @author Koen
 */
public class EnergieIndexReader extends AbstractTrimaranObject {
   
    private Energies energie;
    private int variableName;
    
    /** Creates a new instance of TemplateVariableName */
    public EnergieIndexReader(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergieActivePeriodeP:\n");
        strBuff.append("   energie=" + getEnergie()+"\n");
        return strBuff.toString();
    }    
    
    protected int getVariableName() {
        return variableName;
    }
    
    protected byte[] prepareBuild() throws IOException {
        
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
        DataContainer dc = new DataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setEnergie(new Energies(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
    }

    public Energies getEnergie() {
        return energie;
    }

    public void setEnergie(Energies energie) {
        this.energie = energie;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }


}
