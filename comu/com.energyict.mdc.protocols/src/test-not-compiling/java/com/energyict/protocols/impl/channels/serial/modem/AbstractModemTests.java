package com.energyict.protocols.impl.channels.serial.modem;

import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.SerialComChannelImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Copyrights EnergyICT
 * Date: 22/11/12
 * Time: 12:19
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractModemTests {

    protected static final String RUBBISH_FOR_FLUSH = "rubbishForFlush";
    protected static final String PHONE_NUMBER = "00123456789";
    protected static final int COMMAND_TIMEOUT_VALUE = 100;
    protected final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "CONNECT 9600", "OK", "OK");

    protected final String comPortName = "blabla";

    protected class TestableSerialComChannel extends SerialComChannelImpl {

        private int counter = 0;
        private int index = 0;
        private List<String> responses = new ArrayList<>();

        public TestableSerialComChannel(ServerSerialPort serialPort) {
            super(serialPort);
        }

        public void setResponses(List<String> responses) {
            this.responses = responses;
        }

        @Override
        public int available() {
            if (responses.size() > counter) {
                int availabilities = responses.get(counter).length() - index;
                if (availabilities == 0) {
                    index = 0;
                    counter++;
                }
                return availabilities;
            }
            return -1;
        }

        @Override
        public int doWrite(byte[] bytes) {
            // nothing else to do
            return bytes.length;
        }

        @Override
        public int doRead() {
            if (responses.size() > counter) {
                return (int) responses.get(counter).charAt(index++);
            }
            return -1;
        }
    }

    protected class TimeoutSerialComChannel extends TestableSerialComChannel {

        private final long sleepTime;

        public TimeoutSerialComChannel(ServerSerialPort serialPort, long sleepTime) {
            super(serialPort);
            this.sleepTime = sleepTime;
        }

        @Override
        public int available() {
            int available = super.available();
            if (available <= 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return available;
        }
    }

}
