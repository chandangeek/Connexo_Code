package com.energyict.protocolimpl.ametek;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class Jem extends AbstractProtocol implements MessageProtocol {

    protected final static int REGULAR = 0;
    protected final static int ALTERNATE = 1;

    protected JemProtocolConnection connection;
    protected InputStream inputStream;
    protected OutputStream outputStream;

    protected int profileInterval = 900;
    protected int channelCount = 0;
    protected Date time;
    protected ProfileData pd;
    protected Map registerValues = null;

    public Jem() {
    }

    /**
     * ****************************************************************************************
     * M e s s a g e P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    // message protocol
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while (it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry) it.next();
            System.out.println(messageEntry);
        }
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        System.out.println(messageEntry);
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("sampleCategoryName");
        MessageSpec msgSpec = addBasicMsg("sampleId", "SAMPLETAG", false);
        cat.addMessageSpec(msgSpec);
        theCategories.add(cat);
        return theCategories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
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
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }


    protected void doConnect() throws IOException {
        if (getInfoTypeNodeAddress() == null || getInfoTypeNodeAddressNumber() < 1) {
            throw new IOException("Invalid Node Address");
        }

        if (getInfoTypePassword() == null || (getInfoTypePassword() != null && getInfoTypePassword().length() < 1)) {
            throw new IOException("Invalid Password");
        }


        byte[] send = new byte[13];

        send[0] = (byte) getInfoTypeNodeAddressNumber();
        send[1] = 0x50;
        send[2] = 0x01;
        send[3] = JemProtocolConnection.getCmdStart()[0];
        send[4] = JemProtocolConnection.getCmdStart()[1];

        char pass[] = getInfoTypePassword().toCharArray();

        int i = 0;
        for (; i < pass.length; i++) {
            send[i + 5] = (byte) pass[i];
        }

        i += 5;
        send[i] = JemProtocolConnection.getCmdEnd()[0];
        i++;
        send[i] = JemProtocolConnection.getCmdEnd()[1];

        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        int inval = 0;

        inval = bais.read();
        if (inval != 0x06) {
            throw new IOException("Invalid Response from Send Password.");
        }
    }


    protected void doDisconnect() throws IOException {
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, null, includeEvents);
    }

    /**
     * ****************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getA() != 1) {
            throw new NoSuchRegisterException("Register " + obisCode + " not supported!");
        }

        if (registerValues == null) {
            retrieveRegisters();
        }

//		if(obisCode.getB()<1 || obisCode.getB()>channelCount)
//		throw new NoSuchRegisterException("Register "+obisCode+" not supported!");

        RegisterValue rv = (RegisterValue) registerValues.get(obisCode.toString());

        if (rv != null) {
            return new RegisterValue(obisCode, rv.getQuantity(), rv.getEventTime(), rv.getFromTime(), rv.getToTime(), rv.getReadTime(), rv.getRtuRegisterId(), rv.getText());
        }

        throw new NoSuchRegisterException("Register " + obisCode + " not supported!");
    }

    protected abstract void retrieveRegisters() throws IOException;

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }

    protected List doGetOptionalKeys() {
        return new ArrayList();
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        connection = new JemProtocolConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, getLogger());
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        return connection;
    }

    protected long convertHexToLong(InputStream byteStream, int length) throws IOException {
        return ProtocolUtils.getLong((ByteArrayInputStream) byteStream, length);
    }

    protected long convertHexToLongLE(InputStream byteStream, int length) throws IOException {
        return ProtocolUtils.getLongLE((ByteArrayInputStream) byteStream, length);
    }

    protected SimpleDateFormat getDateFormatter() {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddhhmmss");
        format.setTimeZone(getTimeZone());
        return format;
    }

    protected SimpleDateFormat getShortDateFormatter() {
        SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmm");
        format.setTimeZone(getTimeZone());
        return format;
    }

    protected void processList(ArrayList dataList, Calendar c, Date startDate, Date now) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            c.add(Calendar.SECOND, (getProfileInterval() * -1));
            ArrayList vals = (ArrayList) dataList.get(i);
            if (c.getTime().getTime() >= startDate.getTime() && c.getTime().before(now)) {
                IntervalData id = new IntervalData(c.getTime());
                id.addValues(vals);
                pd.addInterval(id);
            }
        }

    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public JemProtocolConnection getConnection() {
        return connection;
    }
}