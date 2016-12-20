package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteGasParametersMessage extends AbstractMTU155Message {

    protected static final String GAS_DENS_ID = "A.3.6";
    protected static final String AIR_DENS_ID = "A.4.6";
    protected static final String REL_DENS_ID = "A.5.6";

    protected static final String N2_PERC_ID = "A.C.3";
    protected static final String CO2_PERC_ID = "A.C.4";
    protected static final String H2_PERC_ID = "A.C.0";

    protected static final String HCV_ID = "A.C.8";

    protected static final int KMOLT = 5;
    protected static final String MAX_VALUE = "167.77215";

    public WriteGasParametersMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(ConfigurationChangeDeviceMessage.ConfigureGasParameters.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String gasDensityString = getDeviceMessageAttribute(message, DeviceMessageConstants.gasDensityAttributeName).getValue();
        String airDensityString = getDeviceMessageAttribute(message, DeviceMessageConstants.airDensityAttributeName).getValue();
        String relativeDensityString = getDeviceMessageAttribute(message, DeviceMessageConstants.relativeDensityAttributeName).getValue();
        String molecularNitrogenPercentageString = getDeviceMessageAttribute(message, DeviceMessageConstants.molecularNitrogenPercentageAttributeName).getValue();
        String carbonDioxidePercentageString = getDeviceMessageAttribute(message, DeviceMessageConstants.carbonDioxidePercentageAttributeName).getValue();
        String molecularHydrogenPercentageString = getDeviceMessageAttribute(message, DeviceMessageConstants.molecularHydrogenPercentageAttributeName).getValue();
        String higherCalorificValueString = getDeviceMessageAttribute(message, DeviceMessageConstants.higherCalorificValueAttributeName).getValue();

        int gasDensity = validateAndGetDensity(gasDensityString);
        int airDensity = validateAndGetDensity(airDensityString);
        int relDensity = validateAndGetDensity(relativeDensityString);
        int n2Percentage = validateAndGetPercentage(molecularNitrogenPercentageString);
        int co2Percentage = validateAndGetPercentage(carbonDioxidePercentageString);
        int h2Percentage = validateAndGetPercentage(molecularHydrogenPercentageString);
        int hcv = validateAndGetHCV(higherCalorificValueString);

        writeGasParameters(gasDensity, airDensity, relDensity, n2Percentage, co2Percentage, h2Percentage, hcv);
        return null;
    }

    private void writeGasParameters(int gasDensity, int airDensity, int relDensity, int n2Percentage, int co2Percentage, int h2Percentage, int hcv) throws CTRException {
        SealConfig sealConfig = new SealConfig(getFactory());
        sealConfig.breakAndRestoreSeal(SealStatusBit.REMOTE_CONFIG_ANALYSIS);
        getFactory().writeRegister(AttributeType.getQualifierAndValue(), 7, getRawData(gasDensity, airDensity, relDensity, n2Percentage, co2Percentage, h2Percentage, hcv));
    }

    protected int validateAndGetDensity(String densityString) throws CTRException {
        BigDecimal value = new BigDecimal(densityString);
        return validateRangeAndPrecision(value).intValue();
    }

    protected static BigDecimal validateRangeAndPrecision(BigDecimal value) throws CTRException {
        BigDecimal scaledValue = value.movePointRight(KMOLT);
        try {
            int intValue = scaledValue.intValueExact();
            if (intValue > 0x00FFFFFFl) {
                String msg = "Range to high for [" + value + "]. Max value is [" + MAX_VALUE + "]";
                throw new CTRException(msg);
            } else if (intValue < 0) {
                String msg = "Range or precision to high for [" + value + "]. Min value is [0.00001] or [0]";
                throw new CTRException(msg);
            }
        } catch (ArithmeticException e) {
            String msg = "Range or precision to high for [" + value + "]: " + e.getMessage();
            throw new CTRException(msg);
        }
        return scaledValue;
    }

    protected int validateAndGetPercentage(String percentageString) throws CTRException {
        BigDecimal value = new BigDecimal(percentageString);
        if ((value.intValue() > 100) || (value.intValue() < 0)) {
            String msg = "Invalid percentage value [" + percentageString + "]. Percentage must be between [100.0] and [0.0]";
            throw new CTRException(msg);
        }
        return validateRangeAndPrecision(value).intValue();
    }

    protected int validateAndGetHCV(String hcvString) throws CTRException {
        BigDecimal value = new BigDecimal(hcvString);
        return validateRangeAndPrecision(value).intValue();
    }

    protected byte[] getRawData(int gasDensity, int airDensity, int relDensity, int n2Percentage, int co2Percentage, int h2Percentage, int hcv) {
        byte[] rawData = new byte[0];
        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(GAS_DENS_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(gasDensity));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(AIR_DENS_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(airDensity));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(REL_DENS_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(relDensity));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(N2_PERC_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(n2Percentage));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(CO2_PERC_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(co2Percentage));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(H2_PERC_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(h2Percentage));

        rawData = ProtocolTools.concatByteArrays(rawData, new CTRObjectID(HCV_ID).getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = ProtocolTools.concatByteArrays(rawData, getRawData(hcv));

        return rawData;
    }

    protected byte[] getRawData(int value) {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) ((value >> 16) & 0x0FF);
        rawData[1] = (byte) ((value >> 8) & 0x0FF);
        rawData[2] = (byte) ((value >> 0) & 0x0FF);
        return rawData;
    }
}
