/*
 * DepassementQuadratiqueReader.java
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
public class DepassementQuadratiqueReader extends AbstractTrimaranObject {
   
    private DepassementQuadratique depassementQuadratique;
    private int variableName;
    
    /** Creates a new instance of DepassementQuadratiqueReader */
    public DepassementQuadratiqueReader(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DepassementQuadratiqueReader:\n");
        strBuff.append("   depassementQuadratique="+getDepassementQuadratique()+"\n");
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
//        if (dc.getRoot().getNrOfElements() == 5) {
//            if (getTrimaranObjectFactory().getTrimaranPlus().getVDEType().isVDEBASE())
//                getTrimaranObjectFactory().getTrimaranPlus().getVDEType().setVDEType(VDEType.getVDEEJP());
//        }
//        else {
//            if (getTrimaranObjectFactory().getTrimaranPlus().getVDEType().isVDEEJP())
//                getTrimaranObjectFactory().getTrimaranPlus().getVDEType().setVDEType(VDEType.getVDEBASE());
//        }
        
        setDepassementQuadratique(new DepassementQuadratique(dc, getTrimaranObjectFactory().getTrimaran().getTimeZone(), getVariableName()));
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public DepassementQuadratique getDepassementQuadratique() {
        return depassementQuadratique;
    }

    public void setDepassementQuadratique(DepassementQuadratique depassementQuadratique) {
        this.depassementQuadratique = depassementQuadratique;
    }


}
