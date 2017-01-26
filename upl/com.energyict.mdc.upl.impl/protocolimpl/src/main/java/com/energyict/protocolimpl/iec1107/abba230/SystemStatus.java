package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.ArrayList;
import java.util.List;

/** @author  Koen */

public class SystemStatus {

    List<Integer> systemStatuses = new ArrayList<Integer>();
    List<Integer> systemErrors = new ArrayList<Integer>();
    int clockFailureActionMode = -1;
    long value;

    /**
     * Creates a new instance of SystemStatus
     */
    public SystemStatus(byte[] data) throws ProtocolException {
        value = ProtocolUtils.getIntLE(data, 0, 4);
        for (int i = 0; i < 16; i++) {
            systemStatuses.add(ProtocolUtils.getInt(data, i, 1));
        }
        for (int i = 0; i < 4; i++) {
            systemErrors.add(ProtocolUtils.getInt(data, 16 + i, 1));
        }
        clockFailureActionMode = ProtocolUtils.getInt(data, 20, 1);
    }

    public long getValue() {
        return value;
    }

    /**
     * Returns the SystemStatus at the given index.<br></br>
     * <b>Warning:</b> When the status at the given index is not available, then NULL is returned
     */
    public Integer getSystemStatus(int index) {
        try {
            return systemStatuses.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the SystemError at the given index.<br></br>
     * <b>Warning:</b> When the error at the given index is not available, then NULL is returned
     */
    public Integer getSystemError(int index) {
        try {
            return systemErrors.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getClockFailureActionMode() {
        return clockFailureActionMode;
    }
}
