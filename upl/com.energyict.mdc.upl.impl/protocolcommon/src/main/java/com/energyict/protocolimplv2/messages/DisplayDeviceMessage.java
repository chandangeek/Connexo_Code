package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all messages related to a <i>Display</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum DisplayDeviceMessage implements DeviceMessageSpecSupplier {

    CONSUMER_MESSAGE_CODE_TO_PORT_P1(10001, "Send a code message to the P1 port") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    CONSUMER_MESSAGE_TEXT_TO_PORT_P1(10002, "Send a text message to the P1 port") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.p1InformationAttributeName, DeviceMessageConstants.p1InformationAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE(10003, "Set display message") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation));
        }
    },
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(10004, "Set display message with options") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    SET_DISPLAY_MESSAGE_ON_IHD_WITH_OPTIONS(10005, "Set display message on IHD with options") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.DisplayMessageAttributeName, DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.DisplayMessageActivationDate, DeviceMessageConstants.DisplayMessageActivationDefaultTranslation)
            );
        }
    },
    CLEAR_DISPLAY_MESSAGE(10006, "Clear display message") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DISPLAY_GENERAL_PARAMETERS(10007, "General display device parameters") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    booleanSpec(service,            DeviceMessageConstants.DisplayLeadingZero,                          DeviceMessageConstants.DisplayLeadingZeroTranslation, true),
                    booleanSpec(service,            DeviceMessageConstants.DisplayBacklight,                            DeviceMessageConstants.DisplayBacklightTranslation, true),
                    booleanSpec(service,            DeviceMessageConstants.DisplayEOB_Confirm,                          DeviceMessageConstants.DisplayEOB_ConfirmTranslation, true),
                    stringSpec(service,             DeviceMessageConstants.DisplayString_EOB_Confirm,                   DeviceMessageConstants.DisplayString_EOB_ConfirmTranslation, "V EOB"),
                    booleanSpec(service,            DeviceMessageConstants.DisplaySeparators_Display,                   DeviceMessageConstants.DisplaySeparators_DisplayTranslation, true),
                    booleanSpec(service,            DeviceMessageConstants.DisplayTime_Format,                          DeviceMessageConstants.DisplayTime_FormatTranslation, true),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayDate_Format,                          DeviceMessageConstants.DisplayDate_FormatTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(1) ),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayTimeOut_For_Set_Mode,                 DeviceMessageConstants.DisplayTimeOut_For_Set_ModeTranslation, new BigDecimal(10) ),
                    bigDecimalSpec(service,         DeviceMessageConstants.Display_On_TimeOut,                          DeviceMessageConstants.Display_On_TimeOutTranslation, new BigDecimal(8) ),
                    bigDecimalSpec(service,         DeviceMessageConstants.Display_Off_TimeOut,                         DeviceMessageConstants.Display_Off_TimeOutTranslation, new BigDecimal(2) ),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_Mode,   DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_ModeTranslation, new BigDecimal(1)),
                    booleanSpec(service,            DeviceMessageConstants.DisplayExistence_Of_EndOfText,               DeviceMessageConstants.DisplayExistence_Of_EndOfTextTranslation, false),
                    stringSpecWithDefault(service,  DeviceMessageConstants.DisplayEndOfTextString,                      DeviceMessageConstants.DisplayEndOfTextStringTranslation, "<space>" ),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1,      DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1Translation, new BigDecimal(1)),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2,      DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2Translation, new BigDecimal(0)),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayTimeout_For_AltMode,                  DeviceMessageConstants.DisplayTimeout_For_AltModeTranslation, new BigDecimal(10) ),
                    booleanSpec(service,            DeviceMessageConstants.DisplayAutorized_EOB,                        DeviceMessageConstants.DisplayAutorized_EOBTranslation, true),
                    bigDecimalSpec(service,         DeviceMessageConstants.DisplayTimeout_Load_Profile,                 DeviceMessageConstants.DisplayTimeout_Load_ProfileTranslation, new BigDecimal(10)),
                    booleanSpec(service,            DeviceMessageConstants.Displaying_Of_LPMenus,                       DeviceMessageConstants.Displaying_Of_LPMenusTranslation, false),
                    booleanSpec(service,            DeviceMessageConstants.Display_ButtonEmulation_By_Optical_Head,     DeviceMessageConstants.Display_ButtonEmulation_By_Optical_HeadTranslation, false)
            );
        }
    },
    DISPLAY_READOUT_TABLE_PARAMETERS(10008, "Display readout table parameters") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayInternal_Identifier, DeviceMessageConstants.DisplayInternal_IdentifierTranslation, new BigDecimal(0)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplaySequence_Indicator, DeviceMessageConstants.DisplaySequence_IndicatorTranslation, new BigDecimal(0)),
                    stringSpec(service, DeviceMessageConstants.DisplayIdentification_Code, DeviceMessageConstants.DisplayIdentification_CodeTranslation, 5),
                    bigDecimalSpecWithPossibleValues(service, DeviceMessageConstants.DisplayScaler, DeviceMessageConstants.DisplayScalerTranslation, new BigDecimal(0), new BigDecimal(0)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNumber_Of_Decimal, DeviceMessageConstants.DisplayNumber_Of_DecimalTranslation, new BigDecimal(0)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNumber_Of_Display_Historical_Data, DeviceMessageConstants.DisplayNumber_Of_Display_Historical_DataTranslation, new BigDecimal(0)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNumber_Of_Displayable_Digit, DeviceMessageConstants.DisplayNumber_Of_Displayable_DigitTranslation, new BigDecimal(8))
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    DisplayDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private String getNameResourceKey() {
        return DisplayDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public long id() {
        return this.id;
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.DISPLAY,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    protected PropertySpec stringSpecWithDefault(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String defaultValue) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .markRequired()
                .finish();
    }

}