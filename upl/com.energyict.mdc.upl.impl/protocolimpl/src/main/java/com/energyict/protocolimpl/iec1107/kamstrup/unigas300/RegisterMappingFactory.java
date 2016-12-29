package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen
 */
public class RegisterMappingFactory {

    private List<RegisterMapping> registerMappings = new ArrayList<>();

    public static final String UNIGAS_SOFTWARE_REVISION_NUMBER = "UNIGAS software revision number";
    public static final String CI_SOFTWARE_REVISION_NUMBER = "CI software revision number";

    public static final String VM1 = "Forward absolute converter volume, CH1, at measuring conditions (VM1)";
    public static final String VC1_ERR = "Forward disturbed converter volume, CH1, corrected value (VC1_ERR)";
    public static final String VC1 = "Forward undisturbed converter volume, CH1, corrected value (VC1)";
    public static final String VB1 = "Forward undisturbed converter volume, CH1, at base conditions (VB1)";
    public static final String VB1_ERR = "Forward disturbed converter volume, at base conditions (VB1_ERR)";
    public static final String VM2 = "Forward absolute converter volume, CH2, at measuring conditions (VM2)";
    public static final String VM3 = "Forward absolute converter volume, CH3, at measuring conditions (VM3)";

    public static final String STATUS1 = "Status register 1";
    public static final String STATUS2 = "Status register 2";
    public static final String STATUS3 = "Status register 3";
    public static final String STATUS4 = "Status register 4";

    public static final String CF = "Correction factor, at measuring conditions (CF)";
    public static final String C = "Conversion factor, at base conditions (C)";
    public static final String Z = "Compressibility factor, at measuring conditions (Z)";
    public static final String Z_ZB = "Compressibility factor, at base conditions (Z/ZB)";
    public static final String P = "Absolute pressure, at measuring conditions (P)";
    public static final String T = "Absolute temperature, at measuring conditions (T)";

    public static final String PMAX_YESTERDAY = "Maximum value of pressure p yesterday";
    public static final String TMAX_YESTERDAY = "Maximum value of temperature t yesterday";
    public static final String PMIN_YESTERDAY = "Minimum value of pressure p yesterday";
    public static final String TMIN_YESTERDAY = "Minimum value of temperature t yesterday";
    public static final String QC_YESTERDAY = "Maximum value of Qc_nx5 yesterday";
    public static final String QB_YESTERDAY = "Maximum value of Qb_nx5 yesterday";

    public static final String PMAX_LASTMONTH = "Maximum value of pressure p last month";
    public static final String TMAX_LASTMONTH = "Maximum value of temperature t last month";
    public static final String PMIN_LASTMONTH = "Minimum value of pressure p last month";
    public static final String TMIN_LASTMONTH = "Minimum value of temperature t last month";
    public static final String QC_LASTMONTH = "Maximum value of Qc_nx5 last month";
    public static final String QB_LASTMONTH = "Maximum value of Qb_nx5 last month";

    public static final String PMAX_LASTYEAR = "Maximum value of pressure p last year";
    public static final String TMAX_LASTYEAR = "Maximum value of temperature t last year";
    public static final String PMIN_LASTYEAR = "Minimum value of pressure p last year";
    public static final String TMIN_LASTYEAR = "Minimum value of temperature t last year";
    public static final String QC_LASTYEAR = "Maximum value of Qc_nx5 last year";
    public static final String QB_LASTYEAR = "Maximum value of Qb_nx5 last year";

    public static final String PMAX_TODAY = "Maximum value of pressure p today";
    public static final String TMAX_TODAY = "Maximum value of temperature t today";
    public static final String PMIN_TODAY = "Minimum value of pressure p today";
    public static final String TMIN_TODAY = "Minimum value of temperature t today";
    public static final String QC_TODAY = "Maximum value of Qc_nx5 today";
    public static final String QB_TODAY = "Maximum value of Qb_nx5 today";

    public static final String PMAX_CURRENTMONTH = "Maximum value of pressure p current month";
    public static final String TMAX_CURRENTMONTH = "Maximum value of temperature t current month";
    public static final String PMIN_CURRENTMONTH = "Minimum value of pressure p current month";
    public static final String TMIN_CURRENTMONTH = "Minimum value of temperature t current month";
    public static final String QC_CURRENTMONTH = "Maximum value of Qc_nx5 current month";
    public static final String QB_CURRENTMONTH = "Maximum value of Qb_nx5 current month";

