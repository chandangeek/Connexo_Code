/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.InstallationCodeConditionField;
import com.energyict.protocolimplv2.abnt.common.structure.field.ModificationCodeConditionField;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ReadInstallationCodeRequest extends Data<ReadInstallationCodeRequest> {

    private static final int PADDING_DATA_LENGTH = 58;

    private ModificationCodeConditionField modificationCodeCondition;
    private InstallationCodeConditionField installationCodeCondition;
    private PaddingData paddingData;

    public ReadInstallationCodeRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.modificationCodeCondition = new ModificationCodeConditionField(ModificationCodeConditionField.ModificationCodeCondition.READING);
        this.installationCodeCondition = new InstallationCodeConditionField();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                modificationCodeCondition.getBytes(),
                installationCodeCondition.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ReadInstallationCodeRequest parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        modificationCodeCondition.parse(rawData, ptr);
        ptr += modificationCodeCondition.getLength();

        installationCodeCondition.parse(rawData, ptr);
        return this;
    }

    public ModificationCodeConditionField getModificationCodeCondition() {
        return modificationCodeCondition;
    }

    public InstallationCodeConditionField getInstallationCodeCondition() {
        return installationCodeCondition;
    }
}