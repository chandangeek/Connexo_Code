package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 14:00
 * Author: khe
 */
public class ConfigureConsumptionLimitationsSettings extends AbstractConfigMessage {

    public ConfigureConsumptionLimitationsSettings(ACE4000Outbound ace4000) {
        super(ace4000);
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        int numberOfSubIntervals = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.NUMBER_OF_SUBINTERVALS).getValue());
        int subIntervalDuration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SUB_INTERVAL_DURATION).getValue());
        String failMsg = "Consumption limitation configuration message failed, invalid arguments";
        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage(failMsg);
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage(failMsg);
            return;
        }
        int ovl = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.OVERRIDE_RATE).getValue());
        if (ovl < 0 || ovl > 4) {
            failMessage(failMsg);
            return;
        }
        int thresholdTolerance = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE).getValue());
        if (thresholdTolerance < 0 || thresholdTolerance > 100) {
            failMessage(failMsg);
            return;
        }
        int thresholdSelection = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.THRESHOLD_SELECTION).getValue());
        if (thresholdSelection < 0 || thresholdSelection > 1) {
            failMessage(failMsg);
            return;
        }
        List<String> switchingTimesDP0 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0).getValue().split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP0.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (switchingTimesDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> thresholdsDP0 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0).getValue().split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(failMsg);
                    return;
                }
                thresholdsDP0.add(threshold);
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (thresholdsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> unitsDP0 = new ArrayList<Integer>();
        try {
            String[] unitStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.THRESHOLDS_MOMENTS).getValue().split(",");
            for (String threshold : unitStrings) {
                unitsDP0.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (unitsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<String> actionsDP0 = new ArrayList<String>();
        try {
            String[] actionStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0).getValue().split(",");
            for (String action : actionStrings) {
                actionsDP0.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (actionsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }
        List<String> switchingTimesDP1 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1).getValue().split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP1.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (switchingTimesDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> thresholdsDP1 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1).getValue().split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(failMsg);
                    return;
                }
                thresholdsDP1.add(threshold);
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (thresholdsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> unitsDP1 = new ArrayList<Integer>();
        try {
            String[] unitStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.THRESHOLDS_MOMENTS).getValue().split(",");
            for (String threshold : unitStrings) {
                unitsDP1.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (unitsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<String> actionsDP1 = new ArrayList<String>();
        try {
            String[] actionStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1).getValue().split(",");
            for (String action : actionStrings) {
                actionsDP1.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (actionsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> weekProfile = new ArrayList<Integer>();
        try {
            String[] dayStrings = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.DAY_PROFILES).getValue().split(",");
            for (String day : dayStrings) {
                weekProfile.add(Integer.parseInt(day));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (weekProfile.size() != 7) {
            failMessage(failMsg);
            return;
        }

        Date date = null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            String dateString = MessageConverterTools.getDeviceMessageAttribute(getInput(), DeviceMessageConstants.ACTIVATION_DATE).getValue();
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            failMessage(failMsg);
            return;
        }

        trackingId = getAce4000().getObjectFactory().sendConsumptionLimitationConfigurationRequest(date, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration), ovl, thresholdTolerance, thresholdSelection, switchingTimesDP0, thresholdsDP0, unitsDP0, actionsDP0, switchingTimesDP1, thresholdsDP1, unitsDP1, actionsDP1, weekProfile);
    }

    private String pad(String text) {
        if (text.length() == 1) {
            text = "0" + text;
        }
        return text;
    }
}