    public static final String PMAX_CURRENTYEAR = "Maximum value of pressure p current year";
    public static final String TMAX_CURRENTYEAR = "Maximum value of temperature t current year";
    public static final String PMIN_CURRENTYEAR = "Minimum value of pressure p current year";
    public static final String TMIN_CURRENTYEAR = "Minimum value of temperature t current year";
    public static final String QC_CURRENTYEAR = "Maximum value of Qc_nx5 current year";
    public static final String QB_CURRENTYEAR = "Maximum value of Qb_nx5 current year";

    public static final String APPLIANCE_TYPE = "Conversion algorithm and metorological function";

    public static final String DEVICE_SERIAL = "Device serial number";
    public static final String DEVICE_ADDRESS = "Device address";
    public static final String DEVICE_EANCODE = "Device EAN code";

    public static final String FW_VERSION_M = "Device firmware version bottom PCB";
    public static final String FW_VERSION_D = "Device firmware version top PCB";
    public static final String FW_CRC_M = "Device firmware CRC bottom PCB";
    public static final String FW_CRC_D = "Device firmware CRC top PCB";

    public static final String GSM_UPTIME = "GSM uptime";
    public static final String GSM_CONNECTIONTIME = "GSM communication time";
    public static final String GSM_SIGNAL = "GSM signal strength";

    public static final String BATTERY_V_UNILOG = "UNILOG Battery voltage";
    public static final String BATTERY_USED_UNILOG = "UNILOG Battery capacity used";

    public static final String BATTERY_V_UNIGAS = "UNIGAS300 Battery voltage";
    public static final String BATTERY_C_USED = "UNIGAS300 Battery capacity used";
    public static final String BATTERY_C_NEW = "UNIGAS300 Battery capacity in new condition";
    public static final String OPERATING_HOURS = "UNIGAS300 operating hours";

    public static final String SCHEDULER_START = "Start time of scheduler";

