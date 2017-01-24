/**
 *
 */
package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractMbusInstallController;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

import java.io.IOException;

/**
 * AM500 specific implementation fo the installation/deinstallation of an Mbus(gas) device
 *
 * @author gna
 * @since 16-feb-2010
 */
public class GasInstallController extends AbstractMbusInstallController {

	/**
	 * @param protocol
	 */
	public GasInstallController(MeterProtocol protocol) {
		super(protocol);
	}

	/**
	 * Getter for the {@link GasDevice} {@link MeterProtocol}
	 *
	 * @return the parent {@link GasDevice} {@link MeterProtocol}
	 */
	public GasDevice getGasDevice() {
		return (GasDevice) getProtocol();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deinstall() throws IOException {
		getGasDevice().getLogger().info("DEINSTALL (DeCommission) message received");
        getMbusClient().deinstallSlave();
	}

	/**
	 * {@inheritDoc}
	 */
	public void install() throws IOException {
		getGasDevice().getLogger().info("INSTALL (Commission) message received");
        getMbusClient().installSlave(getGasDevice().getPhysicalAddress());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEncryptionKey(byte[] encryptionKey) throws IOException {
		getGasDevice().getLogger().info("SET ENCRYPTION KEY (open key) message received");
        getMbusClient().setEncryptionKey(encryptionKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTransferKey(byte[] transferKey) throws IOException {
		getGasDevice().getLogger().info("SET TRANSFER KEY (encrypted key) message received");
        getMbusClient().setTransportKey(transferKey);
	}

	public void setBothKeysAtOnce(byte[] rawDataWithKeys) throws IOException {
		getGasDevice().getLogger().info("SET BOTH KEY (encryption and encrypted key) message received");
        getMbusClient().setTransportKeyRawData(rawDataWithKeys);
	}

    /**
     * Getter for the MbusClient Object. Depending on the firmwareVersion of the device, a different shortname mapping is used
     *
     * @return the used MbusClient object
     * @throws IOException
     */
    public MBusClient getMbusClient() throws IOException {
        if(getGasDevice().getActiveFirmwareVersion().isHigherOrEqualsThen("2")){
            return getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode(), MbusClientAttributes.VERSION10);
        } else {
            return getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode(), MbusClientAttributes.VERSION9);
        }
    }

    public void writeCaptureDefinition(Array capture_definition) throws IOException {
        getMbusClient().writeCaptureDefinition(capture_definition);
        getGasDevice().getLogger().info("MBus capture_definition was successfully written.");
    }
}
