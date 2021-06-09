package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * Provides a summary of all messages related to device paramters display
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum DisplayDeviceParametersMessage implements DeviceMessageSpecSupplier {

    DISPLAY_GENERAL_PARAMETERS(19, "General display device parameters") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    stringSpec(service,     DeviceMessageConstants.DisplayMessageAttributeName,                 DeviceMessageConstants.DisplayMessageAttributeDefaultTranslation),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayMessageTimeDurationAttributeName,     DeviceMessageConstants.DisplayMessageTimeDurationAttributeDefaultTranslation),
                    dateTimeSpec(service,   DeviceMessageConstants.DisplayMessageActivationDate,                DeviceMessageConstants.DisplayMessageActivationDefaultTranslation),
                    booleanSpec(service,    DeviceMessageConstants.DisplayLeadingZero,                          DeviceMessageConstants.DisplayLeadingZeroTranslation, true),
                    booleanSpec(service,    DeviceMessageConstants.DisplayBacklight,                            DeviceMessageConstants.DisplayBacklightTranslation, true),
                    booleanSpec(service,    DeviceMessageConstants.DisplayEOB_Confirm,                          DeviceMessageConstants.DisplayEOB_ConfirmTranslation, true),
                    stringSpec(service,     DeviceMessageConstants.DisplayString_EOB_Confirm,                   DeviceMessageConstants.DisplayString_EOB_ConfirmTranslation, "V EOB"),
                    booleanSpec(service,    DeviceMessageConstants.DisplaySeparators_Display,                   DeviceMessageConstants.DisplaySeparators_DisplayTranslation, true),
                    booleanSpec(service,    DeviceMessageConstants.DisplayTime_Format,                          DeviceMessageConstants.DisplayTime_FormatTranslation, true),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayDate_Format,                          DeviceMessageConstants.DisplayDate_FormatTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(1) ),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayTimeOut_For_Set_Mode,                 DeviceMessageConstants.DisplayTimeOut_For_Set_ModeTranslation, new BigDecimal(10) ),
                    bigDecimalSpec(service, DeviceMessageConstants.Display_On_TimeOut,                          DeviceMessageConstants.Display_On_TimeOutTranslation, new BigDecimal(8) ),
                    bigDecimalSpec(service, DeviceMessageConstants.Display_Off_TimeOut,                         DeviceMessageConstants.Display_Off_TimeOutTranslation, new BigDecimal(2) ),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_Mode,   DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Normal_ModeTranslation, new BigDecimal(1)),
                    booleanSpec(service,    DeviceMessageConstants.DisplayExistence_Of_EndOfText,               DeviceMessageConstants.DisplayExistence_Of_EndOfTextTranslation, false),
                    stringSpec(service,     DeviceMessageConstants.DisplayEndOfTextString,                      DeviceMessageConstants.DisplayEndOfTextStringTranslation, " "),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1,      DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode1Translation, new BigDecimal(1)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2,      DeviceMessageConstants.DisplayNb_DisplayedHisto_Sets_Altmode2Translation, new BigDecimal(0)),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayTimeout_For_AltMode,                  DeviceMessageConstants.DisplayTimeout_For_AltModeTranslation, new BigDecimal(10) ),
                    booleanSpec(service,    DeviceMessageConstants.DisplayAutorized_EOB,                        DeviceMessageConstants.DisplayAutorized_EOBTranslation, true),
                    bigDecimalSpec(service, DeviceMessageConstants.DisplayTimeout_Load_Profile,                 DeviceMessageConstants.DisplayTimeout_Load_ProfileTranslation, new BigDecimal(10)),
                    booleanSpec(service,    DeviceMessageConstants.Displaying_Of_LPMenus,                       DeviceMessageConstants.Displaying_Of_LPMenusTranslation, false),
                    booleanSpec(service,    DeviceMessageConstants.Display_ButtonEmulation_By_Optical_Head,     DeviceMessageConstants.Display_ButtonEmulation_By_Optical_HeadTranslation, false)
            );
        }
    },

    DISPLAY_READOUT_TABLE_PARAMETERS(7, "Display readout table parameters") {
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

    private final int id;
    private final String defaultNameTranslation;



    DisplayDeviceParametersMessage(int id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return DisplayDeviceParametersMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.DISPLAY,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);
}
