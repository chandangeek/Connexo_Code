package com.energyict.protocolimpl.coronis.waveflow.core.messages;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.util.ArrayList;
import java.util.List;

public class WaveFlowV1Messages extends WaveFlowMessageParser {

    public WaveFlowV1Messages(WaveFlow waveFlow) {
        super.waveFlow = waveFlow;
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();

        MessageCategorySpec cat1 = new MessageCategorySpec("Waveflow general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatusFull", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", false));
        cat1.addMessageSpec(addBasicMsgWithValue("Set number of inputs to be used", "SetNumberOfInputs", false));
        cat1.addMessageSpec(addBasicMsgWithThreeValues("Define the pulse weight [min 1, max 15]", "DefinePulseWeight", true, "Channel input (1, 2, 3 or 4)", "Unit scaler (0 = millilitre, 3 = litre, 5 = hectolitre,... )"));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Waveflow data logging messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Restart data logging in 3 minutes. Mode: [periodic steps (1), weekly (2), monthly (3)]", "RestartDataLogging", false));
        cat1.addMessageSpec(addBasicMsgWithValue("Restart data logging, use start time parameters. Mode: [periodic steps (1), weekly (2), monthly (3)]", "SimpleRestartDataLogging", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set sampling period in seconds", "SetProfileInterval", true));
        cat2.addMessageSpec(addBasicMsgWithTwoAttr("Set start hour of measurement", "SetHourOfMeasurement", true, "Start hour (range: 0 - 23)", "Intended mode (1: periodic, 2: weekly, 3: monthly)"));
        cat2.addMessageSpec(addBasicMsgWithValue("Set day of week (or month) (not available for periodic step logging)", "SetDayOfWeek", true));
        cat2.addMessageSpec(addBasicMsg("Reset the indexes", "ResetIndexes", true));
        cat2.addMessageSpec(addBasicMsg("Stop the data logging", "StopDataLogging", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write index A", "WriteIndexA", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write index B", "WriteIndexB", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write index C", "WriteIndexC", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write index D", "WriteIndexD", true));
        cat2.addMessageSpec(addBasicMsg("Enable weekly data logging", "SetWeeklyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Enable monthly data logging", "SetMonthlyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Enable periodic steps data logging", "SetPeriodicStepLogging", true));
        theCategories.add(cat2);

        MessageCategorySpec cat4 = new MessageCategorySpec("Waveflow leakage configuration");
        cat4.addMessageSpec(addBasicMsgWithValue("Set the measurement step (in minutes)", "SetMeasurementStep", true));
        cat4.addMessageSpec(addBasicMsgWithThreeValues("Set leakage flow threshold (pulses per measurement step)", "SetLeakageThreshold", true, "Residual (0) or extreme (1)", "Input channel (1 = A, 2 = B,...)"));
        cat4.addMessageSpec(addBasicMsgWithThreeValues("Set leakage detection period (multiples of measurement step)", "SetLeakageDetectionPeriod", true, "Residual (0) or extreme (1)", "Input channel (1 = A, 2 = B,...)"));
        theCategories.add(cat4);

        MessageCategorySpec cat5 = new MessageCategorySpec("Waveflow back flow configuration");
        cat5.addMessageSpec(addBasicMsgWithTwoValues("Set simple backflow threshold (in pulses per detection period)", "SetSimpleBackflowThreshold", true, "Input (1 = A, 2 = B)"));
        cat5.addMessageSpec(addBasicMsgWithTwoValues("Set advanced backflow threshold (in pulses per detection period)", "SetAdvancedBackflowThreshold", true, "Input (1 = A, 2 = B)"));
        cat5.addMessageSpec(addBasicMsgWithTwoValues("Set simple backflow detection period (in hours)", "SetSimpleBackflowDetectionPeriod", true, "Input (1 = A, 2 = B)"));
        cat5.addMessageSpec(addBasicMsgWithTwoValues("Set advanced backflow detection period (in multiple of 10 minutes)", "SetAdvancedBackflowDetectionPeriod", true, "Input (1 = A, 2 = B)"));
        cat5.addMessageSpec(addBasicMsgWithAttr("Select backflow detection method", "SetBackflowDetectionMethod", true, "0 = with measurement of water volume, 1 = measurement of water flow-rate"));
        theCategories.add(cat5);

        MessageCategorySpec cat7 = new MessageCategorySpec("Waveflow alarm frames configuration");
        cat7.addMessageSpec(addBasicMsgWithAttr("Initialize the alarm route and set the alarm config byte", "InitializeRoute", true, "Alarm config byte (decimal value)"));
        cat7.addMessageSpec(addBasicMsgWithValue("Set the alarm configuration byte", "SetAlarmConfig", true));
        cat7.addMessageSpec(addBasicMsgWithValue("Set number of repeaters (max 3)", "SetNumberOfRepeaters", true));
        cat7.addMessageSpec(addBasicMsgWithTwoAttr("Set address of repeater", "SetRepeaterAddress", true, "Number of the repeater (1, 2 or 3)", "Address (hex string)"));
        cat7.addMessageSpec(addBasicMsgWithAttr("Set address of the recipient", "SetRecipientAddress", true, "Address (hex string)"));
        theCategories.add(cat7);

        MessageCategorySpec cat8 = new MessageCategorySpec("Waveflow enable alarm frames");
        cat8.addMessageSpec(addBasicMsg("Send alarm on wirecut detection", "SendAlarmOnWirecutDetection", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on end of battery life", "SendAlarmOnBatteryEnd", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on low leak detection", "SendAlarmOnLowLeakDetection", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on high leak detection", "SendAlarmOnHighLeakDetection", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on backflow detection", "SendAlarmOnBackflowDetection", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on valve wirecut", "SendAlarmOnValveWirecut", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on valve close fault", "SendAlarmOnValveCloseFault", true));
        cat8.addMessageSpec(addBasicMsg("Send alarm on threshold detection of credit amount", "SendAlarmOnThresholdDetectionOfCreditAmount", true));
        cat8.addMessageSpec(addBasicMsg("Send all alarms", "SendAllAlarms", true));
        theCategories.add(cat8);

        MessageCategorySpec cat9 = new MessageCategorySpec("Waveflow disable alarm frames");
        cat9.addMessageSpec(addBasicMsg("Disable alarm on wirecut detection", "DisableAlarmOnWirecutDetection", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on end of battery life", "DisableAlarmOnBatteryEnd", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on low leak detection", "DisableAlarmOnLowLeakDetection", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on high leak detection", "DisableAlarmOnHighLeakDetection", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on backflow detection", "DisableAlarmOnBackflowDetection", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on valve wirecut", "DisableAlarmOnValveWirecut", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on valve close fault", "DisableAlarmOnValveCloseFault", true));
        cat9.addMessageSpec(addBasicMsg("Disable alarm on threshold detection of credit amount", "DisableAlarmOnThresholdDetectionOfCreditAmount", true));
        cat9.addMessageSpec(addBasicMsg("Disable all alarms", "DisableAllAlarms", true));
        theCategories.add(cat9);

        MessageCategorySpec cat10 = new MessageCategorySpec("Waveflow application status");
        cat10.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatusFull", true));
        cat10.addMessageSpec(addBasicMsg("Reset low battery flag", "ResetApplicationStatusBit0", true));
        cat10.addMessageSpec(addBasicMsg("Reset tamper (wire cut) flag", "ResetApplicationStatusBit1", true));
        cat10.addMessageSpec(addBasicMsg("Reset no flow flag", "ResetApplicationStatusBit1", true));
        cat10.addMessageSpec(addBasicMsg("Reset tamper (removal) flag", "ResetApplicationStatusBit1", true));
        cat10.addMessageSpec(addBasicMsg("Reset low leak flag", "ResetApplicationStatusBit3", true));
        cat10.addMessageSpec(addBasicMsg("Reset burst (high leak) flag", "ResetApplicationStatusBit4", true));
        cat10.addMessageSpec(addBasicMsg("Reset tamper (magnet) flag", "ResetApplicationStatusBit5", true));
        cat10.addMessageSpec(addBasicMsg("Reset backflow flag", "ResetApplicationStatusBit7", true));
        theCategories.add(cat10);

        MessageCategorySpec cat11 = new MessageCategorySpec("Waveflow wakeup configuration");
        cat11.addMessageSpec(addBasicMsgWithValue("Write wakeup system status word", "SetWakeUpSystemStatusWord", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Write default wakeup period (in seconds) ", "SetDefaultWakeUpPeriod", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Write start time (hour) for 1st time window", "SetStartTimeForTimeWindow1", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Write wakeup period for 1st time window (in seconds)", "SetWakeUpPeriodForTimeWindow1", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Write start time (hour) for 2nd time window", "SetStartTimeForTimeWindow2", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Write wakeup period for 2nd time window (in seconds)", "SetWakeUpPeriodForTimeWindow2", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Enable/disable time windows, by day of the week ", "SetEnableTimeWindowsByDayOfWeek", true));
        cat11.addMessageSpec(addBasicMsgWithValue("Enable/disable wakeup periods, by day of the week", "SetEnableWakeUpPeriodsByDayOfWeek", true));
        theCategories.add(cat11);

        return theCategories;
    }
}