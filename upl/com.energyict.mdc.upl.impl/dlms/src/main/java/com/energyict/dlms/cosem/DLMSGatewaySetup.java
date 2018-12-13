package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.IOException;

/**
 * DLMS Gateway Setup IC  (for the Beacon 3100).
 * 
 * Specified in : https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=G3IntBeacon3100&title=DLMS+gateway+specification
 */
public final class DLMSGatewaySetup extends AbstractCosemObject {

	/** Beacon 3100 OBIS code. */
	public static final ObjectReference DEFAULT_OBIS_CODE = new ObjectReference(new byte[] { 0, 0, (byte)128, 0, 19, (byte)255 });

	/**
	 * Create a new instance.
	 *
	 * @param 	protocolLink		The protocol link.
	 * @param 	objectReference		The object reference, if any.
	 */
	public DLMSGatewaySetup(final ProtocolLink protocolLink, final ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final int getClassId() {
		return DLMSClassId.DLMS_GATEWAY_SETUP.getClassId();
	}

    /**
     * Relay incoming meter push notifications
     *  (0):    DROP,                   // Drop all unsolicited meter notifications
     *  (1):    PASSTHROUGH,            // Translate WPDU header of incoming meter notifications and relay as-is
     *  (2):    ADD_ORIGIN_HEADER,      // Add additional header to incoming meter notifications
     *  (3):    WRAP_AS_SERVER_EVENT    // Wrap incoming meter notifications in RTU server event structure and dispatch as server event
     *
     * @param notificationRelayingType
     * @throws IOException
     */
    public final void setNotificationRelaying(int notificationRelayingType) throws IOException {
        TypeEnum relayType = new TypeEnum(notificationRelayingType);
        this.write(DLMSGatewaySetupAttributes.NOTIFICATION_RELAYING, relayType.getBEREncodedByteArray());
    }

    /**
     * Deciphering incoming meter push notifications before relaying
     *
     * @param decipher
     * @throws IOException
     */
    public final void setNotificationDecipher(boolean decipher) throws IOException {
        BooleanObject notificationDecipher = new BooleanObject(decipher);
        this.write(DLMSGatewaySetupAttributes.NOTIFICATION_DECIPHER, notificationDecipher.getBEREncodedByteArray());
    }


    /**
     * Drop incoming meter push notifications which are not ciphered.
     *
     * @param dropUnencrypted
     * @throws IOException
     */
    public final void setNotificationDropUnencrypted(boolean dropUnencrypted) throws IOException {
        BooleanObject notificationDropUnencrypted = new BooleanObject(dropUnencrypted);
        this.write(DLMSGatewaySetupAttributes.NOTIFICATION_DROP_UNENCRYPTED, notificationDropUnencrypted.getBEREncodedByteArray());
    }


	/**
	 * Drops all virtual logical devices from the gateway and kicks the associated meters from the network.
	 *
	 * @throws 	java.io.IOException		If an IO error occurs.
	 */
	public final void resetGateway() throws IOException {
		this.methodInvoke(DLMSGatewaySetupMethods.RESET_GATEWAY);
	}



    /**
     * Removes the logical device with given ID from the gateway and kicks the associated meter from the network.
     *
     * @param id
     * @throws IOException
     */
    public void removeLogicalDevice(int id) throws IOException {
        Unsigned32 paramId = new Unsigned32(id);
        this.methodInvoke(DLMSGatewaySetupMethods.REMOVE_LOGICAL_DEVICE, paramId.getBEREncodedByteArray());
    }

}
