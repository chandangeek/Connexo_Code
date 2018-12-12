/*
 * TemplateVariableName.java
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
public class TemplateVariableName extends AbstractTrimaranObject {
    
   
    /** Creates a new instance of TemplateVariableName */
    public TemplateVariableName(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public TemplateVariableName() {
        super(null);
    }
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateVariableName()));
    }         
 
    protected int getVariableName() {
        return -1;
    }
    
    protected byte[] prepareBuild() throws IOException {
        
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
    }
    
}
