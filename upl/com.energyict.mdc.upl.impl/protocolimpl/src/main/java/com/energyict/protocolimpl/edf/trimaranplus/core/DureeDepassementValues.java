/*
 * DureeDepassementValues.java
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
public class DureeDepassementValues {
    
    private List dureeDepassements;
    
    /** Creates a new instance of DureeDepassementValues */
    public DureeDepassementValues() {
        setDureeDepassements(new ArrayList());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for(int i=0;i<getDureeDepassements().size();i++) {
            DureeDepassement obj = (DureeDepassement)getDureeDepassements().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
    }

    
    public List getDureeDepassements() {
        return dureeDepassements;
    }

    public void setDureeDepassements(List dureeDepassements) {
        this.dureeDepassements = dureeDepassements;
    }
    
    public void addDureeDepassement(DureeDepassement pmax) {
        getDureeDepassements().add(pmax);
    }
    
    public DureeDepassement getDureeDepassement(int variableName) throws IOException {
        Iterator it = getDureeDepassements().iterator();
        while(it.hasNext()) {
            DureeDepassement obj = (DureeDepassement)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("DureeDepassementIndex, invalid variableName "+variableName);
    }
    
    
}
