package com.energyict.smartmeterprotocolimpl.eict.AM110R.wakeup;

import com.energyict.cbo.Sms;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 22/05/13 - 14:47
 */
public class ProximusSmsSender {

    private static final String PARAM_ID = "id";
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_AUTH = "auth";
    private static final String PARAM_RECIPIENT = "recipient";
    private static final String PARAM_PRODUCT = "product";
    private static final String PARAM_MESSAGE = "message";
    private static final String PARAM_SERVICE_CODE = "servicecode";
    private static final String ENCODING = "UTF-8";

    private final Logger logger;
    private SmsWakeUpDlmsProtocolProperties properties;

    public ProximusSmsSender(SmsWakeUpDlmsProtocolProperties properties,Logger logger) {
        this.logger = logger;
        this.properties = properties;
    }

    public void sendSMS(Sms sms) throws ConnectionException {
        logger.log(Level.SEVERE, "Sending out SMS.");

        StringBuffer sb = new StringBuffer();
        try {
            // 1. Construct data
            Properties params = new Properties();
            params.setProperty(PARAM_ID, "" + System.currentTimeMillis());
            params.setProperty(PARAM_SOURCE, properties.getSmsSource());
            params.setProperty(PARAM_AUTH, properties.getSmsAuthentication());
            params.setProperty(PARAM_SERVICE_CODE, properties.getSmsServiceCode());
            params.setProperty(PARAM_RECIPIENT, sms.getTo());
            params.setProperty(PARAM_MESSAGE, new String(sms.getMessage()));
            params.setProperty(PARAM_PRODUCT, "");

            String data = getRequestParameters(params);

            // 2. Send data
            URL url = new URL(properties.getSmsConnectionUrl());
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // 3. Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            wr.close();
            rd.close();

            // 4. Parse the response
            parseResponseMessage(sb.toString());
            logger.log(Level.INFO, "Successful send out SMS.");
        } catch (Exception e) {
            String msg = "Failed to send out SMS - An error occurred while sending out the SMS message: " + e.getMessage();
            logger.log(Level.SEVERE, msg);
            throw new ConnectionException(msg);
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
     * @throws javax.xml.parsers.ParserConfigurationException
     *
     * @throws org.xml.sax.SAXException when the xml parsing fails
     * @throws java.io.IOException              when the communication fails
     * @throws com.energyict.cbo.BusinessException
     *
     */
    protected void parseResponseMessage(String xml) throws ConnectionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            Element rootElement = document.getDocumentElement();
            Node statusNode = rootElement.getFirstChild();
            if (statusNode.getNodeName().equals("error")) {
                Node errorCodeNode = statusNode.getAttributes().getNamedItem("code");
                String errorCode = errorCodeNode.getTextContent();
                String errorDescription = statusNode.getFirstChild().getTextContent();
                throwError(errorCode, errorDescription);
            } else if (statusNode.getNodeName().equals("ok")) {
                return;
            }
        } catch (ParserConfigurationException e) {
            String msg = "Failed to send out SMS - An error occurred while parsing the response message: " + e.getMessage();
            logger.log(Level.SEVERE, msg);
            throw new ConnectionException(msg);
        } catch (SAXException e) {
            String msg = "Failed to send out SMS - An error occurred while parsing the response message: " + e.getMessage();
            logger.log(Level.SEVERE, msg);
            throw new ConnectionException(msg);
        } catch (IOException e) {
            String msg = "Failed to send out SMS - An error occurred while parsing the response message: " + e.getMessage();
            logger.log(Level.SEVERE, msg);
            throw new ConnectionException(msg);
        } catch (DOMException e) {
            String msg = "Failed to send out SMS - An error occurred while parsing the response message: " + e.getMessage();
            logger.log(Level.SEVERE, msg);
            throw new ConnectionException(msg);
        }
    }

    private void throwError(String errorCode, String errorDescription) throws ConnectionException {
        String errorMessage;
        switch (Integer.parseInt(errorCode)) {
            case 450:
                errorMessage = "Error while sending SMS: Invalid request - " + errorDescription;
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 451:
                errorMessage = "Error while sending SMS: Authentication failure";
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 452:
                errorMessage = "Error while sending SMS: Missing parameter - " + errorDescription;
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 453:
                errorMessage = "Error while sending SMS: Invalid parameter-  " + errorDescription;
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 454:
                errorMessage = "Error while sending SMS: SMS MT message limit exceeded for provider - " + errorDescription;
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 455:
                errorMessage = "Error while sending SMS: PI internal queue full";
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 456:
                errorMessage = "Error while sending SMS: Storage partition exceeded the allocated capacity";
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 457:
                errorMessage = "Error while sending SMS: Service disabled";
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            case 550:
                errorMessage = "Error while sending SMS: Internal error - " + errorDescription;
                logger.log(Level.SEVERE, errorMessage);
                throw new ConnectionException(errorMessage);
            default:
                return;
        }
    }

    /**
     * Build a String with the data representation
     *
     * @param byteBuffer data to build string from
     * @return String with representation of the data
     */
    private String getHexStringFromBytes(byte[] byteBuffer) {
        int i;
        StringBuffer strBuff = new StringBuffer();
        for (i = 1; i <= byteBuffer.length; i++) {
            strBuff.append(outputHexString((int) byteBuffer[i - 1] & 0x000000FF));
        }
        return strBuff.toString();
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
}