package com.elster.protocolimpl.dlms.messaging;

import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.protocol.messaging.MessageCategorySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights Elster GmbH
 * Date: 4/10/13
 * Time: 13:03
 */
public class A1MessageExecutor extends DlmsMessageExecutor
{

    public A1MessageExecutor(Dlms dlms) {
        super(dlms);
    }

    @Override
    protected AbstractDlmsMessage[] getSupportedMessages() {
        return new AbstractDlmsMessage[]{
                new A1WritePDRMessage(this),                 /*2*/
                new A1ApnSetupMessage(this),                 /*1*/
                new A1WanConfigurationMessage(this),         /*7*/
                new A1ConfigureCyclicMode(this),             /*5*/
                new A1ConfigurePreferredDateMode(this),      /*6*/
                new A1ChangeSessionTimeoutMessage(this),     /*4*/
                new A1DisablePassiveTariffMessage(this),     /*20*/
                new ForceSyncClockMessage(this),             /*8*/
                new ChangeKeysMessage(this),                 /*3*/
                new UmiFirmwareUpdateMessage(this),          /*9*/
                new A1ResetAlarmMessage(this),               /*11*/
                new A1ChangeUnitsStatusMessage(this),        /*10*/
                new A1SetBillingPeriodStartMessage(this),    /*14*/
                new A1SetPeriodLengthMessage(this),          /*13*/
                new A1SetOnDemandSnapshotTimeMessage(this),  /*15*/
                new A1ResetUnitsLogMessage(this),            /*12*/
                new A1SetRSSIMultiSampling(this),            /*17*/
                new A1WriteStartOfGasDayMessage(this),       /* */
                new A1WriteSpecialDaysTableMessage(this),    /*18*/
                new A1WritePassiveCalendarMessage(this),     /*19*/
                new A1WriteClockConfigurationMessage(this),  /*16*/
                new A1WriteGasDayConfigurationMessage(this)
        };
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> messageCategories = new ArrayList<MessageCategorySpec>();
        messageCategories.add(getConnectivityCategory());
        messageCategories.add(getTariffCategory());
        messageCategories.add(getCommonParameterCategory());
        messageCategories.add(getDeviceMaintenanceCategory());
        return messageCategories;
    }

    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Change connectivity setup");
        categorySpec.addMessageSpec(A1ApnSetupMessage.getMessageSpec("Change GPRS modem setup parameters", false));
        categorySpec.addMessageSpec(A1WanConfigurationMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1ConfigureCyclicMode.getMessageSpec(false));
        categorySpec.addMessageSpec(A1ConfigurePreferredDateMode.getMessageSpec(false));
        categorySpec.addMessageSpec(A1ChangeSessionTimeoutMessage.getMessageSpec(false));
//        categorySpec.addMessageSpec(WriteAutoConnectMessage.getMessageSpec(true));
//        categorySpec.addMessageSpec(DisableAutoConnectMessage.getMessageSpec(true));
//        categorySpec.addMessageSpec(WriteAutoAnswerMessage.getMessageSpec(true));
//        categorySpec.addMessageSpec(DisableAutoAnswerMessage.getMessageSpec(true));

        return categorySpec;
    }

    private MessageCategorySpec getDeviceMaintenanceCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Device maintenance");
        categorySpec.addMessageSpec(ForceSyncClockMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1ResetAlarmMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(ChangeKeysMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(A1ChangeUnitsStatusMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(A1ResetUnitsLogMessage.getMessageSpec(true));
        categorySpec.addMessageSpec(A1SetRSSIMultiSampling.getMessageSpec(false));
        categorySpec.addMessageSpec(A1WriteClockConfigurationMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1WriteGasDayConfigurationMessage.getMessageSpec(false));

        return categorySpec;
    }

    private MessageCategorySpec getTariffCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Tariff setup");

        categorySpec.addMessageSpec(A1SetBillingPeriodStartMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1SetPeriodLengthMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1SetOnDemandSnapshotTimeMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1WriteSpecialDaysTableMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1WritePassiveCalendarMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1DisablePassiveTariffMessage.getMessageSpec(false));

        return categorySpec;
    }

    private MessageCategorySpec getCommonParameterCategory() {
        MessageCategorySpec categorySpec = new MessageCategorySpec("Change common parameter");
        categorySpec.addMessageSpec(A1WritePDRMessage.getMessageSpec(false));
        categorySpec.addMessageSpec(A1WriteStartOfGasDayMessage.getMessageSpec(false));
        return categorySpec;
    }

}
