package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.info.SealStatusBit;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.math.BigDecimal;

import static com.energyict.protocolimpl.utils.ProtocolTools.concatByteArrays;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteGasParametersMessage extends AbstractMTU155Message {

    private static final String GAS_DENS_ID = "A.3.6";
    private static final String AIR_DENS_ID = "A.4.6";
    private static final String REL_DENS_ID = "A.5.6";

    private static final String N2_PERC_ID = "A.C.3";
    private static final String CO2_PERC_ID = "A.C.4";
    private static final String H2_PERC_ID = "A.C.0";

    private static final String HCV_ID = "A.C.8";

    private static final String MESSAGE_TAG = "WriteGasParameters";
    private static final String MESSAGE_DESCRIPTION = "Configure the gas parameters";
    private static final String ATTR_GAS_DENSITY = "GasDensity";
    private static final String ATTR_AIR_DENSITY = "AirDensity";
    private static final String ATTR_REL_DENSITY = "RelativeDensity";
    private static final String ATTR_N2_PERCENTAGE = "N2_Percentage";
    private static final String ATTR_CO2_PERCENTAGE = "CO2_Percentage";
    private static final String ATTR_H2_PERCENTAGE = "H2_Percentage";
    private static final String ATTR_HCV = "HigherCalorificValue";
    private static final int KMOLT = 5;
    private static final String MAX_VALUE = "167.77215";
    private static final BigDecimal PERCENTAGE_MAX_VALUE = new BigDecimal(100);

    public WriteGasParametersMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String gasDensityAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_GAS_DENSITY);
        String airDensityAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AIR_DENSITY);
        String relDensityAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_REL_DENSITY);
        String n2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_N2_PERCENTAGE);
        String co2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CO2_PERCENTAGE);
        String h2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_H2_PERCENTAGE);
        String hcvAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_HCV);

        int gasDensity = validateAndGetDensity(gasDensityAttr);
        int airDensity = validateAndGetDensity(airDensityAttr);
        int relDensity = validateAndGetDensity(relDensityAttr);
        int n2Percentage = validateAndGetPercentage(n2PercentageAttr);
        int co2Percentage = validateAndGetPercentage(co2PercentageAttr);
        int h2Percentage = validateAndGetPercentage(h2PercentageAttr);
        int hcv = validateAndGetHCV(hcvAttr);

        try {
            SealConfig sealConfig = new SealConfig(getFactory());
            sealConfig.breakAndRestoreSeal(SealStatusBit.REMOTE_CONFIG_ANALYSIS);
            Data data = getFactory().writeRegister(AttributeType.getQualifierAndValue(), 7, getRawData(gasDensity, airDensity, relDensity, n2Percentage, co2Percentage, h2Percentage, hcv));
            if ((data != null) && data instanceof NackStructure) {
                throw new CTRException("Received NACK from device.");
            }
        } catch (CTRException e) {
            throw new BusinessException("Failed to write the GAS parameters to the device: " + e.getMessage());
        }
    }

    private byte[] getRawData(int gasDensity, int airDensity, int relDensity, int n2Percentage, int co2Percentage, int h2Percentage, int hcv) {
        byte[] rawData = new byte[0];
        rawData = concatByteArrays(rawData, new CTRObjectID(GAS_DENS_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(gasDensity));

        rawData = concatByteArrays(rawData, new CTRObjectID(AIR_DENS_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(airDensity));

        rawData = concatByteArrays(rawData, new CTRObjectID(REL_DENS_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(relDensity));

        rawData = concatByteArrays(rawData, new CTRObjectID(N2_PERC_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(n2Percentage));

        rawData = concatByteArrays(rawData, new CTRObjectID(CO2_PERC_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(co2Percentage));

        rawData = concatByteArrays(rawData, new CTRObjectID(H2_PERC_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(h2Percentage));

        rawData = concatByteArrays(rawData, new CTRObjectID(HCV_ID).getBytes());
        rawData = concatByteArrays(rawData, new byte[]{KMOLT});
        rawData = concatByteArrays(rawData, getRawData(hcv));

        return rawData;
    }

    private byte[] getRawData(int value) {
        byte[] rawData = new byte[3];
        rawData[0] = (byte) ((value >> 16) & 0x0FF);
        rawData[1] = (byte) ((value >> 8) & 0x0FF);
        rawData[2] = (byte) ((value >> 0) & 0x0FF);
        return rawData;
    }

    protected int validateAndGetDensity(String densityString) throws BusinessException {
        if (densityString == null) {
            throw new BusinessException("Density cannot be 'null'");
        }
        if ("".equals(densityString)) {
            throw new BusinessException("Density cannot be empty");
        }

        BigDecimal value = null;
        try {
            value = new BigDecimal(densityString);
        } catch (Exception e) {
            throw new BusinessException("Invalid density value [" + densityString + "].", e);
        }

        return validateRangeAndPrecision(value).intValue();
    }

    /**
     * @param value
     * @return
     * @throws BusinessException
     */
    private static BigDecimal validateRangeAndPrecision(BigDecimal value) throws BusinessException {
        BigDecimal scaledValue = value.movePointRight(KMOLT);
        try {
            int intValue = scaledValue.intValueExact();
            if (intValue > 0x00FFFFFFl) {
                throw new BusinessException("Range to high for [" + value + "]. Max value is [" + MAX_VALUE + "]");
            } else if (intValue < 0) {
                throw new BusinessException("Range or precision to high for [" + value + "]. Min value is [0.00001] or [0]");
            }
        } catch (ArithmeticException e) {
            throw new BusinessException("Range or precision to high for [" + value + "]: " + e.getMessage());
        }
        return scaledValue;
    }

    protected int validateAndGetPercentage(String percentageString) throws BusinessException {
        if (percentageString == null) {
            throw new BusinessException("Percentage cannot be 'null'");
        }
        if ("".equals(percentageString)) {
            throw new BusinessException("Percentage cannot be empty");
        }

        BigDecimal value = null;
        try {
            value = new BigDecimal(percentageString);
        } catch (Exception e) {
            throw new BusinessException("Invalid percentage value [" + percentageString + "].", e);
        }

        int percentage = validateRangeAndPrecision(value).intValue();
        if ((percentage > 100) || (percentage < 0)) {
            throw new BusinessException("Invalid percentage value [" + percentageString + "]. Percentage must be between [100.0] and [0.0]");
        }

        return percentage;
    }

    protected int validateAndGetHCV(String hcvString) throws BusinessException {
        if (hcvString == null) {
            throw new BusinessException("HCV cannot be 'null'");
        }
        if ("".equals(hcvString)) {
            throw new BusinessException("HCV cannot be empty");
        }

        BigDecimal value = null;
        try {
            value = new BigDecimal(hcvString);
        } catch (Exception e) {
            throw new BusinessException("Invalid HCV value [" + hcvString + "].", e);
        }

        return validateRangeAndPrecision(value).intValue();
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_GAS_DENSITY, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_AIR_DENSITY, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_REL_DENSITY, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_N2_PERCENTAGE, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_CO2_PERCENTAGE, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_H2_PERCENTAGE, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_HCV, false));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }


}
