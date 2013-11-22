package com.energyict.protocolimpl.debug;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.dialer.core.*;
import com.energyict.protocolimpl.base.DebuggingObserver;
import oracle.jdbc.OracleDriver;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 9:27:00
 */
public class DebugUtils {

    public static final int TIMEOUT = 2 * 60 * 1000;
    private static final int BAUDRATE = 9600;

    private static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    private static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    private static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;


    /**
     * Create, initialise and connect an standard modem dialer
     * Use a given DebuggingObserver to log the data to the screen of to a file
     *
     * @param phoneNumber
     * @param commPort
     * @param modemInit
     * @return
     * @throws LinkException
     * @throws IOException
     */
    public static Dialer getConnectedModemDialer(String phoneNumber, String commPort, String modemInit, DebuggingObserver debuggingObserver) throws LinkException, IOException {
        Dialer dialer = DialerFactory.getStandardModemDialer().newDialer();
        if (debuggingObserver != null) {
            dialer.setStreamObservers(debuggingObserver);
        }
        dialer.init(commPort, modemInit);
        dialer.getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
        dialer.connect(phoneNumber, TIMEOUT);
        return dialer;
    }

    /**
     * Create, initialise and connect a n standard modem dialer
     *
     * @param phoneNumber
     * @param commPort
     * @param modemInit
     * @return
     * @throws LinkException
     * @throws IOException
     */
    public static Dialer getConnectedModemDialer(String phoneNumber, String commPort, String modemInit) throws LinkException, IOException {
        return getConnectedModemDialer(phoneNumber, commPort, modemInit, null);
    }


    public static Dialer getConnectedIPDialer(String remoteHost, DebuggingObserver debuggingObserver) throws IOException, LinkException {
        Dialer dialer = DialerFactory.get("IPDIALER").newDialer();
        if (debuggingObserver != null) {
            dialer.setStreamObservers(debuggingObserver);
        }
        dialer.connect(remoteHost, 60);
        return dialer;
    }

    public static Dialer getConnectedIPDialer(String phoneNumber) throws IOException, LinkException {
        return getConnectedIPDialer(phoneNumber, null);
    }

    /**
     * Create, initialise and connect a direct dialer
     * Use a given DebuggingObserver to log the data to the screen of to a file
     *
     * @param commPort
     * @param baudRate
     * @param dataBits
     * @param parity
     * @param stopBits
     * @param debuggingObserver
     * @return
     * @throws LinkException
     * @throws IOException
     */
    public static Dialer getConnectedDirectDialer(String commPort, int baudRate, int dataBits, int parity, int stopBits, DebuggingObserver debuggingObserver) throws LinkException, IOException {
        Dialer dialer = DialerFactory.getDirectDialer().newDialer();
        if (debuggingObserver != null) {
            dialer.setStreamObservers(debuggingObserver);
        }
        dialer.init(commPort);
        dialer.getSerialCommunicationChannel().setParams(baudRate, dataBits, parity, stopBits);
        dialer.connect();
        return dialer;
    }

    /**
     * Create, initialise and connect a direct dialer
     *
     * @param commPort
     * @param baudRate
     * @param dataBits
     * @param parity
     * @param stopBits
     * @return
     * @throws IOException
     * @throws LinkException
     */
    public static Dialer getConnectedDirectDialer(String commPort, int baudRate, int dataBits, int parity, int stopBits) throws IOException, LinkException {
        return getConnectedDirectDialer(commPort, baudRate, dataBits, parity, stopBits, null);
    }

    /**
	 * Create a new default {@link Environment}
	 */
	public static void createEnvironment() {
		try {
            File propertiesFile = new File(System.getProperty("user.dir") + "\\eiserver.properties");
            FileInputStream fis = new FileInputStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(fis);
            fis.close();
			EnvironmentImpl.setDefault(
                    new DebugEnvironment(
                            properties.getProperty("jdbcUrl"),
                            properties.getProperty("dbUser"),
                            properties.getProperty("dbPassword")));
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

    private static class DebugEnvironment extends EnvironmentImpl {
        private Connection connection;

        private DebugEnvironment (String jdbcUrl, String dbUser, String dbPassword) {
            super();
            this.establishConnection(jdbcUrl, dbUser, dbPassword);
        }

        private void establishConnection (String jdbcUrl, String dbUser, String dbPassword) {
            try {
                DriverManager.registerDriver(new OracleDriver());
                this.connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            }
            catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }

        @Override
        public Connection getConnection () {
            return this.connection;
        }

        @Override
        public boolean useOraLobs () {
            return false;   // function empty_blob() not available
        }

    }

}