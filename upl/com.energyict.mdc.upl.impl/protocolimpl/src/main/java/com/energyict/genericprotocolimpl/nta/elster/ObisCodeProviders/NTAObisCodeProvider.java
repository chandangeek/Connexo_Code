package com.energyict.genericprotocolimpl.nta.elster.ObisCodeProviders;

import com.energyict.obis.ObisCode;

/**
 * Contains functionality for the correct NTA obiscodes
 *
 * <p>
 * Copyrights EnergyICT
 * Date: 3-aug-2010
 * Time: 16:34:15
 * </p>
 */
public class NTAObisCodeProvider implements MbusObisCodeProvider{

    private static final ObisCode hourlyProfileObiscode = ObisCode.fromString("0.1.24.3.0.255");
    private static final ObisCode masterValue1 = ObisCode.fromString("0.1.24.2.1.255");
    private static final ObisCode masterValue2 = ObisCode.fromString("0.1.24.2.2.255");
    private static final ObisCode masterValue3 = ObisCode.fromString("0.1.24.2.3.255");
    private static final ObisCode masterValue4 = ObisCode.fromString("0.1.24.2.4.255");
    private static final ObisCode dailyProfileObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode monthlyProfileObisCode = ObisCode.fromString("0.0.98.1.0.255");

    /**
     * Adjust the B-field of the given ObisCode with the given bField
     * @param oc the ObisCode to adjust
     * @param bField the new value of the B-field (zero based ??? )
     * @return the adjusted ObisCode
     */
    protected ObisCode adjustToMbusChannelObisCode(ObisCode oc, int bField) {
		return new ObisCode(oc.getA(), bField+1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
	}

    /**
     * @return the obisCode for the Hourly profile
     */
    public ObisCode getHourlyProfileObisCode() {
        return hourlyProfileObiscode;  
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
        return dailyProfileObisCode;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return the obisCode for the Mbus Monthly profile
     */
    public ObisCode getMonthlyObisCode() {
        return monthlyProfileObisCode;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
