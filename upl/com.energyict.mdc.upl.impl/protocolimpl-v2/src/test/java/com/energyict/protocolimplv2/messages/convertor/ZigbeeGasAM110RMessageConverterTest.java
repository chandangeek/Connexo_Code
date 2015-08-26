package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ZigbeeGas;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by cisac on 8/17/2015.
 */
public class ZigbeeGasAM110RMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(PricingInformationMessage.SEND_NEW_TARIFF);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SendNewTariff><IncludedFile>Content</IncludedFile></SendNewTariff>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SEND_NEW_PRICE_MATRIX);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SendNewPriceMatrix><IncludedFile>Content</IncludedFile></SendNewPriceMatrix>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetStandingCharge);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetStandingChargeAndActivationDate StandingCharge=\"1\"> </SetStandingChargeAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetStandingChargeAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetStandingChargeAndActivationDate StandingCharge=\"1\" ActivationDate=\"28/10/2013 10:00:00\"> </SetStandingChargeAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SET_CURRENCY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCurrency Currency=\"AAA\"> </SetCurrency>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCurrency Currency=\"AAA\" ActivationDate=\"28/10/2013 10:00:00\"> </SetCurrency>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"12,334,545\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"12,334,545\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"12,334,545\"> </Change_Of_Supplier>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"12,334,545\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetCalorificValue);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCalorificValueAndActivationDate CalorificValue=\"1\"> </SetCalorificValueAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetCalorificValueAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCalorificValueAndActivationDate CalorificValue=\"1\" ActivationDate=\"28/10/2013 10:00:00\"> </SetCalorificValueAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetConversionFactor);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetConversionFactorAndActivationDate ConversionFactor=\"1\"> </SetConversionFactorAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SetConversionFactorAndActivationDate);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetConversionFactorAndActivationDate ConversionFactor=\"1\" ActivationDate=\"28/10/2013 10:00:00\"> </SetConversionFactorAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile><ActivationDate>28/10/2013 10:00:00</ActivationDate></FirmwareUpgrade>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ZigbeeGas();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ZigbeeGasAM110RMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.currency:
                    return "AAA";
                case DeviceMessageConstants.CalorificValue:
                case DeviceMessageConstants.ConversionFactor:
                case DeviceMessageConstants.StandingChargeAttributeName:
                    return new BigDecimal(1);
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                case DeviceMessageConstants.contractsXmlUserFileAttributeName:
                    UserFile mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.loadFileInByteArray()).thenReturn("Content".getBytes(Charset.forName("UTF-8")));
                    return mockedUserFile;
                case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                case DeviceMessageConstants.PricingInformationActivationDateAttributeName:
                case DeviceMessageConstants.ConfigurationChangeActivationDate:
                    return europeanDateTimeFormat.parse("28/10/2013 10:00:00");
                case DeviceMessageConstants.tenantReference:
                    return new BigDecimal(5);
                case DeviceMessageConstants.supplierReference:
                    return new BigDecimal(6);
                case DeviceMessageConstants.ChangeOfSupplierID:
                    return new BigDecimal(7);
                case DeviceMessageConstants.scriptExecuted:
                    return "12,334,545";

                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }

}
