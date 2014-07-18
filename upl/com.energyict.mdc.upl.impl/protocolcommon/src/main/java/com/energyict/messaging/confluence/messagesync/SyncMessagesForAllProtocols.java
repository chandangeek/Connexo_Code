package com.energyict.messaging.confluence.messagesync;

import com.energyict.cim.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.messaging.confluence.messagesync.client.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by dvn on 11/07/2014.
 */
public class SyncMessagesForAllProtocols {

    private static final Logger logger = Logger.getLogger(SyncMessagesForAllProtocols.class.getName());

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException, RemoteException {
        Properties properties = loadProps();
        String token = loginToConfluence(getConfluenceSoapService());

        Iterator<Object> it = properties.keySet().iterator();
        while (it.hasNext()) {
            String javaClassName = (String) it.next();
            try {
                SyncMessagesForProtocol.syncMessagesForProtocol(javaClassName, token);
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Failed to sync messages for protocol " + javaClassName + ": " + e.getMessage());
            }
        }
    }

    private static String loginToConfluence(ConfluenceSoapService soapService) throws java.rmi.RemoteException {
        return soapService.login("protocol-api", "keCx582dws");
    }

    private static Properties loadProps() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("./protocollist.properties"));
        return properties;
    }

    private static ConfluenceSoapService getConfluenceSoapService() throws org.apache.axis.AxisFault, MalformedURLException {
        return SyncMessagesForProtocol.getConfluenceSoapService();
    }
}