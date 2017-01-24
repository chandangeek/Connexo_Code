/**
 * AS230ObisMapper.java
 *
 * Created on 24-nov-2008, 11:46:49 by jme
 *
 */
package com.energyict.protocolimpl.iec1107.as220;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @author jme
 */
public class AS220ObisCodeMapper {

    public static final String ID1 = "Device ID1";
    public static final String ID2 = "Device ID2";
    public static final String ID3 = "Device ID3";
    public static final String ID4 = "Device ID4";
    public static final String ID5 = "Device ID5";
    public static final String ID6 = "Device ID6";
    public static final String SERIAL = "Device Serial number";
    public static final String FIRMWARE = "Device  Firmware/Hardware information";
    public static final String FIRMWAREID = "Firmware version ID";
    public static final String DATETIME = "Date and time (0.9.1 0.9.2)";
    public static final String BILLINGCOUNTER = "Billing counter";
    public static final String BILLINGRESETTIME = "Billing reset time";
//    public static final String PROGRAMCOUNTER = "Program Counter";

    public static final String IEC1107_ID = "Device IEC1107_ID";
    public static final String IEC1107_ADDRESS_OP = "Device IEC1107_ADDRESS_OP (optical)";
    public static final String IEC1107_ADDRESS_EL = "Device IEC1107_ADDRESS_EL (electrical)";


    private LinkedHashMap obisMap = new LinkedHashMap();
    private AS220 aS220 = null;

    public AS220ObisCodeMapper(AS220 aS220) {
        this.aS220 = aS220;
        initObisUnconnected();
    }

    /**
     * Initialize the map of obiscodes without reading the billing counter
     */
    private void initObisUnconnected() {

        this.obisMap.put("1.1.0.1.0.255", BILLINGCOUNTER);
        this.obisMap.put("1.1.0.1.2.255", BILLINGRESETTIME);
        this.obisMap.put("1.1.0.1.2.255", DATETIME);

        this.obisMap.put("1.1.0.2.0.255", FIRMWAREID);

//        this.obisMap.put("0.0.96.1.4.255", PROGRAMCOUNTER);

        this.obisMap.put("1.1.0.0.0.255", SERIAL);
        this.obisMap.put("0.0.96.1.0.255", SERIAL);
        this.obisMap.put("1.1.0.0.1.255", ID1);
        this.obisMap.put("1.1.0.0.2.255", ID2);
        this.obisMap.put("1.1.0.0.3.255", ID3);
        this.obisMap.put("1.1.0.0.4.255", ID4);
        this.obisMap.put("1.1.0.0.5.255", ID5);
        this.obisMap.put("1.1.0.0.6.255", ID6);

        this.obisMap.put("1.1.0.0.7.255", IEC1107_ID);
        this.obisMap.put("1.1.0.0.8.255", IEC1107_ADDRESS_OP);
        this.obisMap.put("1.1.0.0.9.255", IEC1107_ADDRESS_EL);

        this.obisMap.put("1.1.0.0.10.255", FIRMWARE);
        this.obisMap.put("0.0.96.1.5.255", FIRMWARE);

        this.obisMap.put("1.1.32.7.0.255", "U L1, total");
        this.obisMap.put("1.1.52.7.0.255", "U L2, total");
        this.obisMap.put("1.1.72.7.0.255", "U L3, total");
        this.obisMap.put("1.1.31.7.0.255", "I L1, total");
        this.obisMap.put("1.1.51.7.0.255", "I L2, total");
        this.obisMap.put("1.1.71.7.0.255", "I L3, total");
        this.obisMap.put("1.1.21.7.0.255", "+P L1, T0");
        this.obisMap.put("1.1.22.7.0.255", "-P L1, T0");

    }

    /**
     * Initialize the map of obiscodes, and try to read the billing counter to
     * add only the used historical registers to te obismap
     *
     * @throws IOException when something goes wrong while reading the billing counter
     */
    public void initObis() throws IOException {
        String obis;
        String dscr;

        this.obisMap.put("1.1.1.8.0.255", "+A, Time integral 1, T0 (1.8.0)");
        obis = "1.1.1.8.0.VZ";
        dscr = "+A, Time integral 1, T0 (1.8.0*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.1.8.1.255", "+A, Time integral 1, T1 (1.8.1)");
        obis = "1.1.1.8.1.VZ";
        dscr = "+A, Time integral 1, T1 (1.8.1*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.1.8.2.255", "+A, Time integral 1, T2 (1.8.2)");
        obis = "1.1.1.8.2.VZ";
        dscr = "+A, Time integral 1, T1 (1.8.2*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.2.8.0.255", "-A, Time integral 1, T0 (2.8.0)");
        obis = "1.1.2.8.0.VZ";
        dscr = "-A, Time integral 1, T0 (2.8.0*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.2.8.1.255", "-A, Time integral 1, T1 (2.8.1)");
        obis = "1.1.2.8.1.VZ";
        dscr = "-A, Time integral 1, T1 (2.8.1*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.2.8.2.255", "-A, Time integral 1, T2 (2.8.2)");
        obis = "1.1.2.8.2.VZ";
        dscr = "-A, Time integral 1, T2 (2.8.2*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.1.6.1.255", "+P, Max , M1 (1.6.1)");
        obis = "1.1.1.6.1.VZ";
        dscr = "+P, Max , M1 (1.6.1*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.2.6.1.255", "-P, Max , M1 (2.6.1)");
        obis = "1.1.2.6.1.VZ";
        dscr = "-P, Max , M1 (2.6.1*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.1.6.2.255", "+P, Max , M2 (1.6.2)");
        obis = "1.1.1.6.2.VZ";
        dscr = "+P, Max , M2 (1.6.2*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.2.6.2.255", "-P, Max , M2 (2.6.2)");
        obis = "1.1.2.6.2.VZ";
        dscr = "-P, Max , M2 (2.6.2*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.9.6.0.255", "Apparent Power Total (9.6.0)");
        obis = "1.1.9.6.0.VZ";
        dscr = "S, App Total, (9.6.0*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.9.6.1.255", "Apparent Power Rate 1 (9.6.1)");
        obis = "1.1.9.6.1.VZ";
        dscr = "S, App. R1, (9.6.1*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.9.6.2.255", "Apparent Power Rate 2 (9.6.2)");
        obis = "1.1.9.6.2.VZ";
        dscr = "S, App. R2, (9.6.2*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.9.6.3.255", "Apparent Power Rate 3 (9.6.3)");
        obis = "1.1.9.6.3.VZ";
        dscr = "S, App. R3, (9.6.3*";
        addBillingsToObisMap(obis, dscr);

        this.obisMap.put("1.1.9.6.4.255", "Apparent Power Rate 4 (9.6.4)");
        obis = "1.1.9.6.4.VZ";
        dscr = "S, App. R4, (9.6.4*";
        addBillingsToObisMap(obis, dscr);
    }

    /**
     * Add the given obiscode to the obisCodeMap
     *
     * @param obis        the used obisCode
     * @param description the description going with this obiscode
     * @throws IOException
     */
    private void addBillingsToObisMap(String obis, String description) throws IOException {
        for (int i = 0; i < this.aS220.getBillingCount(); i++) {
            String bpOString = obis;
            if (i > 0) {
                bpOString = bpOString + "-" + i;
            }
            String bpDscr = description + (this.aS220.getBillingCount() - i) + ")";
            this.obisMap.put(bpOString, bpDscr);
        }
    }

    /**
     * Get the map with obiscodes, supported by the protocol and the device.
     *
     * @return the map with obiscodes
     */
    public LinkedHashMap getObisMap() {
        return this.obisMap;
    }

}