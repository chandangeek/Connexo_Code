package com.energyict.protocols.mdc.inbound.general.frames.parsing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class that parses the common received parameters from an inbound frame.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 10:55
 * Author: khe
 */
public class InboundParameters {

    private static final String DB_ID_ATTR = "dbaseId";
    private static final String IP_ATTR = "ip";
    private static final String SERIAL_ATTR = "serialId";
    private static final String COM_PORT_ATTR = "comPort";
    private static final String IP_ADDRESS_ATTR = "ipAddress";
    private static final String PORT_ATTR = "ipPort";
    private static final String METER_TYPE_ATTR = "meterType";
    private static final String READ_TIME_ATTR = "readTime";
    private static final int PORT_DEFAULT = 80;

    private String ip = null;
    private String comPort = null;
    private String ipAddress = null;
    private String meterType = null;
    private String serialNumber = null;
    private int ipPort = 80;
    private int dbaseId = -1;
    private Date readTime;

    private String[] parameters;

    public InboundParameters(String[] parameters) {
        this.parameters = parameters.clone();
    }

    public InboundParameters() {
        parameters = new String[0];
    }

    public void parse() {
        dbaseId = parseIntParameter(DB_ID_ATTR);
        ip = parseStringParameter(IP_ATTR);
        serialNumber = parseStringParameter(SERIAL_ATTR);
        comPort = parseStringParameter(COM_PORT_ATTR);
        ipAddress = parseStringParameter(IP_ADDRESS_ATTR);
        ipPort = parseIntParameter(PORT_ATTR, PORT_DEFAULT);
        meterType = parseStringParameter(METER_TYPE_ATTR);
        readTime = parseDateParameter(READ_TIME_ATTR);
    }

    private int parseIntParameter(String name) {
        return parseIntParameter(name, 0);
    }

    private Date parseDateParameter(String name) {
        String readTimeString = parseStringParameter(name);
        return parseReadTime(readTimeString);
    }

    private Date parseReadTime(String readTimeString) {
        if (readTimeString == null || !readTimeString.isEmpty()) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return formatter.parse(readTimeString);
        } catch (ParseException e) {
            return new Date();
        }
    }

    private int parseIntParameter(String name, int defaultValue) {
        String stringValue = parseStringParameter(name);
        if (stringValue == null) {
            return defaultValue;
        }
        else {
            return Integer.parseInt(stringValue);
        }
    }

    private String parseStringParameter(String name) {
        for (String parameter : parameters) {
            String[] nameAndValue = parameter.split("=");
            if (nameAndValue.length == 2 && name.equals(nameAndValue[0])) {
                return nameAndValue[1];
            }
        }
        return null;
    }

    public int getDbaseId() {
        return dbaseId;
    }

    public String getIp() {
        return ip;
    }

    public String getComPort() {
        return comPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMeterType() {
        return meterType;
    }

    public int getIpPort() {
        return ipPort;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getReadTime() {
        return readTime;
    }
}
