package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * @author sva
 * @since 4/11/2014 - 15:10
 */
public class NXT4RegisterFactory implements RegisterProtocol {

    private final NXT4 protocol;

    public NXT4RegisterFactory(NXT4 protocol) {
        this.protocol = protocol;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        byte[] registerData;
        if (getProperties().isDataReadout()) {
            registerData = readRegisterFromDataDump(obisCode);
        } else {
            registerData = readRegisterData(obisCode);
        }

        if (registerContainsQuantity(obisCode)) {
            Quantity quantity = parseQuantity(registerData);
            Date eventTime = parseDate(registerData, 1);
            return new RegisterValue(obisCode, quantity, eventTime);
        } else {
            String text = parseText(registerData);
            return new RegisterValue(obisCode, text);
        }
    }

    private byte[] readRegisterFromDataDump(ObisCode obisCode) throws IOException {
        DataDumpParser ddp = new DataDumpParser(getProtocol().getDataReadout());
        return ddp.getRegisterStrValue(EdisFormatter.formatObisAsEdis(obisCode)).getBytes();
    }

    private boolean registerContainsQuantity(ObisCode obisCode) {
        return obisCode.getC() < 96;
        // Objects with C-field >= 96 do contain general info, instead of a quantity
        // E.g: C-field 96 = General and service entry objects
        //      C-filed 97 = Error register objects
    }

    private byte[] readRegisterData(ObisCode obisCode) throws IOException {
        String objectName = EdisFormatter.formatObisAsEdis(obisCode) + "(;)";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(objectName.getBytes());
        getProtocol().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        return getProtocol().getFlagIEC1107Connection().receiveRawData();
    }

    private Quantity parseQuantity(byte[] data) throws IOException {
        DataParser dp = new DataParser(getProtocol().getTimeZone());
        return dp.parseQuantityBetweenBrackets(data, 0, 0);
    }

    private String parseText(byte[] data) throws IOException {
        DataParser dp = new DataParser(getProtocol().getTimeZone());
        return dp.parseBetweenBrackets(data, 0, 0);
    }

    private Date parseDate(byte[] data, int pos) throws IOException {
        Date date;
        try {
            DataParser dp = new DataParser(getProtocol().getTimeZone());
            VDEWTimeStamp vts = new VDEWTimeStamp(getProtocol().getTimeZone());
            String dateStr = dp.parseBetweenBrackets(data, 0, pos);
            if ("".compareTo(dateStr) == 0) {
                return null;
            }
            vts.parse(dateStr);
            date = vts.getCalendar().getTime();
            return date;
        } catch (DataParseException e) {
            return null;    // Most likely because the data didn't contain a date, then return null
        }
    }

    public NXT4 getProtocol() {
        return protocol;
    }

    private NXT4Properties getProperties() {
        return getProtocol().getProperties();
    }
}
