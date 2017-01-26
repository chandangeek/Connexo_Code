/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class SelfReadConfigurationCommand extends AbstractCommand {

    private int nrOfSelfReadsToStore;

    public final int TYPEMASK = 0x0F;
    public final int DAY_OF_MONTH = 1;
    public final int HOURS_PAST_THE_LAST_ONE = 2;


    private int typeOfSelfRead;
    /* bits 0..3 Type of self read,
                 0 for no automatic self reads,
                 1 for self read on day of month,
                 2 for self read on hours past the last one,
                 3 for self reads daily at midnight
       bit 4 Set if self read to occur on manual or optical demand reset
     * bit 5 Set if demand reset to occur on automatic self read
     * bits 6..7 Self read on hardware input,
                 0 for no self read,
                 1 for self read on NO hardware input,
                 2 is not defined,
                 3 for self read on hardware input
     */

     private int autoSelfReadConfig; // Not used when no automatic self reads or daily self reads are used
     /* byte 2 is day of month for day of month self reads Byte 3 is not used in this case (BCD).
      * bytes 2 - 3 is number of hours past the last demand reset for self read on hours past last one (Hex).
      */


    /** Creates a new instance of TemplateCommand */
    public SelfReadConfigurationCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadConfigurationCommand:\n");
        strBuff.append("   autoSelfReadConfig="+getAutoSelfReadConfig()+"\n");
        strBuff.append("   nrOfSelfReadsToStore="+getNrOfSelfReadsToStore()+"\n");
        strBuff.append("   typeOfSelfRead="+getTypeOfSelfRead()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws ProtocolException {
        return new byte[]{(byte)0x88,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setNrOfSelfReadsToStore((int)ParseUtils.getBCD2LongLE(data, offset++, 1));
        setTypeOfSelfRead(ProtocolUtils.getIntLE(data,offset++, 1));
        if ((getTypeOfSelfRead() & TYPEMASK) == DAY_OF_MONTH)
            setAutoSelfReadConfig((int)ParseUtils.getBCD2LongLE(data, offset++, 1));
        else if ((getTypeOfSelfRead() & TYPEMASK) == HOURS_PAST_THE_LAST_ONE) {
            setAutoSelfReadConfig(ProtocolUtils.getIntLE(data,offset, 1));
            offset+=2;
        }
    }

    public int getNrOfSelfReadsToStore() {
        return nrOfSelfReadsToStore;
    }

    public void setNrOfSelfReadsToStore(int nrOfSelfReadsToStore) {
        this.nrOfSelfReadsToStore = nrOfSelfReadsToStore;
    }

    public int getTypeOfSelfRead() {
        return typeOfSelfRead;
    }

    public void setTypeOfSelfRead(int typeOfSelfRead) {
        this.typeOfSelfRead = typeOfSelfRead;
    }

    public int getAutoSelfReadConfig() {
        return autoSelfReadConfig;
    }

    public void setAutoSelfReadConfig(int autoSelfReadConfig) {
        this.autoSelfReadConfig = autoSelfReadConfig;
    }



}
