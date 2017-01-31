/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveFlow100mWMessages implements MessageProtocol {

     WaveFlow100mW waveFlow100mW;

     WaveFlow100mWMessages(WaveFlow100mW waveFlow100mW) {
	   this.waveFlow100mW = waveFlow100mW;
	 }

	 public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<RestartDataLogging")>=0) {
				waveFlow100mW.getLogger().info("************************* RestartDataLogging *************************");
				waveFlow100mW.restartDataLogging();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<ForceTimeSync")>=0) {
				waveFlow100mW.getLogger().info("************************* ForceTimeSync (waveflow100mW time) *************************");
				waveFlow100mW.forceSetTime();
				return MessageResult.createSuccess(messageEntry);
			}
			if (messageEntry.getContent().indexOf("<SyncWaveFlowRTC")>=0) {
				waveFlow100mW.getLogger().info("************************* SyncWaveFlowRTC (waveflow100mW time) *************************");
				waveFlow100mW.setWaveFlowTime();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<DetectMeter")>=0) {
				waveFlow100mW.getLogger().info("************************* DetectMeter (pair) *************************");
				try {
//					waveFlow100mW.getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(20); // See the waveflow manuazl. We need to set the timeout to at least 20 seconds if we want to do a meterdetect
					waveFlow100mW.getRadioCommandFactory().startMeterDetection();
				}
				finally {
//					waveFlow100mW.getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(2); // be sure to set back to the default!
				}
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<SetProfileInterval")>=0) {
				waveFlow100mW.getLogger().info("************************* Set sampling interval *************************");
				int profileInterval = WaveflowProtocolUtils.parseInt(getTagContents("SetProfileInterval", messageEntry.getContent()));
				if (profileInterval<60) {
					waveFlow100mW.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
					return MessageResult.createFailed(messageEntry);
				}
				else {
					waveFlow100mW.getParameterFactory().writeSamplingPeriod(profileInterval);
					if (waveFlow100mW.getParameterFactory().readSamplingPeriod() == profileInterval) {
						return MessageResult.createSuccess(messageEntry);
					}
					else {
						waveFlow100mW.getLogger().severe("Message set sampling period readback failed!");
						return MessageResult.createFailed(messageEntry);
					}
				}
			}
			else if (messageEntry.getContent().indexOf("<SetAlarmConfig")>=0) {
				waveFlow100mW.getLogger().info("************************* SetAlarmConfig *************************");
				int alarmConfigValue = WaveflowProtocolUtils.parseInt(getTagContents("SetAlarmConfig", messageEntry.getContent()));
				waveFlow100mW.getRadioCommandFactory().setAlarmRoute(alarmConfigValue);
				if (waveFlow100mW.getParameterFactory().readAlarmConfiguration() == alarmConfigValue) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					waveFlow100mW.getLogger().severe("Message set alarm config readback failed!");
					return MessageResult.createFailed(messageEntry);
				}

			}
			else if (messageEntry.getContent().indexOf("<SetOperatingMode")>=0) {
				waveFlow100mW.getLogger().info("************************* SetOperatingMode *************************");
				int operatingModeValue = WaveflowProtocolUtils.parseInt(getTagContents("SetOperatingMode", messageEntry.getContent()));
				waveFlow100mW.getParameterFactory().writeOperatingMode(operatingModeValue);
				if (waveFlow100mW.getParameterFactory().readOperatingMode() == operatingModeValue) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					waveFlow100mW.getLogger().severe("Message set operating mode readback failed!");
					return MessageResult.createFailed(messageEntry);
				}
			}
			else if (messageEntry.getContent().indexOf("<SetDayOfWeek")>=0) {
				waveFlow100mW.getLogger().info("************************* SetDayOfWeek *************************");
				int dayId = WaveflowProtocolUtils.parseInt(getTagContents("SetDayOfWeek", messageEntry.getContent()));
				waveFlow100mW.getParameterFactory().writeDayOfWeek(dayId);
				if (waveFlow100mW.getParameterFactory().readDayOfWeek() == dayId) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					waveFlow100mW.getLogger().severe("Message set day of week readback failed!");
					return MessageResult.createFailed(messageEntry);
				}
			}
			else if (messageEntry.getContent().indexOf("<SetHourOfMeasurement")>=0) {
				waveFlow100mW.getLogger().info("************************* SetHourOfMeasurement *************************");
				int hourId = WaveflowProtocolUtils.parseInt(getTagContents("SetHourOfMeasurement", messageEntry.getContent()));
				waveFlow100mW.getParameterFactory().writeHourOfMeasurement(hourId);
				if (waveFlow100mW.getParameterFactory().readHourOfMeasurement() == hourId) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					waveFlow100mW.getLogger().severe("Message set hour of measurement readback failed!");
					return MessageResult.createFailed(messageEntry);
				}
			}
			else if (messageEntry.getContent().indexOf("<SetApplicationStatus")>=0) {
				waveFlow100mW.getLogger().info("************************* SetApplicationStatus *************************");
				int applicationstatusValue = WaveflowProtocolUtils.parseInt(getTagContents("SetApplicationStatus", messageEntry.getContent()));
				waveFlow100mW.getParameterFactory().writeApplicationStatus(applicationstatusValue);
				return MessageResult.createSuccess(messageEntry);
			}
			else {
				return MessageResult.createFailed(messageEntry);
			}
		}
		catch(Exception e) {
			waveFlow100mW.getLogger().severe("Error parsing message, "+e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
    }

    public List getMessageCategories() {
       List theCategories = new ArrayList();

       MessageCategorySpec cat1 = new MessageCategorySpec("Waveflow100mw messages");
       cat1.addMessageSpec(addBasicMsg("Restart datalogging", "RestartDataLogging", false));
       cat1.addMessageSpec(addBasicMsgWithValue("Reset applicationstatus (default [0])", "SetApplicationStatus", true));
       cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode (default [0x08] 1/week on A)", "SetOperatingMode", false));
       cat1.addMessageSpec(addBasicMsgWithValue("Set alarm configuration", "SetAlarmConfig", false));
       theCategories.add(cat1);

       MessageCategorySpec cat2 = new MessageCategorySpec("Waveflow100mw advanced messages");
       cat2.addMessageSpec(addBasicMsgWithValue("Set day of week (default monday [1])", "SetDayOfWeek", true));
       cat2.addMessageSpec(addBasicMsgWithValue("Set hour of measurement (default 00:00 [0])", "SetHourOfMeasurement", true));
       cat2.addMessageSpec(addBasicMsgWithValue("Set sampling period in seconds (default 1 hour [3600])", "SetProfileInterval", true));
       cat2.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
       cat2.addMessageSpec(addBasicMsg("Detect meter (pair)", "DetectMeter", true));
       theCategories.add(cat2);

       return theCategories;
    }

    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced) {
       MessageValueSpec messageValueSpec = new MessageValueSpec();
       MessageSpec msgSpec = new MessageSpec(keyId, advanced);
       MessageTagSpec tagSpec = new MessageTagSpec(tagName);
       tagSpec.add(messageValueSpec);
       msgSpec.add(tagSpec);
       return msgSpec;
    }

    public String writeMessage(Message msg) {
       return msg.write(this);
    }
    public String writeTag(MessageTag msgTag) {
       StringBuffer buf = new StringBuffer();

       // a. Opening tag
       buf.append("<");
       buf.append( msgTag.getName() );

       // b. Attributes
       for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
           MessageAttribute att = (MessageAttribute)it.next();
           if (att.getValue()==null || att.getValue().length()==0) {
			continue;
		}
           buf.append(" ").append(att.getSpec().getName());
           buf.append("=").append('"').append(att.getValue()).append('"');
       }
       buf.append(">");

       // c. sub elements
       for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
           MessageElement elt = (MessageElement)it.next();
           if (elt.isTag()) {
			buf.append( writeTag((MessageTag)elt) );
		} else if (elt.isValue()) {
               String value = writeValue((MessageValue)elt);
               if (value==null || value.isEmpty()) {
				return "";
			}
               buf.append(value);
           }
       }

       // d. Closing tag
       buf.append("</");
       buf.append( msgTag.getName() );
       buf.append(">");

       return buf.toString();
    }

    public String writeValue(MessageValue value) {
       return value.getValue();
    }

	public void applyMessages(List messageEntries) throws IOException {
	}

	/**
	 * Gets the contents of the given tag.
	 *
	 * @param 	tagName						The name of the tag.
	 * @param 	messageContents				The contents of the message to extract the data from.
	 *
	 * @return	The contents, <code>null</code> if for some reason the contents cannot be extracted. A warning will be issued in that case.
	 */
	private String getTagContents(final String tagName, final String messageContents) {
    	final int startIndex = messageContents.indexOf(new StringBuilder("<").append(tagName).append(">").toString()) + tagName.length() + 2;
    	final int endIndex = messageContents.indexOf(new StringBuilder("</").append(tagName).append(">").toString());

    	if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
    		return messageContents.substring(startIndex, endIndex).trim();
    	} else {
    		waveFlow100mW.getLogger().warning("Cannot get contents of tag [" + tagName + "] out of message [" + messageContents + "], startIndex is [" + startIndex + "], endIndex is [" + endIndex + "]");
    	}

    	return null;
	}
}
