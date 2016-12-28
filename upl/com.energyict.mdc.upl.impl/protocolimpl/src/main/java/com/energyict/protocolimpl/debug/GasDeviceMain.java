/**
 *
 */
package com.energyict.protocolimpl.debug;


import com.energyict.dialer.core.LinkException;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityPolicy;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

import java.io.IOException;
import java.util.Properties;

/**
 * @author gna
 * @since 15-feb-2010
 */
public class GasDeviceMain extends AS220Main {

    private GasDevice gasDevice;

    public static void main(String[] args) {
        GasDeviceMain main = new GasDeviceMain();
        main.setCommPort(COMPORT);
        main.setBaudRate(BAUDRATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
        main.run();
    }

    public GasDevice getGasDevice() {
        if (gasDevice == null) {
            gasDevice = new GasDevice();
            log("Created new instance of " + gasDevice.getClass().getCanonicalName() + " [" + gasDevice.getProtocolVersion() + "]");
        }
        return gasDevice;
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty("MaximumTimeDiff", "300");
        properties.setProperty("MinimumTimeDiff", "1");
        properties.setProperty("CorrectTime", "0");

        properties.setProperty("Retries", "5");
        properties.setProperty("Timeout", "20000");
        //properties.setProperty("ForcedDelay", "500");

        properties.setProperty("SecurityLevel", "1:" + SecurityPolicy.SECURITYPOLICY_NONE);
        properties.setProperty("ProfileInterval", "900");
        properties.setProperty("Password", "00000000");
        properties.setProperty("SerialNumber", "00000000012345758");
        properties.setProperty("NodeAddress", "35016036:1");

        properties.setProperty("AddressingMode", "-1");
        properties.setProperty("Connection", "3");
        properties.setProperty("ClientMacAddress", "2");
        properties.setProperty("ServerLowerMacAddress", "1");
        properties.setProperty("ServerUpperMacAddress", "1");

        properties.setProperty(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
        properties.setProperty(LocalSecurityProvider.DATATRANSPORTKEY, "000102030405060708090A0B0C0D0E0F");

        return properties;
    }

    @Override
    void doDebug() throws LinkException, IOException {

        log(getGasDevice().getSerialNumber());
        log(getGasDevice().getProfileData(true));


        /*
        getGasDevice().getgMeter().getGasValveController().doDisconnect();

		getGasDevice().getgMeter().getGasInstallController().deinstall();

		log(getGasDevice().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.0.96.1.0.255"), 0x08, 1).getValue());
		log(getGasDevice().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.1.96.1.0.255"), 0x08, 1).getValue());
		log(getGasDevice().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.2.96.1.0.255"), 0x08, 1).getValue());
		log(getGasDevice().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.3.96.1.0.255"), 0x08, 1).getValue());
		log(getGasDevice().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.4.24.2.0.255"), 0x08, 4));

		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(getGasDevice().getGasSlotId()-1).getObisCode()).getIdentificationNumber();

		getGasDevice().getMeterConfig();

		log(getGasDevice().getSerialNumber());
		getGasDevice().getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.4.24.3.0.255")).getCapturePeriod();

		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).deinstallSlave();

		log(getGasDevice().getProfileInterval());

	    getGasDevice().getgMeter().getGasInstallController().
    	setEncryptionKey(DLMSUtils.hexStringToByteArray("11223344556677889900AABBCCDDEEFF"));
    	Structure rawDataStruct = new Structure();
    	rawDataStruct.addDataType(new OctetString(DLMSUtils.hexStringToByteArray("11223344556677889900AABBCCDDEEFF")));
    	rawDataStruct.addDataType(new OctetString(DLMSUtils.hexStringToByteArray("8f2b9d68640418dc392d6634c8fc0367")));

    	getGasDevice().getgMeter().getGasInstallController().setBothKeysAtOnce(rawDataStruct.getBEREncodedByteArray());

		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).getIdentificationNumber());
		getGasDevice().getgMeter().getGasValveController().getContactorState();
		getGasDevice().getgMeter().getGasValveController().doConnect();
		getGasDevice().getgMeter().getGasValveController().getContactorState();
		getGasDevice().getgMeter().getGasValveController().doDisconnect();

		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).getCapturePeriod());
		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).getIdentificationNumber());

		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(1).getObisCode()).getCapturePeriod());
		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(1).getObisCode()).getIdentificationNumber());
		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).getCapturePeriod());
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).setCapturePeriod(900);
		log(getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(0).getObisCode()).getCapturePeriod());

		getGasDevice().getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.4.24.3.0.255")).writeCapturePeriodAttr(new Unsigned32(900));
		getGasDevice().getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.1.24.3.0.255"), 4).
		write(new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0x00, 0x00, 0x03, (byte)0x84});

		getGasDevice().getgMeter().GetMbusEventProfile().getBuffer();
        getGasDevice().getgMeter().getGasInstallController().install();
        */

    }

}
