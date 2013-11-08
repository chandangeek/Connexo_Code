/*
 * TempsFonctionnementValues.java
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
public class TempsFonctionnementValues {
    
    private List tempsFonctionnements;
    
    /** Creates a new instance of TempsFonctionnementValues */
    public TempsFonctionnementValues() {
        setTempsFonctionnements(new ArrayList());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for(int i=0;i<getTempsFonctionnements().size();i++) {
            TempsFonctionnement obj = (TempsFonctionnement)getTempsFonctionnements().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
    }

    
    public List getTempsFonctionnements() {
        return tempsFonctionnements;
    }

    public void setTempsFonctionnements(List tempsFonctionnements) {
        this.tempsFonctionnements = tempsFonctionnements;
    }
    
    public void addTempsFonctionnement(TempsFonctionnement pmax) {
        getTempsFonctionnements().add(pmax);
    }
    
    public TempsFonctionnement getTempsFonctionnement(int variableName) throws IOException {
        Iterator it = getTempsFonctionnements().iterator();
        while(it.hasNext()) {
            TempsFonctionnement obj = (TempsFonctionnement)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("TempsFonctionnementIndex, invalid variableName "+variableName);
    }
    
    
}
