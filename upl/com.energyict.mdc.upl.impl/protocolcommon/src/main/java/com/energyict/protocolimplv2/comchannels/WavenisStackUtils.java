package com.energyict.protocolimplv2.comchannels;


import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModule;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModuleInputStream;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModuleOutputStream;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisParameterException;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisProtocolTimeoutException;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStackImpl;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Util class that returns a fully started Wavenis stack implementation based on a given inputstream and outputstream
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 9:48
 * Author: khe231
 */
public class WavenisStackUtils {

    private static final String FRIENDLY_NAME = "MUC___";
    public static final String RF_ADDRESS = "RFAddress";

    public static WavenisStack start(InputStream inputStream, OutputStream outputStream) throws IOException {
        WavenisStack wavenisStack = WavenisStackImpl.getInstance(FRIENDLY_NAME, 1, null);
        wavenisStack.start(inputStream, outputStream);
        wavenisStack.getNetworkManagement().stop();
        initializeRoot(wavenisStack);
        setRelayRouteStatus(wavenisStack);
        return wavenisStack;
    }

    public static void syncClock(WavenisStack wavenisStack) throws IOException {
        boolean failure = true;
        int count = 0;
        while (failure) {
            try {
                wavenisStack.getWaveCard().syncTimeIfNeeded();
                failure = false;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
            count++;
        }
    }

    public static String readFirmwareVersion(WavenisStack wavenisStack) throws IOException {
        int count = 0;
        while (true) {
            try {
                return wavenisStack.getWaveCard().getWavecardSimpleFirmwareInfo().toString();
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    throw e;
                }
                delay();
            }
            count++;
        }
    }

    public static Date readClock(WavenisStack wavenisStack) throws IOException {
        int count = 0;
        while (true) {
            try {
                return wavenisStack.getSettingParameterCommandFactory().readRealtimeClock();
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
            count++;
        }
        return new Date();      //Wavecard doesn't have a clock, move on
    }

    private static void setRelayRouteStatus(WavenisStack wavenisStack) throws IOException {
        boolean failure = true;
        int count = 0;
        while (failure) {
            try {
                wavenisStack.getWaveCard().activateRelayRouteStatus(true);
                failure = false;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
            count++;
        }
    }

    private static void initializeRoot(WavenisStack wavenisStack) throws IOException {
        boolean failure = true;
        int count = 0;
        while (failure) {
            try {
                wavenisStack.getWaveCard().initializeRoot(FRIENDLY_NAME, 1);
                failure = false;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
            count++;
        }
    }

    private static void delay() {
        delay(5000);
    }

    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    private static void validateRadioAddress(String radioAddress, String[] repeaterRadioAddresses, String fullRFAddress) {
        ProtocolRuntimeException invalidPropertyFormatException = DeviceConfigurationException.invalidPropertyFormat(RF_ADDRESS, fullRFAddress, "Each radio address should consist out of 12 hexadecimal characters.");
        if (radioAddress.length() != 12) {
            throw invalidPropertyFormatException;
        } else {
            try {
                Long.parseLong(radioAddress, 16);
            } catch (NumberFormatException e) {
                throw invalidPropertyFormatException;
            }
            if (repeaterRadioAddresses != null) {
                for (String repeaterRadioAddress : repeaterRadioAddresses) {
                    if (repeaterRadioAddress.length() != 12) {
                        throw invalidPropertyFormatException;
                    }
                    try {
                        Long.parseLong(repeaterRadioAddress, 16);
                    } catch (NumberFormatException e) {
                        throw invalidPropertyFormatException;
                    }
                }
            }
        }
    }

    public static class WavenisRoute {

        private String radioAddress;
        private String[] repeaterAddresses;

        public String getRadioAddress() {
            return radioAddress;
        }

        public String[] getRepeaterAddresses() {
            return repeaterAddresses;
        }

        public WavenisRoute(String radioAddress, String[] repeaterAddresses) {
            this.radioAddress = radioAddress;
            this.repeaterAddresses = repeaterAddresses;
        }
    }

    private static WavenisRoute getWavenisRoute(String fullRFAddress) {

        //radioaddress[_repeater1][,repeater2][,repeater3]
        //001122334455_001122334455,001122334455,001122334455

        String[] splitNetworkId = fullRFAddress.split("_");
        String radioAddress = splitNetworkId[0];
        String[] repeaterRadioAddresses = null;

        if (splitNetworkId.length > 1) {
            repeaterRadioAddresses = splitNetworkId[1].split(",");
        }
        validateRadioAddress(radioAddress, repeaterRadioAddresses, fullRFAddress);
        return new WavenisRoute(radioAddress, repeaterRadioAddresses);
    }

    /**
     * Use the Wavenis stack to create an input and output stream to the RF module.
     */
    public static WavenisInputOutStreams createInputOutStreams(String fullRFAddress, WavenisStack wavenisStack) {
        WavenisRoute radioAddress = getWavenisRoute(fullRFAddress);
        WaveModule waveModule;
        try {
            waveModule = wavenisStack.getWaveModuleFactory().find(radioAddress.getRadioAddress());
        } catch (IOException e) {
            //Underlying implementation never throws an IOException...
            throw CodingException.protocolImplementationError("Unexpected IOException while finding WavenisModule from radio address " + radioAddress.getRadioAddress());
        }
        if (radioAddress.getRepeaterAddresses() != null) {
            try {
                if (waveModule != null) {
                    waveModule.changeRoute(radioAddress.getRepeaterAddresses());
                }
            } catch (IOException e) {
                throw DeviceConfigurationException.invalidPropertyFormat(RF_ADDRESS, fullRFAddress, "Each radio address should consist out of 12 hexadecimal characters.");
            }
        }
        WaveModuleOutputStream outputStream = new WaveModuleOutputStream(waveModule);
        return new WavenisInputOutStreams(new WaveModuleInputStream(outputStream), outputStream);
    }

    public static class WavenisInputOutStreams {
        public final InputStream inputStream;
        public final OutputStream outputStream;

        public WavenisInputOutStreams(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }
    }

}