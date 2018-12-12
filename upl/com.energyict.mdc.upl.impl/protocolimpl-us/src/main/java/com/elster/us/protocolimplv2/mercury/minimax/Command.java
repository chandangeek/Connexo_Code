package com.elster.us.protocolimplv2.mercury.minimax;

/**
 * Represents the two-byte commands sent to the device
 */
public enum Command {
    SN, // Sign on
    SF, // Sign off
    RD, // Read single
    RG, // Read group
    WD, // Write data
    EM, // Read events (MiniMax)
    RE, // Read events (other devices)
    DM  // Date multiple (read audit trail)
}
