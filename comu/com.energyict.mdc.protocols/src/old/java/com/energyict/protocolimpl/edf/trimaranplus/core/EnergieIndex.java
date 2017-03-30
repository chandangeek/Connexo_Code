/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EnergieIndex.java
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
public class EnergieIndex {
    
    private List energies;
    
    /** Creates a new instance of EnergieIndex */
    public EnergieIndex() {
        setEnergies(new ArrayList());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for(int i=0;i<getEnergies().size();i++) {
            Energie obj = (Energie)getEnergies().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
    }

    
    public List getEnergies() {
        return energies;
    }

    public void setEnergies(List energies) {
        this.energies = energies;
    }
    
    public void addEnergie(Energie energie) {
        getEnergies().add(energie);
    }
    
    public Energie getEnergie(int variableName) throws IOException {
        Iterator it = getEnergies().iterator();
        while(it.hasNext()) {
            Energie obj = (Energie)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("EnergieIndex, invalid variableName "+variableName);
    }
    
    
}
