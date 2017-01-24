/*
 * VoltageAndCurrentBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class VoltageAndCurrentBasePage extends AbstractBasePage {

    private IntegrationConstant integrationConstant;

    private int form; // 0=5a,5s,6a,6s,9a,9s,10s 1=8a,8s
    private int elements; // 0=3, 1=2 and 2.5




    /** Creates a new instance of VoltageAndCurrentBasePage */
    public VoltageAndCurrentBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("VoltageAndCurrentBasePage:\n");
        strBuff.append("   elements="+getElements()+"\n");
        strBuff.append("   form="+getForm()+"\n");
        strBuff.append("   integrationConstant="+getIntegrationConstant()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(808,1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setIntegrationConstant(IntegrationConstant.findIntegrationConstants(data[0]&0x3f));
        setForm((((int)data[0]&0xff) >> 6) & 0x01);
        setElements((((int)data[0]&0xff) >> 7) & 0x01);


    }

    public IntegrationConstant getIntegrationConstant() {
        return integrationConstant;
    }

    public void setIntegrationConstant(IntegrationConstant integrationConstant) {
        this.integrationConstant = integrationConstant;
    }

    public int getForm() {
        return form;
    }

    public void setForm(int form) {
        this.form = form;
    }

    public int getElements() {
        return elements;
    }

    public void setElements(int elements) {
        this.elements = elements;
    }


} // public class RealTimeBasePage extends AbstractBasePage
