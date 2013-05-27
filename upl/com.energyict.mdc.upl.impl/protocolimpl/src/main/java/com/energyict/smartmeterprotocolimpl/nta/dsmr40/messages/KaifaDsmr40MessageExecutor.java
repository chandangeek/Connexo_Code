package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;

/**
 * Copyrights EnergyICT
 * Date: 10/04/13
 * Time: 16:14
 * Author: khe
 */
public class KaifaDsmr40MessageExecutor extends Dsmr40MessageExecutor {

    /**
     * The IBM Kaifa meter only accepts value 0x01 as boolean TRUE.
     */
    protected int getBooleanValue() {
        return 0x01;
    }

    public KaifaDsmr40MessageExecutor(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }
}