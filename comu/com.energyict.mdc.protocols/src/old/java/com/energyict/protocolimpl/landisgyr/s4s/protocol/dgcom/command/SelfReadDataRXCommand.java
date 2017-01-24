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
public class SelfReadDataRXCommand extends AbstractCommand {

    private int selfReadIndex;
    private Date selfReadTimestamp;
    private long batteryCarryOvertime; // from 13h read command
    private int touStatus; // TOU status (byte 1 of 81h)
    private PreviousIntervalDemandCommand previousIntervalDemandCommand; // 14h
    private PreviousSeasonTOUDataRXCommand previousSeasonTOUDataRXCommand; // ABh
    private RateBinsAndTotalEnergyRXCommand RateBinsAndTotalEnergyRXCommand; // A0h
    private NegativeEnergyCommand negativeEnergyCommand; // 53h
    private HighestMaximumDemandsCommand highestMaximumDemandsCommand; // 74h
    private CurrentSeasonCumDemandAndLastResetRXCommand currentSeasonCumDemandAndLastResetRXCommand; // AAh
    private CurrentSeasonTOUDemandDataRXCommand currentSeasonTOUDemandDataRXCommand; // ACh
    private PreviousSeasonDemandDataCommand previousSeasonDemandDataCommand; // 4Ch
    private ThirdMetricValuesCommand thirdMetricValuesCommand; // 99h


