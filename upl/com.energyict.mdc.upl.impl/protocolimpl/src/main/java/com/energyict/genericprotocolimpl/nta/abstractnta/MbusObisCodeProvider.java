package com.energyict.genericprotocolimpl.nta.abstractnta;

import com.energyict.obis.ObisCode;

/**
 * Summary of the different Obiscodes between the NTA and OMS specification
 * <p/>
 * <p>
 * Copyrights EnergyICT<br>
 * Date: 3-aug-2010<br>
 * Time: 16:28:57<br>
 * </p>
 */
public interface MbusObisCodeProvider {

    /**
     * @return the obisCode for the Hourly profile
     */
    ObisCode getHourlyProfileObisCode();

    /**
     * @return the ObisCode for the Master register Total value
     */
    ObisCode getMasterRegisterTotal();

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Hourly profile
     */
    ObisCode getHourlyProfileObisCode(int bField);

    /**
     * @return the obisCode for the Master register value 1
     */
    ObisCode getMasterRegisterValue1();

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 1
     */
    ObisCode getMasterRegisterValue1(int bField);

    /**
     * @return the obisCode for the Master register value 2
     */
    ObisCode getMasterRegisterValue2();

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 2
     */
    ObisCode getMasterRegisterValue2(int bField);

    /**
     * @return the obisCode for the Master register value 3
     */
    ObisCode getMasterRegisterValue3();

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 3
     */
    ObisCode getMasterRegisterValue3(int bField);

    /**
     * @return the obisCode for the Master register value 4
     */
    ObisCode getMasterRegisterValue4();

    /**
     * @param bField the value to adjust the B-field
     * @return the obisCode for the Master register value 4
     */
    ObisCode getMasterRegisterValue4(int bField);

    /**
     * @return the obisCode for the Mbus Daily profile
     */
    ObisCode getDailyProfileObisCode();

    /**
     * @return the obisCode for the Mbus Monthly profile
     */
    ObisCode getMonthlyObisCode();
}
