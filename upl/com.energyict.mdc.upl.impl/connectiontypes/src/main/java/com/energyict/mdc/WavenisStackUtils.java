package com.energyict.mdc;


import com.energyict.concentrator.communication.driver.rf.eictwavenis.*;

import java.io.*;
import java.util.Date;

/**
 * Util class that returns a fully started Wavenis stack implementation based on a given inputstream and outputstream
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 9:48
 * Author: khe
 */
public class WavenisStackUtils {

    private static final String FRIENDLY_NAME = "MUC___";

    public static WavenisStack start(InputStream inputStream, OutputStream outputStream) throws IOException {
        WavenisStack wavenisStack = WavenisStackImpl.getInstance(FRIENDLY_NAME, 1, null);
        wavenisStack.start(inputStream, outputStream);
        wavenisStack.getNetworkManagement().stop();
        initializeRoot(wavenisStack);
        setRelayRouteStatus(wavenisStack);
        return wavenisStack;
    }

    public static void syncClock(WavenisStack wavenisStack) throws IOException {
        boolean success = false;
        int count = 0;
        while (!success) {
            try {
                wavenisStack.getWaveCard().syncTimeIfNeeded();
                success = true;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
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
        }
        return new Date();      //Wavecard doesn't have a clock, move on
    }

    private static void setRelayRouteStatus(WavenisStack wavenisStack) throws IOException {
        boolean success = false;
        int count = 0;
        while (!success) {
            try {
                wavenisStack.getWaveCard().activateRelayRouteStatus(true);
                success = true;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
        }
    }

    private static void initializeRoot(WavenisStack wavenisStack) throws IOException {
        boolean success = false;
        int count = 0;
        while (!success) {
            try {
                wavenisStack.getWaveCard().initializeRoot(FRIENDLY_NAME, 1);
                success = true;
            } catch (WavenisProtocolTimeoutException e) {
                if (count >= 2) {
                    break;
                }
                delay();
            } catch (WavenisParameterException e) {
                //Parameter doesn't exist, move on
                break;
            }
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
        }
    }

    private static void validateRadioAddress(String radioAddress, String[] repeaterRadioAddresses) throws IOException {
        if (radioAddress.length() != 12) {
            throw new IOException("Invalid radio address [" + radioAddress + "]");
        } else {
            try {
                Long.parseLong(radioAddress, 16);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid radio address [" + radioAddress + "]");
            }
            if (repeaterRadioAddresses != null) {
                for (String repeaterRadioAddress : repeaterRadioAddresses) {
                    if (repeaterRadioAddress.length() != 12) {
                        throw new IOException("Invalid radio repeater address [" + repeaterRadioAddress + "]");
                    }
                    try {
                        Long.parseLong(repeaterRadioAddress, 16);
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid radio repeater address [" + repeaterRadioAddress + "]");
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

    public static WavenisRoute getWavenisRoute(String networkId) throws IOException {

        //radioaddress[_repeater1][,repeater2][,repeater3]
        //001122334455_001122334455,001122334455,001122334455

        String[] splitNetworkId = networkId.split("_");
        String radioAddress = splitNetworkId[0];
        String[] repeaterRadioAddresses = null;

        if (splitNetworkId.length > 1) {
            repeaterRadioAddresses = splitNetworkId[1].split(",");
        }
        validateRadioAddress(radioAddress, repeaterRadioAddresses);
        return new WavenisRoute(radioAddress, repeaterRadioAddresses);
    }

    /**
     * Use the Wavenis stack to create a link to the RF module. The resulting link contains an inputstream and an outputstream.
     */
    public static WaveModuleLinkAdaptor createLink(String rfAddress, WavenisStack wavenisStack) throws IOException {
        WavenisRoute radioAddress = getWavenisRoute(rfAddress);
        WaveModule waveModule = wavenisStack.getWaveModuleFactory().find(radioAddress.getRadioAddress());
        WaveModuleLinkAdaptor waveModuleLinkAdaptor = new WaveModuleLinkAdaptor();
        if (radioAddress.getRepeaterAddresses() != null) {
            waveModule.changeRoute(radioAddress.getRepeaterAddresses());
        }
        //TODO waveModule.setConfigRFResponseTimeoutInMs(); ?
        waveModuleLinkAdaptor.init(waveModule);
        return waveModuleLinkAdaptor;
    }
}