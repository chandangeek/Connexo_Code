package com.elster.protocolimpl.lis100.testutils;

import java.io.*;
import java.util.ArrayList;

/**
 * class to read a lis100 temp file and presents the data as a object factory and a data stream
 * <p/>
 * User: heuckeg
 * Date: 02.02.11
 * Time: 09:17
 */
@SuppressWarnings({"unused"})
public class TempReader {

    private String deviceNo;
    private String swVersion;
    private String totalCounter;
    private String setblCounter;
    private String stateRegister;
    private String cpValue;
    private String interval;
    private String customerNo;
    private String meterNo;
    private String factor;
    private String unit;
    private String calcType;
    private String h2Bom;
    private String bod;

    private ArrayList<Integer> ivData = new ArrayList<Integer>();

    public TempReader(InputStream stream) {
        BufferedReader file = new BufferedReader(new InputStreamReader(stream));

        h2Bom = "0";
        bod = "0600";

        /* parse temp file */
        String line;
        String[] data;
        try {
            /* 1. line: Date,Time,Device no, Readout date */
            line = file.readLine();
            data = line.split(",");
            deviceNo = data[2].trim();

            /* 2. line:  SW version,Total counter RO,set. counter RO, ...  */
            line = file.readLine();
            data = line.split(",");
            swVersion = data[0].trim();
            totalCounter = data[1].trim();
            setblCounter = data[2].trim();

            /* 3. line:  state register,cp value,Interval */
            line = file.readLine();
            data = line.split(",");
            stateRegister = data[0].trim();
            cpValue = data[1].trim();
            interval = data[2].trim();

            /* 4. line:  Customer no,Meter no, factor | unit | calc type, ... */
            line = file.readLine();
            data = line.split(",");
            customerNo = data[0].trim();
            meterNo = data[1].trim();
            line = data[2].trim();
            if (line.length() > 0) {
                data = line.split("\\|");
                factor = data[0].trim();
                unit = data[1].trim();
                calcType = data[2].trim();
            } else {
                factor = "1.0";
                unit = "m3";
                calcType = "VZ";
            }

            /* 5. line: skip... */
            file.readLine();

            /* 6. line: skip... */
            file.readLine();

            /* 7. line: skip... */
            line = file.readLine();
            data = line.split(",");
            if ((data.length > 1) && ( data[1].length() > 0)) {
                h2Bom = data[1];
            }

            /* 8. line: skip... */
            file.readLine();

            String v;
            int i;
            do {
                line = file.readLine();
                if (line != null) {
                    data = line.split(",");

                    for (String s : data) {
                        v = s.trim();
                        if (v.length() > 0) {
                            if ((v.startsWith("&h")) || (v.startsWith("&H"))) {
                                i = Integer.parseInt(v.substring(2), 16);
                            } else {
                                i = Integer.parseInt(v);
                            }
                            ivData.add(i);
                        }
                    }
                }
            } while (line != null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public String getTotalCounter() {
        return totalCounter;
    }

    public String getSetblCounter() {
        return setblCounter;
    }

    public String getStateRegister() {
        return stateRegister;
    }

    public String getCpValue() {
        return cpValue;
    }

    public String getInterval() {
        return interval;
    }

    public String getCustomerNo() {
        return customerNo;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public String getFactor() {
        return factor;
    }

    public String getUnit() {
        return unit;
    }

    public String getCalcType() {
        return calcType;
    }

    public String getH2Bom() {
        return h2Bom;
    }

    public String getBod() {
        return bod;
    }
    public ArrayList<Integer> getIvData() {
        return ivData;
    }

}
