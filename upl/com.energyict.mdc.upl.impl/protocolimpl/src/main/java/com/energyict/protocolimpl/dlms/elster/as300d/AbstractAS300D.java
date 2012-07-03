package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This abstract AS300D class contains all the unused or deprecated methods, to avoid
 * <p/>
 * Copyrights EnergyICT
 * Date: 23/02/12
 * Time: 14:40
 */
public abstract class AbstractAS300D extends PluggableMeterProtocol implements MessageProtocol {

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar lastReading = Calendar.getInstance();
        lastReading.add(Calendar.DAY_OF_MONTH, -1);
        return getProfileData(lastReading.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }


    public void initializeDevice() throws IOException {
        // No init required
    }

    public void release() throws IOException {
        // No release required
    }

    public void setCache(Object cacheObject) {
        // TODO: Implement this method
    }

    public Object getCache() {
        return null;  // TODO: Implement this method
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;  // TODO: Implement this method
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        // TODO: Implement this method
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public void applyMessages(List messageEntries) {
    }

    public String writeTag(MessageTag tag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(tag.getName());

        // b. Attributes
        for (Iterator it = tag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().length() == 0)) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = tag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.length() == 0)) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("\n\n</");
        buf.append(tag.getName());
        buf.append(">");

        return buf.toString();

    }

}
