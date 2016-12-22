package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleRegisterObject;
import com.elster.dlms.types.basic.ObisCode;
import com.energyict.cbo.BusinessException;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteGasParametersMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_TAG = "WriteGasParameters";
    private static final String MESSAGE_DESCRIPTION = "Configure the gas parameters";
    private static final String ATTR_GAS_DENSITY = "GasDensity";
    private static final String ATTR_REL_DENSITY = "RelativeDensity";
    private static final String ATTR_N2_PERCENTAGE = "N2_Percentage";
    private static final String ATTR_CO2_PERCENTAGE = "CO2_Percentage";
    private static final String ATTR_CO_PERCENTAGE = "CO_Percentage";
    private static final String ATTR_H2_PERCENTAGE = "H2_Percentage";
    private static final String ATTR_CH4_PERCENTAGE = "Methane_Percentage";
    private static final String ATTR_HCV = "CalorificValue";

    public WriteGasParametersMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String gasDensityAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_GAS_DENSITY);
        String relDensityAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_REL_DENSITY);
        String n2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_N2_PERCENTAGE);
        String co2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CO2_PERCENTAGE);
        String coPercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CO_PERCENTAGE);
        String h2PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_H2_PERCENTAGE);
        String ch4PercentageAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CH4_PERCENTAGE);
        String hcvAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_HCV);

        BigDecimal gasDensity = validateValue(gasDensityAttr, ATTR_GAS_DENSITY,
                new BigDecimal("500.0"), new BigDecimal("1000.0"), 1);
        BigDecimal relDensity = validateValue(relDensityAttr, ATTR_REL_DENSITY,
                new BigDecimal("0.5"), new BigDecimal("1.0"), 3);
        BigDecimal cv = validateValue(hcvAttr, ATTR_HCV,
                new BigDecimal("5000"), new BigDecimal("15000"), 0);
        BigDecimal n2Percentage = validateValue(n2PercentageAttr, ATTR_N2_PERCENTAGE,
                new BigDecimal("0.0"), new BigDecimal("50.0"), 2);
        BigDecimal h2Percentage = validateValue(h2PercentageAttr, ATTR_H2_PERCENTAGE,
                new BigDecimal("0.0"), new BigDecimal("20.0"), 2);
        BigDecimal co2Percentage = validateValue(co2PercentageAttr, ATTR_CO2_PERCENTAGE,
                new BigDecimal("0.0"), new BigDecimal("50.0"), 2);
        BigDecimal coPercentage = validateValue(coPercentageAttr, ATTR_CO_PERCENTAGE,
                new BigDecimal("0.0"), new BigDecimal("5.0"), 4);
        BigDecimal ch4Percentage = validateValue(ch4PercentageAttr, ATTR_CH4_PERCENTAGE,
                new BigDecimal("30.0"), new BigDecimal("100.0"), 4);

        try {
            writeGasParameters(gasDensity, relDensity, cv, n2Percentage, h2Percentage, co2Percentage,
                    coPercentage, ch4Percentage);
        } catch (IOException e) {
            throw new BusinessException("Failed to write the GAS parameters to the device: " + e.getMessage());
        }
    }

    private void writeGasParameters(BigDecimal gasDensity, BigDecimal relDensity, BigDecimal cv,
                                    BigDecimal n2Percentage, BigDecimal h2Percentage, BigDecimal co2Percentage,
                                    BigDecimal coPercentage, BigDecimal ch4Percentage) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        writeSingleValue(objectManager, gasDensity, Ek280Defs.DENSITY_GAS_BASE_COND);
        writeSingleValue(objectManager, relDensity, Ek280Defs.DENSITY_RATIO);
        writeSingleValue(objectManager, n2Percentage, Ek280Defs.GAV_NITROGEN_CONT_CURR);
        writeSingleValue(objectManager, h2Percentage, Ek280Defs.GAV_HYDROGEN_CONT_CURR);
        writeSingleValue(objectManager, co2Percentage, Ek280Defs.GAV_CARBONDIOXID_CONT_CURR);
        writeSingleValue(objectManager, coPercentage, Ek280Defs.GAV_CARBONOXID_CONT_CURR);
        writeSingleValue(objectManager, ch4Percentage, Ek280Defs.GAV_METHAN_CONT_CURR);
        writeSingleValue(objectManager, cv, Ek280Defs.CALORIFIC_VALUE_COMP_CURR);
    }

    protected BigDecimal validateValue(String value, String valName, BigDecimal lowerLimit, BigDecimal upperLimit,
                                       int decimals) throws BusinessException {

        if (value == null) {
            throw new BusinessException(String.format("%s cannot be 'null'", valName));
        }
        if ("".equals(value)) {
            throw new BusinessException(String.format("%s cannot be empty", valName));
        }

        BigDecimal bd = new BigDecimal(value);
        BigDecimal result = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);

        if (result.compareTo(lowerLimit) < 0) {
            throw new BusinessException(String.format("%s value exceeds lower limit. Min. value is %s",
                    valName, lowerLimit.toString()));
        }

        if (result.compareTo(upperLimit) > 0) {
            throw new BusinessException(String.format("%s value exceeds upper limit. Max. value is %s",
                    valName, upperLimit.toString()));
        }

        return result;
    }

    private void writeSingleValue(SimpleCosemObjectManager objectManager, BigDecimal value, ObisCode obisCode) throws IOException {

        SimpleRegisterObject ro = objectManager.getSimpleCosemObject(obisCode, SimpleRegisterObject.class);
        ro.setBigDecimalValue(value);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        tagSpec.add(new MessageAttributeSpec(ATTR_GAS_DENSITY, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_REL_DENSITY, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_N2_PERCENTAGE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_CO2_PERCENTAGE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_CO_PERCENTAGE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_H2_PERCENTAGE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_CH4_PERCENTAGE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_HCV, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
