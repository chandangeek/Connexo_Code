/*
 * DureeDepassementReader.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimaranplus.core.axdr.*;
import java.io.*;
import java.util.*;


/**
 *
 * @author Koen
 */
public class DureeDepassementReader extends AbstractTrimaranObject {
   
    private DureeDepassement dureeDepassement;
    private int variableName;
    
    /** Creates a new instance of DureeDepassementReader */
    public DureeDepassementReader(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DureeDepassementReader:\n");
        strBuff.append("   dureeDepassement="+getDureeDepassement()+"\n");
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
        setDureeDepassement(new DureeDepassement(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public DureeDepassement getDureeDepassement() {
        return dureeDepassement;
    }

    public void setDureeDepassement(DureeDepassement dureeDepassement) {
        this.dureeDepassement = dureeDepassement;
    }


}
