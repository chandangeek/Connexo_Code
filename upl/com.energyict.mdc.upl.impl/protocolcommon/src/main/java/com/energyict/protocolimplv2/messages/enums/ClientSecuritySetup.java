package com.energyict.protocolimplv2.messages.enums;

import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 11/11/2016.
 */
public enum ClientSecuritySetup {
    Management(1, ObisCode.fromString("0.0.43.0.0.255")),
    Consumer_Information(103, ObisCode.fromString("0.0.43.0.1.255")),
    Data_Readout(2, ObisCode.fromString("0.0.43.0.2.255")),
    Installation(5, ObisCode.fromString("0.0.43.0.3.255")),
    Maintenance(6, ObisCode.fromString("0.0.43.0.4.255")),
    Certification(7, ObisCode.fromString("0.0.43.0.5.255"));

    /** Client ID to be used. */
    private final int clientId;

    /** OBIS code of the Security Setup */
    private final ObisCode securitySetupOBIS;

    /**
     * Create a new instance.
     *
     * @param 	id						Client ID.
     * @param 	securitySetupOBIS		Security setup OBIS code for specified client.
     */
    private ClientSecuritySetup(final int id, final ObisCode securitySetupOBIS) {
        this.clientId = id;
        this.securitySetupOBIS = securitySetupOBIS;
    }

    /**
     * Returns the OBIS of the SecuritySetup.
     *
     * @return	The OBIS of the SecuritySetup.
     */
    public final ObisCode getSecuritySetupOBIS() {
        return securitySetupOBIS;
    }

    /**
     * Returns the client with the given ID.
     *
     * @param 	clientId		ID of the requested client.
     *
     * @return	The matching client, <code>null</code> if not known.
     */
    public static final ClientSecuritySetup getByID(final int clientId) {
        for (final ClientSecuritySetup client : ClientSecuritySetup.values()) {
            if (client.clientId == clientId) {
                return client;
            }
        }

        return null;
    }

    public static String[] getClients() {
        ClientSecuritySetup[] clients = values();
        String[] result = new String[clients.length];
        for (int index = 0; index < clients.length; index++) {
            result[index] = clients[index].name();
        }
        return result;
    }

    public final int getID(){
        return clientId;
    }

}
