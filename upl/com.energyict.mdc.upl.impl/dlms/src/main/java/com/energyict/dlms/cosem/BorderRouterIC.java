package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.BorderRouterAttributes;
import com.energyict.dlms.cosem.methods.BorderRouterMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Border Router Setup IC
 * class id = 20020, version = 0, logical name = 0-168:96.176.0.255 (00A860B000FF)
 * The border router setup IC allows for configuring the G3-PLC layer 3 router functionality.
 */
public class BorderRouterIC extends AbstractCosemObject {

    private static final ObisCode OBIS_CODE_BORDER_ROUTER = ObisCode.fromString("0.168.96.176.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public BorderRouterIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.BORDER_ROUTER.getClassId();
    }

    @Override
    public ObisCode getObisCode() {
        return getDefaultObisCode();
    }

    public static ObisCode getDefaultObisCode() {
        return OBIS_CODE_BORDER_ROUTER;
    }

    /**
     * Indicates if the border router component is active.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType isActive() throws IOException {
        return readDataType(BorderRouterAttributes.IS_ACTIVE);
    }

    /**
     * Contains array of routing_entry.
     *
     * @return Array of structures containing the current routing prefixes assigned to the PAN.
     * @throws IOException
     */
    public Array readRoutingEntries() throws IOException {
        return readDataType(BorderRouterAttributes.ROUTING_ENTRIES, Array.class);
    }

    /**
     * Holds the IPv4 NAT configuration. Changes to this attribute are applied after disconnecting
     * from the DLMS server.
     *
     * @return IPv4 NAT configuration
     * @throws IOException
     */
    public Structure readNATConfiguration() throws IOException {
        return readDataType(BorderRouterAttributes.NAT_CONFIGURATION, Structure.class);
    }

    /**
     * Holds the prefix delegation state.
     * If TRUE, the ULA prefix assigned on the PAN is derived from the delegated prefix in the incoming DHCPv6 offers.
     * If FALSE, the ULA prefix should be configured manually.
     *
     * @return prefix delegation state
     * @throws IOException
     */
    public boolean readPrefixDelegation() throws IOException {
        return readDataType(BorderRouterAttributes.PREFIX_DELEGATION, BooleanObject.class).getState();
    }

    /**
     * Holds the router advertisement configuration.
     * Changes to this attribute are applied after disconnecting from the DLMS server.
     *
     * @return router advertisement configuration
     * @throws IOException
     */
    public Structure readRAConfig() throws IOException {
        return readDataType(BorderRouterAttributes.RA_CONFIG, Structure.class);
    }

    /**
     * Holds the RIP configuration.
     * Changes to this attribute are applied after disconnecting from the DLMS server.
     *
     * @return RIP configuration
     * @throws IOException
     */
    public Structure readRIPConfig() throws IOException {
        return readDataType(BorderRouterAttributes.RIP_CONFIG, Structure.class);
    }

    /**
     * Adds a new routing entry to the routing setup
     * @param requestData
     * @throws IOException
     */
    public final void addRoutingEntry(Structure requestData) throws IOException {
        this.methodInvoke(BorderRouterMethods.ADD_ROUTING_ENTRY, requestData.getBEREncodedByteArray());
    }

    /**
     * Removes the routing entry associated with the given ID
     * @param routingEntryId
     * @throws IOException
     */
    public final void removeRoutingEntry(int routingEntryId) throws IOException {
        this.methodInvoke(BorderRouterMethods.REMOVE_ROUTING_ENTRY, new Unsigned16(routingEntryId).getBEREncodedByteArray());
    }

    /**
     * Drops all routing entries and restarts the border router
     * @throws IOException
     */
    public final void resetRouter() throws IOException {
        this.methodInvoke(BorderRouterMethods.RESET_ROUTER);
    }
}
