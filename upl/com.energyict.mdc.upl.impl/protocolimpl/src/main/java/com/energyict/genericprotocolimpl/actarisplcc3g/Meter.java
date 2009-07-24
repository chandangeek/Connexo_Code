package com.energyict.genericprotocolimpl.actarisplcc3g;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*; 
import java.util.logging.Logger;

import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.protocol.messaging.*;

public class Meter implements GenericProtocol, Messaging { 
    
    private final static boolean ADVANCED = true;
    
    public List getMessageCategories() {
        
        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageSpec msgSpec = null;
        
        msgSpec = addBasicMsg("Connect", RtuMessageConstant.CONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        return theCategories;
        
    }
    
    public String writeMessage(Message msg) {
        return msg.write(this);
    }
    
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag())
                buf.append(writeTag((MessageTag) elt));
            else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");
        
        return buf.toString();
    }
    
    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    public void execute(
            CommunicationScheduler scheduler, Link link, Logger logger) 
            throws BusinessException, SQLException, IOException {
        
        String msg = 
            "Meter Protocol can not be executed directly," +
    		"only by going through the concentrator.";
        
        throw new BusinessException( msg );
        
    }

    public void addProperties(Properties properties) { }

    public String getVersion() {
        return "$Revision: 1.6 $";
    }

    public List getOptionalKeys() {
        return new ArrayList();
    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

	public long getTimeDifference() {
		// TODO Auto-generated method stub
		return 0;
	}
    
  

}
