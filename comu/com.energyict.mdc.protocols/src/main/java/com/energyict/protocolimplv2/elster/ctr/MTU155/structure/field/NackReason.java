/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class NackReason extends AbstractField<NackReason> {

    private int reason;

    public NackReason() {
        this(0);
    }

    public NackReason(int reason) {
        this.reason = reason;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(reason, getLength());
    }

    public NackReason parse(byte[] rawData, int offset) throws CTRParsingException {
        reason = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    /**
     * Gets a description for the reason of the Nack
     * @return reason description
     */
    public String getDescription() {
        String reasonDescription = "[" + getReason() + "] ";
        switch (getReason()) {
            case 0x040: reasonDescription += "Generic Can be used as generic NACK in cases where there are no specific codes."; break;
            case 0x041: reasonDescription += "Access denied If the password has not been recognized."; break;
            case 0x042: reasonDescription += "Function not implemented The function has not been implemented in the application."; break;
            case 0x043: reasonDescription += "Response to the Write. An object or one of its attributes is not supported by the application."; break;
            case 0x044: reasonDescription += "Data structure not implemented. The data structure is not supported by the application."; break;
            case 0x045: reasonDescription += "Response to the Query (Overflow). More data items have been requested than the permitted number."; break;
            case 0x046: reasonDescription += "Incorrect data field. An error in the data field has been detected for the command received."; break;
            case 0x047: reasonDescription += "Function not allowed. The requested function is not allowed because privileges are not held."; break;
            case 0x048: reasonDescription += "Function $ - Down Loader Downloader cannot be enabled due to inconsistency between parameters."; break;
            case 0x049: reasonDescription += "Non-availability. The Client process is temporarily busy and cannot execute the command."; break;
            case 0x04A: reasonDescription += "Response to the Write. A write operation was not executed due to a session error."; break;
            case 0x04B: reasonDescription += "Response to the Execute Function. The execute function specified by Obj.id cannot be executed for reasons other than privileges."; break;
            case 0x04C: reasonDescription += "Encryption error CPA irregularity detected after decryption."; break;
            case 0x04D: reasonDescription += "Function D-Down Loading One DL segment not received correctly."; break;
            case 0x04E: reasonDescription += "Response to Write The Write cannot be accepted because the date of validity has expired."; break;
            case 0x04F: reasonDescription += "Response to The Execute cannot be accepted."; break;
            case 0x050: reasonDescription += "Response to Write or Execute. The Write or Execute cannot be executed because the condition is 'under maintenance'."; break;
            case 0x052: reasonDescription += "Response to Write or Execute. The Write or Execute cannot be executed because there is a seal preventing it."; break;
            default: reasonDescription += "Unknown NACK reason code."; break;
        }
        return reasonDescription;
    }

    public int getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
