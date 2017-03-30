/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.DataType;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DstSettings extends AbstractRegister {

    public DstSettings(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.DstSettings.getId();
    }

    private Date start;
    private int startMonth;
    private int startDay;
    private int startWDay;
    private int startHour;
    private DaylightAlgorithm startAlgorithm;

    private Date end;
    private int endMonth;
    private int endDay;
    private int endWDay;
    private int endHour;
    private DaylightAlgorithm endAlgorithm;

    private int writeMode;  // 0:  start date - 1: end date - 2: algorithms to use

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
        return start;
    }

    public DaylightAlgorithm getStartAlgorithm() {
        return startAlgorithm;
    }

    public DaylightAlgorithm getEndAlgorithm() {
        return endAlgorithm;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getStartWDay() {
        return startWDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public int getEndDay() {
        return endDay;
    }

    public int getEndWDay() {
        return endWDay;
    }

    public int getEndHour() {
        return endHour;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        startMonth = values.get(1).getValue();
        startDay = values.get(2).getValue();
        startWDay = values.get(3).getValue() == 0 ? 7 : values.get(3).getValue();
        startHour = 2;

        start.set(Calendar.MONTH, startMonth -1);
        start.set(Calendar.DAY_OF_WEEK, startWDay);
        start.set(Calendar.DAY_OF_MONTH, startDay);
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        startAlgorithm = DaylightAlgorithm.valueFromOrdinal(values.get(4).getValue());

        endMonth = values.get(6).getValue();
        endDay = values.get(7).getValue();
        endWDay = values.get(8).getValue() == 0 ? 7 : values.get(8).getValue();
        endHour = 3;

        end.set(Calendar.MONTH, endMonth -1);
        end.set(Calendar.DAY_OF_WEEK, endWDay);
        end.set(Calendar.DAY_OF_MONTH, endDay);
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        endAlgorithm = DaylightAlgorithm.valueFromOrdinal(values.get(9).getValue());

        this.start = start.getTime();
        this.end = end.getTime();
    }

    @Override
    protected byte[] getWriteASDU() {
        return ASDU.WriteRegister.getIdBytes();
    }

    @Override
    protected byte[] getWriteBytes() {
        byte[] request = null;
        int index = 0;

        if (writeMode == 0 ) {
            request = new byte[6];
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) startMonth;
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) startDay;
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) startWDay;
        }   else if (writeMode == 1) {
            request = new byte[6];
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) endMonth;
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) endDay;
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) endWDay;
        }   else if (writeMode == 2) {
            request = new byte[4];
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) startAlgorithm.ordinal();
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) endAlgorithm.ordinal();
        }  else {
            request = new byte[0];
        }
        return request;
    }

    public void setStart(int startMonth, int startDay, int startWDay) {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startWDay = startWDay;
        this.writeMode = 0;
    }

    public void setEnd(int endMonth, int endDay, int endWDay) {
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endMonth = endWDay;
        this.writeMode = 1;
    }

    public void setAlgorithms(DaylightAlgorithm startAlgorithm, DaylightAlgorithm endAlgorithm) throws IOException {
        this.startAlgorithm =  startAlgorithm;
        this.endAlgorithm = endAlgorithm;
        this.writeMode = 2;
    }
}