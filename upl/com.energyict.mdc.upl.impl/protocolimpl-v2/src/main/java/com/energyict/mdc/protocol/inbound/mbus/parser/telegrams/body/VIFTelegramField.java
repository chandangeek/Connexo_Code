/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.MeasureUnit;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramDateMasks;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.VIFUnitMultiplierMasks;

public class VIFTelegramField extends TelegramField {

    public static int EXTENSION_BIT_MASK = 0x80; 		// 1000 0000
    private static final int LAST_TWO_BIT_OR_MASK = 0x03; 	// 0000 0011
    private static final int LAST_THREE_BIT_OR_MASK = 0x07; 	// 0000 0111
    private static final int UNIT_MULTIPLIER_MASK = 0x7F; 	// 0111 1111

    private boolean extensionBit = false;
    private MeasureUnit mUnit = MeasureUnit.NONE;
    private VIFUnitMultiplierMasks type;
    private int multiplier = 0;

    private TelegramVariableDataRecord parent;

    public VIFTelegramField(MerlinLogger logger) {
        super(logger);
    }

    public void parse() {
        String vifField = this.fieldParts.get(0);
        int iVifField = Converter.hexToInt(vifField);

        logger.debug("\t* Parsing VIF=" + vifField);

        if ((iVifField & VIFTelegramField.EXTENSION_BIT_MASK) == VIFTelegramField.EXTENSION_BIT_MASK) {
            this.extensionBit = true;
            logger.debug("\t - extension = true");
        }

        if (iVifField == VIFUnitMultiplierMasks.FIRST_EXT_VIF_CODES.getValue()) {
            // load from next VIFE according to table 29 from DIN_EN_13757_3
            logger.debug("\t - this is first VIFE");
        } else if (iVifField == VIFUnitMultiplierMasks.SECOND_EXT_VIF_CODES.getValue()) {
            // load from next VIFE according to table 29 from DIN_EN_13757_3
            logger.debug("\t - this is second VIFE");
        } else {
            // first get rid of the first (extension) bit
            int iVifFieldNoExt = (iVifField & (VIFTelegramField.UNIT_MULTIPLIER_MASK));

            logger.debug("\t - VIF (no ext)=" + iVifFieldNoExt);

            // first check against complete (no wildcards) bit masks
            // can't check with switch case because we need a constant value there
            if (iVifFieldNoExt == VIFUnitMultiplierMasks.DATE.getValue()) {
                this.type = VIFUnitMultiplierMasks.DATE;
                logger.debug("\t - type=DATE");
                this.parseDate(this.parent.getDif().getDataFieldLengthAndEncoding());
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.DATE_TIME_GENERAL.getValue()) {
                logger.debug("\t - type=DATE_TIME_GENERAL");
                this.parseDate(this.parent.getDif().getDataFieldLengthAndEncoding());
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.UNITS_FOR_HCA.getValue()) {
                // NO UNIT
                logger.debug("\t - type=UNITS_FOR_HCA");
                this.type = VIFUnitMultiplierMasks.UNITS_FOR_HCA;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.RES_THIRD_VIFE_TABLE.getValue()) {
                // NO UNIT
                logger.debug("\t - type=RES_THIRD_VIFE_TABLE");
                this.type = VIFUnitMultiplierMasks.RES_THIRD_VIFE_TABLE;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.FABRICATION_NO.getValue()) {
                // NO UNIT
                logger.debug("\t - type=FABRICATION_NO");
                this.type = VIFUnitMultiplierMasks.FABRICATION_NO;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.IDENTIFICATION.getValue()) {
                // NO UNIT
                logger.debug("\t - type=IDENTIFICATION");
                this.type = VIFUnitMultiplierMasks.IDENTIFICATION;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.ADDRESS.getValue()) {
                logger.debug("\t - type=ADDRESS");
                this.type = VIFUnitMultiplierMasks.ADDRESS;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.VIF_FOLLOWING.getValue()) {
                logger.debug("\t - type=VIF_FOLLOWING");
                this.type = VIFUnitMultiplierMasks.VIF_FOLLOWING;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.ANY_VIF.getValue()) {
                // see section  6.4
                logger.debug("\t - type=ANY_VIF");
                this.type = VIFUnitMultiplierMasks.ANY_VIF;
            } else if (iVifFieldNoExt == VIFUnitMultiplierMasks.MANUFACTURER_SPEC.getValue()) {
                // VIFE and data is manufacturer specific
                logger.debug("\t - type=MANUFACTURER_SPEC");
                this.type = VIFUnitMultiplierMasks.MANUFACTURER_SPEC;
            } else if (parseLastTwoBitsSet(iVifFieldNoExt)) {
                logger.debug("\t - last 2 bits set");
            } else if (parseLastThreeBitsSet(iVifFieldNoExt)) {
                logger.debug("\t - last 3 bits set");
            } else {
                logger.warn("\t - cannot decode " + iVifFieldNoExt);
            }
        }
    }

