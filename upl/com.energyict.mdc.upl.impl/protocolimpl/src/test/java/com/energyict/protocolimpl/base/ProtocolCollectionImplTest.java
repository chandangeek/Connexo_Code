package com.energyict.protocolimpl.base;

import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.protocolcollections.ProtocolCollectionImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 13-jan-2011
 * Time: 16:06:13
 */
public class ProtocolCollectionImplTest {

    public static final String EICT_PROTOCOL_CLASS = "com.energyict.protocolimpl.";
    public static final String ELSTER_PROTOCOL_CLASS = "com.elster.protocolimpl.";
    public static Logger logger = null;

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(ProtocolCollectionImplTest.class.getName());
        }
        return logger;
    }

    @Test
    public void testProtocolVersions() {
        ProtocolCollectionImpl collection = new ProtocolCollectionImpl();
        List<String> classNames = collection.getProtocolClasses();

        List<String> oldFormat = new ArrayList<String>();
        List<String> missingKeyWord = new ArrayList<String>();
        List<String> wrongFormat = new ArrayList<String>();
        List<String> missingVersion = new ArrayList<String>();

        for (String className : classNames) {
            if (className.startsWith(EICT_PROTOCOL_CLASS) || className.startsWith(ELSTER_PROTOCOL_CLASS)) {
                try {
                    Object object = Class.forName(className).newInstance();
                    if (object instanceof MeterProtocol) {
                        MeterProtocol protocol = (MeterProtocol) object;
                        String version = protocol.getProtocolVersion();
                        if ((version == null) || (version.trim().equals(""))) {
                            missingVersion.add(className);
                        } else if (version.equals("$Date$")) {
                            missingKeyWord.add(className);
                        } else if (version.startsWith("$Revision: ")) {
                            oldFormat.add(className);
                        } else if (version.startsWith("Revision ")) {
                            oldFormat.add(className);
                        } else if (!version.startsWith("$Date: ")) {
                            wrongFormat.add(className);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Warning: " + e.getClass().getSimpleName() + " " + e.getMessage() + " - [" + className + "]");
                }
            }
        }

        if (oldFormat.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following protocol classes still use old \"$Revision$\" format in getProtocolVersion(). We should upgrade this to \"$Date$\" format to be conform:");
            for (String className : oldFormat) {
                sb.append("\r\n").append("    *  ").append(className);
            }
            sb.append("\r\n");
            fail(sb.toString());
        }

        if (wrongFormat.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following protocol classes returned an invalid format for getProtocolVersion(). It should be \"$Date$\"");
            for (String className : wrongFormat) {
                sb.append("\r\n").append("    *  ").append(className);
            }
            sb.append("\r\n");
            fail(sb.toString());
        }

        if (missingVersion.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following protocol classes don't have a version or getProtocolVersion() returned null:");
            for (String className : missingVersion) {
                sb.append("\r\n").append("    *  ").append(className);
            }
            sb.append("\r\n");
            fail(sb.toString());
        }

        if (missingKeyWord.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following protocol classes are using the correct \"$Date$\" format in getProtocolVersion() but SVN Keyword Date is not set for these classes:");
            for (String className : missingKeyWord) {
                sb.append("\r\n").append("    *  ").append(className);
            }
            fail(sb.toString());
        }

    }

}
