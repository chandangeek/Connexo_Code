package com.elster.genericprotocolimpl.dlms.ek280.debug;

import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Copyrights
 * Date: 7/06/11
 * Time: 10:41
 */
public class EK280Main extends AbstractDebuggingMain<Dlms> {

    private static final String COM_PORT = "COM40";
    private static final int BAUD_RATE = 19200;
    private static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    private static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    private static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;

    private Dlms dlmsProtocol = null;

    @Override
    Dlms getMeterProtocol() {
        if (dlmsProtocol == null) {
            dlmsProtocol = new Dlms();
        }
        return dlmsProtocol;
    }

    @Override
    Properties getProperties() {
        Properties properties = new Properties();
        return properties;
    }

    public static void main(String[] args) {
        EK280Main main = new EK280Main();
        main.setCommPort(COM_PORT);
        main.setBaudRate(BAUD_RATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
        main.setObserverFilename(null);
        main.setShowCommunication(true);
        main.setTimeZone(TimeZone.getDefault());
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {

    }

}
