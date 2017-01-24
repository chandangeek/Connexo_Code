/*
 * TempsFonctionnementReader.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class TempsFonctionnementReader extends AbstractTrimaranObject {
   
    private TempsFonctionnement tempsFonctionnement;
    private int variableName;
    
    /** Creates a new instance of TempsFonctionnementReader */
    public TempsFonctionnementReader(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TempsFonctionnementReader:\n");
        strBuff.append("   tempsFonctionnement="+getTempsFonctionnement()+"\n");
        return strBuff.toString();
    }    
    
    protected int getVariableName() {
        return variableName;
    }
    
    protected byte[] prepareBuild() throws IOException {
        
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setTempsFonctionnement(new TempsFonctionnement(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public TempsFonctionnement getTempsFonctionnement() {
        return tempsFonctionnement;
    }

    public void setTempsFonctionnement(TempsFonctionnement tempsFonctionnement) {
        this.tempsFonctionnement = tempsFonctionnement;
    }


}
