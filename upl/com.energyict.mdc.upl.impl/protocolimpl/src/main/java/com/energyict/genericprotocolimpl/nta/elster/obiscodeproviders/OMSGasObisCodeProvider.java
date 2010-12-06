package com.energyict.genericprotocolimpl.nta.elster.obiscodeproviders;

import com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider;
import com.energyict.obis.ObisCode;

/**
 * Contains functionality for the correct OMS ObisCodes
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3-aug-2010
 * Time: 16:34:43
 */
public class OMSGasObisCodeProvider implements MbusObisCodeProvider {
    private static final ObisCode hourlyProfileObiscode = ObisCode.fromString("0.1.24.3.0.255");
    private static final ObisCode currentMasterValue = ObisCode.fromString("7.1.3.0.0.255");
    private static final ObisCode masterValue1 = ObisCode.fromString("7.1.3.0.1.255");
    private static final ObisCode masterValue2 = ObisCode.fromString("7.1.3.0.2.255");
    private static final ObisCode masterValue3 = ObisCode.fromString("7.1.3.0.3.255");
    private static final ObisCode masterValue4 = ObisCode.fromString("7.1.3.0.4.255");

    /**
     * Adjust the B-field of the given ObisCode with the given bField
     *
     * @param oc     the ObisCode to adjust
     * @param bField the new value of the B-field (zero based ??? )
     * @return the adjusted ObisCode
     */
    protected ObisCode adjustToMbusChannelObisCode(ObisCode oc, int bField) {
        return new ObisCode(oc.getA(), bField + 1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
    }

    /**
     * @return the obisCode for the Hourly profile
     */
    public ObisCode getHourlyProfileObisCode() {
        return hourlyProfileObiscode;
    }

    /**
     * @return the ObisCode for the Master register Total value
     */
    public ObisCode getMasterRegisterTotal() {
        return currentMasterValue;
    }

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Hourly profile
     */
    public ObisCode getHourlyProfileObisCode(int bField) {
        return adjustToMbusChannelObisCode(getHourlyProfileObisCode(), bField);
    }

    /**
     * @return the obisCode for the Master register value 1
     */
    public ObisCode getMasterRegisterValue1() {
        return masterValue1;
    }

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 1
     */
    public ObisCode getMasterRegisterValue1(int bField) {
        return adjustToMbusChannelObisCode(getMasterRegisterValue1(), bField);
    }

    /**
     * @return the obisCode for the Master register value 2
     */
    public ObisCode getMasterRegisterValue2() {
        return masterValue2;
    }

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 2
     */
    public ObisCode getMasterRegisterValue2(int bField) {
        return adjustToMbusChannelObisCode(getMasterRegisterValue2(), bField);
    }

    /**
     * @return the obisCode for the Master register value 3
     */
    public ObisCode getMasterRegisterValue3() {
        return masterValue3;
    }

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 3
     */
    public ObisCode getMasterRegisterValue3(int bField) {
        return adjustToMbusChannelObisCode(getMasterRegisterValue3(), bField);
    }

    /**
     * @return the obisCode for the Master register value 4
     */
    public ObisCode getMasterRegisterValue4() {
        return masterValue4;
    }

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 4
     */
    public ObisCode getMasterRegisterValue4(int bField) {
        return adjustToMbusChannelObisCode(getMasterRegisterValue4(), bField);
    }

    /**
     * @return the obisCode for the Mbus Daily profile
     */
    public ObisCode getDailyProfileObisCode() {

        // TODO fill in the correct ObisCodes for the DailyProfile

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return the obisCode for the Mbus Monthly profile
     */
    public ObisCode getMonthlyObisCode() {

        // TODO fill in the correct ObisCodes for the MonthlyProfile

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
