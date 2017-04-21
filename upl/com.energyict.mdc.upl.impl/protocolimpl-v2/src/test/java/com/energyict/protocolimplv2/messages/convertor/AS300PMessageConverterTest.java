package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.PricingInformationMessage;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.Duration;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by cisac on 8/17/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class AS300PMessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(PricingInformationMessage.SEND_NEW_TARIFF.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SendNewTariff><IncludedFile>Content</IncludedFile></SendNewTariff>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SEND_NEW_PRICE_MATRIX.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SendNewPriceMatrix><IncludedFile>Content</IncludedFile></SendNewPriceMatrix>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetStandingCharge.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetStandingChargeAndActivationDate StandingCharge=\"1\"> </SetStandingChargeAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SetStandingChargeAndActivationDate.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetStandingChargeAndActivationDate StandingCharge=\"1\" ActivationDate=\"28/10/2013 10:00:00\"> </SetStandingChargeAndActivationDate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SET_CURRENCY.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCurrency Currency=\"2\"> </SetCurrency>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PricingInformationMessage.SET_CURRENCY_AND_ACTIVATION_DATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetCurrency Currency=\"2\" ActivationDate=\"28/10/2013 10:00:00\"> </SetCurrency>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ImportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Supplier_ImportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ImportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier_ImportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ExportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Supplier_ExportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ExportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier_ExportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectControlReconnect> </DisconnectControlReconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectControlDisconnect> </DisconnectControlDisconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetControlMode Control mode (range 0 - 6)=\"3\"> </SetControlMode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>path</IncludedFile></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>path</IncludedFile><ActivationDate>28/10/2013 10:00:00</ActivationDate></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Engineer_PIN PIN=\"12345555\" Timeout=\"300\"> </Change_Engineer_PIN>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN_AND_ACTIVATION_DATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Engineer_PIN PIN=\"12345555\" Timeout=\"300\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Engineer_PIN>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS300P(propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new AS300PMessageConverter(propertySpecService, this.nlsService, this.converter, this.deviceMessageFileExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.currency:
                    return "2";
                case DeviceMessageConstants.StandingChargeAttributeName:
                    return new BigDecimal(1);
                case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                    return "path";
                case DeviceMessageConstants.contractsXmlUserFileAttributeName:
                case DeviceMessageConstants.PricingInformationUserFileAttributeName:
                    DeviceMessageFile deviceMessageFile1 = mock(DeviceMessageFile.class);
                    when(deviceMessageFileExtractor.contents(eq(deviceMessageFile1), any(Charset.class))).thenReturn("Content");
                    return deviceMessageFile1;
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
                    return "13,434,35";
                case DeviceMessageConstants.contactorModeAttributeName:
                    return new BigDecimal(3);
                case DeviceMessageConstants.engineerPin:
                    return "12345555";
                case DeviceMessageConstants.engineerPinTimeout:
                    return Duration.ofSeconds(300);
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
