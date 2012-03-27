package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.util.*;

public class WaveFlowDLMSWMessages implements MessageProtocol {

    private static final ObisCode DISCONNECT_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    AbstractDLMS abstractDLMS;
	
     WaveFlowDLMSWMessages(AbstractDLMS abstractDLMS) {
	   this.abstractDLMS = abstractDLMS;
	 }

	 public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<PairMeter")>=0) {
				abstractDLMS.getLogger().info("************************* PairMeter *************************");

                try {
                    int baudrate = WaveflowProtocolUtils.parseInt(getTagContents("PairMeter", messageEntry.getContent()));
                    return abstractDLMS.pairWithEMeter(baudrate) ? MessageResult.createSuccess(messageEntry) : MessageResult.createFailed(messageEntry);
                } catch (IOException e) {
                    abstractDLMS.getLogger().severe("Unable to pair meter: " + e.getMessage());
                    return MessageResult.createFailed(messageEntry);
                }

            }
			else if (messageEntry.getContent().indexOf("<ForceTimeSync")>=0) {
				abstractDLMS.getLogger().info("************************* ForceTimeSync (e-meter time)*************************");
				abstractDLMS.forceSetTime();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<RemoteDisconnect")>=0) {
				abstractDLMS.getLogger().info("************************* RemoteDisconnect *************************");
                abstractDLMS.getTransparantObjectAccessFactory().executeObjectAction(DISCONNECT_OBISCODE, 1);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<RemoteConnect")>=0) {
				abstractDLMS.getLogger().info("************************* RemoteConnect *************************");
                abstractDLMS.getTransparantObjectAccessFactory().executeObjectAction(DISCONNECT_OBISCODE, 2);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<SyncWaveFlowRTC")>=0) {
				abstractDLMS.getLogger().info("************************* SyncWaveFlowRTC (waveflow100mW time)*************************");
				abstractDLMS.setWaveFlowTime();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<SetAlarmConfig")>=0) {
				abstractDLMS.getLogger().info("************************* SetAlarmConfig *************************");
				int alarmConfigValue = WaveflowProtocolUtils.parseInt(getTagContents("SetAlarmConfig", messageEntry.getContent()));
				abstractDLMS.getRadioCommandFactory().setAlarmRoute(alarmConfigValue);
				if (abstractDLMS.getParameterFactory().readAlarmConfiguration() == alarmConfigValue) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					abstractDLMS.getLogger().severe("Message set alarm configuration readback failed!");
					return MessageResult.createFailed(messageEntry);
				}
			}
			else if (messageEntry.getContent().indexOf("<SetOperatingMode")>=0) {
				abstractDLMS.getLogger().info("************************* SetOperatingMode *************************");
				int operatingModeValue = WaveflowProtocolUtils.parseInt(getTagContents("SetOperatingMode", messageEntry.getContent()));
				abstractDLMS.getParameterFactory().writeOperatingMode(operatingModeValue);
				if (abstractDLMS.getParameterFactory().readOperatingMode() == operatingModeValue) {
					return MessageResult.createSuccess(messageEntry);
				}
				else {
					abstractDLMS.getLogger().severe("Message set operating mode readback failed!");
					return MessageResult.createFailed(messageEntry);
				}
			}
			else if (messageEntry.getContent().indexOf("<SetApplicationStatus")>=0) {
				abstractDLMS.getLogger().info("************************* SetApplicationStatus *************************");
				int applicationStatusValue = WaveflowProtocolUtils.parseInt(getTagContents("SetApplicationStatus", messageEntry.getContent()));
				abstractDLMS.getParameterFactory().writeApplicationStatus(applicationStatusValue);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<InitializeKey")>=0) {
				abstractDLMS.getLogger().info("************************* InitializeKey *************************");
				abstractDLMS.initializeEncryption(getTagContents("InitializeKey", messageEntry.getContent()));
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<RenewKey")>=0) {
				abstractDLMS.getLogger().info("************************* RenewKey *************************");
				String content = getTagContents("RenewKey", messageEntry.getContent());
				String[] keys = content.split(",");
				if (keys.length != 2) {
					abstractDLMS.getLogger().severe("Invalid content for renew key message ["+content+"]");
					return MessageResult.createFailed(messageEntry);
				}
				abstractDLMS.renewEncryptionKey(keys[0], keys[1]);
				return MessageResult.createSuccess(messageEntry);
			}
			else {
				return MessageResult.createFailed(messageEntry);
			}
		}
		catch(Exception e) {
			abstractDLMS.getLogger().severe("Error parsing message, "+e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();

        MessageCategorySpec cat1 = new MessageCategorySpec("WaveflowDLMS messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set the applicationstatus (0 to reset)", "SetApplicationStatus", false, "0"));
        cat1.addMessageSpec(addBasicMsg("Force sync the e-meter time", "ForceTimeSync", false));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("WaveflowDLMS advanced messages");
        cat2.addMessageSpec(addBasicMsg("Remote disconnect", "RemoteDisconnect", true));
        cat2.addMessageSpec(addBasicMsg("Remote connect", "RemoteConnect", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Set alarm configuration (0..7)", "SetAlarmConfig", true, "7"));
        cat2.addMessageSpec(addBasicMsgWithValue("Set the operating mode (0..7)", "SetOperatingMode", true, "7"));
        cat2.addMessageSpec(addBasicMsgWithValue("Detect e-meter (pair) (9600 or 19200 (default) baud)", "PairMeter", true, "19200"));
        cat2.addMessageSpec(addBasicMsg("Force sync the waveflow time", "SyncWaveFlowRTC", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Initialize key (new 128 bit key) ", "InitializeKey", true, "new 128 bit key in hex notation"));
        cat2.addMessageSpec(addBasicMsgWithValue("Renew key (old 128 bit key, new 128 bit key)", "RenewKey", true, "old 128 bit key in hex notation,new 128 bit key in hex notation"));
        theCategories.add(cat2);

        return theCategories;
    }

    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    private MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced, String defaultValue) {
       MessageSpec msgSpec = new MessageSpec(keyId, advanced);
       MessageTagSpec tagSpec = new MessageTagSpec(tagName);
       MessageValueSpec mspec = new MessageValueSpec();
       mspec.setValue(defaultValue);
       tagSpec.add(mspec);
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
               if (value==null || value.length()==0) {
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
	private final String getTagContents(final String tagName, final String messageContents) {
    	final int startIndex = messageContents.indexOf(new StringBuilder("<").append(tagName).append(">").toString()) + tagName.length() + 2;
    	final int endIndex = messageContents.indexOf(new StringBuilder("</").append(tagName).append(">").toString());
    	
    	if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
    		return messageContents.substring(startIndex, endIndex).trim();
    	} else {
    		abstractDLMS.getLogger().warning("Cannot get contents of tag [" + tagName + "] out of message [" + messageContents + "], startIndex is [" + startIndex + "], endIndex is [" + endIndex + "]");
    	}
    	
    	return null;
	}	
}
