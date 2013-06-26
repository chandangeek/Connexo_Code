package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;

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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ConfigurationChangeDeviceMessage.ConfigureGasParameters.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String gasDensityString = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        String airDensityString = message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue().trim();
        String relativeDensityString = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();
        String molecularNitrogenPercentageString = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();
        String carbonDioxidePercentageString = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();
        String molecularHydrogenPercentageString = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();
        String higherCalorificValueString = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();

        try {
            int gasDensity = validateAndGetDensity(collectedMessage, gasDensityString);
            int airDensity = validateAndGetDensity(collectedMessage, airDensityString);
            int relDensity = validateAndGetDensity(collectedMessage, relativeDensityString);
            int n2Percentage = validateAndGetPercentage(collectedMessage, molecularNitrogenPercentageString);
            int co2Percentage = validateAndGetPercentage(collectedMessage, carbonDioxidePercentageString);
            int h2Percentage = validateAndGetPercentage(collectedMessage, molecularHydrogenPercentageString);
            int hcv = validateAndGetHCV(collectedMessage, higherCalorificValueString);

            writeGasParameters(gasDensity, airDensity, relDensity, n2Percentage, co2Percentage, h2Percentage, hcv);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private void writeGasParameters(int gasDensity, int airDensity, int relDensity, int n2Percentage, int co2Percentage, int h2Percentage, int hcv) throws CTRException {
        SealConfig sealConfig = new SealConfig(getFactory());
        sealConfig.breakAndRestoreSeal(SealStatusBit.REMOTE_CONFIG_ANALYSIS);
        addWriteDataBlockToWDBList(getFactory().getWriteDataBlockID());
        getFactory().writeRegister(AttributeType.getQualifierAndValue(), 7, getRawData(gasDensity, airDensity, relDensity, n2Percentage, co2Percentage, h2Percentage, hcv));
        addWriteDataBlockToWDBList(getFactory().getWriteDataBlockID());
    }

    protected int validateAndGetDensity(CollectedMessage collectedMessage, String densityString) throws CTRException {
        BigDecimal value = new BigDecimal(densityString);
        return validateRangeAndPrecision(collectedMessage, value).intValue();
    }

    protected static BigDecimal validateRangeAndPrecision(CollectedMessage collectedMessage, BigDecimal value) throws CTRException {
        BigDecimal scaledValue = value.movePointRight(KMOLT);
        try {
            int intValue = scaledValue.intValueExact();
            if (intValue > 0x00FFFFFFl) {
                String msg = "Range to high for [" + value + "]. Max value is [" + MAX_VALUE + "]";
                collectedMessage.setDeviceProtocolInformation(msg);
                throw new CTRException(msg);
            } else if (intValue < 0) {
                String msg = "Range or precision to high for [" + value + "]. Min value is [0.00001] or [0]";
                collectedMessage.setDeviceProtocolInformation(msg);
                throw new CTRException(msg);
            }
        } catch (ArithmeticException e) {
            String msg = "Range or precision to high for [" + value + "]: " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        return scaledValue;
    }

    protected int validateAndGetPercentage(CollectedMessage collectedMessage, String percentageString) throws CTRException {
        BigDecimal value = new BigDecimal(percentageString);
        if ((value.intValue() > 100) || (value.intValue() < 0)) {
            String msg = "Invalid percentage value [" + percentageString + "]. Percentage must be between [100.0] and [0.0]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        return validateRangeAndPrecision(collectedMessage, value).intValue();
    }

    protected int validateAndGetHCV(CollectedMessage collectedMessage, String hcvString) throws CTRException {
        BigDecimal value = new BigDecimal(hcvString);
        return validateRangeAndPrecision(collectedMessage, value).intValue();
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
