/**
 * 
 */
package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;

import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractMbusInstallController;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

/**
 * AM500 specific implementation fo the installation/deinstallation of an Mbus(gas) device
 * 
 * @author gna
 * @since 16-feb-2010
 *
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
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().
				getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode()).deinstallSlave();
	}

	/**
	 * {@inheritDoc}
	 */
	public void install() throws IOException {
		getGasDevice().getLogger().info("INSTALL (Commission) message received");
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().
				getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode()).installSlave(getGasDevice().getPhysicalAddress());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEncryptionKey(byte[] encryptionKey) throws IOException {
		getGasDevice().getLogger().info("SET ENCRYPTION KEY (open key) message received");
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().
				getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode()).setEncryptionKey(encryptionKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTransferKey(byte[] transferKey) throws IOException {
		getGasDevice().getLogger().info("SET TRANSFER KEY (encrypted key) message received");
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().
				getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode()).setTransportKey(transferKey);
	}

	public void setBothKeysAtOnce(byte[] rawDataWithKeys) throws IOException {
		getGasDevice().getLogger().info("SET BOTH KEY (encryption and encrypted key) message received");
		getGasDevice().getCosemObjectFactory().getMbusClient(getGasDevice().getMeterConfig().
				getMbusClient(getGasDevice().getPhysicalAddress()).getObisCode()).setTransportKeyRawData(rawDataWithKeys);
	}
}
