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

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;

import java.io.IOException;


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
        strBuff.append("*** EnergieIndexReader: ***\n");
        strBuff.append("	- energie = " + getEnergie()+"\n");
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
        
//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/EnergieIndexes.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	fos.write(data);
//    	fos.close();
        
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
