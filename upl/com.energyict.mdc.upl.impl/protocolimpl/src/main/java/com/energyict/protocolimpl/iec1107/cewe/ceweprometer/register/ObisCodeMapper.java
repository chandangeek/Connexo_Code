package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.AbstractValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.EnergyValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.FloatValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.InstantValuesFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.IntegerValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.MaximumDemandValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.StringValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.TOUValueFactory;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory.TimeValueFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author fbo
 */

public class ObisCodeMapper {

    public static final int D_MD_1 = 6;
    public static final int D_MD_2 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK;
    public static final int D_MD_3 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK + 1;

    private final CewePrometer prometer;

    /**
     * Collection for sorting the keys
     */
    private Set<ObisCodeWrapper> keys = new LinkedHashSet<ObisCodeWrapper>();
    /**
     * HashMap with the ValueFactories per ObisCode
     */
    private Map<ObisCodeWrapper, AbstractValueFactory> obisMap = new HashMap<ObisCodeWrapper, AbstractValueFactory>();

    /**
     * Creates a new instance of ObisCodeMapping
     */
    public ObisCodeMapper(CewePrometer cewePrometer) throws IOException {
        this.prometer = cewePrometer;
        init();
    }

    /**
     * @return a RegisterInfo for the obiscode
     */
    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        AbstractValueFactory vFactory = get(obisCode);
        if (vFactory == null) {
            return new RegisterInfo("not supported");
        }
        return new RegisterInfo(vFactory.getDescription());
    }

    /**
     * @return a RegisterValue for the obiscode
     */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        AbstractValueFactory vFactory = get(obisCode);
        if (vFactory == null) {
            throw new NoSuchRegisterException();
        }
        return vFactory.getRegisterValue(obisCode);
    }

    /**
     * Retrieves objects from the ObisCodeMap
     */
    public AbstractValueFactory get(ObisCode o) {
        return obisMap.get(new ObisCodeWrapper(o));
    }

    /**
     * Add objects to the ObisCodeMap
     */
    private void putGeneric(AbstractValueFactory factory) {
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    /**
     * Add objects to the ObisCodeMap
     */
    private void putStd(String ocs, ProRegister[] rArray, int fieldIdx) {
        ObisCode oc = ObisCode.fromString(ocs);
        EnergyValueFactory factory = new EnergyValueFactory(oc, rArray, fieldIdx, prometer);
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    private void putFactory(AbstractValueFactory f, ObisCodeWrapper ocw) {
        if (keys.contains(ocw)) {
            throw new IllegalArgumentException("obiscode already exists " + ocw);
        }
        keys.add(ocw);
        obisMap.put(ocw, f);
    }

    private void putMD(String oc, int mdId) {
        ObisCode obisCode = ObisCode.fromString(oc);
        MaximumDemandValueFactory factory = new MaximumDemandValueFactory(obisCode, mdId, prometer);
        factory.setObisCode(ObisCode.fromString(oc));
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    private void putTOU(String oc, int touPhenomenon) {

        ObisCode o = ObisCode.fromString(oc);

        for (int e = 1; e < 9; e++) {

            String f = "" + o.getF();
            if (o.getF() == 0) {
                f = "VZ";
            }
            if (o.getF() < 0) {
                f = "VZ" + o.getF();
            }

            String oString = "";
            oString += o.getA() + ".";
            oString += o.getB() + ".";
            oString += o.getC() + ".";
            oString += o.getD() + ".";
            oString += e + ".";
            oString += f;

            ObisCode eo = ObisCode.fromString(oString);

            TOUValueFactory fct = new TOUValueFactory(eo, touPhenomenon, prometer);
            ObisCodeWrapper ocw = new ObisCodeWrapper(eo);

            putFactory(fct, ocw);

        }

    }

    /**
     * Add objects to the ObisCodeMap
     */
    private void putTime(String oc) {
        ObisCode o = ObisCode.fromString(oc);
        TimeValueFactory factory = new TimeValueFactory(o, prometer);
        factory.setObisCode(ObisCode.fromString(oc));
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    /**
     * @return construct extended logging
     */
    public String getExtendedLogging() throws IOException {
        StringBuffer result = new StringBuffer();
        List obisList = getMeterSupportedObisCodes();
        Iterator i = obisList.iterator();
        while (i.hasNext()) {
            ObisCode obc = (ObisCode) i.next();
            result.append(obc.toString() + " " + getRegisterInfo(obc) + "\n");
            result.append(getRegisterValue(obc).toString() + "\n");
        }
        return result.toString();
    }

    /**
     * @return short desciption of ALL the possibly available obiscodes
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("All possibly supported ObisCodes \n");
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            ObisCodeWrapper key = (ObisCodeWrapper) i.next();
            try {
                result.append(key + " " + getRegisterInfo(key.getObisCode()) + "\n");
            } catch (IOException e) {
                result.append(key + " exception for info ");
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * This is the init for the actual values, this method does not
     * read any register configuration information, since that requires
     * communication.
     *
     * @throws IOException
     */
    private void init() throws IOException {

        String[] f = new String[]{
                "255", "VZ", "VZ-1", "VZ-2", "VZ-3", "VZ-4", "VZ-5", "VZ-6", "VZ-7",
                "VZ-8", "VZ-9", "VZ-10", "VZ-11", "VZ-12", "VZ-13"
        };

        for (int bpI = 0; bpI < f.length; bpI++) {

            /* Energy registers */
            putStd("1.1.1.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 0);
            putStd("1.1.2.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 1);
            putStd("1.1.5.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 2);
            putStd("1.1.6.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 3);
            putStd("1.1.7.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 4);
            putStd("1.1.8.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 5);
            putStd("1.1.9.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 6);
            putStd("1.1.10.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 7);
            putStd("1.1.3.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 8);
            putStd("1.1.4.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 9);
            putStd("1.1.128.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 10);
            putStd("1.1.129.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 11);

            /* External Registers */
            putStd("1.1.131.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 0);
            putStd("1.1.132.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 1);
            putStd("1.1.133.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 2);
            putStd("1.1.134.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 3);
            putStd("1.1.135.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 4);
            putStd("1.1.136.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 5);
            putStd("1.1.137.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 6);
            putStd("1.1.138.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 7);

            putTOU("1.1.1.8.1." + f[bpI], CeweRegisters.TOU_ACTIVE_ENERGY_IMP);
            putTOU("1.1.2.8.1." + f[bpI], CeweRegisters.TOU_ACTIVE_ENERGY_EXP);
            putTOU("1.1.3.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_IMP);
            putTOU("1.1.4.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_EXP);
            putTOU("1.1.128.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_IND);
            putTOU("1.1.129.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_CAP);
            putTOU("1.1.5.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_QI);
            putTOU("1.1.6.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_QII);
            putTOU("1.1.7.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_QIII);
            putTOU("1.1.8.8.1." + f[bpI], CeweRegisters.TOU_REACTIVE_ENERGY_QIV);
            putTOU("1.1.9.8.1." + f[bpI], CeweRegisters.TOU_APPARENT_ENERGY_IMP);
            putTOU("1.1.10.8.1." + f[bpI], CeweRegisters.TOU_APPARENT_ENERGY_EXP);

            putTOU("1.1.131.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_1);
            putTOU("1.1.132.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_2);
            putTOU("1.1.133.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_3);
            putTOU("1.1.134.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_4);
            putTOU("1.1.135.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_5);
            putTOU("1.1.136.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_6);
            putTOU("1.1.137.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_7);
            putTOU("1.1.138.8.1." + f[bpI], CeweRegisters.TOU_EXTERNAL_REG_8);


            /* Maximum demand */
            int mdIdx = CeweRegisters.MD_ACTIVE_POWER_IMP;
            putMD("1.1.1." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.1." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.1." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_ACTIVE_POWER_EXP;
            putMD("1.1.2." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.2." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.2." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_IMP;
            putMD("1.1.3." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.3." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.3." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_EXP;
            putMD("1.1.4." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.4." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.4." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_IND;
            putMD("1.1.128." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.128." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.128." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_CAP;
            putMD("1.1.129." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.129." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.129." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_QI;
            putMD("1.1.5." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.5." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.5." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_QII;
            putMD("1.1.6." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.6." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.6." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_QIII;
            putMD("1.1.7." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.7." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.7." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_REACTIVE_POWER_QIV;
            putMD("1.1.8." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.8." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.8." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_APPARENT_POWER_IMP;
            putMD("1.1.9." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.9." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.9." + D_MD_3 + ".0." + f[bpI], mdIdx);

            mdIdx = CeweRegisters.MD_APPARENT_POWER_EXP;
            putMD("1.1.10." + D_MD_1 + ".0." + f[bpI], mdIdx);
            putMD("1.1.10." + D_MD_2 + ".0." + f[bpI], mdIdx);
            putMD("1.1.10." + D_MD_3 + ".0." + f[bpI], mdIdx);

            /* create obiscodes for time register */
            putTime("1.1.0.1.0." + f[bpI]);

        }

        putStd("1.1.21.8.0.255", prometer.getRegisters().getrEenergy(), 12);
        putStd("1.1.41.8.0.255", prometer.getRegisters().getrEenergy(), 13);
        putStd("1.1.61.8.0.255", prometer.getRegisters().getrEenergy(), 14);
        putStd("1.1.22.8.0.255", prometer.getRegisters().getrEenergy(), 15);
        putStd("1.1.42.8.0.255", prometer.getRegisters().getrEenergy(), 16);
        putStd("1.1.62.8.0.255", prometer.getRegisters().getrEenergy(), 17);


        putGeneric(new FloatValueFactory("0.0.96.50.1.255", prometer, prometer.getRegisters().getrVoltageUnbalance(), Unit.get("%")));
        putGeneric(new FloatValueFactory("0.0.96.9.0.255", prometer, prometer.getRegisters().getrInternalTemp(), Unit.get(BaseUnit.DEGREE_CELSIUS))); // Meter internal temperature
        putGeneric(new IntegerValueFactory("0.0.96.6.6.255", prometer, prometer.getRegisters().getrBatteryLeft(), Unit.get("s"))); // Battery backup time left

        putInstant("1.1.32.7.0.255", 1);  // Phase voltage L1
        putInstant("1.1.52.7.0.255", 2);  // Phase voltage L2
        putInstant("1.1.72.7.0.255", 3);  // Phase voltage L3

        putInstant("1.1.32.7.1.255", 4);  // line voltage L1-L2
        putInstant("1.1.52.7.1.255", 5);  // line voltage L2-L3
        putInstant("1.1.72.7.1.255", 6);  // line voltage L3-L1

        putInstant("1.1.31.7.0.255", 7);  // Current L1
        putInstant("1.1.51.7.0.255", 8);  // Current L2
        putInstant("1.1.71.7.0.255", 9);  // Current L3

        putInstant("1.1.32.7.2.255", 10);  // Phase symmetry voltage L1
        putInstant("1.1.52.7.2.255", 11);  // Phase symmetry voltage L2
        putInstant("1.1.72.7.2.255", 12);  // Phase symmetry voltage L3

        putInstant("1.1.31.7.2.255", 13);  // Phase symmetry current L1
        putInstant("1.1.51.7.2.255", 14);  // Phase symmetry current L2
        putInstant("1.1.71.7.2.255", 15);  // Phase symmetry current L3

        putInstant("1.1.81.7.1.255", 16);  // Phase angle L1-L2
        putInstant("1.1.81.7.12.255", 17); // Phase angle L2-L3
        putInstant("1.1.81.7.20.255", 18); // Phase angle L3-L1

        putInstant("1.1.33.7.0.255", 19);  // Power factor L1
        putInstant("1.1.53.7.0.255", 20);  // Power factor L2
        putInstant("1.1.73.7.0.255", 21);  // Power factor L3

        putStringValue("0.0.96.1.0.255", prometer.getRegisters().getrSerial()); // Serial number
        putStringValue("0.0.96.1.1.255", prometer.getRegisters().getrGeneralInfo(0)); // General info 1
        putStringValue("0.0.96.1.2.255", prometer.getRegisters().getrGeneralInfo(1)); // General info 2
        putStringValue("0.0.96.1.3.255", prometer.getRegisters().getrGeneralInfo(2)); // General info 3
        putStringValue("0.0.96.1.4.255", prometer.getRegisters().getrGeneralInfo(3)); // General info 4

        putGeneric(new StringValueFactory("1.0.0.2.0.255", prometer.getFirmwareVersionObject().getVersionString(), prometer));  // Firmware version

    }

    private void putStringValue(String obisCode, ProRegister register) {
        StringValueFactory factory = new StringValueFactory(obisCode, register, prometer);
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    private void putInstant(String obisCode, int valueIndex) {
        InstantValuesFactory factory = new InstantValuesFactory(obisCode, prometer.getRegisters().getrInstantValues(), prometer, valueIndex);
        ObisCodeWrapper obisCodeWrapper = new ObisCodeWrapper(factory.getObisCode());
        putFactory(factory, obisCodeWrapper);
    }

    /**
     * @return list of all ObisCodes supported by the currently connected
     *         meter.  By trial and error.
     */
    private List getMeterSupportedObisCodes() throws IOException {
        ArrayList validObisCodes = new ArrayList();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            ObisCodeWrapper key = (ObisCodeWrapper) i.next();
            ObisCode oc = key.getObisCode();
            // if no exception is thrown, the ObisCode is supported
            try {
                getRegisterValue(oc);
                validObisCodes.add(oc);
            } catch (NoSuchRegisterException nre) {
                // if an exception is thrown, the ObisCode is not available.
                //nre.printStackTrace();
            }
        }
        return validObisCodes;
    }

}
