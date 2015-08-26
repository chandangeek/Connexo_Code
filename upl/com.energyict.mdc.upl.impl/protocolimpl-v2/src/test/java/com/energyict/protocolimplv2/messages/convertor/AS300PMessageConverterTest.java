package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P;
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
public class AS300PMessageConverterTest extends AbstractMessageConverterTest {

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
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_TENANT_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Tenant Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Tenant>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ImportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Supplier_ImportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ImportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier_ImportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ExportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\"> </Change_Of_Supplier_ExportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Of_Supplier_ExportEnergy Tenant_Reference=\"5\" Supplier_Reference=\"6\" Supplier_Id=\"7\" Script_executed=\"13,434,35\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Of_Supplier_ExportEnergy>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectControlReconnect> </DisconnectControlReconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectControlDisconnect> </DisconnectControlDisconnect>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetControlMode Control mode (range 0 - 6)=\"3\"> </SetControlMode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>Content</IncludedFile><ActivationDate>28/10/2013 10:00:00</ActivationDate></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Engineer_PIN PIN=\"12345555\" Timeout=\"300\"> </Change_Engineer_PIN>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN_AND_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_Engineer_PIN PIN=\"12345555\" Timeout=\"300\" ActivationDate=\"28/10/2013 10:00:00\"> </Change_Engineer_PIN>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS300P();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new AS300PMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.currency:
                    return "AAA";
                case DeviceMessageConstants.StandingChargeAttributeName:
                    return new BigDecimal(1);
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                case DeviceMessageConstants.contractsXmlUserFileAttributeName:
                case DeviceMessageConstants.PricingInformationUserFileAttributeName:
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
                    return "13,434,35";
                case DeviceMessageConstants.contactorModeAttributeName:
                    return new BigDecimal(3);
                case DeviceMessageConstants.engineerPin:
                    return "12345555";
                case DeviceMessageConstants.engineerPinTimeout:
                    return TimeDuration.seconds(300);
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
