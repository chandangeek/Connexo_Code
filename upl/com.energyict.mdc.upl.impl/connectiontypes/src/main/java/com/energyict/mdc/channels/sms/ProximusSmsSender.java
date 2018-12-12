package com.energyict.mdc.channels.sms;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 12/03/12
 * Time: 15:49
 */
public class ProximusSmsSender {

    private static final String PARAM_ID = "id";
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_AUTH = "auth";
    private static final String PARAM_RECIPIENT = "recipient";
    private static final String PARAM_PRODUCT = "product";
    private static final String PARAM_MESSAGE = "message";
    private static final String PARAM_SERVICE_CODE = "servicecode";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_PID = "pid";
    private static final String PARAM_DCS = "dcs";

    private static final String VAL_TYPE = "binary";
    private static final byte[] VAL_PID_BINARY = {(byte) 0xFF};
    private static final byte[] VAL_DCS = {(byte) 0xf6};
    private static final String ENCODING = "UTF-8";

    private String connectionURL;
    private String source;
    private String authentication;
    private String serviceCode;

    private Logger logger = Logger.getLogger(getClass().getName());

    public byte[] sendSms(String phoneNumber, byte[] messageContent) {
        StringBuilder builder = new StringBuilder();
        ResultType result;
        result = ResultType.SUCCESSFUL;
        try {
            // 1. Construct data
            Properties params = new Properties();
            params.setProperty(PARAM_ID, "" + System.currentTimeMillis());
            params.setProperty(PARAM_SOURCE, getSource());
            params.setProperty(PARAM_AUTH, getAuthentication());
            params.setProperty(PARAM_SERVICE_CODE, getServiceCode());
            params.setProperty(PARAM_RECIPIENT, phoneNumber);
            params.setProperty(PARAM_MESSAGE, getHexStringFromBytes(messageContent).toUpperCase());
            params.setProperty(PARAM_PRODUCT, "");
            params.setProperty(PARAM_TYPE, VAL_TYPE);
            params.setProperty(PARAM_PID, getHexStringFromBytes(VAL_PID_BINARY));
            params.setProperty(PARAM_DCS, getHexStringFromBytes(VAL_DCS));

            String data = getRequestParameters(params);

            // 2. Send data
            logger.log(Level.INFO, "Sending out the SMS message towards " + phoneNumber);
            URL url = new URL(getConnectionURL());
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // 3. Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                builder.append(line);
            }
            wr.close();
            rd.close();

            // 4. Parse the response
            result = parseResponseMessage(builder.toString());
            return result.getByteStream();
        } catch (Exception e) {
            result = ResultType.SEND_OUT_ERROR;
            result.setFailureInformation(e.getMessage());
            logger.log(Level.SEVERE, result.getDescription());
            return result.getByteStream();
        }
    }

    private String getRequestParameters(Properties params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Object[] keys = params.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            if (key instanceof String) {
                if (i == 0) {
                    sb.append(key).append("=").append(params.getProperty((String) key, ""));
                } else {
                    sb.append("&").append(URLEncoder.encode((String) key, ENCODING)).append("=").append(URLEncoder.encode(params.getProperty((String) key, ""), ENCODING));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parse the received XML and extract its status
     * E.g.: <?xml version="1.0" encoding="UTF-8"?><status><ok/></status>
     *
     * @param xml - the response XML content
     */
    protected ResultType parseResponseMessage(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        ResultType result = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            Element rootElement = document.getDocumentElement();
            Node statusNode = rootElement.getFirstChild();
            if ("error".equals(statusNode.getNodeName())) {
                Node errorCodeNode = statusNode.getAttributes().getNamedItem("code");
                String errorCode = errorCodeNode.getTextContent();
                String errorDescription = statusNode.getFirstChild().getTextContent();

                result = ResultType.findResultTypeForCode(errorCode);
                result.setFailureInformation(errorDescription);
                logger.log(Level.SEVERE, result.getDescription());
            } else if ("ok".equals(statusNode.getNodeName())) {
                result = ResultType.SUCCESSFUL;
            }
        } catch (ParserConfigurationException | SAXException | DOMException | IOException e) {
            result = ResultType.PARSING_ERROR;
            result.setFailureInformation(e.getMessage());
            logger.log(Level.SEVERE, result.getDescription());
        }
        return result;
    }

    /**
     * Build a String with the data representation
     *
     * @param byteBuffer data to build string from
     * @return String with representation of the data
     */
    private String getHexStringFromBytes(byte[] byteBuffer) {
        int i;
        StringBuilder builder = new StringBuilder();
        for (i = 1; i <= byteBuffer.length; i++) {
            builder.append(outputHexString((int) byteBuffer[i - 1] & 0x000000FF));
        }
        return builder.toString();
    }

    /**
     * Convert int to byte and build String.
     * E.g. val: 10, String output: "0A"
     *
     * @param bKar int value to convert
     * @return String result
     */
    private String outputHexString(int bKar) {
        String str = new String();
        str += String.valueOf((char) convertHexLSB(bKar));
        str += String.valueOf((char) convertHexMSB(bKar));
        return str;
    }

    /**
     * Return low nibble, converted to ascii
     *
     * @param bKar int value to extract low nibble from
     * @return int value of low nibble
     */
    private int convertHexLSB(int bKar) {
        if (((bKar / 16) >= 0) && ((bKar / 16) <= 9)) {
            return (((bKar / 16) + 48));
        } else if (((bKar / 16) >= 0xA) && ((bKar / 16) <= 0xF)) {
            return (((bKar / 16) + 55));
        } else {
            return 0;
        }
    }

    /**
     * Return high nibble, converted to ascii
     *
     * @param bKar int value to extract high nibble from
     * @return int value of high nibble
     */
    private int convertHexMSB(int bKar) {
        if (((bKar % 16) >= 0) && ((bKar % 16) <= 9)) {
            return (((bKar % 16) + 48));
        } else if (((bKar % 16) >= 0xA) && ((bKar % 16) <= 0xF)) {
            return (((bKar % 16) + 55));
        } else {
            return 0;
        }
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2013-06-26 15:21:25 +0200 (Wed, 26 Jun 2013) $";
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public enum ResultType {

        SUCCESSFUL("-1", "OK"),

        INVALID_REQUEST("450", "Invalid request"),
        AUTHENTICATION_FAILURE("451", "Authentication failure"),
        MISSING_PARAMETER("452", "Missing parameter"),
        INVALID_PARAMETER("453", "Invalid parameter"),
        SMS_MT_MESSAGE_LIMIT_EXCEEDED("454", "SMS MT message limit exceeded for provider"),
        INTERNAL_QUEUE_FULL("455", "PI internal queue full"),
        STORAGE_PARTITION_EXCEEDED_THE_ALLOCATED_CAPACITY("456", "Storage partition exceeded the allocated capacity"),
        SERVICE_DISABLED("475", "Service disabled"),
        INTERNAL_ERROR("550", "Internal error"),

        SEND_OUT_ERROR("-1","Encountered an exception while sending out the SMS"),
        PARSING_ERROR("-1", "Encountered an exception while parsing the response");

        private String code;
        private String textual;
        private String failureInformation;

        ResultType(String code, String textual) {
            this.code = code;
            this.textual = textual;
        }

        public void setFailureInformation(String failureInformation) {
            this.failureInformation = failureInformation;
        }

        public String getDescription() {
            if (hasFailureInformation()) {
                return textual + ": " + failureInformation;
            } else {
                return textual;
            }
        }

        public String getFailureInformation() {
            return failureInformation;
        }

        public byte[] getByteStream() {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byteStream.write(this.ordinal());       // Enum ordinal value
            if (hasFailureInformation()) {          // Length of description + description
                byte[] bytes;
                if (getFailureInformation().length() > 255) {
                    bytes = getFailureInformation().substring(0, 255).getBytes();
                } else {
                    bytes = getFailureInformation().getBytes();
                }
                byteStream.write(bytes.length);
                byteStream.write(bytes, 0, bytes.length);
            } else {
                byteStream.write(0);
            }
            return byteStream.toByteArray();
        }

        private boolean hasFailureInformation() {
            return failureInformation != null && !failureInformation.isEmpty();
        }

        public static ResultType findResultTypeForCode(String code) {
            for (ResultType type : ResultType.values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return ResultType.INTERNAL_ERROR;
        }

        public static ResultType getResultTypeFromByteStream(byte[] bytes) {
            ResultType type = ResultType.values()[bytes[0]];
            if (bytes[1] != 0) {
                byte[] subArray = ProtocolTools.getSubArray(bytes, 2);
                type.setFailureInformation(ProtocolTools.getAsciiFromBytes(subArray));
            }
            return type;
        }
    }
}