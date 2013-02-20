/**
 *
 */
package com.energyict.genericprotocolimpl.bgbz3;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.mdw.core.Device;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 * @since 23-dec-2009
 */
public class BgbZ3 {

//    private CommunicationScheduler scheduler;
    private Link link;
    private Logger logger;
    private Device rtu;
    private URL url;
    private StringBuilder responseData;
    private DataImporter di;
    private StoreObject storeObject;

//    /**
//     * {@inheritDoc}
//     */
//    public void execute(CommunicationScheduler scheduler, Link link,
//                        Logger logger) throws BusinessException, SQLException, IOException {
//        this.scheduler = scheduler;
//        this.link = link;
//        this.logger = logger;
//        try {
//
//            init();
//
//            requestData();
//
//            importData(responseData.toString(), this.di);
//            storeObject.addAll(di.getRegisterValueMap());
//
//        } finally {
//            if (storeObject != null) {
//                Environment.getDefault().execute(storeObject);
//            }
//        }
//    }

    /**
     * Init some values
     *
     * @throws ConnectionException
     */
    private void init() throws ConnectionException {
//        this.rtu = this.scheduler.getRtu();
        // the the url from a proper property on the connectionType
//        constructUrl(this.rtu.getPhoneNumber());
        this.di = new DataImporter(this.rtu.getRegisters());
        this.storeObject = new StoreObject();
    }

    /**
     * Construct the proper URL
     *
     * @param ipAddress the IP-address to use
     * @throws ConnectionException if the URL isn't correctly formed
     */
    protected void constructUrl(String ipAddress) throws ConnectionException {
        try {
            this.url = new URL("http://" + ipAddress + "/ChannelData.xml");
        } catch (MalformedURLException e) {
            this.logger.log(Level.FINEST, e.getMessage());
            throw new ConnectionException("URL could not be formed because IP address(phone number) is not valid.");
        }
    }

    /**
     * Send the actual GET
     *
     * @throws IOException
     */
    protected void requestData() throws IOException {
        this.responseData = new StringBuilder();

        try {
            URLConnection uc = this.url.openConnection();
            InputStream is = uc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            do {
                line = br.readLine();
                if (line != null) {
                    this.responseData.append(line);
                }
            } while (line != null);

        } catch (IOException e) {
            this.logger.log(Level.FINEST, e.getMessage());
            throw e;
        }

    }

    /**
     * {@inheritDoc}
     */
    public long getTimeDifference() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void addProperties(Properties properties) {
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return "$Date$";
    }

    /**
     * {@inheritDoc}
     */
    public List getOptionalKeys() {
        return new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    public List getRequiredKeys() {
        return new ArrayList();
    }
//
//    @Override
//    public List<PropertySpec> getRequiredProperties() {
//        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
//    }
//
//    @Override
//    public List<PropertySpec> getOptionalProperties() {
//        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
//    }
//
//    @Override
//    public void addProperties(TypedProperties properties) {
//        addProperties(properties.toStringProperties());
//    }

//    /**
//     * @return the scheduler
//     */
//    protected CommunicationScheduler getScheduler() {
//        return scheduler;
//    }

    /**
     * @return the link
     */
    protected Link getLink() {
        return link;
    }

    /**
     * @return the logger
     */
    protected Logger getLogger() {
        return logger;
    }
//
//    /**
//     * @param scheduler the scheduler to set
//     */
//    protected void setScheduler(CommunicationScheduler scheduler) {
//        this.scheduler = scheduler;
//    }

    /**
     * @param link the link to set
     */
    protected void setLink(Link link) {
        this.link = link;
    }

    /**
     * @param logger the logger to set
     */
    protected void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @return the url
     */
    protected URL getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    protected void setUrl(URL url) {
        this.url = url;
    }

    /**
     * @return the responseData
     */
    protected StringBuilder getResponseData() {
        return responseData;
    }

    /**
     * @param responseData the responseData to set
     */
    protected void setResponseData(StringBuilder responseData) {
        this.responseData = responseData;
    }

    /**
     * Importer for the XML data
     *
     * @param data
     * @param handler
     * @throws BusinessException
     */
    protected void importData(String data, DefaultHandler handler) throws BusinessException {
        try {

            byte[] bai = data.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (ParserConfigurationException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (SAXException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (IOException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        }
    }

}
