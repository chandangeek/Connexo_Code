package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class AutomaticDemandResetConfigurationRecord extends AbstractField<AutomaticDemandResetConfigurationRecord> {

    public static final int LENGTH = 3;

    private AutomaticDemandResetCondition demandResetCondition;
    private BcdEncodedField dayOfDemandReset;
    private BcdEncodedField hourOfDemandReset;

    public AutomaticDemandResetConfigurationRecord() {
        this.demandResetCondition = new AutomaticDemandResetCondition();
        this.dayOfDemandReset = new BcdEncodedField();
        this.hourOfDemandReset = new BcdEncodedField();
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                demandResetCondition.getBytes(),
                dayOfDemandReset.getBytes(),
                hourOfDemandReset.getBytes()
        );
    }

    @Override
    public AutomaticDemandResetConfigurationRecord parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

       demandResetCondition.parse(rawData, ptr);
        ptr += demandResetCondition.getLength();

        dayOfDemandReset.parse(rawData, ptr);
        ptr += dayOfDemandReset.getLength();

        hourOfDemandReset.parse(rawData, ptr);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public AutomaticDemandResetCondition getDemandResetCondition() {
        return demandResetCondition;
    }

    public void setDemandResetCondition(AutomaticDemandResetCondition demandResetCondition) {
        this.demandResetCondition = demandResetCondition;
    }

    public BcdEncodedField getDayOfDemandReset() {
        return dayOfDemandReset;
    }

    public void setDayOfDemandReset(BcdEncodedField dayOfDemandReset) {
        this.dayOfDemandReset = dayOfDemandReset;
    }

    public BcdEncodedField getHourOfDemandReset() {
        return hourOfDemandReset;
    }

    public void setHourOfDemandReset(BcdEncodedField hourOfDemandReset) {
        this.hourOfDemandReset = hourOfDemandReset;
    }
}