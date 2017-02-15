package com.energyict.protocolimpl.ametek;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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

public abstract class Jem extends AbstractProtocol implements MessageProtocol {

    static final int REGULAR = 0;
    static final int ALTERNATE = 1;

    protected JemProtocolConnection connection;
    protected InputStream inputStream;
    protected OutputStream outputStream;

    protected int profileInterval = 900;
    protected int channelCount = 0;
    protected Date time;
    protected ProfileData pd;
    protected Map registerValues = null;

    public Jem(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        for (Object messageEntry1 : messageEntries) {
            MessageEntry messageEntry = (MessageEntry) messageEntry1;
            System.out.println(messageEntry);
        }
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        System.out.println(messageEntry);
        return MessageResult.createSuccess(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
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

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    @Override
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
        int inval = bais.read();
        if (inval != 0x06) {
            throw new IOException("Invalid Response from Send Password.");
        }
    }

    @Override
    protected void doDisconnect() {
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, null, includeEvents);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getA() != 1) {
            throw new NoSuchRegisterException("Register " + obisCode + " not supported!");
        }

        if (registerValues == null) {
            retrieveRegisters();
        }

        RegisterValue rv = (RegisterValue) registerValues.get(obisCode.toString());

        if (rv != null) {
            return new RegisterValue(obisCode, rv.getQuantity(), rv.getEventTime(), rv.getFromTime(), rv.getToTime(), rv.getReadTime(), rv.getRtuRegisterId(), rv.getText());
        }

        throw new NoSuchRegisterException("Register " + obisCode + " not supported!");
    }

    protected abstract void retrieveRegisters() throws IOException;

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        connection = new JemProtocolConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, getLogger());
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        return connection;
    }

    long convertHexToLong(InputStream byteStream, int length) throws IOException {
        return ProtocolUtils.getLong((ByteArrayInputStream) byteStream, length);
    }

    long convertHexToLongLE(InputStream byteStream, int length) throws IOException {
        return ProtocolUtils.getLongLE((ByteArrayInputStream) byteStream, length);
    }

    protected SimpleDateFormat getDateFormatter() {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddhhmmss");
        format.setTimeZone(getTimeZone());
        return format;
    }

    SimpleDateFormat getShortDateFormatter() {
        SimpleDateFormat format = new SimpleDateFormat("MMddyyhhmm");
        format.setTimeZone(getTimeZone());
        return format;
    }

    void processList(List dataList, Calendar c, Date startDate, Date now) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            c.add(Calendar.SECOND, (getProfileInterval() * -1));
            List vals = (ArrayList) dataList.get(i);
            if (c.getTime().getTime() >= startDate.getTime() && c.getTime().before(now)) {
                IntervalData id = new IntervalData(c.getTime());
                id.addValues(vals);
                pd.addInterval(id);
            }
        }
    }

    @Override
    public int getProfileInterval() {
        return profileInterval;
    }

    public JemProtocolConnection getConnection() {
        return connection;
    }

}