package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.iec1107.as220.AS220MessageType;

public class WaveFlowMessages implements MessageProtocol {
	
     WaveFlow waveFlow;
	
     WaveFlowMessages(WaveFlow waveFlow) {
	   this.waveFlow = waveFlow;
	 }

     private final String stripOffTag(String content) {
 	   return content.substring(content.indexOf(">")+1,content.lastIndexOf("<"));
     }
    
	 public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<RestartDataLogging")>=0) {
				waveFlow.getLogger().info("************************* RestartDataLogging *************************");
				waveFlow.restartDataLogging();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<ForceTimeSync")>=0) {
				waveFlow.getLogger().info("************************* ForceTimeSync *************************");
				waveFlow.forceSetTime();
				return MessageResult.createSuccess(messageEntry);
			}
			else if (messageEntry.getContent().indexOf("<SetProfileInterval")>=0) {
				waveFlow.getLogger().info("************************* Set sampling interval *************************");
				int profileInterval = Integer.parseInt(stripOffTag(messageEntry.getContent()));
				if (profileInterval<60) {
					waveFlow.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
					return MessageResult.createFailed(messageEntry);
				}
				else {
					waveFlow.getParameterFactory().writeSamplingPeriod(profileInterval);
					return MessageResult.createSuccess(messageEntry);
				}
			}
			else {
				return MessageResult.createFailed(messageEntry);
			}
		}
		catch(IOException e) {
			waveFlow.getLogger().severe("Error parsing message, "+e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
    }
	 
    public List getMessageCategories() {
       List theCategories = new ArrayList();
       
       MessageCategorySpec cat1 = new MessageCategorySpec("Waveflow messages");
       cat1.addMessageSpec(addBasicMsg("Restart datalogging", "RestartDataLogging", false));
       theCategories.add(cat1);
       
       MessageCategorySpec cat2 = new MessageCategorySpec("Waveflow advanced messages");
       cat2.addMessageSpec(addBasicMsgWithValue("Set sampling period in seconds", "SetProfileInterval", true));
       cat2.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
       cat2.addMessageSpec(addBasicMsg("Detect meter", "DetectMeter", true));
       theCategories.add(cat2);
       
       return theCategories;
    }
   
    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    protected MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced) {
       MessageSpec msgSpec = new MessageSpec(keyId, advanced);
       MessageTagSpec tagSpec = new MessageTagSpec(tagName);
       tagSpec.add(new MessageValueSpec());
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
}
