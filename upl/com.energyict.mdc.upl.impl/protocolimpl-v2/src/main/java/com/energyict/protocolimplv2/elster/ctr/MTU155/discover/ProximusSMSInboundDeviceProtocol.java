package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Sms;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.ServletBasedInboundDeviceProtocol} interface
 * that will support inbound SMS communication for the CTR protocols (MTU155 and EK155), using Proximus as telecom operator.
 *
 * @author sva
 * @since 24/06/13 - 14:31
 */
public class ProximusSMSInboundDeviceProtocol extends AbstractSMSServletBasedInboundDeviceProtocol {

    private static final int HEX = 16;
    private static final int BITS = 8;

    //Parameter names
    private static final String TEXT = "text";
    private static final String BINARY = "binary";
    private static final String DCS = "dcs";
    private static final String TYPE = "type";
    private static final String MESSAGE = "message";
    private static final String SENDER = "sender";
    private static final String AUTH = "auth";
    private static final String SOURCE = "source";
    private static final String PID = "pid";
    private static final String ID = "id";
    private static final String RECIPIENT = "recipient";

    //Response strings
    private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<status>";
    private static final String XML_SUFFIX = "\n</status>";
    private static final String ERROR_PREFIX = "\n    <" + "error code=\"";
    private static final String ERROR_SUFFIX = "</error>";


    private ResultType resultType = ResultType.OK;

