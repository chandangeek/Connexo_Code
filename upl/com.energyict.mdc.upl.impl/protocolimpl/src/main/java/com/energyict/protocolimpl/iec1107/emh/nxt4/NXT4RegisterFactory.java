package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.upl.NoSuchRegisterException;

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
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sva
 * @since 4/11/2014 - 15:10
 */
public class NXT4RegisterFactory implements RegisterProtocol {

    private static ObisCode BILLING_COUNT_OBIS = ObisCode.fromString("1.0.0.1.0.255");
    private static ObisCode BILLING_TIMESTAMP_BASE_OBIS = ObisCode.fromString("0.0.0.1.2.255");

    private final NXT4 protocol;
    private int billingCount = -1;
    private Map<Integer, Date> billingPointDates = new HashMap<Integer, Date>();

    public NXT4RegisterFactory(NXT4 protocol) {
        this.protocol = protocol;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        Date billingPointTime = null;

        if (obisCode.getF() != 255) {
            int f = Math.abs(obisCode.getF());
            if (f >= getBillingCount()) {
                String msg = "Failed to read register " + obisCode + " - Invalid billing point, billing count is only " + getBillingCount();
                getProtocol().getLogger().warning(msg);
                throw new NoSuchRegisterException(msg);
            }

            byte billingPoint = (byte) (getBillingCount() - f);
            obisCode = ProtocolTools.setObisCodeField(obisCode, 5, billingPoint);
            billingPointTime = readBillingPointDate(billingPoint);
        }

        byte[] registerData = readRawRegister(obisCode);
        if (registerContainsBillingTimeStamp(obisCode)) {
            return new RegisterValue(obisCode, null, null, null, billingPointTime, new Date(), 0, billingPointTime.toString());
        } else if (registerContainsQuantity(obisCode)) {
            Quantity quantity = parseQuantity(registerData);
            Date eventTime = parseDate(registerData, 1);
            return new RegisterValue(obisCode, quantity, eventTime, billingPointTime);
        } else {
            String text = parseText(registerData);
            return new RegisterValue(obisCode, null, null, null, billingPointTime, new Date(), 0, text);
        }
    }

    private byte[] readRawRegister(ObisCode obisCode) throws IOException {
        byte[] registerData;
        if (getProperties().isDataReadout()) {
            registerData = readRegisterFromDataDump(obisCode);
        } else {
            registerData = readRegisterData(obisCode);
        }
        return registerData;
    }

    private byte[] readRegisterFromDataDump(ObisCode obisCode) throws IOException {
        try {
            DataDumpParser ddp = new DataDumpParser(getProtocol().getDataReadout());
            return ddp.getRegisterStrValue(EdisFormatter.formatObisAsEdis(obisCode)).getBytes();
        } catch (NoSuchRegisterException e) {
            String msg = "Failed to read object " + obisCode + " - NXT4RegisterFactory, readRegisterFromDataDump, " + e.getLocalizedMessage();
            getProtocol().getLogger().warning(msg);
            throw new NoSuchRegisterException(msg);
        }
    }

    private boolean registerContainsBillingTimeStamp(ObisCode obisCode) {
        ObisCode baseObis = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        return baseObis.equals(BILLING_TIMESTAMP_BASE_OBIS);
    }

    private boolean registerContainsQuantity(ObisCode obisCode) {
        return obisCode.getC() < 96;
        // Objects with C-field >= 96 do contain general info, instead of a quantity
        // E.g: C-field 96 = General and service entry objects
        //      C-filed 97 = Error register objects
    }

    private int getBillingCount() throws IOException {
        if (this.billingCount == -1) {
            byte[] data = readRawRegister(BILLING_COUNT_OBIS);
            this.billingCount = parseQuantity(data).getAmount().intValue();
        }
        return this.billingCount;
    }

    private Date readBillingPointDate(int billingPoint) throws IOException {
        if (! this.billingPointDates.containsKey(billingPoint)) {
            ObisCode billingPointObis = ProtocolTools.setObisCodeField(BILLING_TIMESTAMP_BASE_OBIS, 5, (byte) billingPoint);
            byte[] data = readRawRegister(billingPointObis);
            this.billingPointDates.put(billingPoint, parseDate(data, 0));
        }

        return this.billingPointDates.get(billingPoint);
    }

    private byte[] readRegisterData(ObisCode obisCode) throws IOException {
        String objectName = EdisFormatter.formatObisAsEdis(obisCode) + "(;)";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(objectName.getBytes());
        getProtocol().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] rawData = getProtocol().getFlagIEC1107Connection().receiveRawData();
        checkForError(rawData, obisCode);
        return rawData;
    }

    private void checkForError(byte[] rawData, ObisCode obisCode) throws NoSuchRegisterException {
        String dataStr = new String(rawData);
        if (dataStr.contains("ERROR")) {
            String msg = "Failed to read object " + obisCode + " - NXT4RegisterFactory, readRegisterData, " + getProtocol().getExceptionInfo("ERROR");
            getProtocol().getLogger().warning(msg);
            throw new NoSuchRegisterException(msg);
        }
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