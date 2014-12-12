package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class ConnectionTypeField extends AbstractField<ConnectionTypeField> {

    public static final int LENGTH = 1;

    private int connectionTypeCode;
    private ConnectionType connectionType;

    public ConnectionTypeField() {
        this.connectionType = ConnectionType.UNDEFINED;
    }

    public ConnectionTypeField(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(connectionTypeCode, LENGTH);
    }

    @Override
    public ConnectionTypeField parse(byte[] rawData, int offset) throws ParsingException {
        connectionTypeCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        connectionType = ConnectionType.fromConnectionCode(connectionTypeCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public String getConnectionTypeInfo() {
        return getConnectionType().getConnectionInfo();
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public enum ConnectionType {
        UNDEFINED(0, "Undefined"),
        STAR(1, "Star"),
        DELTA_OPENED(2, "Open delta"),
        BI_PHASE(3, "Bi-phase 120 degrees"),
        MONO_PHASE(4, "mono-phase two wire"),
        PARALLEL_SERIES(5, "Parallel series"),
        GROUNDED_DELTA(6, "Grounded delta"),
        MONO_PHASE_THREEE_WIRE(7, "Mono-phase three wire");

        private final int connectionCode;
        private final String connectionInfo;

        private ConnectionType(int connectionCode, String connectionInfo) {
            this.connectionCode = connectionCode;
            this.connectionInfo = connectionInfo;
        }

        public String getConnectionInfo() {
            return connectionInfo;
        }

        public int getConnectionCode() {
            return connectionCode;
        }

        public static ConnectionType fromConnectionCode(int statusCode) {
            for (ConnectionType version : ConnectionType.values()) {
                if (version.getConnectionCode() == statusCode) {
                    return version;
                }
            }
            return ConnectionType.UNDEFINED;
        }
    }
}