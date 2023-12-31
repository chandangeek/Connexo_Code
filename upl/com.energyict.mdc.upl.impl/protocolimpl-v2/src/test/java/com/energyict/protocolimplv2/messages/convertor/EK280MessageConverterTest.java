package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.elster.protocolimpl.dlms.EK280;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 13/08/2015 - 17:01
 */
@RunWith(MockitoJUnitRunner.class)
public class EK280MessageConverterTest extends AbstractV2MessageConverterTest {

    private Date activationDate = new Date(1441101600000L);

    @Test
    public void testMessageConversion_ChangeCredentials() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_modem_setup APN=\"MyTestAPN\" Username=\"MyTestUserName\" Password=\"MyTestPassword\"> </GPRS_modem_setup>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_WriteNewPdrNumber() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.WriteNewPDRNumber.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WritePDR PdrToWrite=\"PDR\"> </WritePDR>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ClearPassiveTariff() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ClearPassiveTariff/>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ChangeSecurityKeys() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_SECURITY_KEYS.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ChangeKeys ClientId=\"1\" NewAuthenticationKey=\"AUTH_Key\" NewEncryptionKey=\"ENCR_Key\"> </ChangeKeys>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureAllGasParameters() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureAllGasParameters.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteGasParameters GasDensity=\"500\" RelativeDensity=\"0.6\" N2_Percentage=\"20\" CO2_Percentage=\"30\" CO_Percentage=\"40\" H2_Percentage=\"50\" Methane_Percentage=\"60\" CalorificValue=\"10000\"> </WriteGasParameters>", messageEntry
                .getContent());
    }

    @Test
    public void testMessageConversion_ChangeMeterLocation() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ChangeMeterLocation.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<MeterLocation Location=\"New location\"> </MeterLocation>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureGasMeterMasterData() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteMeterMasterData MeterType=\"MASS\" MeterCaliber=\"100\" MeterSerial=\"MeterSerial\"> </WriteMeterMasterData>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureAutoAnswer() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ConfigureAutoAnswer.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetAutoAnswer AutoAnswerId=\"1\" AutoAnswerStart=\"start\" AutoAnswerEnd=\"end\"> </SetAutoAnswer>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_DisableAutoAnswer() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.DisableAutoAnswer.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisableAutoAnswer AutoAnswerId=\"1\"> </DisableAutoAnswer>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ConfigureAutoConnect() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ConfigureAutoConnect.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetAutoConnect AutoConnectId=\"1\" AutoConnectMode=\"2\" AutoConnectStart=\"start\" AutoConnectEnd=\"end\" Destination1=\"localhost\"> </SetAutoConnect>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_DisableAutoConnect() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(NetworkConnectivityMessage.DisableAutoConnect.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisableAutoConnect AutoConnectId=\"1\"> </DisableAutoConnect>", messageEntry.getContent());
    }

    @Test
    public void testMessageConversion_ActivityCalendarSend() {
        OfflineDeviceMessage offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE.get(propertySpecService, this.nlsService, this.converter));
        MessageEntry messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expectedDate = dateFormat.format(activationDate);
        assertEquals("<UploadPassiveTariff CodeTableId=\"base64_codeTable\" ActivationTime=\"" + expectedDate + "\" DefaultTariff=\"3\"> </UploadPassiveTariff>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new EK280(this.calendarFinder, this.calendarExtractor, propertySpecService, deviceMessageFileFinder, deviceMessageFileExtractor, nlsService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new EK280MessageConverter(propertySpecService, this.nlsService, this.converter, calendarFinder, this.calendarExtractor, this.deviceMessageFileExtractor, deviceMessageFileFinder, keyAccessorTypeExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.apnAttributeName:
                return "MyTestAPN";
            case DeviceMessageConstants.usernameAttributeName:
                return "MyTestUserName";
            case DeviceMessageConstants.passwordAttributeName:
                KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
                when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("MyTestPassword");
                return keyAccessorType;
            case DeviceMessageConstants.clientMacAddress:
                return BigDecimal.ONE;
            case DeviceMessageConstants.newAuthenticationKeyAttributeName:
                keyAccessorType = mock(KeyAccessorType.class);
                when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("AUTH_Key");
                return keyAccessorType;
            case DeviceMessageConstants.newEncryptionKeyAttributeName:
                keyAccessorType = mock(KeyAccessorType.class);
                when(keyAccessorTypeExtractor.passiveValueContent(keyAccessorType)).thenReturn("ENCR_Key");
                return keyAccessorType;
            case DeviceMessageConstants.newPDRAttributeName:
                return "PDR";
            case DeviceMessageConstants.gasDensityAttributeName:
                return BigDecimal.valueOf(500);
            case DeviceMessageConstants.relativeDensityAttributeName:
                return BigDecimal.valueOf(0.6);
            case DeviceMessageConstants.molecularNitrogenPercentageAttributeName:
                return BigDecimal.valueOf(20);
            case DeviceMessageConstants.carbonDioxidePercentageAttributeName:
                return BigDecimal.valueOf(30);
            case DeviceMessageConstants.carbonOxidePercentageAttributeName:
                return BigDecimal.valueOf(40);
            case DeviceMessageConstants.molecularHydrogenPercentageAttributeName:
                return BigDecimal.valueOf(50);
            case DeviceMessageConstants.methanePercentageAttributeName:
                return BigDecimal.valueOf(60);
            case DeviceMessageConstants.higherCalorificValueAttributeName:
                return BigDecimal.valueOf(10000);
            case DeviceMessageConstants.meterLocationAttributeName:
                return "New location";
            case DeviceMessageConstants.meterTypeAttributeName:
                return "MASS";
            case DeviceMessageConstants.meterCaliberAttributeName:
                return BigDecimal.valueOf(100);
            case DeviceMessageConstants.meterSerialNumberAttributeName:
                return "MeterSerial";
            case DeviceMessageConstants.windowAttributeName:
                return BigDecimal.ONE;
            case DeviceMessageConstants.autoConnectMode:
                return NetworkConnectivityMessage.AutoConnectMode.InsideWindow.getDescription();
            case DeviceMessageConstants.autoConnectStartTime:
                return "start";
            case DeviceMessageConstants.autoConnectEndTime:
                return "end";
            case DeviceMessageConstants.autoConnectDestination1:
                return "localhost";
            case DeviceMessageConstants.autoConnectDestination2:
                return "N/A";
            case DeviceMessageConstants.autoAnswerStartTime:
                return "start";
            case DeviceMessageConstants.autoAnswerEndTime:
                return "end";
            case DeviceMessageConstants.activityCalendarNameAttributeName:
                return "TariffCalendar";
            case DeviceMessageConstants.activityCalendarAttributeName:
                return "base64_codeTable";
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
                return activationDate;
            case DeviceMessageConstants.defaultTariffCodeAttrributeName:
                return "3";
            default:
                return "";
        }
    }
}