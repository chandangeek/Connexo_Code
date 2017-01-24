package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetConfigurationRecord;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConfigureAutomaticDemandResetRequest extends Data<ConfigureAutomaticDemandResetRequest> {

    private static final int PADDING_DATA_LENGTH = 57;

    private AutomaticDemandResetConfigurationRecord automaticDemandResetConfigurationRecord;

    private PaddingData paddingData;

    public ConfigureAutomaticDemandResetRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.automaticDemandResetConfigurationRecord = new AutomaticDemandResetConfigurationRecord();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                automaticDemandResetConfigurationRecord.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ConfigureAutomaticDemandResetRequest parse(byte[] rawData, int offset) throws ParsingException {
        automaticDemandResetConfigurationRecord.parse(rawData, offset);
        return this;
    }

    public AutomaticDemandResetConfigurationRecord getAutomaticDemandResetConfigurationRecord() {
        return automaticDemandResetConfigurationRecord;
    }

    public void setAutomaticDemandResetConfigurationRecord(AutomaticDemandResetConfigurationRecord automaticDemandResetConfigurationRecord) {
        this.automaticDemandResetConfigurationRecord = automaticDemandResetConfigurationRecord;
    }
}