    /**
     * Creates a new instance of RegisterMappingFactory
     */
    public RegisterMappingFactory() {
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        add(VM1, "1:13.0.0", "7.1.0.13.0.255");
        add(VC1, "1:11.1.0", "7.2.0.11.1.255");
        add(VC1_ERR, "1:12.0.0", "7.1.0.12.0.255");
        add(VB1, "1:11.2.0", "7.2.0.11.2.255");
        add(VB1_ERR, "1:12.1.0", "7.1.0.12.1.255");
        add(VM2, "2:13.0.0", "7.2.0.13.0.255");
        add(VM3, "3:13.0.0", "7.3.0.13.0.255");

        add(STATUS1, "97:97.1", "0.0.97.97.1.255");
        add(STATUS2, "97:97.2", "0.0.97.97.2.255");
        add(STATUS3, "97:97.3", "0.0.97.97.3.255");
        add(STATUS4, "97:97.4", "0.0.97.97.4.255");

        add(CF, "1:51.0.0", "7.1.0.51.0.255");
        add(C, "1:52.2.0", "7.1.0.52.2.255");
        add(Z, "1:53.0.0", "7.1.0.53.0.255");
        add(Z_ZB, "1:53.2.0", "7.1.0.53.2.255");
        add(P, "1:42.0.0", "7.1.0.42.0.255");
        add(T, "1:41.0.0", "7.1.0.41.0.255");

        add(PMAX_YESTERDAY, "C.95.1", "7.1.42.21.1.255");
        add(TMAX_YESTERDAY, "C.95.2", "7.1.41.21.1.255");
        add(PMIN_YESTERDAY, "C.95.3", "7.1.42.18.1.255");
        add(TMIN_YESTERDAY, "C.95.4", "7.1.41.18.1.255");
        add(QC_YESTERDAY, "C.95.5", "7.1.43.23.1.255");
        add(QB_YESTERDAY, "C.95.6", "7.1.43.22.1.255");

        add(PMAX_LASTMONTH, "C.95.11", "7.1.42.21.3.255");
        add(TMAX_LASTMONTH, "C.95.12", "7.1.41.21.3.255");
        add(PMIN_LASTMONTH, "C.95.13", "7.1.42.18.3.255");
        add(TMIN_LASTMONTH, "C.95.14", "7.1.41.18.3.255");
        add(QC_LASTMONTH, "C.95.15", "7.1.43.23.3.255");
        add(QB_LASTMONTH, "C.95.16", "7.1.43.22.3.255");

        add(PMAX_LASTYEAR, "C.95.21", "7.1.42.21.5.255");
        add(TMAX_LASTYEAR, "C.95.22", "7.1.41.21.5.255");
        add(PMIN_LASTYEAR, "C.95.23", "7.1.42.18.5.255");
        add(TMIN_LASTYEAR, "C.95.24", "7.1.41.18.5.255");
        add(QC_LASTYEAR, "C.95.25", "7.1.43.23.5.255");
        add(QB_LASTYEAR, "C.95.26", "7.1.43.22.5.255");

        add(PMAX_TODAY, "C.95.31", "7.1.42.21.0.255");
        add(TMAX_TODAY, "C.95.32", "7.1.41.21.0.255");
        add(PMIN_TODAY, "C.95.33", "7.1.42.18.0.255");
        add(TMIN_TODAY, "C.95.34", "7.1.41.18.0.255");
        add(QC_TODAY, "C.95.35", "7.1.43.23.0.255");
        add(QB_TODAY, "C.95.36", "7.1.43.22.0.255");

        add(PMAX_CURRENTMONTH, "C.95.41", "7.1.42.21.2.255");
        add(TMAX_CURRENTMONTH, "C.95.42", "7.1.41.21.2.255");
        add(PMIN_CURRENTMONTH, "C.95.43", "7.1.42.18.2.255");
        add(TMIN_CURRENTMONTH, "C.95.44", "7.1.41.18.2.255");
        add(QC_CURRENTMONTH, "C.95.45", "7.1.43.23.2.255");
        add(QB_CURRENTMONTH, "C.95.46", "7.1.43.22.2.255");

        add(PMAX_CURRENTYEAR, "C.95.51", "7.1.42.21.4.255");
        add(TMAX_CURRENTYEAR, "C.95.52", "7.1.41.21.4.255");
        add(PMIN_CURRENTYEAR, "C.95.53", "7.1.42.18.4.255");
        add(TMIN_CURRENTYEAR, "C.95.54", "7.1.41.18.4.255");
        add(QC_CURRENTYEAR, "C.95.55", "7.1.43.23.4.255");
        add(QB_CURRENTYEAR, "C.95.56", "7.1.43.22.4.255");

        add(APPLIANCE_TYPE, "C.1.1", "7.1.53.12.0.255");

        add(DEVICE_SERIAL, "C.1.0", "0.0.96.1.0.255");
        add(DEVICE_ADDRESS, "C.90.1", "0.0.96.1.1.255");
        add(DEVICE_EANCODE, "C.96.0", "0.0.96.1.2.255");

        add(FW_VERSION_D, "0:0.2.0", "7.1.0.2.1.0");
        add(FW_VERSION_M, "0:0.2.1", "7.1.0.2.1.1");

        add(FW_CRC_D, "C.91.3", "0.0.96.50.0.1");
        add(FW_CRC_M, "C.91.4", "0.0.96.50.0.2");

        add(SCHEDULER_START, "C.90.11", "0.0.96.50.0.3");
        add(GSM_UPTIME, "C.90.4", "0.0.96.50.0.4");
        add(GSM_CONNECTIONTIME, "C.90.5", "0.0.96.50.0.5");
        add(GSM_SIGNAL, "C.90.7", "0.0.96.50.0.6");

        add(BATTERY_V_UNILOG, "C.90.6", "0.1.96.6.3.255");
        add(BATTERY_USED_UNILOG, "C.90.21", "0.1.96.6.1.255");

        add(BATTERY_V_UNIGAS, "C.6.3", "0.0.96.6.3.255");
        add(BATTERY_C_USED, "C.6.1", "0.0.96.6.1.255");
        add(BATTERY_C_NEW, "C.6.4", "0.0.96.6.4.255");
        add(OPERATING_HOURS, "C.8.0", "0.0.96.8.0.255");

    }

    private void add(String description, String register, String obis) {
        registerMappings.add(new RegisterMapping(description, register, ObisCode.fromString(obis)));
    }

    public List<RegisterMapping> getRegisterMappings() {
        return registerMappings;
    }

    public String findRegisterCode(ObisCode obisCode) throws NoSuchRegisterException {
        return this.findRegisterMapping(obisCode).getRegisterCode();
    }

    public ObisCode findObisCode(String description) throws IOException {
        return this.registerMappings
                .stream()
                .filter(registerMapping -> registerMapping.getDescription().equals(description))
                .findAny()
                .map(RegisterMapping::getObisCode)
                .orElseThrow(() -> new IOException("Not found!!!"));
    }

    public RegisterMapping findRegisterMapping(ObisCode obisCode) throws NoSuchRegisterException {
        return this.registerMappings
                .stream()
                .filter(registerMapping -> registerMapping.getObisCode().equals(obisCode))
                .findAny()
                .orElseThrow(() -> new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!"));
    }

}