    private DeviceIdentifier deviceIdentifier;
    private List<CollectedData> collectedDataList = new ArrayList<>();

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        context.setCryptographer(new CTRCryptographer());
        super.initializeDiscoveryContext(context);
    }

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            Sms sms = readParameters(this.request);

            TypedProperties allRelevantProperties = TypedProperties.empty();
            TypedProperties deviceProtocolProperties = getContext().getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
            TypedProperties deviceConnectionTypeProperties = getContext().getInboundDAO().getDeviceConnectionTypeProperties(getDeviceIdentifier(), getContext().getComPort());
            if (deviceProtocolProperties == null || deviceConnectionTypeProperties == null) {
                throw new NotFoundException("Device [" + getDeviceIdentifier() + "] not found.");
            }

            allRelevantProperties.setAllProperties(deviceProtocolProperties);
            allRelevantProperties.setAllProperties(deviceConnectionTypeProperties);
            allRelevantProperties.setProperty(MeterProtocol.SERIALNUMBER, getDeviceSerialNumber());

            List<SecurityProperty> protocolSecurityProperties = getContext().getInboundDAO().getDeviceProtocolSecurityProperties(this.deviceIdentifier, getContext().getComPort());
            MTU155Properties mtu155Properties = new MTU155Properties(new Mtu155SecuritySupport().convertToTypedProperties(protocolSecurityProperties));
            SMSFrame smsFrame = ((CTRCryptographer) getContext().getCryptographer()).decryptSMS(mtu155Properties, sms.getMessage());

            SmsHandler smsHandler = new SmsHandler(getDeviceIdentifier(), allRelevantProperties);
            smsHandler.parseSMSFrame(smsFrame);
            addCollectedData(smsHandler.getCollectedDataList());

            return DiscoverResultType.DATA;
        } catch (CTRException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(e);
        }
    }

    /**
     * Reads the parameters from the HTTP post or get.
     * Creates an SMS object with the given data.
     *
     * @param request: containing the parameters
     * @return the sms object
     */
    private Sms readParameters(HttpServletRequest request) {
        setDeviceIdentifier(new CTRPhoneNumberDeviceIdentifier(checkParameter(request.getParameter(SENDER), SENDER)));
        String auth = checkParameter(request.getParameter(AUTH), AUTH);
        String source = checkParameter(request.getParameter(SOURCE), SOURCE);

        if (!auth.equals(authenticationPropertyValue()) || !source.equals(sourcePropertyValue())) {
            setResultType(ResultType.AUTHENTICATION_FAILURE);
            IOException e = new IOException("Authentication failure: the API_source (" + source + ") and API_authentication (" + auth + ") of the received SMS do not match the credentials stored in EIMaster.");
            throw MdcManager.getComServerExceptionFactory().createCipheringException(e);
        }

        String dcsString = isValidHexString(checkParameter(request.getParameter(DCS), DCS), DCS);
        String pidString = isValidHexString(checkParameter(request.getParameter(PID), PID), PID);
        if (resultTypeIs(ResultType.OK)) {
            byte[] dcs = getBytesFromHexString(dcsString);
            byte[] pid = getBytesFromHexString(pidString);
        }
        String type = checkParameter(request.getParameter(TYPE), TYPE);
        String from = checkParameter(request.getParameter(SENDER), SENDER);
        String id = checkParameter(request.getParameter(ID), ID);
        String to = checkParameter(request.getParameter(RECIPIENT), RECIPIENT);
        byte[] message = getMessageContent(checkParameter(request.getParameter(MESSAGE), MESSAGE), type, MESSAGE);
        Date date = new Date();  //current date

        Sms sms = new Sms(from, to, date, source, id, BITS, message);
        return sms;
    }

    /**
     * Decode a given url and extract the value of a parameter out of it
     *
     * @param text:      the url
     * @param parameter: the name of the parameter in the url
     * @return the decoded text
     */
    private String checkParameter(String text, String parameter) {
        if (text == null && TYPE.equals(parameter)) {
            return TEXT;     //default type
        }

        if (text == null && isRequiredParameter(parameter)) {
            setResultType(ResultType.MISSING_PARAMETER);
            resultType.addAdditionInformation(parameter);
            throw MdcManager.getComServerExceptionFactory().missingProperty(parameter);
        }
        return text != null ? text : "";
    }

    private boolean isRequiredParameter(String parameter) {
        return (ID.equals(parameter)
                || SOURCE.equals(parameter)
                || SENDER.equals(parameter)
                || RECIPIENT.equals(parameter));
    }

    private boolean resultTypeIs(ResultType resultType) {
        return getResultType().getCode() == resultType.getCode();
    }

    /**
     * Checks if the string contains invalid characters (non hexadecimal)
     * or if the string's length is odd
     *
     * @param request:   a given string
     * @param parameter: what parameter is the string? (for error description purposes)
     * @return the valid string
     */
    private String isValidHexString(String request, String parameter) {
        if ("".equals(request)) {
            return request;
        }
        if ((request.length()) % 2 != 0) {
            setResultType(ResultType.INVALID_PARAMETER);
            if (resultTypeIs(ResultType.INVALID_PARAMETER)) {
                resultType.addAdditionInformation(parameter);
            }
            return "";
        }
        if (!request.matches("\\p{XDigit}+")) {
            setResultType(ResultType.INVALID_PARAMETER);
            if (resultTypeIs(ResultType.INVALID_PARAMETER)) {
                resultType.addAdditionInformation(parameter);
            }
            return "";
        }
        return request;
    }

    private byte[] getBytesFromHexString(final String hexString) {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += 2) {
            bb.write(Integer.parseInt(hexString.substring(i, i + 2), HEX));
        }
        return bb.toByteArray();
    }

    /**
     * convert a string message to a fitting byte array
     *
     * @param request:   the text string
     * @param type:      the type (text or binary)
     * @param parameter: the name of the parameter
     * @return a byte array representing the text string
     */
    private byte[] getMessageContent(String request, String type, String parameter) {
        if (request == null) {
            return new byte[0];
        }
        if (TEXT.equals(type)) {
            return request.getBytes();
        } else if (BINARY.equals(type)) {
            return getBytesFromHexString(isValidHexString(request, parameter));
        } else {
            setResultType(ResultType.INVALID_PARAMETER);
            if (resultTypeIs(ResultType.INVALID_PARAMETER)) {
                getResultType().addAdditionInformation(TYPE);
            }
            return new byte[0];
        }
    }

    private String getDeviceSerialNumber() {
        OfflineDevice device = getContext().getInboundDAO().findOfflineDevice(getDeviceIdentifier());
        if (device != null) {
            return device.getSerialNumber();
        } else {
            return "";
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        try {
            response.setContentType("text/xml; charset=UTF-8");
            PrintWriter out = response.getWriter();
            response.setStatus(getResultType().getCode());
            out.println(getReply());
            out.close();
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createConnectionCommunicationException(e);
        }
    }

    /**
     * Create an HTTP 1.1 Response
     *
     * @return the http 1.1 response
     */
    private String getReply() {
        String reply;
        if (resultTypeIs(ResultType.OK) && (additionalInformationIs("")) || additionalInformationIs(ResultType.OK.getDescription())) {
            reply = XML_PREFIX + "\n    <" + ResultType.OK.getDescription() + "\">" + XML_SUFFIX;
        } else {
            reply = XML_PREFIX + ERROR_PREFIX + getResultType().getCode() + "\">" + getResultType().getMessage() + ERROR_SUFFIX + XML_SUFFIX;
        }
        return reply;
    }

    private boolean additionalInformationIs(String s) {
        return s.equals(getResultType().getAdditionInformation());
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    private void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return collectedDataList;
    }

    private void addCollectedData(List<CollectedData> collectedDatas) {
        this.collectedDataList.addAll(collectedDatas);
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        if (resultTypeIs(ResultType.OK)) {
            this.resultType.setAdditionInformation(""); //Clear the additional information section
        }
        this.resultType = resultType;
    }

    public enum ResultType {

        OK(200, "ok"),
        INVALID_REQUEST(450, "Invalid request"),
        AUTHENTICATION_FAILURE(451, "Authentication failure"),
        MISSING_PARAMETER(452, "Missing parameter"),
        INVALID_PARAMETER(453, "Invalid parameter"),
        LIMIT_EXCEEDED(454, "SMS MT message limit exceeded"),
        QUEUE_FULL(455, "PI internal queue full"),
        CAPACITY_EXCEEDED(456, "Storage partition exceeded the allocated capacity"),
        SERVICE_DISABLED(457, "Service disabled"),
        INTERNAL_ERROR(550, "Internal error");

        private int code;
        private String description;
        private String additionInformation;

        ResultType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getAdditionInformation() {
            return additionInformation;
        }

        public void setAdditionInformation(String additionInformation) {
            this.additionInformation = additionInformation;
        }

        public void addAdditionInformation(String additionInformation) {
            if (this.additionInformation == null || this.additionInformation.isEmpty()) {
                this.additionInformation = additionInformation;
            } else {
                this.additionInformation += (", " + additionInformation);
            }
        }

        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append(getDescription());
            if (getAdditionInformation() != null && !getAdditionInformation().isEmpty()) {
                builder.append(": ");
                builder.append(additionInformation);
            }
            return builder.toString();
        }

    }
}