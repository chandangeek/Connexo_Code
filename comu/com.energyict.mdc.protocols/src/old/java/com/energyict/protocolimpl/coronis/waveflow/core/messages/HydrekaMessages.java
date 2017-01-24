package com.energyict.protocolimpl.coronis.waveflow.core.messages;

import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.Hydreka;

import java.util.ArrayList;
import java.util.List;

public class HydrekaMessages extends WaveFlowMessageParser {

    public HydrekaMessages(Hydreka hydreka) {
        super.waveFlow = hydreka;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec cat1 = new MessageCategorySpec("Waveflow general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsgWithTwoAttr("Set operating mode with mask", "SetOperatingModeWithMask", false, "Operation mode (decimal value)", "Writing mask (decimal value)"));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatusFull", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", false));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Hydreka sequencing parameters");
        cat2.addMessageSpec(addBasicMsgWithValue("Set reading hour of the leakage status", "SetLeakageStatusReadingHour", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set reading hour of the histogram", "SetHistogramReadingHour", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set period of the RTC resync (in hours)", "SetRTCResyncPeriod", false));
        theCategories.add(cat2);

        MessageCategorySpec cat6 = new MessageCategorySpec("Waveflow push frame parameters");
        cat6.addMessageSpec(addBasicMsg("Enable push frames", "EnablePushFrames", true));
        cat6.addMessageSpec(addBasicMsgWithThreeAttr("Start push frame mechanism ", "StartPushFrames", true, "Command (e.g. Daily Hydreka data = 39)", "Transmission period (range: 1 - 63)", "Transmission period unit (0: minute, 1: hour, 2: day)"));
        cat6.addMessageSpec(addBasicMsg("Disable push frames", "DisablePushFrames", true));
        cat6.addMessageSpec(addBasicMsgWithFourValues("Set starting hour, minutes and seconds of the mechanism", "SetStartOfMechanism", true, "starting hour (0 - 23)", "minutes (0 - 59)", "seconds (0 - 59)"));
        cat6.addMessageSpec(addBasicMsgWithValue("Set push frame transmission period (in minutes!)", "SetTransmissionPeriod", true));
        cat6.addMessageSpec(addBasicMsgWithValue("Set max cancellation timeout (1 - 10 seconds)", "SetMaxCancelTimeout", true));
        cat6.addMessageSpec(addBasicMsgWithAttr("Add applicative command to the push frame command buffer", "AddCommandToBuffer", true, "Command (e.g. Daily Hydreka data = 39)"));
        theCategories.add(cat6);

        MessageCategorySpec cat7 = new MessageCategorySpec("Waveflow alarm frames configuration");
        cat7.addMessageSpec(addBasicMsg("Enable all alarms", "SendAllAlarms", true));
        cat7.addMessageSpec(addBasicMsg("Disable all alarms", "DisableAllAlarms", true));
        cat7.addMessageSpec(addBasicMsgWithValue("Set the alarm configuration byte", "SetAlarmConfig", true));
        cat7.addMessageSpec(addBasicMsgWithAttr("Initialize the alarm route and set the alarm config byte", "InitializeRoute", true, "Alarm config byte (decimal value)"));
        cat7.addMessageSpec(addBasicMsgWithAttr("Set the time slot granularity (in minutes)", "SetTimeSlotGranularity", true, "Allowed: 15, 30 or 60 minutes"));
        cat7.addMessageSpec(addBasicMsgWithAttr("Set the time slot duration (in seconds)", "SetTimeSlotDuration", true, "Allowed: 30, 45, 60, 90 or 120 seconds"));
        cat7.addMessageSpec(addBasicMsg("Enable the time slot mechanism", "EnableTimeSlotMechanism", true));
        cat7.addMessageSpec(addBasicMsg("Disable the time slot mechanism", "DisableTimeSlotMechanism", true));
        cat7.addMessageSpec(addBasicMsgWithValue("Set number of repeaters (max 3)", "SetNumberOfRepeaters", true));
        cat7.addMessageSpec(addBasicMsgWithTwoAttr("Set address of repeater", "SetRepeaterAddress", true, "Number of the repeater (1, 2 or 3)", "Address (hex string)"));
        cat7.addMessageSpec(addBasicMsgWithAttr("Set address of the recipient", "SetRecipientAddress", true, "Address (hex string)"));
        theCategories.add(cat7);

        return theCategories;
    }
}