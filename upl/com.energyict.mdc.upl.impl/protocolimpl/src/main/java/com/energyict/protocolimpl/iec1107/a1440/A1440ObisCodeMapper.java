package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jme
 *
 */
class A1440ObisCodeMapper {

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
    public static final String ERROR_REGISTER = "Error register";
    public static final String ALARM_REGISTER = "Alarm register";

    public static final String IEC1107_ID = "Device IEC1107_ID";
    public static final String IEC1107_ADDRESS_OP = "Device IEC1107_ADDRESS_OP (optical)";
    public static final String IEC1107_ADDRESS_EL = "Device IEC1107_ADDRESS_EL (electrical)";

    public static final String PARAMETER_IDENTIFICATION = "Parameter identification";

    private final Map<String, String> obisMap = new LinkedHashMap<>();
    private final A1440 a1440;

    A1440ObisCodeMapper(A1440 a1440) {
        this.a1440 = a1440;
        initObisUnconnected();
    }

    /**
     * Initialize the map of obiscodes without reading the billing counter
     */
    private void initObisUnconnected() {

        this.obisMap.put("1.1.0.1.0.255", BILLINGCOUNTER);
        this.obisMap.put("1.1.0.1.2.255", DATETIME );

        this.obisMap.put("1.1.0.2.0.255", FIRMWAREID);
        this.obisMap.put("1.1.0.2.1.255", PARAMETER_IDENTIFICATION);

        this.obisMap.put("1.1.0.0.0.255", SERIAL);
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

        this.obisMap.put("1.1.32.7.0.255", "U L1, total");
        this.obisMap.put("1.1.52.7.0.255", "U L2, total");
        this.obisMap.put("1.1.72.7.0.255", "U L3, total");

        this.obisMap.put("1.1.31.7.0.255", "I L1, total");
        this.obisMap.put("1.1.51.7.0.255", "I L2, total");
        this.obisMap.put("1.1.71.7.0.255", "I L3, total");

        this.obisMap.put("1.1.1.7.0.255",  "+P total, T0");
        this.obisMap.put("1.1.21.7.0.255", "+P L1, T0");
        this.obisMap.put("1.1.41.7.0.255", "+P L2, T0");
        this.obisMap.put("1.1.61.7.0.255", "+P L3, T0");

        this.obisMap.put("1.1.2.7.0.255",  "-P total, T0");
        this.obisMap.put("1.1.22.7.0.255", "-P L1, T0");
        this.obisMap.put("1.1.42.7.0.255", "-P L2, T0");
        this.obisMap.put("1.1.62.7.0.255", "-P L3, T0");

        this.obisMap.put("0.0.97.97.0.255", ERROR_REGISTER);
        this.obisMap.put("0.0.97.97.1.255", ALARM_REGISTER);
    }

    public void initObis() throws IOException {
        this.obisMap.put( "1.1.1.8.0.255", "+A, Time integral 1, T0 (1.8.0)" );
        addBillingPoints("1.1.1.8.0.VZ", "+A, Time integral 1, T0 (1.8.0*");

        this.obisMap.put( "1.1.1.8.1.255", "+A, Time integral 1, T1 (1.8.1)" );
        addBillingPoints("1.1.1.8.1.VZ", "+A, Time integral 1, T1 (1.8.1*");

        this.obisMap.put( "1.1.1.8.2.255", "+A, Time integral 1, T2 (1.8.2)" );
        addBillingPoints("1.1.1.8.2.VZ","+A, Time integral 1, T1 (1.8.2*");

        this.obisMap.put( "1.1.2.8.0.255", "-A, Time integral 1, T0 (2.8.0)" );
        addBillingPoints("1.1.2.8.0.VZ", "-A, Time integral 1, T0 (2.8.0*");

        this.obisMap.put( "1.1.2.8.1.255", "-A, Time integral 1, T1 (2.8.1)" );
        addBillingPoints("1.1.2.8.1.VZ", "-A, Time integral 1, T1 (2.8.1*");

        this.obisMap.put( "1.1.2.8.2.255", "-A, Time integral 1, T2 (2.8.2)" );
        addBillingPoints("1.1.2.8.2.VZ", "-A, Time integral 1, T2 (2.8.2*");

        this.obisMap.put( "1.1.1.6.1.255", "+P, Max , M1 (1.6.1)" );
        addBillingPoints("1.1.1.6.1.VZ", "+P, Max , M1 (1.6.1*");

        this.obisMap.put( "1.1.2.6.1.255", "-P, Max , M1 (2.6.1)" );
        addBillingPoints("1.1.2.6.1.VZ", "-P, Max , M1 (2.6.1*");

        this.obisMap.put( "1.1.1.6.2.255", "+P, Max , M2 (1.6.2)" );
        addBillingPoints("1.1.1.6.2.VZ", "+P, Max , M2 (1.6.2*");

        this.obisMap.put( "1.1.2.6.2.255", "-P, Max , M2 (2.6.2)" );
        addBillingPoints("1.1.2.6.2.VZ", "-P, Max , M2 (2.6.2*");

    }

    private void addBillingPoints(String obis, String dscr) throws IOException {
        int numberOfBillingPointsInDevice = Math.min(this.a1440.getBillingCount(), 100);
        for( int i = 0; i < numberOfBillingPointsInDevice; i ++ ) {
            String bpOString = obis;
            if( i > 0 ) {bpOString = bpOString + "-" + i;}
            this.obisMap.put(bpOString, buildDescription(dscr, i, this.a1440.getBillingCount()));
        }
    }

    private String buildDescription(String baseDescription, int index, int billingCount) {
        return baseDescription + ProtocolUtils.buildStringDecimal( (billingCount - index)%100, 2) + ")";
    }

    /**
     * Get the map with obiscodes, supported by the protocol and the device.
     * @return the map with obiscodes
     */
    Map<String, String> getObisMap() {
        return this.obisMap;
    }

}
