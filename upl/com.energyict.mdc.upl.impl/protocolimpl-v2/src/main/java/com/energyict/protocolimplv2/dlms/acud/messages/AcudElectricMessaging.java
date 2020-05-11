package com.energyict.protocolimplv2.dlms.acud.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcudElectricMessaging extends AcudMessaging {

    public AcudElectricMessaging(AbstractDlmsProtocol protocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, propertySpecService, nlsService, converter, messageFileExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList(super.getSupportedMessages());
        supportedMessages.addAll(Arrays.asList(
                CreditDeviceMessage.UPDATE_MONEY_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                CreditDeviceMessage.UPDATE_CONSUMPTION_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                CreditDeviceMessage.UPDATE_TIME_CREDIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_CURRENT_OVER_LIMIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_CURRENT_OVER_LIMIT_TIME_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_VOLTAGE_UNDER_LIMIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_VOLTAGE_UNDER_LIMIT_TIME_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_LIPF_UNDER_LIMIT_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter()),
                LoadBalanceDeviceMessage.SET_LIPF_UNDER_LIMIT_TIME_THRESHOLD.get(getPropertySpecService(), getNlsService(), getConverter())
                )
        );
        return supportedMessages;
    }

    protected AcudMessageExecutor createMessageExecutor() {
        return new AcudElectricMessageExecutor(getProtocol(), getProtocol().getCollectedDataFactory(), getProtocol().getIssueFactory());
    }
}
