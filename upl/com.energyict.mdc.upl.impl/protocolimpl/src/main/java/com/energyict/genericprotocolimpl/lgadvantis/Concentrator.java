package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.*;
import com.energyict.dialer.core.Link;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.*;
import com.energyict.protocol.messaging.*;
import com.energyict.xml.xmlhelper.DomHelper;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Concentrator implements GenericProtocol, Messaging {

    /* property timeout (milliseconds), defaults to 60 s */
    private int msTimeout = 60000;

    private String url;

    /* property port */
    private int port = 80;

    private CommunicationScheduler scheduler;
    private Logger logger;

    private Rtu concentrator;
    private CosemFactory cosemFactory;


    public Concentrator() {
    }

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger)
            throws BusinessException, SQLException, IOException {

        this.scheduler = scheduler;
        this.logger = logger;

        this.concentrator = scheduler.getRtu();
        this.cosemFactory = new CosemFactory(concentrator.getDeviceTimeZone());

        init();

        try {

            Iterator i = getAllMeters().iterator();
            while (i.hasNext()) {
                handleMeter((EeItem) i.next());
            }

            handleConcentrator();

        } catch (ParserConfigurationException e) {

            e.printStackTrace();
        } catch (SAXException e) {

            e.printStackTrace();
        }

    }

    String post(String message) throws IOException {

        if (message == null) {
            logger.log(Level.INFO, "No Message to send to this device");
            return null;
        }

        HttpTimeoutHandler th = new HttpTimeoutHandler(msTimeout);
        URL aUrl = new URL("http", url, port, "/xml/form", th);

        URLConnection conn = aUrl.openConnection();

        conn.setDoOutput(true);
        logger.log(Level.INFO, "post message : " + message);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(message);
        wr.flush();
        logger.log(Level.INFO, "post message complete");

        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        BufferedReader bReader = new BufferedReader(reader);

        StringBuffer sb = new StringBuffer();
        String line;

        while ((line = bReader.readLine()) != null) {
            sb.append(line);
        }

        wr.close();
        bReader.close();

        logger.log(Level.INFO, "received message : " + sb.toString());

        return sb.toString();

    }

    private void handleConcentrator() throws IOException {
        setTime();

        try {

            Environment.getDefault().execute(new ReadConcentratorTransaction(this, concentrator));

        } catch (BusinessException e) {

            /* log & continue to next meter */
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();

        } catch (SQLException e) {

            /* log & continue to next meter */
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();

        }
    }

    private void handleMeter(EeItem meter) throws IOException {
        System.out.println(meter);
        try {

            Environment.getDefault().execute(new ReadMeterTransaction(this, concentrator, meter));

        } catch (BusinessException e) {

            /* log & continue to next meter */
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();

        } catch (SQLException e) {

            /* log & continue to next meter */
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();

        }

    }

    private void setTime() throws IOException {

        /* Don't worry about clock sets over interval boundaries */

        Date cTime = new Date(); /* change this */
        Date now = new Date();

        long sDiff = (now.getTime() - cTime.getTime()) / 1000;
        long sAbsDiff = Math.abs(sDiff);
        logger.info("Difference between metertime and systemtime is " + sDiff * 1000 + " ms");

        long max = getCommunicationProfile().getMaximumClockDifference();
        long min = getCommunicationProfile().getMinimumClockDifference();

        if ((sAbsDiff < max) && (sAbsDiff > min)) {

            logger.severe("Adjust meter time to system time");

        }

    }

    /* Property code */

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.cbo.ConfigurationSupport#getOptionalKeys()
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(Constant.PK_TIMEOUT);
        return result;
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.cbo.ConfigurationSupport#getRequiredKeys()
    */
    public List getRequiredKeys() {
        List result = new ArrayList();
        return result;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.mdw.core.Pluggable#addProperties(java.util.Properties)
    */
    public void addProperties(Properties properties) {

        StringBuffer sb = new StringBuffer();

        Iterator i = getRequiredKeys().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (!properties.containsKey(key)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(key);
            }
        }

        if (sb.length() > 0) {
            throw new RuntimeException("Missing properties: " + sb);
        }

        String sTimeout = (String) properties.get(Constant.PK_TIMEOUT);
        if (null != sTimeout) {

            try {

                msTimeout = Integer.parseInt(sTimeout);

            } catch (NumberFormatException nfe) {
                String m = "Timeout could not be parsed (" + sTimeout + ")";
                throw new RuntimeException(m);
            }
        }

    }

    private void init() throws IOException {

        String phone = this.concentrator.getPhoneNumber();
        if ("".equals(phone)) {
            String msg = "Phone nr (cotaining URI) is missing.";
            throw new IOException(msg);
        }

        String s[] = phone.split(":");

        try {

            url = s[0];
            port = 80;

            if (s.length > 1) {
                /* a port number is set */
                port = Integer.parseInt(s[1]);

                if (port < 0 | port > 65535) {

                    String msg =
                            "Port number in invalid rage:"
                                    + port + " must be between 0 and 65535";

                    getLogger().severe(msg);
                    throw new RuntimeException(msg);

                }


            }

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            String msg = "Phone number contains invalid port nr" + s[1];
            getLogger().severe(msg);
            throw new IOException(msg);
        }

    }

    public Logger getLogger() {
        return logger;
    }

    public TimeZone getTimeZone() {
        return concentrator.getTimeZone();
    }

    public String getVersion() {
        return "$Revision: 1.21 $";
    }

    CommunicationProfile getCommunicationProfile() {
        return scheduler.getCommunicationProfile();
    }

    Calendar yesterday() {

        Calendar c = Calendar.getInstance(getTimeZone());

        c.add(Calendar.DAY_OF_MONTH, -1);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;

    }

    /* return all meters connected to a concentrator */
    public List getAllMeters() throws IOException, ParserConfigurationException, SAXException {

        List result = new ArrayList();

        String xml = post(newGetMeterList());

        MeterListSaxHandler dh = MeterListSaxHandler.toXmlDataHandler(xml);

        Iterator bcplIter = dh.getBcpl().iterator();
        while (bcplIter.hasNext()) {
            Bcpl bcpl = (Bcpl) bcplIter.next();

            Iterator eeIter = bcpl.getEeItems().iterator();
            while (eeIter.hasNext()) {
                EeItem eeItem = (EeItem) eeIter.next();
                result.add(eeItem);
            }

        }

        return result;

    }

    /**
     * create message to retrieve all meters
     */
    String newGetMeterList() {

        DomHelper dh = new DomHelper(XmlTag.RELEVE_CPL);
        dh.getRootElement().setAttribute(XmlTag.VERSION, "1.0");

        Element rq = dh.addElement(XmlTag.TELEREL_RQ);
        rq.setAttribute(XmlTag.IDENT, "040000000000");

        Element wr = dh.addElement(rq, XmlTag.READ_RQ);
        Element ec = dh.addElement(wr, XmlTag.EXP_CMD);
        dh.addElement(ec, XmlTag.NET_REG);

        return dh.toXmlString();

    }

    CosemFactory getCosemFactory() {
        return cosemFactory;
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
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
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

    public List getMessageCategories() {

        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageSpec msgSpec = null;

//        msgSpec = addBasicMsg("Connect", RtuMessageConstant.CONNECT_LOAD, !ADVANCED);
//        cat.addMessageSpec(msgSpec);
//        
//        msgSpec = addBasicMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, !ADVANCED);
//        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;

    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    public long getTimeDifference() {
        // TODO Auto-generated method stub
        return 0;
    }


}
