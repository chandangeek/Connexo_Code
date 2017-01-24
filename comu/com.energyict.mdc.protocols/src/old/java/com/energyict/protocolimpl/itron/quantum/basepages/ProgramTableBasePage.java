/*
 * ProgramTableBasePage.java
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class ProgramTableBasePage extends AbstractBasePage {

    private boolean alternate;
    private List programEntries;

    /** Creates a new instance of ProgramTableBasePage */
    public ProgramTableBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        if (isAlternate())
            strBuff.append("Alternate ProgramTableBasePage:\n");
        else
            strBuff.append("ProgramTableBasePage:\n");
        Iterator it = getProgramEntries().iterator();
        int count=0;
        while(it.hasNext()) {
            ProgramEntry p = (ProgramEntry)it.next();
            strBuff.append("ProgramEntry "+(count++)+" "+p+"\n");
        }
        //strBuff.append("   programEntries="+getProgramEntries()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        if (isAlternate())
            return new BasePageDescriptor(414,128);
        else
            return new BasePageDescriptor(680,128);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setProgramEntries(new ArrayList());

        while(data[offset+1] != 0) {
            getProgramEntries().add(new ProgramEntry(data,offset));
            offset+=ProgramEntry.size();
        }
    }

    public List getProgramEntries() {
        return programEntries;
    }

    public void setProgramEntries(List programEntries) {
        this.programEntries = programEntries;
    }

    public boolean isAlternate() {
        return alternate;
    }

    public void setAlternate(boolean alternate) {
        this.alternate = alternate;
    }



} // public class RealTimeBasePage extends AbstractBasePage
