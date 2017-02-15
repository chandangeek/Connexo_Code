/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
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
public class ReadInstallationCodeResponse extends Data<ReadInstallationCodeResponse> {

    private static final int PADDING_DATA_LENGTH = 235;
    private static final int INSTALLATION_CODE_LENGTH = 14;

    private ModificationCodeConditionField modificationCodeCondition;
    private InstallationCodeConditionField installationCodeCondition;
    private BcdEncodedField installationCode;
    private PaddingData paddingData;

    public ReadInstallationCodeResponse(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.modificationCodeCondition = new ModificationCodeConditionField(ModificationCodeConditionField.ModificationCodeCondition.READING);
        this.installationCodeCondition = new InstallationCodeConditionField();
        this.installationCode = new BcdEncodedField(INSTALLATION_CODE_LENGTH);
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                modificationCodeCondition.getBytes(),
                installationCodeCondition.getBytes(),
                installationCode.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ReadInstallationCodeResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        modificationCodeCondition.parse(rawData, ptr);
        ptr += modificationCodeCondition.getLength();

        installationCodeCondition.parse(rawData, ptr);
        ptr += installationCodeCondition.getLength();

        installationCode.parse(rawData, ptr);
        return this;
    }

    public ModificationCodeConditionField getModificationCodeCondition() {
        return modificationCodeCondition;
    }

    public InstallationCodeConditionField getInstallationCodeCondition() {
        return installationCodeCondition;
    }

    public String getInstallationCodeAsHex() {
        return installationCode.getText();
    }

    public String getInstallationCode() {
        return ProtocolTools.getAsciiFromBytes(
                ProtocolTools.getBytesFromHexString(installationCode.getText(), "")
        );
    }
}