    public boolean parseLastTwoBitsSet(int iVifFieldNoExt) {
        // set last two bits to 1 so that we can check against our other masks
        int iVifFieldNoExtLastTwo = (iVifFieldNoExt | VIFTelegramField.LAST_TWO_BIT_OR_MASK);
        int onlyLastTwoBits = iVifFieldNoExt & VIFTelegramField.LAST_TWO_BIT_OR_MASK;

        if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.ON_TIME.getValue()) {
            this.type = VIFUnitMultiplierMasks.ON_TIME;
            switch (onlyLastTwoBits) {
                case 0:
                    this.mUnit = MeasureUnit.SECONDS;
                    break;
                case 1:
                    this.mUnit = MeasureUnit.MINUTES;
                    break;
                case 2:
                    this.mUnit = MeasureUnit.HOURS;
                    break;
                case 3:
                    this.mUnit = MeasureUnit.DAYS;
                    break;
                default:
                    logger.warn("Cannot decode ON_TIME last two bytes" + onlyLastTwoBits);
                    break;
            }
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.OPERATING_TIME.getValue()) {
            this.type = VIFUnitMultiplierMasks.OPERATING_TIME;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.FLOW_TEMPERATURE.getValue()) {
            this.type = VIFUnitMultiplierMasks.FLOW_TEMPERATURE;
            this.multiplier = onlyLastTwoBits - 3;
            this.mUnit = MeasureUnit.C;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.RETURN_TEMPERATURE.getValue()) {
            this.type = VIFUnitMultiplierMasks.RETURN_TEMPERATURE;
            this.multiplier = onlyLastTwoBits - 3;
            this.mUnit = MeasureUnit.C;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.TEMPERATURE_DIFFERENCE.getValue()) {
            this.type = VIFUnitMultiplierMasks.TEMPERATURE_DIFFERENCE;
            this.multiplier = onlyLastTwoBits - 3;
            this.mUnit = MeasureUnit.K;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.EXTERNAL_TEMPERATURE.getValue()) {
            this.type = VIFUnitMultiplierMasks.EXTERNAL_TEMPERATURE;
            this.multiplier = onlyLastTwoBits - 3;
            this.mUnit = MeasureUnit.C;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.PRESSURE.getValue()) {
            this.type = VIFUnitMultiplierMasks.PRESSURE;
            this.multiplier = onlyLastTwoBits - 3;
            this.mUnit = MeasureUnit.BAR;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.AVG_DURATION.getValue()) {
            this.type = VIFUnitMultiplierMasks.AVG_DURATION;
        } else if (iVifFieldNoExtLastTwo == VIFUnitMultiplierMasks.ACTUALITY_DURATION.getValue()) {
            this.type = VIFUnitMultiplierMasks.ACTUALITY_DURATION;
        }
        else {
            return false;
        }
        return true;
    }

    public boolean parseLastThreeBitsSet(int iVifFieldNoExt) {
        // set last three bits to 1 so that we can check against our other masks
        int iVifFieldNoExtLastThree = (iVifFieldNoExt | VIFTelegramField.LAST_THREE_BIT_OR_MASK);

        int onlyLastThreeBits = iVifFieldNoExt & VIFTelegramField.LAST_THREE_BIT_OR_MASK;

        if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.ENERGY_WH.getValue()) {
            this.type = VIFUnitMultiplierMasks.ENERGY_WH;
            this.multiplier = onlyLastThreeBits - 3;
            this.mUnit = MeasureUnit.WH;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.ENERGY_J.getValue()) {
            this.type = VIFUnitMultiplierMasks.ENERGY_J;
            this.multiplier = onlyLastThreeBits;
            this.mUnit = MeasureUnit.J;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.VOLUME.getValue()) {
            this.type = VIFUnitMultiplierMasks.VOLUME;
            this.multiplier = onlyLastThreeBits - 6;
            this.mUnit = MeasureUnit.M3;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.MASS.getValue()) {
            this.type = VIFUnitMultiplierMasks.MASS;
            this.multiplier = onlyLastThreeBits - 3;
            this.mUnit = MeasureUnit.KG;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.POWER_W.getValue()) {
            this.type = VIFUnitMultiplierMasks.POWER_W;
            this.multiplier = onlyLastThreeBits - 3;
            this.mUnit = MeasureUnit.W;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.POWER_J_H.getValue()) {
            this.type = VIFUnitMultiplierMasks.POWER_J_H;
            this.multiplier = onlyLastThreeBits;
            this.mUnit = MeasureUnit.J_H;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.VOLUME_FLOW.getValue()) {
            this.type = VIFUnitMultiplierMasks.VOLUME_FLOW;
            this.multiplier = onlyLastThreeBits - 6;
            this.mUnit = MeasureUnit.M3_H;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.VOLUME_FLOW_EXT.getValue()) {
            this.type = VIFUnitMultiplierMasks.VOLUME_FLOW_EXT;
            this.multiplier = onlyLastThreeBits - 7;
            this.mUnit = MeasureUnit.M3_MIN;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.VOLUME_FLOW_EXT_S.getValue()) {
            this.type = VIFUnitMultiplierMasks.VOLUME_FLOW_EXT_S;
            this.multiplier = onlyLastThreeBits - 9;
            this.mUnit = MeasureUnit.M3_S;
        } else if (iVifFieldNoExtLastThree == VIFUnitMultiplierMasks.MASS_FLOW.getValue()) {
            this.type = VIFUnitMultiplierMasks.MASS_FLOW;
            this.multiplier = onlyLastThreeBits - 3;
            this.mUnit = MeasureUnit.KG_H;
        } else {
            return false;
        }
        return true;
    }

    private void parseDate(int dateType) {
        if (dateType == TelegramDateMasks.DATE.getValue()) {
            mUnit = MeasureUnit.DATE;
            logger.debug("\t - DATE");
        } else if (dateType == TelegramDateMasks.DATE_TIME.getValue()) {
            this.type = VIFUnitMultiplierMasks.DATE_TIME;
            mUnit = MeasureUnit.DATE_TIME; // TIME before
            logger.debug("\t - DATE_TIME");
        } else if (dateType == TelegramDateMasks.EXT_TIME.getValue()) {
            this.type = VIFUnitMultiplierMasks.EXTENTED_TIME;
            mUnit = MeasureUnit.DATE_TIME;
            logger.debug("\t - DATE_TIME / EXTENDED_TIME");
        } else if (dateType == TelegramDateMasks.EXT_DATE_TIME.getValue()) {
            this.type = VIFUnitMultiplierMasks.EXTENTED_DATE_TIME;
            mUnit = MeasureUnit.EPOCH_TIME;
            logger.debug("\t - EPOCH-TIME");
        } else {
            logger.warn("\t - Cannot parse as date: " + dateType);
        }
    }

    public TelegramVariableDataRecord getParent() {
        return parent;
    }

    public void setParent(TelegramVariableDataRecord parent) {
        this.parent = parent;
    }

    public boolean isExtensionBit() {
        return extensionBit;
    }

    public VIFUnitMultiplierMasks getType() {
        return type;
    }

    public void setType(VIFUnitMultiplierMasks type) {
        this.type = type;
    }

    public void setExtensionBit(boolean extensionBit) {
        this.extensionBit = extensionBit;
    }


    public MeasureUnit getMeasureUnit() {
        return mUnit;
    }

    public void setMeasureUnit(MeasureUnit mUnit) {
        this.mUnit = mUnit;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public void debugOutput() {
        if (this.getFieldParts().size() == 0) {
            return;
        }
        logger.debug("VIF-Field: ");
        logger.debug("\tExtension-Bit: \t\t" + this.extensionBit);

        String vifField = this.fieldParts.get(0);
        int iVifField = Converter.hexToInt(vifField);
        logger.debug("\tField (String): \t" + vifField);
        logger.debug("\tField (compl): \t\t" + Integer.toBinaryString(iVifField));
        int iVifFieldBits = (iVifField & VIFTelegramField.UNIT_MULTIPLIER_MASK);
        logger.debug("\tField-Value: \t\t" + Integer.toBinaryString(iVifFieldBits));
        logger.debug("\tField-Type: \t\t" + this.type);

        logger.debug("\tField-Unit: \t\t" + this.mUnit);
        logger.debug("\tField-Multiplier: \t" + this.multiplier);
    }

    public boolean isProfile() {
        return this.fieldParts != null
                && this.fieldParts.size() > 0
                && this.fieldParts.get(0).equalsIgnoreCase("93"); // exception for Merlin
    }
}