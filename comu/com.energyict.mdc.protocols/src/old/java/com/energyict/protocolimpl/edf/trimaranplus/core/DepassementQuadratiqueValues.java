/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DepassementQuadratiqueValues.java
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
public class DepassementQuadratiqueValues {
    
    private List depassementQuadratiques;
    
    /** Creates a new instance of DepassementQuadratiqueValues */
    public DepassementQuadratiqueValues() {
        setDepassementQuadratiques(new ArrayList());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for(int i=0;i<getDepassementQuadratiques().size();i++) {
            DepassementQuadratique obj = (DepassementQuadratique)getDepassementQuadratiques().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
    }

    
    public List getDepassementQuadratiques() {
        return depassementQuadratiques;
    }

    public void setDepassementQuadratiques(List depassementQuadratiques) {
        this.depassementQuadratiques = depassementQuadratiques;
    }
    
    public void addDepassementQuadratique(DepassementQuadratique pmax) {
        getDepassementQuadratiques().add(pmax);
    }
    
    public DepassementQuadratique getDepassementQuadratique(int variableName) throws IOException {
        Iterator it = getDepassementQuadratiques().iterator();
        while(it.hasNext()) {
            DepassementQuadratique obj = (DepassementQuadratique)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("DepassementQuadratiqueIndex, invalid variableName "+variableName);
    }
    
    
}
