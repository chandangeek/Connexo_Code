package com.energyict.genericprotocolimpl.elster.ctr.frame;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:36:04
 */
public class SMSFrame extends AbstractCTRFrame<SMSFrame> {

    @Override
    public SMSFrame parse(byte[] rawData, int offset) {
        super.parse(rawData, offset);
        return this;
    }
    
}
