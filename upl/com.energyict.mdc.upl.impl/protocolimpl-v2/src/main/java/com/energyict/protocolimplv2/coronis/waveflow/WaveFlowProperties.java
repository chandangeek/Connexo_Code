package com.energyict.protocolimplv2.coronis.waveflow;

import com.energyict.cpo.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 16:21
 * Author: khe
 */
public class WaveFlowProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";

    private static final String PROP_SCALE_A = "ScaleA";
    private static final String PROP_SCALE_B = "ScaleB";
    private static final String PROP_SCALE_C = "ScaleC";
    private static final String PROP_SCALE_D = "ScaleD";
    private static final String PROP_MULTIPLIER_A = "MultiplierA";
    private static final String PROP_MULTIPLIER_B = "MultiplierB";
    private static final String PROP_MULTIPLIER_C = "MultiplierC";
    private static final String PROP_MULTIPLIER_D = "MultiplierD";

    private static final String APPLICATION_STATUS_VARIANT = "ApplicationStatusVariant";
    private static final String INITIAL_RF_COMMAND = "InitialRFCommand";
    private static final String ROUND_DOWN_TO_NEAREST_INTERVAL = "RoundDownToNearestInterval";
    private static final String ENABLE_MULTI_FRAME_MODE = "EnableMultiFrameMode";
    private static final String WAVENIS_BUBBLE_UP_INFO = "WavenisBubbleUpInfo";


    private TypedProperties properties;

    public WaveFlowProperties(TypedProperties properties) {
        this.properties = properties;
    }

    /**
     * Getter for the (required) RFAddress property.
     * Format is radioaddress[_repeater1][,repeater2][,repeater3]
     */
    public String getRFAddress() {
        return properties.getStringProperty(WavenisStackUtils.RF_ADDRESS);
    }

    /**
     * Getter for the RF timeout property
     */
    public int getTimeout() {
        return properties.getIntegerProperty(TIMEOUT, BigDecimal.valueOf(5000)).intValue();
    }

    /**
     * Getter for the RF retry property
     */
    public int getRetries() {
        return properties.getIntegerProperty(RETRIES, BigDecimal.valueOf(2)).intValue();
    }

    public void addProperties(TypedProperties allProperties) {
        properties.setAllProperties(allProperties);
    }

    public int getScaleA() {
        return properties.getIntegerProperty(PROP_SCALE_A, BigDecimal.valueOf(0)).intValue();
    }

    public int getScaleB() {
        return properties.getIntegerProperty(PROP_SCALE_B, BigDecimal.valueOf(0)).intValue();
    }

    public int getScaleC() {
        return properties.getIntegerProperty(PROP_SCALE_C, BigDecimal.valueOf(0)).intValue();
    }

    public int getScaleD() {
        return properties.getIntegerProperty(PROP_SCALE_D, BigDecimal.valueOf(0)).intValue();
    }

    public int getMultiplierA() {
        return properties.getIntegerProperty(PROP_MULTIPLIER_A, BigDecimal.valueOf(1)).intValue();
    }

    public int getMultiplierB() {
        return properties.getIntegerProperty(PROP_MULTIPLIER_B, BigDecimal.valueOf(1)).intValue();
    }

    public int getMultiplierC() {
        return properties.getIntegerProperty(PROP_MULTIPLIER_C, BigDecimal.valueOf(1)).intValue();
    }

    public int getMultiplierD() {
        return properties.getIntegerProperty(PROP_MULTIPLIER_D, BigDecimal.valueOf(1)).intValue();
    }

    public int getApplicationStatusVariant() {
        return properties.getIntegerProperty(APPLICATION_STATUS_VARIANT, BigDecimal.valueOf(0)).intValue();
    }

    public int getInitialRFCommand() {
        return properties.getIntegerProperty(INITIAL_RF_COMMAND, BigDecimal.valueOf(0)).intValue();
    }

    public boolean usesInitialRFCommand() {
        return getInitialRFCommand() != 0;
    }

    public boolean isRoundDownToNearestInterval() {
        return properties.<Boolean>getTypedProperty(ROUND_DOWN_TO_NEAREST_INTERVAL, false);
    }

    public boolean isEnableMultiFrameMode() {
        return properties.<Boolean>getTypedProperty(ENABLE_MULTI_FRAME_MODE, false);
    }

    public int getWavenisBubbleUpInfo() {
        String propertyValue = properties.<String>getTypedProperty(WAVENIS_BUBBLE_UP_INFO, "USED,1,28800,28800,1,000000000000");
        String[] split = propertyValue.split(",");
        if (split.length != 6) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(WAVENIS_BUBBLE_UP_INFO, propertyValue, "Should contain 6 comma separated parts.");
        }
        int startMoment;
        try {
            startMoment = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException(WAVENIS_BUBBLE_UP_INFO, propertyValue, "Bubble up start moment should be an integer.");
        }
        return startMoment;
    }

    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> required = new ArrayList<>();
        required.add(PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_SCALE_A));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_SCALE_B));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_SCALE_C));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_SCALE_D));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_MULTIPLIER_A));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_MULTIPLIER_B));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_MULTIPLIER_C));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(PROP_MULTIPLIER_D));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(APPLICATION_STATUS_VARIANT));
        required.add(PropertySpecFactory.bigDecimalPropertySpec(INITIAL_RF_COMMAND));
        required.add(PropertySpecFactory.booleanPropertySpecWithoutThreeState(ROUND_DOWN_TO_NEAREST_INTERVAL));
        required.add(PropertySpecFactory.booleanPropertySpecWithoutThreeState(ENABLE_MULTI_FRAME_MODE));
        required.add(PropertySpecFactory.stringPropertySpec(WAVENIS_BUBBLE_UP_INFO));
        return required;
    }
}