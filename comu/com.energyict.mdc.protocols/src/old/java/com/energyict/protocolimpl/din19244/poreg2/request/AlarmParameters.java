/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.DataType;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;
import com.energyict.protocolimpl.din19244.poreg2.request.register.AbstractRegister;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmParameters extends AbstractRegister {

    public AlarmParameters(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int response = data[0] & 0xFF;
        if (response != getExpectedResponseType()) {
            throw new IOException("Unexpected response. Expected: " + Response.getDescription(getExpectedResponseType()) + ", received: " + Response.getDescription(response));
        }
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.AlarmParam.getId();
    }

    @Override
    protected int getResponseASDU() {
        return ASDU.RegisterResponse.getId();
    }

    private Date date;

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    protected byte[] getWriteASDU() {
        return ASDU.WriteRegister.getIdBytes();
    }

    @Override
    protected byte[] getWriteBytes() {
        Calendar timeStamp = getTimeStamp();

        byte[] request = new byte[19];
        byte[] years = ProtocolTools.getBytesFromInt(timeStamp.get(Calendar.YEAR), 2);
        int offset = 0;

        request[offset++] = (byte) DataType.Word.getId();
        request[offset++] = (byte) years[1];
        request[offset++] = (byte) years[0];
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) (timeStamp.get(Calendar.MONTH) + 1);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) timeStamp.get(Calendar.DAY_OF_MONTH);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) (timeStamp.get(Calendar.DAY_OF_WEEK) - 1);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) 0;
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) timeStamp.get(Calendar.HOUR_OF_DAY);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) timeStamp.get(Calendar.MINUTE);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) timeStamp.get(Calendar.SECOND);
        request[offset++] = (byte) DataType.Byte.getId();
        request[offset++] = (byte) 0;

        return request;
    }

    private Calendar getTimeStamp() {
        Calendar now = Calendar.getInstance(poreg.getTimeZone());
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.DAY_OF_WEEK, now.get(Calendar.DAY_OF_WEEK));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE) + 2);
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        return cal;
    }
}