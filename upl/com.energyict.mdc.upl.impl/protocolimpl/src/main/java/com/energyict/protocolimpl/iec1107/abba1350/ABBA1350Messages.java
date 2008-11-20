/**
 * ABBA1350Messages.java
 * 
 * Created on 19-nov-2008, 13:15:45 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.abba1350;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

/**
 * @author jme
 *
 */
public class ABBA1350Messages {
	
	private static final int DEBUG = 1; 
	
    private static String UPLOAD_SPC = "SPC_DATA";
    private static String UPLOAD_SPCU = "SPCU_DATA";

    private static String UPLOAD_SPC_DISPLAY = "Upload 'Switch Point Clock' settings (Class 4)";
    private static String UPLOAD_SPCU_DISPLAY = "Upload 'Switch Point Clock Update' settings (Class 32)";
    
    private static final int UPLOAD_SPC_LENGTH = 285 * 2;
    private static final int UPLOAD_SPCU_LENGTH = 285 * 2;
    
    private static final int UPLOAD_SPC_CLASSNR = 4;
    private static final int UPLOAD_SPCU_CLASSNR = 34;
    

	private ABBA1350 abba1350 = null;
	
	public ABBA1350Messages(ABBA1350 abba1350) {
		this.abba1350 = abba1350;
	}

//--------------------------------------------------------------------------------------------------------------------------
	
	public List getMessageCategories() {
		sendDebug("getMessageCategories()");

		List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("'Switch Point Clock' Messages");
        
        cat.addMessageSpec(addBasicMsg(UPLOAD_SPC_DISPLAY, UPLOAD_SPC, false));
        cat.addMessageSpec(addBasicMsg(UPLOAD_SPCU_DISPLAY, UPLOAD_SPCU, false));
        
        theCategories.add(cat);
        return theCategories;
	}

	public void applyMessages(List messageEntries) {
		sendDebug("applyMessages(List messageEntries)");
        if (DEBUG >= 2) {
    		Iterator it = messageEntries.iterator();
            while(it.hasNext()) {
                MessageEntry messageEntry = (MessageEntry)it.next();
                sendDebug(messageEntry.toString());
            }
        }
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		sendDebug("queryMessage(MessageEntry messageEntry)");

		try {
			if (isThisMessage(messageEntry, UPLOAD_SPC)) {
				sendDebug("************************* " + UPLOAD_SPC_DISPLAY + " *************************");
				writeClassSettings(messageEntry, UPLOAD_SPC, UPLOAD_SPC_DISPLAY, UPLOAD_SPC_CLASSNR, UPLOAD_SPC_LENGTH);
				return MessageResult.createSuccess(messageEntry);
			}
			else if (isThisMessage(messageEntry, UPLOAD_SPCU)) {
				sendDebug("************************* " + UPLOAD_SPCU_DISPLAY + " *************************");
				writeClassSettings(messageEntry, UPLOAD_SPCU, UPLOAD_SPCU_DISPLAY, UPLOAD_SPCU_CLASSNR, UPLOAD_SPCU_LENGTH);
				return MessageResult.createSuccess(messageEntry);
			}

		}
		catch(IOException e) {
			sendDebug(e.getMessage());
		}

		return MessageResult.createFailed(messageEntry);
	}

	public String writeValue(MessageValue value) {
		sendDebug("writeValue(MessageValue value)");
		return value.getValue();
	}
	
	public String writeMessage(Message msg) {
		sendDebug("writeMessage(Message msg)");
		return msg.write(this.abba1350);
	}
	
	public String writeTag(MessageTag tag) {
		sendDebug("writeTag(MessageTag tag)");
		
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append( tag.getName() );
        
        // b. Attributes
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute)it.next();
            if (att.getValue()==null || att.getValue().length()==0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");
        
        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag())
                buf.append( writeTag((MessageTag)elt) );
            else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if (value==null || value.length()==0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("\n\n</");
        buf.append( tag.getName() );
        buf.append(">");
        
        return buf.toString();    
	
	}

//--------------------------------------------------------------------------------------------------------------------------
	
	

    private static MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
	

	private void writeClassSettings(MessageEntry messageEntry, String messageName, String messageDisplayName, int messageClassNumber, int messageLength) throws IOException {
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final int MAX_PACKETSIZE = 48;
		
		String returnValue = "";
		String iec1107Command = "";

		int first = 0;
		int last = 0;
		int offset = 0;
		int length = 0;

		if (abba1350.getISecurityLevel() < 1) throw new IOException("Message " + messageDisplayName + " needs at least security level 1. Current level: " + abba1350.getISecurityLevel());
		
		String message = ABBA1350Utils.getXMLAttributeValue(messageName, messageEntry.getContent());
		message = ABBA1350Utils.cleanAttributeValue(message);
		sendDebug("Cleaned attribute value: " + message);
		if (message.length() != messageLength) throw new IOException("Wrong length !!! Length should be " + messageLength + " but was " + message.length());
		if (!ABBA1350Utils.containsOnlyTheseCharacters(message.toUpperCase(), "0123456789ABCDEF")) throw new IOException("Invalid characters in message. Only the following characters are allowed: '0123456789ABCDEFabcdef'");
		
		do {
			last = first + MAX_PACKETSIZE;
			if (last >= message.length()) last = message.length();
			String rawdata = message.substring(first, last);
			
			length = rawdata.length() / 2;
			offset = first / 2;
			
			iec1107Command = "C" + ProtocolUtils.buildStringHex(messageClassNumber, 2);
			iec1107Command += ProtocolUtils.buildStringHex(length, 4);
			iec1107Command += ProtocolUtils.buildStringHex(offset, 4);
			iec1107Command += "(" + rawdata + ")";
			
			sendDebug(	" classNumber: " + ProtocolUtils.buildStringHex(messageClassNumber, 2) + 
						" First: " + ProtocolUtils.buildStringHex(first, 4) + 
						" Last: " + ProtocolUtils.buildStringHex(last, 4) + 
						" Offset: " + ProtocolUtils.buildStringHex(offset, 4) + 
						" Length: " + ProtocolUtils.buildStringHex(length, 4) +
						" Sending iec1107Command: [ W1." + iec1107Command + " ]"
			);

			returnValue = abba1350.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(WRITE1, iec1107Command.getBytes());
			if (returnValue != null) throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
			first = last;

		} while (first < message.length());

	}
	
	private static boolean isThisMessage(MessageEntry messageEntry, String tag) {
		return (ABBA1350Utils.getXMLAttributeValue(tag, messageEntry.getContent()) != null);
	}

	private void sendDebug(String string) {
		if (DEBUG >= 1) this.abba1350.sendDebug(string);
	}

}
