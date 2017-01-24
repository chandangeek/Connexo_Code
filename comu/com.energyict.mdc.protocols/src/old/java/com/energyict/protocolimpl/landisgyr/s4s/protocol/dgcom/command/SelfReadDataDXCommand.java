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

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class SelfReadDataDXCommand extends AbstractCommand {

    private int selfReadIndex;
    private int touStatus; // TOU status (byte 1 of 81h)
    private Date selfReadTimestamp; // 7 bytes Time(3) + Date(4)
    private long batteryCarryOvertime; // from 13h read command
    private HighestMaximumDemandsCommand highestMaximumDemandsCommand; // 84h
    private RateBinsAndTotalEnergyDXCommand rateBinsAndTotalEnergyDXCommand; // 05h
    private CurrentSeasonTOUDemandDataDXCommand currentSeasonTOUDemandDataDXCommand; // 61h
    private CurrentSeasonCumulativeDemandDataDXCommand currentSeasonCumulativeDemandDataDXCommand; // 5Bh
    private CurrentSeasonLastResetValuesDXCommand currentSeasonLastResetValuesDXCommand; // 85h
    private PreviousSeasonTOUDataDXCommand previousSeasonTOUDataDXCommand; // 5Ch
    private PreviousSeasonLastResetValuesDXCommand previousSeasonLastResetValuesDXCommand; // 86h

    // 6 reserved bytes

    /** Creates a new instance of TemplateCommand */
    public SelfReadDataDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        //strBuff.append("SelfReadDataDXCommand:\n");
        strBuff.append("SelfReadDataDXCommand "+getSelfReadIndex()+" at "+getSelfReadTimestamp()+":\n");
        strBuff.append("   batteryCarryOvertime="+getBatteryCarryOvertime()+"\n");
        strBuff.append("   currentSeasonCumulativeDemandDataDXCommand="+getCurrentSeasonCumulativeDemandDataDXCommand()+"\n");
        strBuff.append("   currentSeasonLastResetValuesDXCommand="+getCurrentSeasonLastResetValuesDXCommand()+"\n");
        strBuff.append("   currentSeasonTOUDemandDataDXCommand="+getCurrentSeasonTOUDemandDataDXCommand()+"\n");
        strBuff.append("   highestMaximumDemandsCommand="+getHighestMaximumDemandsCommand()+"\n");
        strBuff.append("   previousSeasonLastResetValuesDXCommand="+getPreviousSeasonLastResetValuesDXCommand()+"\n");
        strBuff.append("   previousSeasonTOUDataDXCommand="+getPreviousSeasonTOUDataDXCommand()+"\n");
        strBuff.append("   rateBinsAndTotalEnergyDXCommand="+getRateBinsAndTotalEnergyDXCommand()+"\n");
        strBuff.append("   selfReadTimestamp="+getSelfReadTimestamp()+"\n");
        strBuff.append("   touStatus="+getTouStatus()+"\n");
        return strBuff.toString();
    }


    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX()) {
            setSize(280);
            return new byte[]{(byte)0x1A,(byte)getSelfReadIndex(),0,0,0,0,0,0,0};
        }
        else
            throw new IOException("SelfReadDataDXCommand, only for DX meters!");
    }

    private void parseTimestamp(byte[] data, int offset) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS4s().getTimeZone());
        cal.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[offset]));
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset+1]));
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset+2]));
        int year = ProtocolUtils.BCD2hex(data[offset+4]);
        cal.set(Calendar.YEAR,year>50?1900+year:2000+year);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset+5]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset+6])-1);
        setSelfReadTimestamp(cal.getTime());
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setTouStatus((int)data[offset++]&0xFF);
        parseTimestamp(data,offset); offset+=7;
        setBatteryCarryOvertime(ParseUtils.getBCD2LongLE(data,offset, 4)); offset+=4;

        setHighestMaximumDemandsCommand(new HighestMaximumDemandsCommand(getCommandFactory()));
        getHighestMaximumDemandsCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=40;

        setRateBinsAndTotalEnergyDXCommand(new RateBinsAndTotalEnergyDXCommand(getCommandFactory()));
        getRateBinsAndTotalEnergyDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=30;

        setCurrentSeasonTOUDemandDataDXCommand(new CurrentSeasonTOUDemandDataDXCommand(getCommandFactory()));
        getCurrentSeasonTOUDemandDataDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=32;

        setCurrentSeasonCumulativeDemandDataDXCommand(new CurrentSeasonCumulativeDemandDataDXCommand(getCommandFactory()));
        getCurrentSeasonCumulativeDemandDataDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=32;

        setCurrentSeasonLastResetValuesDXCommand(new CurrentSeasonLastResetValuesDXCommand(getCommandFactory()));
        getCurrentSeasonLastResetValuesDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=14;

        setPreviousSeasonTOUDataDXCommand(new PreviousSeasonTOUDataDXCommand(getCommandFactory()));
        getPreviousSeasonTOUDataDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=100;

        setPreviousSeasonLastResetValuesDXCommand(new PreviousSeasonLastResetValuesDXCommand(getCommandFactory()));
        getPreviousSeasonLastResetValuesDXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=14;
    }

    public int getTouStatus() {
        return touStatus;
    }

    public void setTouStatus(int touStatus) {
        this.touStatus = touStatus;
    }

    public Date getSelfReadTimestamp() {
        return selfReadTimestamp;
    }

    public void setSelfReadTimestamp(Date selfReadTimestamp) {
        this.selfReadTimestamp = selfReadTimestamp;
    }

    public long getBatteryCarryOvertime() {
        return batteryCarryOvertime;
    }

    public void setBatteryCarryOvertime(long batteryCarryOvertime) {
        this.batteryCarryOvertime = batteryCarryOvertime;
    }

    public HighestMaximumDemandsCommand getHighestMaximumDemandsCommand() {
        return highestMaximumDemandsCommand;
    }

    public void setHighestMaximumDemandsCommand(HighestMaximumDemandsCommand highestMaximumDemandsCommand) {
        this.highestMaximumDemandsCommand = highestMaximumDemandsCommand;
    }

    public RateBinsAndTotalEnergyDXCommand getRateBinsAndTotalEnergyDXCommand() {
        return rateBinsAndTotalEnergyDXCommand;
    }

    public void setRateBinsAndTotalEnergyDXCommand(RateBinsAndTotalEnergyDXCommand rateBinsAndTotalEnergyDXCommand) {
        this.rateBinsAndTotalEnergyDXCommand = rateBinsAndTotalEnergyDXCommand;
    }

    public CurrentSeasonTOUDemandDataDXCommand getCurrentSeasonTOUDemandDataDXCommand() {
        return currentSeasonTOUDemandDataDXCommand;
    }

    public void setCurrentSeasonTOUDemandDataDXCommand(CurrentSeasonTOUDemandDataDXCommand currentSeasonTOUDemandDataDXCommand) {
        this.currentSeasonTOUDemandDataDXCommand = currentSeasonTOUDemandDataDXCommand;
    }

    public CurrentSeasonCumulativeDemandDataDXCommand getCurrentSeasonCumulativeDemandDataDXCommand() {
        return currentSeasonCumulativeDemandDataDXCommand;
    }

    public void setCurrentSeasonCumulativeDemandDataDXCommand(CurrentSeasonCumulativeDemandDataDXCommand currentSeasonCumulativeDemandDataDXCommand) {
        this.currentSeasonCumulativeDemandDataDXCommand = currentSeasonCumulativeDemandDataDXCommand;
    }

    public CurrentSeasonLastResetValuesDXCommand getCurrentSeasonLastResetValuesDXCommand() {
        return currentSeasonLastResetValuesDXCommand;
    }

    public void setCurrentSeasonLastResetValuesDXCommand(CurrentSeasonLastResetValuesDXCommand currentSeasonLastResetValuesDXCommand) {
        this.currentSeasonLastResetValuesDXCommand = currentSeasonLastResetValuesDXCommand;
    }

    public PreviousSeasonTOUDataDXCommand getPreviousSeasonTOUDataDXCommand() {
        return previousSeasonTOUDataDXCommand;
    }

    public void setPreviousSeasonTOUDataDXCommand(PreviousSeasonTOUDataDXCommand previousSeasonTOUDataDXCommand) {
        this.previousSeasonTOUDataDXCommand = previousSeasonTOUDataDXCommand;
    }

    public PreviousSeasonLastResetValuesDXCommand getPreviousSeasonLastResetValuesDXCommand() {
        return previousSeasonLastResetValuesDXCommand;
    }

    public void setPreviousSeasonLastResetValuesDXCommand(PreviousSeasonLastResetValuesDXCommand previousSeasonLastResetValuesDXCommand) {
        this.previousSeasonLastResetValuesDXCommand = previousSeasonLastResetValuesDXCommand;
    }

    public int getSelfReadIndex() {
        return selfReadIndex;
    }

    public void setSelfReadIndex(int selfReadIndex) {
        this.selfReadIndex = selfReadIndex;
    }
}
