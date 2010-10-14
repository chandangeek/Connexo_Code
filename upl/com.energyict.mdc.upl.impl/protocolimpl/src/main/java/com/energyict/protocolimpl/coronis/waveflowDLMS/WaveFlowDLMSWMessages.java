package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.iec1107.as220.AS220MessageType;

public class WaveFlowDLMSWMessages implements MessageProtocol {
	
	 AbstractDLMS abstractDLMS;
	
     WaveFlowDLMSWMessages(AbstractDLMS abstractDLMS) {
	   this.abstractDLMS = abstractDLMS;
	 }

     private final String stripOffTag(String content) {
 	   return content.substring(content.indexOf(">")+1,content.lastIndexOf("<"));
     }
    
	 public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<PairMeter")>=0) {
				abstractDLMS.getLogger().info("************************* PairMeter *************************");
				
		    	try {
		    		abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardAwakeningPeriod(1);
		    		abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(20);
		    		abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(110);
				
					if (abstractDLMS.pairWithEMeter()) {
						return MessageResult.createSuccess(messageEntry);
					}
					else {
						return MessageResult.createFailed(messageEntry);
					}
		    	}
				finally {
					abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(2);
					abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(1100);
					abstractDLMS.getEscapeCommandFactory().setAndVerifyWavecardAwakeningPeriod(10);
				}
			}
			else {
				return MessageResult.createFailed(messageEntry);
			}
		}
		catch(IOException e) {
			abstractDLMS.getLogger().severe("Error parsing message, "+e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
    }
	 
    public List getMessageCategories() {
       List theCategories = new ArrayList();
       
       MessageCategorySpec cat1 = new MessageCategorySpec("WaveflowDLMS messages");
       cat1.addMessageSpec(addBasicMsg("Pair with the e-meter", "PairMeter", false));
       theCategories.add(cat1);
       
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
