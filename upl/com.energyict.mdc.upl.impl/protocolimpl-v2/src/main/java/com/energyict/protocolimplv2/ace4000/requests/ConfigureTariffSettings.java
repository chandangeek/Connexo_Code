package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeObjectValidator;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeTableBase64Parser;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeObject;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ConfigureTariffSettings extends AbstractConfigMessage {

    public ConfigureTariffSettings(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int number = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.UNIQUE_TARIFF_ID_NO).getValue());
        int numberOfRates = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.NUMBER_OF_TARIFF_RATES).getValue());
        String codeTableBase64 = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.CODE_TABLE_ID).getValue();

        if (numberOfRates > 4 || numberOfRates < 0) {
            failMessage("Tariff configuration failed, invalid number of rates");
            return;
        }

        try {
            CodeObject codeObject = validateAndGetCodeObject(codeTableBase64);
            trackingId = getAce4000().getObjectFactory().sendTariffConfiguration(number, numberOfRates, codeObject);
        } catch (ApplicationException | IOException e) {  //Thrown while parsing the code table
            failMessage("Tariff configuration failed, invalid code table settings: " + e.getMessage());
        }
    }

    private CodeObject validateAndGetCodeObject(String codeTableBase64) throws IOException {
        try {
            CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
            CodeObjectValidator.validateCodeObject(codeObject);
            return codeObject;
        } catch (BusinessException e) {
            throw new IOException(e.getMessage());
        }
    }
}