package com.energyict.smartmeterprotocolimpl.eict.AM110R.wakeup;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <pre>
 * The SMS Wakeup will send a wakeup SMS to the device by using Proximus as SMS carrier. The delivery of the SMS is not guaranteed.
 * Afterwards a polling mechanism is started to see if the meter is accessible (a connection could be made to the IP, which indicates the device is awake).
 * An exception is thrown when the SMS could not be delivered to Proximus or the polling timed out (the device didn't woke up before timeout).
 * </pre>
 *
 * @author sva
 */
public class SmsWakeup {

    private static final int semaphorePermits;

    private static final int DEBUG_LEVEL = -1;

    static {
        semaphorePermits = Integer.parseInt(Environment.getDefault().getProperty("smswakeup.semaphore.maxpermits", "25"));
    }

    private static final Semaphore semaphore = new Semaphore(semaphorePermits, true);

    private Logger logger;

    private final String ipAddressAndPort;
    private final SmsWakeUpDlmsProtocolProperties smsProperties;
    private SocketStreamConnection socketStreamConnection;

    /**
     * Constructor
     *
     * @param ipAddressAndPort  the ipAddress and port for the GPRS connection
     * @param smsProperties  the {@link com.energyict.smartmeterprotocolimpl.eict.AM110R.common.SmsWakeUpDlmsProtocolProperties} to be used
     * @param logger the used <CODE>Logger</CODE>
     */
    public SmsWakeup(String ipAddressAndPort, SmsWakeUpDlmsProtocolProperties smsProperties, Logger logger) throws BusinessException {
        this.ipAddressAndPort = ipAddressAndPort;
        this.smsProperties = smsProperties;
        this.logger = logger;
        checkAllRequiredPropertiesArePresent();
    }

    /**
     * Triggers the wakeup
     *
     * @throws com.energyict.cbo.BusinessException if a business error occurred, or when the acquireThread was interrupted
     * @throws java.io.IOException       if parameters aren't correctly configured, when WSDL isn't found or when interrupted while sleeping
     */
    public void doWakeUp() throws IOException, BusinessException {
        try {
            // get a lock on the semaphore
            log(5, "Request Lock at " + Calendar.getInstance().getTime());
            semaphore.acquire();
            try {
                log(5, "Got Lock at " + Calendar.getInstance().getTime());

                sendWakeupSMS();

            } finally {
                semaphore.release();
                log(5, "Released Lock at " + Calendar.getInstance().getTime());
            }
        } catch (InterruptedException e) {
            this.logger.info(e.getMessage());
            throw new BusinessException("Interrupted while waiting for a lock on the SmsWakeup thread");
        }

        // wait and check until the device is online and accessible
        waitAndCheckForOnlineDevice();
    }

    /**
     * Verify if all needed properties for the wakeup are present
     */
    private void checkAllRequiredPropertiesArePresent() throws BusinessException {
        if (smsProperties.getSmsConnectionUrl().equals("")) {
            throw new BusinessException("For SMS wakeup property SmsConnectionURL is required. Please correct first.");
        } else if (smsProperties.getSmsSource().equals("")) {
            throw new BusinessException("For SMS wakeup property SmsSource is required. Please correct first.");
        } else if (smsProperties.getSmsAuthentication().equals("")) {
            throw new BusinessException("For SMS wakeup property SmsAuthentication is required. Please correct first.");
        } else if (smsProperties.getSmsServiceCode().equals("")) {
            throw new BusinessException("For SMS wakeup property SmsServiceCode is required. Please correct first.");
        } else if (smsProperties.getSmsPhoneNumber().equals("")) {
            throw new BusinessException("For SMS wakeup property SmsPhoneNumber is required. Please correct first.");
        }
    }

    /**
     * Create the actual call.
     * Find all necessary parameters on the device, including his attributes and send the trigger.
     * Afterwards, check the response to see if the SMS will be sent to the device so we can start polling the IP-Address
     *
     * @throws java.io.IOException when the wsdl is not found, or when certain attributes are not correctly filled in
     */
    private void sendWakeupSMS() throws ConnectionException {
        log(5, "In createWakeupCall");

        ProximusSmsSender smsSender = new ProximusSmsSender(smsProperties, this.logger);
        Sms wakeupSms = new Sms("", smsProperties.getSmsPhoneNumber(), new Date(), "Proximus", "0", 8, "EICT".getBytes());
        smsSender.sendSMS(wakeupSms);
    }

    /**
     * Polling mechanism to connect to the meter.
     *
     * @throws com.energyict.cbo.BusinessException if there isn't an updated IP-address
     * @throws java.io.IOException       during the sleep
     */
    private void waitAndCheckForOnlineDevice() throws BusinessException, IOException {
        long protocolTimeout = System.currentTimeMillis() + smsProperties.getPollTimeOut();
        logger.info("Polling until device becomes available on GPRS.");

        sleep(smsProperties.getFirstPollDelay());
        socketStreamConnection = tryToConnectToDevice(ipAddressAndPort);

       while (socketStreamConnection == null) {
            if ((System.currentTimeMillis() - protocolTimeout) > 0) {
                throw new BusinessException("Could not connect to the device - Polling timeout exceeded.");
            }
            sleep(smsProperties.getSecondPollDelay());
            socketStreamConnection = tryToConnectToDevice(ipAddressAndPort);
        }
        logger.log(Level.INFO, "WakeUp finished - Connected to " + ipAddressAndPort);
    }

    /**
     * Try to connect to the device.
     * If the device is not yet awake (the connect fails), null will be returned
     *
     * @param ipAddressAndPort
     * @return the opened SocketStreamConnection or null
     */
    private SocketStreamConnection tryToConnectToDevice(String ipAddressAndPort) {
        SocketStreamConnection socketStreamConnection = new SocketStreamConnection(ipAddressAndPort);
        try {
            socketStreamConnection.open();
            return socketStreamConnection;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Hold the thread for the given sleeptime
     *
     * @param sleepTime
     * @throws java.io.IOException when we get interrupted while sleeping
     */
    private void sleep(long sleepTime) throws IOException {
        ProtocolTools.delay(sleepTime);
    }

    public SocketStreamConnection getSocketStreamConnection() {
        return socketStreamConnection;
    }

    private void log(int level, String msg) {
        if (level <= this.DEBUG_LEVEL) {
            this.logger.info(msg);
        }
    }

    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }
}
