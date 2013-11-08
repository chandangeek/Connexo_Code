/*
 * PmaxValues.java
 *
 * Created on 22 februari 2007, 10:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class PmaxValues {
    
    private List pmaxs;
    
    /** Creates a new instance of PmaxValues */
    public PmaxValues() {
        setPmaxs(new ArrayList());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for(int i=0;i<getPmaxs().size();i++) {
            Pmax obj = (Pmax)getPmaxs().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
    }

    
    public List getPmaxs() {
        return pmaxs;
    }

    public void setPmaxs(List pmaxs) {
        this.pmaxs = pmaxs;
    }
    
    public void addPmax(Pmax pmax) {
        getPmaxs().add(pmax);
    }
    
    public Pmax getPmax(int variableName) throws IOException {
        Iterator it = getPmaxs().iterator();
        while(it.hasNext()) {
            Pmax obj = (Pmax)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("PmaxIndex, invalid variableName "+variableName);
    }
    
    
}
