/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channel.serial.SerialComChannelImpl;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.propertyspec.MockPropertySpecService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractModemTests {

    protected static PropertySpecService propertySpecService = new MockPropertySpecService();

    protected static final String RUBBISH_FOR_FLUSH = "rubbishForFlush";
    protected static final String PHONE_NUMBER = "00123456789";
    protected static final int COMMAND_TIMEOUT_VALUE = 2000;
    protected static final int TEST_TIMEOUT_MILLIS = 10000;
    protected static final int TEST_LONG_TIMEOUT_MILLIS = 20000;
    protected final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "CONNECT 9600", "OK", "OK");

    protected final String comPortName = "blabla";

    protected class TestableSerialComChannel extends SerialComChannelImpl {

        private int counter = 0;
        private int index = 0;
        private List<String> responses = new ArrayList<String>();

        public TestableSerialComChannel(ServerSerialPort serialPort) {
            super(serialPort, ComChannelType.SerialComChannel);
        }

        public void setResponses(List<String> responses) {
            this.responses = responses;
        }

        @Override
        protected int doAvailable() {
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
        protected int doAvailable() {
            int available = super.doAvailable();
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
