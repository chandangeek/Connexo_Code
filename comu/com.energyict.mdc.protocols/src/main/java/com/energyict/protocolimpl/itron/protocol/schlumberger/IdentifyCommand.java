/*
 * IdentifyCommand.java
 *
 * Created on 8 september 2006, 9:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class IdentifyCommand extends AbstractCommand{

    private String unitId=new String(new byte[]{0,0,0,0,0,0,0,0}); // 8 bytes identification number to address the remote unit 00000000 means ALL meters
    private String unitType=new String(new byte[]{0,0,0}); // 3 ASCII characters addresses the type of remote unit
    private int memStart;
    private int memStop;



    /** Creates a new instance of IdentifyCommand */
    public IdentifyCommand(SchlumbergerProtocol schlumbergerProtocol) {
        super(schlumbergerProtocol);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IdentifyCommand:\n");
        strBuff.append("   unitId="+getUnitId()+"\n");
        strBuff.append("   unitType="+getUnitType()+"\n");
        return strBuff.toString();
    }

    protected Command preparebuild() throws IOException {
        Command command = new Command('I');
        // ,unitType,unitId,
        byte[] data = new byte[3+8];
        if (getUnitType() != null)
            System.arraycopy(getUnitType().getBytes(), 0, data, 0, 3);

        if (getUnitId() != null)
            System.arraycopy(getUnitId().getBytes(), 0, data, 3, getUnitId().length());


        //System.arraycopy(getUnitId()==0?ParseUtils.getArray(0, 8):ParseUtils.buildStringHexExtendedWithSpaces(getUnitId(),8).getBytes(), 0, data, 3, 8);
        command.setData(data);
        return command;
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        unitType = new String(ProtocolUtils.getSubArray2(data, offset, 3));
        offset+=3;
        setUnitId(new String(ProtocolUtils.getSubArray2(data, offset, 8)));
        offset+=8;
        setMemStart(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setMemStop(ProtocolUtils.getInt(data,offset, 3));
    }



    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public int getMemStart() {
        return memStart;
    }

    public void setMemStart(int memStart) {
        this.memStart = memStart;
    }

    public int getMemStop() {
        return memStop;
    }

    public void setMemStop(int memStop) {
        this.memStop = memStop;
    }

}