    /** Creates a new instance of TemplateCommand */
    public SelfReadDataRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadDataRXCommand "+getSelfReadIndex()+" at "+getSelfReadTimestamp()+":\n");
        strBuff.append("   batteryCarryOvertime="+getBatteryCarryOvertime()+"\n");
        strBuff.append("   currentSeasonCumDemandAndLastResetRXCommand="+getCurrentSeasonCumDemandAndLastResetRXCommand()+"\n");
        strBuff.append("   currentSeasonTOUDemandDataRXCommand="+getCurrentSeasonTOUDemandDataRXCommand()+"\n");
        strBuff.append("   highestMaximumDemandsCommand="+getHighestMaximumDemandsCommand()+"\n");
        strBuff.append("   negativeEnergyCommand="+getNegativeEnergyCommand()+"\n");
        strBuff.append("   previousIntervalDemandCommand="+getPreviousIntervalDemandCommand()+"\n");
        strBuff.append("   previousSeasonDemandDataCommand="+getPreviousSeasonDemandDataCommand()+"\n");
        strBuff.append("   previousSeasonTOUDataRXCommand="+getPreviousSeasonTOUDataRXCommand()+"\n");
        strBuff.append("   rateBinsAndTotalEnergyRXCommand="+getRateBinsAndTotalEnergyRXCommand()+"\n");
        strBuff.append("   selfReadIndex="+getSelfReadIndex()+"\n");
        strBuff.append("   selfReadTimestamp="+getSelfReadTimestamp()+"\n");
        strBuff.append("   thirdMetricValuesCommand="+getThirdMetricValuesCommand()+"\n");
        strBuff.append("   touStatus="+getTouStatus()+"\n");
        return strBuff.toString();
    }


    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
                setSize(1012);
            else
                setSize(736);
            return new byte[]{(byte)0xA8,(byte)getSelfReadIndex(),0,0,0,0,0,0,0};
        }
        else
            throw new IOException("SelfReadDataRXCommand, only for RX meters!");
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
        byte[] temp;
        parseTimestamp(data,offset); offset+=7;
        setBatteryCarryOvertime(ParseUtils.getBCD2LongLE(data,offset, 4)); offset+=4;
        setTouStatus((int)data[offset++]&0xFF);

        setPreviousIntervalDemandCommand(new PreviousIntervalDemandCommand(getCommandFactory()));
        getPreviousIntervalDemandCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=8;

        setPreviousSeasonTOUDataRXCommand(new PreviousSeasonTOUDataRXCommand(getCommandFactory()));
        temp=ProtocolUtils.getSubArray2(data, offset, 300); offset+=300;
        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
            temp=ProtocolUtils.concatByteArrays(temp, ProtocolUtils.getSubArray2(data, 770, 116));
        getPreviousSeasonTOUDataRXCommand().parse(temp);

        setRateBinsAndTotalEnergyRXCommand(new RateBinsAndTotalEnergyRXCommand(getCommandFactory())); // A0h
        getRateBinsAndTotalEnergyRXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=72;

        setNegativeEnergyCommand(new NegativeEnergyCommand(getCommandFactory())); // 53h
        temp=ProtocolUtils.getSubArray2(data, offset, 6); offset+=6;
        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
            temp=ProtocolUtils.concatByteArrays(temp, ProtocolUtils.getSubArray2(data, 926, 6));
        getNegativeEnergyCommand().parse(temp);

        setHighestMaximumDemandsCommand(new HighestMaximumDemandsCommand(getCommandFactory())); // 74h
        temp=ProtocolUtils.getSubArray2(data, offset, 70); offset+=70;
        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
            temp=ProtocolUtils.concatByteArrays(temp, ProtocolUtils.getSubArray2(data, 886, 40));
        getHighestMaximumDemandsCommand().parse(temp);

        setCurrentSeasonCumDemandAndLastResetRXCommand(new CurrentSeasonCumDemandAndLastResetRXCommand(getCommandFactory())); // AAh
        getCurrentSeasonCumDemandAndLastResetRXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=120;

        setCurrentSeasonTOUDemandDataRXCommand(new CurrentSeasonTOUDemandDataRXCommand(getCommandFactory())); // ACh
        getCurrentSeasonTOUDemandDataRXCommand().parse(ProtocolUtils.getSubArray(data, offset)); offset+=100;

        setPreviousSeasonDemandDataCommand(new PreviousSeasonDemandDataCommand(getCommandFactory())); // 4Ch
        temp=ProtocolUtils.getSubArray2(data, offset, 46); offset+=46;
        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
            temp=ProtocolUtils.concatByteArrays(temp, ProtocolUtils.getSubArray2(data, 734, 18));
        getPreviousSeasonDemandDataCommand().parse(temp);

        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00) {
            temp=ProtocolUtils.concatByteArrays(temp, ProtocolUtils.getSubArray2(data, 752, 18));
            setThirdMetricValuesCommand(new ThirdMetricValuesCommand(getCommandFactory())); // 99h
            getThirdMetricValuesCommand().parse(temp);
        }


    }

    public int getSelfReadIndex() {
        return selfReadIndex;
    }

    public void setSelfReadIndex(int selfReadIndex) {
        this.selfReadIndex = selfReadIndex;
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

    public int getTouStatus() {
        return touStatus;
    }

    public void setTouStatus(int touStatus) {
        this.touStatus = touStatus;
    }

    public PreviousIntervalDemandCommand getPreviousIntervalDemandCommand() {
        return previousIntervalDemandCommand;
    }

    public void setPreviousIntervalDemandCommand(PreviousIntervalDemandCommand previousIntervalDemandCommand) {
        this.previousIntervalDemandCommand = previousIntervalDemandCommand;
    }

    public PreviousSeasonTOUDataRXCommand getPreviousSeasonTOUDataRXCommand() {
        return previousSeasonTOUDataRXCommand;
    }

    public void setPreviousSeasonTOUDataRXCommand(PreviousSeasonTOUDataRXCommand previousSeasonTOUDataRXCommand) {
        this.previousSeasonTOUDataRXCommand = previousSeasonTOUDataRXCommand;
    }

    public RateBinsAndTotalEnergyRXCommand getRateBinsAndTotalEnergyRXCommand() {
        return RateBinsAndTotalEnergyRXCommand;
    }

    public void setRateBinsAndTotalEnergyRXCommand(RateBinsAndTotalEnergyRXCommand RateBinsAndTotalEnergyRXCommand) {
        this.RateBinsAndTotalEnergyRXCommand = RateBinsAndTotalEnergyRXCommand;
    }

    public NegativeEnergyCommand getNegativeEnergyCommand() {
        return negativeEnergyCommand;
    }

    public void setNegativeEnergyCommand(NegativeEnergyCommand negativeEnergyCommand) {
        this.negativeEnergyCommand = negativeEnergyCommand;
    }

    public HighestMaximumDemandsCommand getHighestMaximumDemandsCommand() {
        return highestMaximumDemandsCommand;
    }

    public void setHighestMaximumDemandsCommand(HighestMaximumDemandsCommand highestMaximumDemandsCommand) {
        this.highestMaximumDemandsCommand = highestMaximumDemandsCommand;
    }

    public CurrentSeasonCumDemandAndLastResetRXCommand getCurrentSeasonCumDemandAndLastResetRXCommand() {
        return currentSeasonCumDemandAndLastResetRXCommand;
    }

    public void setCurrentSeasonCumDemandAndLastResetRXCommand(CurrentSeasonCumDemandAndLastResetRXCommand currentSeasonCumDemandAndLastResetRXCommand) {
        this.currentSeasonCumDemandAndLastResetRXCommand = currentSeasonCumDemandAndLastResetRXCommand;
    }

    public CurrentSeasonTOUDemandDataRXCommand getCurrentSeasonTOUDemandDataRXCommand() {
        return currentSeasonTOUDemandDataRXCommand;
    }

    public void setCurrentSeasonTOUDemandDataRXCommand(CurrentSeasonTOUDemandDataRXCommand currentSeasonTOUDemandDataRXCommand) {
        this.currentSeasonTOUDemandDataRXCommand = currentSeasonTOUDemandDataRXCommand;
    }

    public PreviousSeasonDemandDataCommand getPreviousSeasonDemandDataCommand() {
        return previousSeasonDemandDataCommand;
    }

    public void setPreviousSeasonDemandDataCommand(PreviousSeasonDemandDataCommand previousSeasonDemandDataCommand) {
        this.previousSeasonDemandDataCommand = previousSeasonDemandDataCommand;
    }

    public ThirdMetricValuesCommand getThirdMetricValuesCommand() {
        return thirdMetricValuesCommand;
    }

    public void setThirdMetricValuesCommand(ThirdMetricValuesCommand thirdMetricValuesCommand) {
        this.thirdMetricValuesCommand = thirdMetricValuesCommand;
    }


}
